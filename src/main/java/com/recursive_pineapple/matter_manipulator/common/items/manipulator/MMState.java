package com.recursive_pineapple.matter_manipulator.common.items.manipulator;

import static com.recursive_pineapple.matter_manipulator.common.utils.MMUtils.min;
import static com.recursive_pineapple.matter_manipulator.common.utils.MMUtils.signum;
import static com.recursive_pineapple.matter_manipulator.common.utils.Mods.AppliedEnergistics2;
import static com.recursive_pineapple.matter_manipulator.common.utils.Mods.GregTech;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

import gregtech.common.blocks.BlockMachines;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.features.ILocatable;
import appeng.api.implementations.parts.IPartCable;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.parts.IPartItem;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.DimensionalCoord;
import appeng.tile.misc.TileSecurity;
import appeng.tile.networking.TileWireless;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.gtnewhorizon.structurelib.util.XSTR;
import com.recursive_pineapple.matter_manipulator.asm.Optional;
import com.recursive_pineapple.matter_manipulator.common.building.AEAnalysisResult;
import com.recursive_pineapple.matter_manipulator.common.building.AEPartData;
import com.recursive_pineapple.matter_manipulator.common.building.BlockAnalyzer;
import com.recursive_pineapple.matter_manipulator.common.building.BlockAnalyzer.RegionAnalysis;
import com.recursive_pineapple.matter_manipulator.common.building.BlockSpec;
import com.recursive_pineapple.matter_manipulator.common.building.GTAnalysisResult;
import com.recursive_pineapple.matter_manipulator.common.building.ImmutableBlockSpec;
import com.recursive_pineapple.matter_manipulator.common.building.InteropConstants;
import com.recursive_pineapple.matter_manipulator.common.building.PendingBlock;
import com.recursive_pineapple.matter_manipulator.common.data.WeightedSpecList;
import com.recursive_pineapple.matter_manipulator.common.items.MMUpgrades;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator.ManipulatorTier;
import com.recursive_pineapple.matter_manipulator.common.persist.NBTJsonAdapter;
import com.recursive_pineapple.matter_manipulator.common.persist.StaticEnumJsonAdapter;
import com.recursive_pineapple.matter_manipulator.common.persist.UIDJsonAdapter;
import com.recursive_pineapple.matter_manipulator.common.persist.WeightedListJsonAdapter;
import com.recursive_pineapple.matter_manipulator.common.uplink.IUplinkMulti;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods.Names;

import org.joml.Vector3i;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * The NBT state of a manipulator.
 */
public class MMState {

    static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(UniqueIdentifier.class, new UIDJsonAdapter())
        .registerTypeAdapter(NBTTagCompound.class, new NBTJsonAdapter())
        .registerTypeAdapter(ForgeDirection.class, new StaticEnumJsonAdapter<>(ForgeDirection.class))
        .registerTypeAdapter(WeightedSpecList.class, new WeightedListJsonAdapter())
        .create();

    @SerializedName("jv")
    private int jsonVersion = 0;
    @SerializedName("dv")
    private int dataVersion = 0;

    public MMConfig config = new MMConfig();

    public Long encKey, uplinkAddress;
    public double charge;

    public BitSet installedUpgrades = new BitSet();
    public int upgradeProvidedCapabilities;

    public transient ItemMatterManipulator manipulator;

    @Optional(Names.APPLIED_ENERGISTICS2)
    public transient TileSecurity securityTerminal;
    @Optional(Names.APPLIED_ENERGISTICS2)
    public transient IGridNode gridNode;
    @Optional(Names.APPLIED_ENERGISTICS2)
    public transient IGrid grid;
    @Optional(Names.APPLIED_ENERGISTICS2)
    public transient IStorageGrid storageGrid;
    @Optional(Names.APPLIED_ENERGISTICS2)
    public transient IMEMonitor<IAEItemStack> itemStorage;

    public static MMState load(NBTTagCompound tag) {
        JsonObject obj = (JsonObject) MMUtils.toJsonObject(tag);

        migrateJson(obj);

        MMState state = GSON.fromJson(obj, MMState.class);

        if (state == null) state = new MMState();
        if (state.config == null) state.config = new MMConfig();

        state.migrate();
        state.onLoad();

        return state;
    }

