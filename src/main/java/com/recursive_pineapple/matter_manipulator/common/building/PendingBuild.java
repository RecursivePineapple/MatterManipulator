package com.recursive_pineapple.matter_manipulator.common.building;

import static com.recursive_pineapple.matter_manipulator.common.utils.MMUtils.sendErrorToPlayer;
import static com.recursive_pineapple.matter_manipulator.common.utils.MMUtils.sendInfoToPlayer;
import static com.recursive_pineapple.matter_manipulator.common.utils.MMUtils.sendWarningToPlayer;
import static com.recursive_pineapple.matter_manipulator.common.utils.Mods.GregTech;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

import com.gtnewhorizon.gtnhlib.util.CoordinatePacker;
import com.recursive_pineapple.matter_manipulator.MMMod;
import com.recursive_pineapple.matter_manipulator.asm.Optional;
import com.recursive_pineapple.matter_manipulator.common.building.BlockAnalyzer.IBlockApplyContext;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator.ManipulatorTier;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState.PlaceMode;
import com.recursive_pineapple.matter_manipulator.common.networking.Messages;
import com.recursive_pineapple.matter_manipulator.common.networking.SoundResource;
import com.recursive_pineapple.matter_manipulator.common.utils.BigFluidStack;
import com.recursive_pineapple.matter_manipulator.common.utils.BigItemStack;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods.Names;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.booleans.BooleanObjectImmutablePair;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

/**
 * Handles all building logic.
 */
public class PendingBuild extends AbstractBuildable {

    private final Deque<PendingBlock> pendingBlocks;
    private final HashSet<Long> visited = new HashSet<>();

    private final LongList errors = new LongArrayList();
    private final LongList warnings = new LongArrayList();

    public PendingBuild(
        EntityPlayer player,
        MMState state,
        ManipulatorTier tier,
        List<PendingBlock> pendingBlocks
    ) {
        super(player, state, tier);
        this.pendingBlocks = new ArrayDeque<>(pendingBlocks);
    }

