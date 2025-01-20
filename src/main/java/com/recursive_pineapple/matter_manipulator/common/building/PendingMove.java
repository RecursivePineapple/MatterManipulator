package com.recursive_pineapple.matter_manipulator.common.building;

import static com.recursive_pineapple.matter_manipulator.common.utils.MMUtils.sendInfoToPlayer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.IIC2Enet;
import gregtech.api.metatileentity.BaseMetaTileEntity;

import com.recursive_pineapple.matter_manipulator.asm.Optional;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator.ManipulatorTier;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.Location;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState;
import com.recursive_pineapple.matter_manipulator.common.networking.SoundResource;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods.Names;
import com.recursive_pineapple.matter_manipulator.mixin.BlockCaptureDrops;

import WayofTime.alchemicalWizardry.api.event.TeleposeEvent;
import it.unimi.dsi.fastutil.Pair;

/**
 * Handles all moving logic.
 */
public class PendingMove extends AbstractBuildable {

    private List<Pair<Location, Location>> moves = null;

    public PendingMove(EntityPlayer player, MMState state, ManipulatorTier tier) {
        super(player, state, tier);
    }

    @Override
    public void tryPlaceBlocks(ItemStack stack, EntityPlayer player) {
        resetWarnings();

        if (moves == null) {
            initMoves();
        }

        World world = player.worldObj;

        BlockSpec source = new BlockSpec();
        BlockSpec target = new BlockSpec();

        int ops = 0;
        var iter = moves.listIterator(moves.size());

        // try to move `placeSpeed` blocks from here to there
        while (ops < tier.placeSpeed && iter.hasPrevious()) {
            Pair<Location, Location> move = iter.previous();

            Location s = move.left();
            Location d = move.right();

            // if either block is protected, ignore them completely and print a warning
            if (!isEditable(world, s.x, s.y, s.z) || !isEditable(world, d.x, d.y, d.z)) {
                continue;
            }

            BlockSpec.fromBlock(source, world, s.x, s.y, s.z);

            if (source.isAir()) {
                iter.remove();
                continue;
            }

            if (source.getBlock().getBlockHardness(world, s.x, s.y, s.z) < 0) {
                MMUtils.sendErrorToPlayer(
                    player,
                    String.format("Could not move invulnerable source block X=%d, Y=%d, Z=%d", s.x, s.y, s.z)
                );
                continue;
            }

            BlockSpec.fromBlock(target, world, d.x, d.y, d.z);

            // check if we can remove the existing target block
            boolean canPlace = switch (state.config.removeMode) {
                case NONE -> target.getBlock().isAir(world, d.x, d.y, d.z);
                case REPLACEABLE -> target.getBlock().isReplaceable(world, d.x, d.y, d.z);
                case ALL -> true;
            };

            canPlace &= target.getBlock().getBlockHardness(world, d.x, d.y, d.z) >= 0;

            if (!canPlace) {
                MMUtils.sendErrorToPlayer(
                    player,
                    String.format("Destination was blocked for source block X=%d, Y=%d, Z=%d", d.x, d.y, d.z)
                );
                continue;
            }

            // remove the existing block if needed
            if (!target.getBlock().isAir(world, d.x, d.y, d.z)) {
                if (!tryConsumePower(stack, world, d.x, d.y, d.z, target)) {
                    MMUtils.sendErrorToPlayer(player, "Matter Manipulator ran out of EU.");
                    break;
                }

                removeBlock(world, d.x, d.y, d.z, target);
            }

            // if we can't move the source block then skip it for now
            if (!source.getBlock().canPlaceBlockAt(world, d.x, d.y, d.z)) {
                continue;
            }

            if (!tryConsumePower(stack, world, s.x, s.y, s.z, source)) {
                MMUtils.sendErrorToPlayer(player, "Matter Manipulator ran out of EU.");
                break;
            }

            // try to move the source block into the (now empty) target block
            if (!swapBlocks(world, s, source, d, target)) {
                MMUtils.sendErrorToPlayer(
                    player,
                    String.format("Could not move block X=%d, Y=%d, Z=%d: %s", s.x, s.y, s.z, source.getDisplayName())
                );
            }

            playSound(world, s.x, s.y, s.z, SoundResource.MOB_ENDERMEN_PORTAL);
            playSound(world, d.x, d.y, d.z, SoundResource.MOB_ENDERMEN_PORTAL);

            iter.remove();
            ops++;
        }

        playSounds();
        actuallyGivePlayerStuff();

        if (ops > 0) {
            sendInfoToPlayer(player, "Moved " + ops + " blocks (" + moves.size() + " remaining)");
        } else {
            sendInfoToPlayer(player, "Finished moving blocks.");
        }
    }

    @Override
    public void onStopped() {

    }