    public NBTTagCompound save() {
        return (NBTTagCompound) MMUtils.toNbt(GSON.toJsonTree(this));
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public MMState clone() {
        MMState copy = load(save());

        copy.manipulator = this.manipulator;

        return copy;
    }

    private static void migrateJson(JsonObject obj) {
        int version = obj.has("jv") ? obj.get("jv").getAsInt() : 0;

        if (version == 0) {
            if (obj.get("config") instanceof JsonObject config) {
                config.remove("corners");
                config.remove("edges");
                config.remove("faces");
                config.remove("volumes");
                config.remove("cables");
                config.remove("replaceWhitelist");
                config.remove("replaceWith");
            }
            version = 1;
        }

        obj.addProperty("jv", version);
    }

    private void migrate() {

    }

    private void onLoad() {
        for (MMUpgrades upgrade : getInstalledUpgrades()) {
            upgradeProvidedCapabilities |= upgrade.providesCaps;
        }
    }

    /**
     * True if the ME system could be connected to.
     */
    public boolean hasMEConnection() {
        if (!AppliedEnergistics2.isModLoaded()) return false;

        return encKey != null && securityTerminal != null && gridNode != null && grid != null && storageGrid != null && itemStorage != null;
    }

    /**
     * Tries to connect to an ME system, if possible.
     */
    @Optional(Names.APPLIED_ENERGISTICS2)
    public boolean connectToMESystem() {
        grid = null;
        storageGrid = null;
        itemStorage = null;

        if (encKey == null) return false;

        ILocatable grid = AEApi.instance()
            .registries()
            .locatable()
            .getLocatableBy(encKey);

        if (grid instanceof TileSecurity security) {
            this.securityTerminal = security;
            this.gridNode = security.getGridNode(ForgeDirection.UNKNOWN);
            if (this.gridNode != null) {
                this.grid = this.gridNode.getGrid();
                this.storageGrid = this.grid.getCache(IStorageGrid.class);
                if (this.storageGrid != null) {
                    this.itemStorage = this.storageGrid.getItemInventory();
                }
            }
        }

        return hasMEConnection();
    }

    @Optional(Names.APPLIED_ENERGISTICS2)
    private transient IWirelessAccessPoint prevAccessPoint;

    /**
     * Checks if the player is currently within range of an access point and the access point is online.
     */
    public boolean canInteractWithAE(EntityPlayer player) {
        if (!AppliedEnergistics2.isModLoaded()) return false;

        if (grid == null) { return false; }

        IEnergyGrid eg = grid.getCache(IEnergyGrid.class);
        if (!eg.isNetworkPowered()) { return false; }

        ISecurityGrid sec = grid.getCache(ISecurityGrid.class);
        if (!sec.hasPermission(player, SecurityPermissions.EXTRACT) || !sec.hasPermission(player, SecurityPermissions.INJECT)) { return false; }

        if (checkAEDistance(player, prevAccessPoint)) { return true; }

        for (IGridNode node : grid.getMachines(TileWireless.class)) {
            if (checkAEDistance(player, (IWirelessAccessPoint) node.getMachine())) {
                prevAccessPoint = (IWirelessAccessPoint) node.getMachine();
                return true;
            }
        }

        prevAccessPoint = null;

        return false;
    }

    @Optional(Names.APPLIED_ENERGISTICS2)
    private boolean checkAEDistance(EntityPlayer player, IWirelessAccessPoint accessPoint) {
        if (accessPoint != null && accessPoint.getGrid() == grid && accessPoint.isActive()) {
            DimensionalCoord coord = accessPoint.getLocation();

            if (coord.getWorld().provider.dimensionId != player.worldObj.provider.dimensionId) { return false; }

            double distance = player.getDistanceSq(coord.x, coord.y, coord.z);

            return Math.pow(accessPoint.getRange(), 2) >= distance;
        } else {
            return false;
        }
    }

    public transient IUplinkMulti uplink;

    /**
     * Tries to connect to the uplink, if possible.
     */
    public boolean connectToUplink() {
        uplink = null;

        if (uplinkAddress != null && uplinkAddress != 0) {
            uplink = IUplinkMulti.getUplink(uplinkAddress);

            if (uplink != null) {
                if (!uplink.isActive()) {
                    uplink = null;
                }
            }
        }

        return hasUplinkConnection();
    }

    public boolean hasUplinkConnection() {
        return uplink != null;
    }

    public boolean hasCap(int cap) {
        return (manipulator.tier.capabilities & cap) == cap || (upgradeProvidedCapabilities & cap) == cap;
    }

    public Transform getTransform() {
        if (config.transform == null) config.transform = new Transform();
        return config.transform;
    }

    // #region Pending blocks

    /**
     * Gets the pending blocks for this manipulator.
     * Note: moving uses a special algorithm, so its value returned here should only be used for drawing the hints.
     */
    public List<PendingBlock> getPendingBlocks(ManipulatorTier tier, World world) {
        return switch (config.placeMode) {
            case COPYING, MOVING -> getAnalysis(world);
            case GEOMETRY -> getGeomPendingBlocks(world);
            case EXCHANGING -> getExchangeBlocks(tier, world);
            case CABLES -> getCableBlocks(world);
        };
    }

    private List<PendingBlock> getAnalysis(World world) {
        Location coordA = config.coordA;
        Location coordB = config.coordB;
        Location coordC = config.coordC;

        if (!Location.areCompatible(coordA, coordB, coordC) || !coordA.isInWorld(world)) { return new ArrayList<>(); }

        // MOVING's result is only used visually since it has a special build algorithm
        RegionAnalysis analysis = BlockAnalyzer
            .analyzeRegion(world, coordA, coordB, config.placeMode == PlaceMode.COPYING ? true : false);

        if (config.placeMode == PlaceMode.COPYING) {
            Transform t = getTransform();

            t.cacheRotation();

            // apply rotation
            for (PendingBlock block : analysis.blocks) {
                Vector3i v = t.apply(block.toVec());

                block.x = v.x;
                block.y = v.y;
                block.z = v.z;

                block.transform(t);
            }

            // offset to the correct location (needs to be after rotating)
            for (PendingBlock block : analysis.blocks) {
                block.x += coordC.x;
                block.y += coordC.y;
                block.z += coordC.z;
            }

            // copy the blocks (arraying)
            if (config.arraySpan != null) {
                int sx = config.arraySpan.x;
                int sy = config.arraySpan.y;
                int sz = config.arraySpan.z;

                List<PendingBlock> base = new ArrayList<>(analysis.blocks);
                analysis.blocks.clear();

                for (int y = Math.min(sy, 0); y <= Math.max(sy, 0); y++) {
                    for (int z = Math.min(sz, 0); z <= Math.max(sz, 0); z++) {
                        for (int x = Math.min(sx, 0); x <= Math.max(sx, 0); x++) {
                            int dx = x * (analysis.deltas.x + (analysis.deltas.x < 0 ? -1 : 1));
                            int dy = y * (analysis.deltas.y + (analysis.deltas.y < 0 ? -1 : 1));
                            int dz = z * (analysis.deltas.z + (analysis.deltas.z < 0 ? -1 : 1));

                            Vector3i d = new Vector3i(dx, dy, dz);

                            t.apply(d);

                            for (PendingBlock original : base) {
                                PendingBlock dup = original.clone();
                                dup.x += d.x;
                                dup.y += d.y;
                                dup.z += d.z;
                                analysis.blocks.add(dup);
                            }
                        }
                    }
                }
            }

            analysis.deltas = t.apply(analysis.deltas);

            t.uncacheRotation();
        } else {
            for (PendingBlock block : analysis.blocks) {
                block.x += coordC.x;
                block.y += coordC.y;
                block.z += coordC.z;
            }
        }

        return analysis.blocks;
    }

    private List<PendingBlock> getExchangeBlocks(ManipulatorTier tier, World world) {
        Location coordA = config.coordA;
        Location coordB = config.coordB;

        if (!Location.areCompatible(coordA, coordB) || !coordA.isInWorld(world)) { return new ArrayList<>(); }

        if (config.replaceWhitelist == null || config.replaceWhitelist.specs.isEmpty()) { return new ArrayList<>(); }

        Vector3i deltas = MMUtils.getRegionDeltas(coordA, coordB);

        ArrayList<PendingBlock> pending = new ArrayList<>();

        XSTR rng = new XSTR(config.hashCode());

        BlockSpec existing = new BlockSpec();

        boolean replacingAir = config.replaceWhitelist.contains(BlockSpec.air());

        for (Vector3i voxel : MMUtils.getBlocksInBB(coordA, deltas)) {
            int x = voxel.x;
            int y = voxel.y;
            int z = voxel.z;

            if (!replacingAir) {
                if (world.isAirBlock(x, y, z)) continue;
            }

            BlockSpec.fromBlock(existing, world, x, y, z);

            if (!replacingAir) {
                if (existing.isAir()) continue;
            }

            if (AppliedEnergistics2.isModLoaded()) {
                // get ae cable if possible
                MMUtils.getAECable(existing, world, x, y, z);
            }

            if (!config.replaceWhitelist.contains(existing)) continue;

            ImmutableBlockSpec replacement = config.replaceWith.get(rng);
            ImmutableBlockSpec block = replacement;

            if (hasCap(ItemMatterManipulator.ALLOW_CABLES)) {
                if (AppliedEnergistics2.isModLoaded() && MMUtils.isAECable(replacement)) {
                    block = InteropConstants.AE_BLOCK_CABLE.toSpec();
                }
            }

            PendingBlock rep = block.instantiate(world, x, y, z);

            rep.analyze(world.getTileEntity(x, y, z), PendingBlock.ANALYZE_ALL);
            rep.migrate();

            if (hasCap(ItemMatterManipulator.ALLOW_CABLES)) {
                if (AppliedEnergistics2.isModLoaded() && MMUtils.isAECable(replacement)) {
                    placingAECable(rep, replacement);
                }
            }

            pending.add(rep);
        }

        return pending;
    }

    @Optional(Names.APPLIED_ENERGISTICS2)
    private void placingAECable(PendingBlock pendingBlock, ImmutableBlockSpec cable) {
        if (pendingBlock.ae == null) pendingBlock.ae = new AEAnalysisResult();

        AEAnalysisResult ae = (AEAnalysisResult) pendingBlock.ae;

        if (ae.mAEParts == null) ae.mAEParts = new AEPartData[7];

        ae.mAEParts[ForgeDirection.UNKNOWN.ordinal()] = new AEPartData(((IPartItem) cable.getItem()).createPartFromItemStack(cable.toStack(1)));
    }

    private List<PendingBlock> getCableBlocks(World world) {
        Location coordA = config.coordA;
        Location coordB = config.coordB;

        if (!Location.areCompatible(coordA, coordB) || !coordA.isInWorld(world)) { return new ArrayList<>(); }

        Vector3i a = coordA.toVec();
        Vector3i b = pinToAxes(a, coordB.toVec());

        ArrayList<PendingBlock> out = new ArrayList<>();

        if (config.cables == null) {
            BlockSpec pooled = new BlockSpec();

            for (Vector3i voxel : getLineVoxels(a.x, a.y, a.z, b.x, b.y, b.z)) {
                if (AppliedEnergistics2.isModLoaded()) {
                    if (MMUtils.getAECable(pooled, world, voxel.x, voxel.y, voxel.z)) {
                        PendingBlock pendingBlock = InteropConstants.AE_BLOCK_CABLE.toSpec().instantiate(world, voxel.x, voxel.y, voxel.z);

                        pendingBlock.analyze(world.getTileEntity(voxel.x, voxel.y, voxel.z), PendingBlock.ANALYZE_ALL);

                        clearAECable(pendingBlock);

                        out.add(pendingBlock);

                        continue;
                    }
                }

                out.add(BlockSpec.AIR.instantiate(world, voxel.x, voxel.y, voxel.z));
            }
        } else {
            Block block = Block.getBlockFromItem(config.cables.getItem());

            if (GregTech.isModLoaded()) {
                getGTCables(a, b, out, block, world, config.cables);
            }

            if (AppliedEnergistics2.isModLoaded()) {
                getAECables(a, b, out, block, world, config.cables);
            }
        }

        return out;
    }

    @Optional(Names.GREG_TECH_NH)
    private void getGTCables(Vector3i a, Vector3i b, List<PendingBlock> out, Block block, World world, ImmutableBlockSpec cable) {
        if (block instanceof BlockMachines) {
            int end = 0, start = 0;

            // calculate the start & end mConnections flags
            switch (new Vector3i(b).sub(a).maxComponent()) {
                case 0: {
                    start = b.x < a.x ? ForgeDirection.EAST.flag : ForgeDirection.WEST.flag;
                    end = b.x > a.x ? ForgeDirection.EAST.flag : ForgeDirection.WEST.flag;
                    break;
                }
                case 1: {
                    start = b.y < a.y ? ForgeDirection.UP.flag : ForgeDirection.DOWN.flag;
                    end = b.y > a.y ? ForgeDirection.UP.flag : ForgeDirection.DOWN.flag;
                    break;
                }
                case 2: {
                    start = b.z < a.z ? ForgeDirection.SOUTH.flag : ForgeDirection.NORTH.flag;
                    end = b.z > a.z ? ForgeDirection.SOUTH.flag : ForgeDirection.NORTH.flag;
                    break;
                }
            }

            List<Vector3i> voxels = getLineVoxels(a.x, a.y, a.z, b.x, b.y, b.z);

            for (int i = 0; i < voxels.size(); i++) {
                Vector3i voxel = voxels.get(i);

                GTAnalysisResult gt = GTAnalysisResult.analyze(world.getTileEntity(voxel.x, voxel.y, voxel.z));

                if (gt == null) gt = new GTAnalysisResult();

                if (i > 0) gt.mConnections |= start;
                if (i < voxels.size() - 1) gt.mConnections |= end;

                PendingBlock pendingBlock = cable.instantiate(world, voxel.x, voxel.y, voxel.z);

                pendingBlock.gt = gt;

                out.add(pendingBlock);
            }
        }
    }

    @Optional(Names.APPLIED_ENERGISTICS2)
    private void getAECables(Vector3i a, Vector3i b, List<PendingBlock> out, Block block, World world, ImmutableBlockSpec cableSpec) {
        if (cableSpec.getItem() instanceof IPartItem partItem) {
            if (partItem.createPartFromItemStack(cableSpec.toStack(1)) instanceof IPartCable cable) {
                for (Vector3i voxel : getLineVoxels(a.x, a.y, a.z, b.x, b.y, b.z)) {
                    int x = voxel.x, y = voxel.y, z = voxel.z;

                    AEAnalysisResult ae;

                    if (InteropConstants.AE_BLOCK_CABLE.matches(world.getBlock(x, y, z), 0)) {
                        ae = AEAnalysisResult.analyze(world.getTileEntity(voxel.x, voxel.y, voxel.z));
                        ae.mAEParts[ForgeDirection.UNKNOWN.ordinal()] = new AEPartData(cable);
                    } else {
                        ae = new AEAnalysisResult();
                        ae.mAEParts = new AEPartData[7];
                        ae.mAEParts[ForgeDirection.UNKNOWN.ordinal()] = new AEPartData(cable);
                    }

                    PendingBlock pendingBlock = cableSpec.instantiate(world, x, y, z);

                    pendingBlock.ae = ae;

                    out.add(pendingBlock);
                }
            }
        }
    }

    @Optional(Names.APPLIED_ENERGISTICS2)
    private void clearAECable(PendingBlock pendingBlock) {
        if (pendingBlock.ae == null) return;

        AEAnalysisResult ae = (AEAnalysisResult) pendingBlock.ae;

        if (ae.mAEParts == null) return;

        ae.mAEParts[ForgeDirection.UNKNOWN.ordinal()] = null;
    }

    private List<PendingBlock> getGeomPendingBlocks(World world) {
        Location coordA = config.coordA;
        Location coordB = config.coordB;
        Location coordC = config.coordC;

        if (!Location.areCompatible(coordA, coordB) || !coordA.isInWorld(world)) { return new ArrayList<>(); }

        if (config.shape.requiresC()) {
            if (!Location.areCompatible(coordA, coordC) || !coordA.isInWorld(world)) { return new ArrayList<>(); }
        }

        int x1 = config.coordA.x;
        int y1 = config.coordA.y;
        int z1 = config.coordA.z;
        int x2 = config.coordB.x;
        int y2 = config.coordB.y;
        int z2 = config.coordB.z;

        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        int maxZ = Math.max(z1, z2);

        ArrayList<PendingBlock> pending = new ArrayList<>();

        switch (config.shape) {
            case LINE: {
                iterateLine(pending, x1, y1, z1, x2, y2, z2);
                break;
            }
            case CUBE: {
                iterateCube(pending, minX, minY, minZ, maxX, maxY, maxZ);
                break;
            }
            case SPHERE: {
                iterateSphere(pending, minX, minY, minZ, maxX, maxY, maxZ);
                break;
            }
            case CYLINDER: {
                iterateCylinder(pending, coordA.toVec(), coordB.toVec(), coordC.toVec());
                break;
            }
        }

        return pending;
    }

    private static List<Vector3i> getLineVoxels(int x1, int y1, int z1, int x2, int y2, int z2) {
        List<Vector3i> voxels = new ArrayList<>();

        int dx = Math.abs(x1 - x2), dy = Math.abs(y1 - y2), dz = Math.abs(z1 - z2);
        int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1, sz = z1 < z2 ? 1 : -1;

        voxels.add(new Vector3i(x1, y1, z1));

        if (dx >= dy && dx >= dz) {
            int p1 = 2 * dy - dx;
            int p2 = 2 * dz - dx;

            while (x1 != x2) {
                x1 += sx;

                if (p1 >= 0) {
                    y1 += sy;
                    p1 -= 2 * dx;
                }
                if (p2 >= 0) {
                    z1 += sz;
                    p2 -= 2 * dx;
                }

                p1 += 2 * dy;
                p2 += 2 * dz;

                voxels.add(new Vector3i(x1, y1, z1));
            }
        } else if (dy >= dx && dy >= dz) {
            int p1 = 2 * dx - dy;
            int p2 = 2 * dz - dy;

            while (y1 != y2) {
                y1 += sy;

                if (p1 >= 0) {
                    x1 += sx;
                    p1 -= 2 * dy;
                }
                if (p2 >= 0) {
                    z1 += sz;
                    p2 -= 2 * dy;
                }

                p1 += 2 * dx;
                p2 += 2 * dz;

                voxels.add(new Vector3i(x1, y1, z1));
            }
        } else {
            int p1 = 2 * dy - dz;
            int p2 = 2 * dx - dz;

            while (z1 != z2) {
                z1 += sz;

                if (p1 >= 0) {
                    y1 += sy;
                    p1 -= 2 * dz;
                }
                if (p2 >= 0) {
                    x1 += sx;
                    p2 -= 2 * dz;
                }

                p1 += 2 * dy;
                p2 += 2 * dx;

                voxels.add(new Vector3i(x1, y1, z1));
            }
        }

        return voxels;
    }

    private void iterateLine(ArrayList<PendingBlock> pending, int x1, int y1, int z1, int x2, int y2, int z2) {
        XSTR rng = new XSTR(config.hashCode());

        for (Vector3i voxel : getLineVoxels(x1, y1, z1, x2, y2, z2)) {
            pending.add(config.edges.get(rng).instantiate(config.coordA.worldId, voxel.x, voxel.y, voxel.z));
        }
    }

    private void iterateCube(
        ArrayList<PendingBlock> pending,
        int minX,
        int minY,
        int minZ,
        int maxX,
        int maxY,
        int maxZ
    ) {
        XSTR rng = new XSTR(config.hashCode());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int insideCount = 0;

                    if (x > minX && x < maxX) insideCount++;
                    if (y > minY && y < maxY) insideCount++;
                    if (z > minZ && z < maxZ) insideCount++;

                    ImmutableBlockSpec spec = switch (insideCount) {
                        case 0 -> config.corners.get(rng);
                        case 1 -> config.edges.get(rng);
                        case 2 -> config.faces.get(rng);
                        case 3 -> config.volumes.get(rng);
                        default -> BlockSpec.AIR;
                    };

                    pending.add(spec.instantiate(config.coordA.worldId, x, y, z).setOrders(insideCount, insideCount));
                }
            }
        }
    }

    private void iterateSphere(
        ArrayList<PendingBlock> pending,
        int minX,
        int minY,
        int minZ,
        int maxX,
        int maxY,
        int maxZ
    ) {
        XSTR rng = new XSTR(config.hashCode());

        int sx = maxX - minX + 1;
        int sy = maxY - minY + 1;
        int sz = maxZ - minZ + 1;

        double rx = sx / 2.0;
        double ry = sy / 2.0;
        double rz = sz / 2.0;

        boolean[][][] present = new boolean[sx + 2][sy + 2][sz + 2];

        for (int x = 0; x < sx; x++) {
            for (int y = 0; y < sy; y++) {
                for (int z = 0; z < sz; z++) {
                    // the ternaries here check whether the given axis is 1, in which case this is a circle and not a
                    // sphere
                    // spotless:off
                    double distance = Math.sqrt(
                        (rx > 1 ? Math.pow((x - rx + 0.5) / rx, 2.0) : 0) +
                            (ry > 1 ? Math.pow((y - ry + 0.5) / ry, 2.0) : 0) +
                            (rz > 1 ? Math.pow((z - rz + 0.5) / rz, 2.0) : 0)
                    );
                    // spotless:on

                    if (distance <= 1) {
                        PendingBlock block = config.volumes.get(rng)
                            .instantiate(config.coordA.worldId, x + minX, y + minY, z + minZ)
                            .setOrders(1, 1);

                        present[x + 1][y + 1][z + 1] = true;
                        pending.add(block);
                    }
                }
            }
        }

        ArrayList<ForgeDirection> directions = new ArrayList<>();

        if (rx > 1) {
            directions.add(ForgeDirection.EAST);
            directions.add(ForgeDirection.WEST);
        }

        if (ry > 1) {
            directions.add(ForgeDirection.UP);
            directions.add(ForgeDirection.DOWN);
        }

        if (rz > 1) {
            directions.add(ForgeDirection.NORTH);
            directions.add(ForgeDirection.SOUTH);
        }

        for (PendingBlock block : pending) {
            for (ForgeDirection dir : directions) {
                if (!present[block.x - minX + 1 + dir.offsetX][block.y - minY + 1 + dir.offsetY][block.z - minZ + 1 + dir.offsetZ]) {
                    block.setBlock(config.faces.get(rng));
                    block.buildOrder = 0;
                    block.renderOrder = 0;
                    break;
                }
            }
        }
    }

    private void iterateCylinder(ArrayList<PendingBlock> pending, Vector3i coordA, Vector3i coordB, Vector3i coordC) {
        XSTR rng = new XSTR(config.hashCode());

        Vector3i b2 = pinToPlanes(coordA, coordB);
        Vector3i height = pinToLine(coordA, b2, coordC).sub(coordA);

        Vector3i delta = new Vector3i(b2).sub(coordA);

        delta.x += signum(delta.x);
        delta.y += signum(delta.y);
        delta.z += signum(delta.z);

        // the deltas for each dimension (A/B/Height)
        int dA = 0, dB = 0, dH = 0;
        // used to determine the final block position
        Vector3i vecA, vecB, vecH;

        // calculate the delta vectors for each axis
        // this is kinda cursed and I don't really understand it anymore, so good luck changing it
        switch (delta.minComponent()) {
            case 0: {
                dA = delta.y;
                dB = delta.z;
                dH = height.x;
                vecA = new Vector3i(0, signum(delta.y), 0);
                vecB = new Vector3i(0, 0, signum(delta.z));
                vecH = new Vector3i(signum(height.x), 0, 0);
                break;
            }
            case 1: {
                dA = delta.x;
                dB = delta.z;
                dH = height.y;
                vecA = new Vector3i(signum(delta.x), 0, 0);
                vecB = new Vector3i(0, 0, signum(delta.z));
                vecH = new Vector3i(0, signum(height.y), 0);
                break;
            }
            case 2: {
                dA = delta.x;
                dB = delta.y;
                dH = height.z;
                vecA = new Vector3i(signum(delta.x), 0, 0);
                vecB = new Vector3i(0, signum(delta.y), 0);
                vecH = new Vector3i(0, 0, signum(height.z));
                break;
            }
            default: {
                throw new AssertionError();
            }
        }

        int absA = Math.abs(dA);
        int absB = Math.abs(dB);
        int absH = Math.abs(dH) + 1; // I have no idea why this +1 is needed

        float rA = absA / 2f;
        float rB = absB / 2f;

        boolean[][][] present = new boolean[absA + 2][absH + 2][absB + 2];

        // generate the blocks in A,B,H space
        // at this point, x=A, z=B, and y=H
        for (int a = 0; a < absA; a++) {
            for (int b = 0; b < absB; b++) {
                double distance = Math.pow((a - rA + 0.5) / rA, 2.0) + Math.pow((b - rB + 0.5) / rB, 2.0);

                if (distance <= 1) {
                    for (int h = 0; h < absH; h++) {
                        PendingBlock block = config.volumes.get(rng)
                            .instantiate(config.coordA.worldId, a, h, b)
                            .setOrders(2, 0);

                        present[a + 1][h + 1][b + 1] = true;
                        pending.add(block);
                    }
                }
            }
        }

        // check the adjacent blocks for each block and determine whether the block should be a volume, edge, or face
        for (PendingBlock block : pending) {
            byte adj = 0;

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                if (present[block.x + 1 + dir.offsetX][block.y + 1 + dir.offsetY][block.z + 1 + dir.offsetZ]) {
                    adj |= dir.flag;
                }
            }

            // I know this looks :ConcerningRead: but this is just an easy way to check which blocks are adjacent to
            // this one

            // if this block is missing an adjacent block, it's not a volume
            if (adj != 0b111111) {
                // if this block is missing one of the N/S/E/W blocks, it's an edge (the surface)
                if ((adj & 0b111100) == 0b111100) {
                    block.setBlock(config.edges.get(rng));
                    block.buildOrder = 1;
                    block.renderOrder = 1;
                } else {
                    // otherwise, it's a face (top & bottom)
                    block.setBlock(config.faces.get(rng));
                    block.buildOrder = 2;
                    block.renderOrder = 0;
                }
            }
        }

        // transform the positions of each block from relative A,B,H space into absolute X,Y,Z space
        for (PendingBlock block : pending) {
            int a = block.x, b = block.z, h = block.y;

            // why, yes, that is an integer matrix
            block.x = a * vecA.x + b * vecB.x + h * vecH.x + coordA.x;
            block.y = a * vecA.y + b * vecB.y + h * vecH.y + coordA.y;
            block.z = a * vecA.z + b * vecB.z + h * vecH.z + coordA.z;
        }
    }

    // #endregion

    /**
     * Pins a point to the axis planes around an origin.
     *
     * @return The pinned point
     */
    public static Vector3i pinToPlanes(Vector3i origin, Vector3i point) {
        int dX = Math.abs(point.x - origin.x);
        int dY = Math.abs(point.y - origin.y);
        int dZ = Math.abs(point.z - origin.z);

        int shortest = min(dX, dY, dZ);

        if (shortest == dX) {
            return new Vector3i(origin.x, point.y, point.z);
        } else if (shortest == dY) {
            return new Vector3i(point.x, origin.y, point.z);
        } else {
            return new Vector3i(point.x, point.y, origin.z);
        }
    }

    /**
     * Pins a point to the normal of the axis plane described by origin,b.
     *
     * @param origin The origin
     * @param b A point on an axis plane of origin
     * @param point The point to pin
     * @return The pinned point on the normal
     */
    public static Vector3i pinToLine(Vector3i origin, Vector3i b, Vector3i point) {
        return switch (new Vector3i(b).sub(origin).minComponent()) {
            case 0 -> new Vector3i(point.x, origin.y, origin.z);
            case 1 -> new Vector3i(origin.x, point.y, origin.z);
            case 2 -> new Vector3i(origin.x, origin.y, point.z);
            default -> throw new AssertionError();
        };
    }

    /**
     * Pins a point to the cardinal axes.
     */
    public static Vector3i pinToAxes(Vector3i origin, Vector3i point) {
        return switch (new Vector3i(point).sub(origin).maxComponent()) {
            case 0 -> new Vector3i(point.x, origin.y, origin.z);
            case 1 -> new Vector3i(origin.x, point.y, origin.z);
            case 2 -> new Vector3i(origin.x, origin.y, point.z);
            default -> throw new AssertionError();
        };
    }

    public boolean hasUpgrade(MMUpgrades upgrade) {
        return upgrade != null && installedUpgrades.get(upgrade.bit);
    }

    public boolean installUpgrade(MMUpgrades upgrade) {
        if (installedUpgrades.get(upgrade.bit)) return false;

        installedUpgrades.set(upgrade.bit);

        return true;
    }

    public boolean couldAcceptUpgrade(ManipulatorTier tier, MMUpgrades upgrade) {
        return tier.allowedUpgrades.contains(upgrade) && !installedUpgrades.get(upgrade.bit);
    }

    public Set<MMUpgrades> getInstalledUpgrades() {
        ObjectOpenHashSet<MMUpgrades> set = new ObjectOpenHashSet<>();

        for (MMUpgrades upgrade : MMUpgrades.values()) {
            if (hasUpgrade(upgrade)) set.add(upgrade);
        }

        return set;
    }

    public static enum Shape {

        LINE,
        CUBE,
        SPHERE,
        CYLINDER;

        public boolean requiresC() {
            return switch (this) {
                case LINE, CUBE, SPHERE -> false;
                case CYLINDER -> true;
            };
        }
    }

    public static enum PendingAction {
        MOVING_COORDS,
        MARK_COPY_A,
        MARK_COPY_B,
        MARK_CUT_A,
        MARK_CUT_B,
        MARK_PASTE,
        GEOM_SELECTING_BLOCK,
        EXCH_SET_TARGET,
        EXCH_ADD_REPLACE,
        EXCH_SET_REPLACE,
        PICK_CABLE,
        MARK_ARRAY,
    }

    public static enum BlockSelectMode {
        CORNERS,
        EDGES,
        FACES,
        VOLUMES,
        ALL,
    }

    public static enum BlockRemoveMode {
        NONE,
        REPLACEABLE,
        ALL
    }

    public static enum PlaceMode {
        GEOMETRY,
        MOVING,
        COPYING,
        EXCHANGING,
        CABLES,
    }
}