    @Override
    public void tryPlaceBlocks(ItemStack stack, EntityPlayer player) {
        resetWarnings();
        refillPower(stack);

        List<PendingBlock> toPlace = new ArrayList<>(tier.placeSpeed);

        Integer lastChunkX = null, lastChunkZ = null;
        int shuffleCount = 0;

        World world = player.worldObj;
        ProxiedWorld proxiedWorld = new ProxiedWorld(world);

        PendingBuildApplyContext applyContext = new PendingBuildApplyContext(stack);

        BlockSpec pooled = new BlockSpec();

        // check every pending block that's left
        while (toPlace.size() < tier.placeSpeed && !pendingBlocks.isEmpty()) {
            PendingBlock next = pendingBlocks.getFirst();

            int x = next.x, y = next.y, z = next.z;

            int chunkX = x >> 4;
            int chunkZ = z >> 4;

            // if this block's chunk isn't loaded, ignore it completely
            if (!Objects.equals(chunkX, lastChunkX) || !Objects.equals(chunkZ, lastChunkZ)) {
                if (!world.getChunkProvider().chunkExists(chunkX, chunkZ)) {
                    pendingBlocks.removeFirst();
                    continue;
                } else {
                    lastChunkX = chunkX;
                    lastChunkZ = chunkZ;
                }
            }

            if (y < 0 || y > 255) {
                pendingBlocks.removeFirst();
                continue;
            }

            // if this block is protected, ignore it completely and print a warning
            if (!isEditable(world, x, y, z)) {
                pendingBlocks.removeFirst();
                continue;
            }

            // if this block is different from the last one, stop checking blocks
            // since the pending blocks are sorted by their contained block, this is usually true
            if (!toPlace.isEmpty() && !next.spec.isEquivalent(toPlace.get(0).spec)) {
                break;
            }

            PendingBlock existing = PendingBlock.fromBlock(world, x, y, z);

            if (next.spec.isAir() && existing.getBlock().isAir(world, x, y, z)) {
                pendingBlocks.removeFirst();
                continue;
            }

            existing.analyze(world.getTileEntity(x, y, z), PendingBlock.ANALYZE_ARCH);

            // if the existing block is the same as the one we're trying to place, just apply its tile data
            if (PendingBlock.areEquivalent(existing, next)) {
                PendingBlock block = pendingBlocks.removeFirst();

                if (supportsConfiguring()) {
                    applyContext.pendingBlock = block;
                    block.apply(applyContext, world);
                    playSound(world, x, y, z, SoundResource.MOB_ENDERMEN_PORTAL);
                }

                continue;
            }

            // checks if the existing block is removable
            boolean canPlace = switch (state.config.removeMode) {
                case NONE -> existing.getBlock().isAir(world, x, y, z);
                case REPLACEABLE -> existing.getBlock().isReplaceable(world, x, y, z);
                case ALL -> true;
            };

            canPlace &= existing.getBlock().getBlockHardness(world, x, y, z) >= 0;

            // we don't want to remove these even though they'll never be placed because we want to see how many blocks
            // couldn't be placed
            if (!canPlace) {
                pendingBlocks.addLast(pendingBlocks.removeFirst());
                shuffleCount++;

                if (shuffleCount > pendingBlocks.size()) {
                    break;
                } else {
                    continue;
                }
            }

            // if there's an existing block then remove it if possible
            if (!existing.getBlock().isAir(world, x, y, z)) {
                if (!state.hasCap(ItemMatterManipulator.ALLOW_REMOVING)) {
                    pendingBlocks.removeFirst();
                    continue;
                }

                if (!tryConsumePower(stack, world, x, y, z, existing.spec)) {
                    sendErrorToPlayer(player, StatCollector.translateToLocal("mm.info.error.out_of_eu"));
                    break;
                }
            }

            proxiedWorld.airX = x;
            proxiedWorld.airY = y;
            proxiedWorld.airZ = z;

            // check block dependencies for things like levers
            // if we can't place this block, shuffle it to the back of the list
            if (!next.getBlock().canPlaceBlockAt(proxiedWorld, next.x, next.y, next.z)) {
                pendingBlocks.addLast(pendingBlocks.removeFirst());
                shuffleCount++;

                // if we've shuffled every block, then we'll never be able to place any of them
                if (shuffleCount > pendingBlocks.size()) {
                    break;
                } else {
                    continue;
                }
            }

            if (!tryConsumePower(stack, world, x, y, z, next.spec)) {
                sendErrorToPlayer(player, StatCollector.translateToLocal("mm.info.error.out_of_eu"));
                break;
            }

            long coord = CoordinatePacker.pack(x, y, z);

            if (!visited.add(coord)) {
                MMMod.LOG.warn("Tried to place block twice! " + next);
                pendingBlocks.removeFirst();
                continue;
            }

            toPlace.add(pendingBlocks.removeFirst());
        }

        // check if we could place any blocks
        if (toPlace.isEmpty()) {
            if (!pendingBlocks.isEmpty()) {
                sendErrorToPlayer(
                    player,
                    StatCollector.translateToLocalFormatted(
                        "mm.info.error.could_not_place",
                        pendingBlocks.size()
                    )
                );
            } else {
                sendInfoToPlayer(player, StatCollector.translateToLocal("mm.info.finished_placing"));
            }

            actuallyGivePlayerStuff();
            playSounds();
            return;
        }

        PendingBlock first = toPlace.get(0);

        ItemStack perBlock = first.getStack();
        long total = 0;
        BigItemStack extracted = null;

        // if the block we're placing isn't free (ae cable busses) we need to consume it
        if (!first.isFree()) {
            total = toPlace.size() * (long) perBlock.stackSize;

            List<BigItemStack> extractedStacks = tryConsumeItems(Arrays.asList(BigItemStack.create(perBlock).setStackSize(total)), CONSUME_PARTIAL).right();

            extracted = extractedStacks.isEmpty() ? null : extractedStacks.get(0);

            if (extracted == null) {
                sendWarningToPlayer(
                    player,
                    StatCollector.translateToLocalFormatted(
                        "mm.info.warning.could_not_find",
                        toPlace.size()
                    )
                );
                sendWarningToPlayer(
                    player,
                    String.format(
                        "  %s x %d",
                        first.getDisplayName(),
                        total
                    )
                );

                for (PendingBlock pending : toPlace) {
                    pendingBlocks.add(pending);

                    long coord = CoordinatePacker.pack(pending.x, pending.y, pending.z);

                    visited.remove(coord);
                }

                toPlace.clear();
            }
        }

        int i = 0;
        for (; i < toPlace.size(); i++) {
            PendingBlock pending = toPlace.get(i);

            int x = pending.x;
            int y = pending.y;
            int z = pending.z;

            playSound(world, x, y, z, SoundResource.MOB_ENDERMEN_PORTAL);

            int metadata = pending.spec.getBlockMeta();

            BlockSpec existing = BlockSpec.fromBlock(pooled, world, x, y, z);

            if (existing.equals(pending.spec)) {
                // somehow the block already exists, despite us checking to make sure that this shouldn't happen
                // just to be safe, we only consume the item when we actually place something
                if (supportsConfiguring()) {
                    applyContext.pendingBlock = pending;
                    pending.apply(applyContext, world);
                }

                world.notifyBlockOfNeighborChange(x, y, z, Blocks.air);
                continue;
            }

            if (extracted != null && extracted.stackSize < perBlock.stackSize) {
                break;
            }

            if (!existing.isAir()) {
                removeBlock(world, x, y, z, existing);
            }

            if (!pending.spec.isAir()) {
                Block block = pending.getBlock();

                if (pending.getItem() instanceof ItemBlock itemBlock) {
                    itemBlock.placeBlockAt(
                        perBlock,
                        player,
                        player.worldObj,
                        x,
                        y,
                        z,
                        getDefaultPlaceSide(pending.spec).ordinal(),
                        0,
                        0,
                        0,
                        metadata
                    );
                } else {
                    if (!world.setBlock(x, y, z, block, metadata, 3)) {
                        continue;
                    }

                    if (world.getBlock(x, y, z) == block) {
                        block.onBlockPlacedBy(world, x, y, z, player, stack);
                        block.onPostBlockPlaced(world, x, y, z, metadata);
                    }
                }
            }

            if (extracted != null) {
                extracted.stackSize -= perBlock.stackSize;
            }

            applyContext.pendingBlock = pending;
            pending.apply(applyContext, world);
        }

        if (extracted != null && i < toPlace.size()) {
            sendWarningToPlayer(
                player,
                StatCollector.translateToLocalFormatted(
                    "mm.info.warning.could_not_find",
                    toPlace.size() - i
                )
            );
            sendWarningToPlayer(
                player,
                String.format(
                    "  %s x %d",
                    first.getDisplayName(),
                    total - (toPlace.size() - i) * perBlock.stackSize
                )
            );
        }

        sendInfoToPlayer(
            player,
            StatCollector.translateToLocalFormatted(
                "mm.info.placed_remaining",
                i,
                pendingBlocks.size()
            )
        );

        if (extracted != null && extracted.stackSize >= perBlock.stackSize) {
            // extra stuff left over somehow
            MMMod.LOG.error(
                "Didn't consume enough items! " + perBlock
                    .getDisplayName() + "; expected to consume " + total + ", but consumed " + (total - extracted.stackSize)
            );
            givePlayerItems(extracted.toStacks().toArray(new ItemStack[0]));
        }

        for (; i < toPlace.size(); i++) {
            PendingBlock pending = toPlace.get(i);

            pendingBlocks.add(pending);

            long coord = CoordinatePacker.pack(pending.x, pending.y, pending.z);

            visited.remove(coord);
        }

        actuallyGivePlayerStuff();
        playSounds();
    }