    private void initMoves() {
        moves = new ArrayList<>();

        Location startA = state.config.coordA;
        Location startB = state.config.coordB;
        Location dest = state.config.coordC;

        int x1 = startA.x;
        int y1 = startA.y;
        int z1 = startA.z;
        int x2 = startB.x;
        int y2 = startB.y;
        int z2 = startB.z;

        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        int maxZ = Math.max(z1, z2);

        int worldId = startA.worldId;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int dX = x - x1;
                    int dY = y - y1;
                    int dZ = z - z1;

                    moves.add(
                        Pair.of(
                            new Location(worldId, x, y, z),
                            new Location(worldId, dest.x + dX, dest.y + dY, dest.z + dZ)
                        )
                    );
                }
            }
        }
    }

    // 'borrowed' from
    // https://github.com/GTNewHorizons/BloodMagic/blob/master/src/main/java/WayofTime/alchemicalWizardry/common/block/BlockTeleposer.java#L158
    public static boolean swapBlocks(World world, Location s, BlockSpec spec1, Location d, BlockSpec spec2) {

        World worldS = world.provider.dimensionId == s.worldId ? world : s.getWorld();
        int sx = s.x;
        int sy = s.y;
        int sz = s.z;
        World worldD = world.provider.dimensionId == d.worldId ? world : d.getWorld();
        int dx = d.x;
        int dy = d.y;
        int dz = d.z;

        TileEntity tileEntityS = worldS.getTileEntity(sx, sy, sz);
        TileEntity tileEntityD = worldD.getTileEntity(dx, dy, dz);

        NBTTagCompound tagS = new NBTTagCompound();
        NBTTagCompound tagD = new NBTTagCompound();

        if (tileEntityS != null) {
            tileEntityS.writeToNBT(tagS);
        }

        if (tileEntityD != null) {
            tileEntityD.writeToNBT(tagD);
        }

        Block blockS = worldS.getBlock(sx, sy, sz);
        Block blockD = worldD.getBlock(dx, dy, dz);

        if (blockS.equals(Blocks.air) && blockD.equals(Blocks.air)) { return false; }

        int metaS = worldS.getBlockMetadata(sx, sy, sz);
        int metaD = worldD.getBlockMetadata(dx, dy, dz);

        if (Mods.BloodMagic.isModLoaded()) {
            if (!allowTelepose(worldS, worldD, s, spec1, d, spec2)) { return false; }
        }

        // CLEAR TILES
        if (blockD != null) {
            TileEntity tileToSet = blockD.createTileEntity(worldD, metaD);

            worldD.setTileEntity(dx, dy, dz, tileToSet);
        }

        if (blockS != null) {
            TileEntity tileToSet = blockS.createTileEntity(worldS, metaS);

            worldS.setTileEntity(sx, sy, sz, tileToSet);
        }

        // TILES CLEARED
        BlockCaptureDrops.captureDrops(blockS);
        BlockCaptureDrops.captureDrops(blockD);

        worldD.setBlock(dx, dy, dz, blockS, metaS, 3);

        if (tileEntityS != null) {
            TileEntity newTileEntityI = TileEntity.createAndLoadEntity(tagS);

            worldD.setTileEntity(dx, dy, dz, newTileEntityI);

            newTileEntityI.xCoord = dx;
            newTileEntityI.yCoord = dy;
            newTileEntityI.zCoord = dz;
        }

        worldS.setBlock(sx, sy, sz, blockD, metaD, 3);

        // delete any items that were dropped
        BlockCaptureDrops.stopCapturingDrops(blockS);
        BlockCaptureDrops.stopCapturingDrops(blockD);

        if (tileEntityD != null) {
            TileEntity newTileEntityF = TileEntity.createAndLoadEntity(tagD);

            worldS.setTileEntity(sx, sy, sz, newTileEntityF);

            newTileEntityF.xCoord = sx;
            newTileEntityF.yCoord = sy;
            newTileEntityF.zCoord = sz;

            if (Mods.GregTech.isModLoaded()) {
                if (newTileEntityF instanceof IGregTechTileEntity igte && igte.getMetaTileEntity() instanceof BaseMetaTileEntity bmte) {
                    bmte.setCableUpdateDelay(100);
                }

                if (newTileEntityF instanceof IIC2Enet enet) {
                    enet.doEnetUpdate();
                }
            }
        }

        return true;
    }

    @Optional(Names.BLOOD_MAGIC)
    private static boolean allowTelepose(World worldI, World worldF, Location s, BlockSpec spec1, Location d, BlockSpec spec2) {
        TeleposeEvent evt = new TeleposeEvent(
            worldI,
            s.x,
            s.y,
            s.z,
            spec1.getBlock(),
            spec2.getBlockMeta(),
            worldF,
            d.x,
            d.y,
            d.z,
            spec2.getBlock(),
            spec2.getBlockMeta()
        );
        if (MinecraftForge.EVENT_BUS.post(evt)) return false;
        return true;
    }
}