    @Override
    public void onStopped() {
        if (pendingItems.size() > 0 || pendingFluids.size() > 0) {
            MMMod.LOG.error("Build stopped without delivering all items! There's a bug somewhere!");
        }

        actuallyGivePlayerStuff();

        Messages.BuildStatus.sendToPlayer((EntityPlayerMP) player, Pair.of(errors, warnings));
    }

    private boolean supportsConfiguring() {
        // self-explanatory
        if (state.hasCap(ItemMatterManipulator.ALLOW_CONFIGURING)) return true;

        // lower tiers support cables, but not copying
        // since exchanging or placing cables requires configuring, we need to return true for these two
        if (state.config.placeMode == PlaceMode.EXCHANGING) return true;
        if (state.config.placeMode == PlaceMode.CABLES) return true;

        return false;
    }

    private ForgeDirection getDefaultPlaceSide(ImmutableBlockSpec spec) {
        if (Mods.GregTech.isModLoaded() && MMUtils.isGTCable(spec)) { return ForgeDirection.UNKNOWN; }

        return ForgeDirection.NORTH;
    }

    public class PendingBuildApplyContext implements IBlockApplyContext {

        public static final double EU_PER_ACTION = 8192;

        public ItemStack manipulatorItemStack;
        public PendingBlock pendingBlock;

        public PendingBuildApplyContext(ItemStack manipulatorItemStack) {
            this.manipulatorItemStack = manipulatorItemStack;
        }

        @Override
        public World getWorld() {
            return player.worldObj;
        }

        @Override
        public int getX() {
            return pendingBlock.x;
        }

        @Override
        public int getY() {
            return pendingBlock.y;
        }

        @Override
        public int getZ() {
            return pendingBlock.z;
        }

        @Override
        public TileEntity getTileEntity() {
            if (pendingBlock.isInWorld(player.worldObj)) {
                return player.worldObj.getTileEntity(pendingBlock.x, pendingBlock.y, pendingBlock.z);
            } else {
                return null;
            }
        }

        @Override
        public EntityPlayer getRealPlayer() {
            return player;
        }

        @Override
        public boolean tryApplyAction(double complexity) {
            return PendingBuild.this.tryConsumePower(
                manipulatorItemStack,
                pendingBlock.x,
                pendingBlock.y,
                pendingBlock.z,
                EU_PER_ACTION * complexity
            );
        }

        @Override
        public BooleanObjectImmutablePair<List<BigItemStack>> tryConsumeItems(List<BigItemStack> items, int flags) {
            return PendingBuild.this.tryConsumeItems(items, flags);
        }

        @Override
        public void givePlayerItems(List<BigItemStack> items) {
            PendingBuild.this.givePlayerItems(items);
        }

        @Override
        public void givePlayerFluids(List<BigFluidStack> fluids) {
            PendingBuild.this.givePlayerFluids(fluids);
        }

        @Override
        public void warn(String message) {
            String blockName = null;

            if (pendingBlock.isInWorld(player.worldObj)) {
                if (GregTech.isModLoaded()) blockName = getGTBlockName(pendingBlock);

                if (blockName == null) {
                    BlockSpec spec = BlockSpec.fromBlock(null, player.worldObj, pendingBlock.x, pendingBlock.y, pendingBlock.z);

                    if (InteropConstants.AE_BLOCK_CABLE.matches(spec)) {
                        blockName = InteropConstants.AE_BLOCK_CABLE.toSpec().getDisplayName();
                    } else {
                        blockName = spec.getDisplayName();
                    }
                }
            }

            sendWarningToPlayer(
                player,
                StatCollector.translateToLocalFormatted(
                    "mm.info.warning",
                    pendingBlock.x,
                    pendingBlock.y,
                    pendingBlock.z,
                    blockName != null ? " (" + blockName + ")" : "",
                    message
                )
            );

            PendingBuild.this.warnings.add(CoordinatePacker.pack(pendingBlock.x, pendingBlock.y, pendingBlock.z));
        }

        @Override
        public void error(String message) {

            String blockName = null;

            if (pendingBlock.isInWorld(player.worldObj)) {
                if (GregTech.isModLoaded()) blockName = getGTBlockName(pendingBlock);

                BlockSpec spec = BlockSpec.fromBlock(null, player.worldObj, pendingBlock.x, pendingBlock.y, pendingBlock.z);

                if (InteropConstants.AE_BLOCK_CABLE.matches(spec)) {
                    blockName = InteropConstants.AE_BLOCK_CABLE.toSpec().getDisplayName();
                } else {
                    blockName = spec.getDisplayName();
                }
            }

            sendErrorToPlayer(
                player,
                StatCollector.translateToLocalFormatted(
                    "mm.info.error",
                    pendingBlock.x,
                    pendingBlock.y,
                    pendingBlock.z,
                    blockName != null ? " (" + blockName + ")" : "",
                    message
                )
            );

            PendingBuild.this.errors.add(CoordinatePacker.pack(pendingBlock.x, pendingBlock.y, pendingBlock.z));
        }
    }

    @Optional(Names.GREG_TECH)
    private String getGTBlockName(PendingBlock pendingBlock) {
        if (player.worldObj.getTileEntity(pendingBlock.x, pendingBlock.y, pendingBlock.z) instanceof IGregTechTileEntity igte) {
            IMetaTileEntity imte = igte.getMetaTileEntity();
            if (imte != null) { return imte.getLocalName(); }
        }

        return null;
    }
}
