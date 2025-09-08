package com.recursive_pineapple.matter_manipulator.common.building;

import static com.recursive_pineapple.matter_manipulator.common.utils.MMUtils.sendErrorToPlayer;
import static com.recursive_pineapple.matter_manipulator.common.utils.MMUtils.sendInfoToPlayer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.IIC2Enet;
import gregtech.api.metatileentity.BaseMetaTileEntity;

import com.github.bsideup.jabel.Desugar;
import com.recursive_pineapple.matter_manipulator.asm.Optional;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator.ManipulatorTier;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.Location;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMConfig;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState;
import com.recursive_pineapple.matter_manipulator.common.networking.SoundResource;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods.Names;
import com.recursive_pineapple.matter_manipulator.mixin.BlockCaptureDrops;

import WayofTime.alchemicalWizardry.api.event.TeleposeEvent;
import codechicken.multipart.MultipartHelper;
import codechicken.multipart.TileMultipart;
import it.unimi.dsi.fastutil.Pair;
import tectech.thing.metaTileEntity.pipe.MTEPipeData;
import tectech.thing.metaTileEntity.pipe.MTEPipeLaser;

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
        refillPower(stack);

        if (moves == null) {
            initMoves();
        }

        World world = player.worldObj;

        BlockSpec source = new BlockSpec();
        BlockSpec target = new BlockSpec();

        int ops = 0;
        var iter = moves.listIterator(moves.size());

        ArrayList<Pair<Location, Location>> shuffled = new ArrayList<>();

        // try to move `placeSpeed` blocks from here to there
        while (ops < tier.placeSpeed && iter.hasPrevious()) {
            Pair<Location, Location> move = iter.previous();

            Location s = move.left();
            Location d = move.right();

            // if either block is protected, ignore them completely and print a warning
            if (!isEditable(world, s.x, s.y, s.z) || !isEditable(world, d.x, d.y, d.z)) {
                sendErrorToPlayer(
                    player,
                    StatCollector.translateToLocalFormatted(
                        "mm.info.error.could_not_move_protected_block",
                        s.x,
                        s.y,
                        s.z,
                        source.getDisplayName()
                    )
                );
                iter.remove();
                continue;
            }

            BlockSpec.fromBlock(source, world, s.x, s.y, s.z);

            if (source.isAir()) {
                iter.remove();
                continue;
            }

            if (source.getBlock().getBlockHardness(world, s.x, s.y, s.z) < 0) {
                sendErrorToPlayer(
                    player,
                    StatCollector.translateToLocalFormatted(
                        "mm.info.error.could_not_move_invulnerable_block",
                        s.x,
                        s.y,
                        s.z,
                        source.getDisplayName()
                    )
                );
                iter.remove();
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
                sendErrorToPlayer(
                    player,
                    StatCollector.translateToLocalFormatted(
                        "mm.info.error.could_not_move_blocked_block",
                        d.x,
                        d.y,
                        d.z,
                        source.getDisplayName()
                    )
                );
                iter.remove();
                continue;
            }

            // remove the existing block if needed
            if (!target.getBlock().isAir(world, d.x, d.y, d.z)) {
                if (!tryConsumePower(stack, world, d.x, d.y, d.z, target)) {
                    sendErrorToPlayer(player, StatCollector.translateToLocal("mm.info.error.out_of_eu"));
                    break;
                }

                removeBlock(world, d.x, d.y, d.z, target);
            }

            // if we can't move the source block then skip it for now
            if (!source.getBlock().canPlaceBlockAt(world, d.x, d.y, d.z)) {
                shuffled.add(move);
                iter.remove();
                continue;
            }

            if (!tryConsumePower(stack, world, s.x, s.y, s.z, source)) {
                sendErrorToPlayer(player, StatCollector.translateToLocal("mm.info.error.out_of_eu"));
                break;
            }

            // try to move the source block into the (now empty) target block
            if (!moveBlock(world, s, source, d, target)) {
                sendErrorToPlayer(
                    player,
                    StatCollector.translateToLocalFormatted(
                        "mm.info.error.could_not_move_block",
                        s.x,
                        s.y,
                        s.z,
                        source.getDisplayName()
                    )
                );
            }

            playSound(world, s.x, s.y, s.z, SoundResource.MOB_ENDERMEN_PORTAL);
            playSound(world, d.x, d.y, d.z, SoundResource.MOB_ENDERMEN_PORTAL);

            iter.remove();
            ops++;
        }

        moves.addAll(shuffled);

        playSounds();
        actuallyGivePlayerStuff();

        if (ops > 0) {
            sendInfoToPlayer(
                player,
                StatCollector.translateToLocalFormatted(
                    "mm.info.process_move",
                    ops,
                    moves.size()
                )
            );
        } else {
            sendInfoToPlayer(
                player,
                StatCollector.translateToLocal("mm.info.finished_move")
            );
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

        MMConfig.VoxelAABB cut = new MMConfig.VoxelAABB(startA.toVec(), startB.toVec());
        MMConfig.VoxelAABB paste = cut.clone().moveOrigin(dest.toVec());

        if (cut.toBoundingBox().intersectsWith(paste.toBoundingBox())) {
            MMUtils.sendErrorToPlayer(player, StatCollector.translateToLocal("mm.info.error.move_overlapping"));
            return;
        }

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
    @SuppressWarnings("unchecked")
    public static boolean moveBlock(World world, Location s, BlockSpec spec1, Location d, BlockSpec spec2) {

        World worldS = world.provider.dimensionId == s.worldId ? world : s.getWorld();
        int sx = s.x;
        int sy = s.y;
        int sz = s.z;
        World worldD = world.provider.dimensionId == d.worldId ? world : d.getWorld();
        int dx = d.x;
        int dy = d.y;
        int dz = d.z;

        Block blockS = worldS.getBlock(sx, sy, sz);
        Block blockD = worldD.getBlock(dx, dy, dz);

        if (blockS.equals(Blocks.air) && blockD.equals(Blocks.air)) return false;

        if (Mods.BloodMagic.isModLoaded()) {
            if (!allowTelepose(worldS, worldD, s, spec1, d, spec2)) return false;
        }

        BlockMover<Object> source = (BlockMover<Object>) getBlockMover(worldS, sx, sy, sz);
        BlockMover<Object> dest = (BlockMover<Object>) getBlockMover(worldD, dx, dy, dz);

        BlockCaptureDrops.captureDrops(blockS);
        BlockCaptureDrops.captureDrops(blockD);
        BlockCaptureDrops.captureDrops(world);

        Object sourceState = source.remove(worldS, sx, sy, sz);
        Object destState = dest.remove(worldD, dx, dy, dz);

        source.place(worldD, dx, dy, dz, sourceState);
        dest.place(worldS, sx, sy, sz, destState);

        // delete any items that were dropped
        BlockCaptureDrops.stopCapturingDrops(blockS);
        BlockCaptureDrops.stopCapturingDrops(blockD);
        BlockCaptureDrops.stopCapturingDrops(world);

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
        return !MinecraftForge.EVENT_BUS.post(evt);
    }

    private static BlockMover<?> getBlockMover(World world, int x, int y, int z) {
        for (BlockMovers blockMover : BlockMovers.values()) {
            if (blockMover.blockMover.canMove(world, x, y, z)) return blockMover.blockMover;
        }

        throw new IllegalStateException();
    }

    private enum BlockMovers {

        @Optional(Names.FORGE_MULTIPART)
        FMP(FMPBlockMover.INSTANCE),
        @Optional(Names.GREG_TECH_NH)
        GT(GTBlockMover.INSTANCE),
        Standard(StandardBlockMover.INSTANCE);

        public final BlockMover<?> blockMover;

        BlockMovers(BlockMover<?> blockMover) {
            this.blockMover = blockMover;
        }
    }

    interface BlockMover<State> {

        boolean canMove(World world, int x, int y, int z);

        State remove(World world, int x, int y, int z);

        void place(World world, int x, int y, int z, State state);
    }

    @Desugar
    private record StandardBlock(Block block, int meta, NBTTagCompound tileData) {

    }

    private static class StandardBlockMover implements BlockMover<StandardBlock> {

        public static final StandardBlockMover INSTANCE = new StandardBlockMover();

        @Override
        public boolean canMove(World world, int x, int y, int z) {
            return true;
        }

        @Override
        public StandardBlock remove(World world, int x, int y, int z) {
            Block block = world.getBlock(x, y, z);
            int meta = world.getBlockMetadata(x, y, z);
            NBTTagCompound tag = null;

            TileEntity te = world.getTileEntity(x, y, z);

            if (te != null) {
                tag = new NBTTagCompound();
                te.writeToNBT(tag);
            }

            world.setTileEntity(x, y, z, null);
            world.setBlockToAir(x, y, z);

            return new StandardBlock(block, meta, tag);
        }

        @Override
        public void place(World world, int x, int y, int z, StandardBlock standardBlock) {
            world.setBlock(x, y, z, standardBlock.block, standardBlock.meta, 3);

            if (standardBlock.tileData != null) {
                TileEntity te = TileEntity.createAndLoadEntity(standardBlock.tileData);

                te.xCoord = x;
                te.yCoord = y;
                te.zCoord = z;

                world.setTileEntity(x, y, z, te);
            }
        }
    }

    private static class GTBlockMover extends StandardBlockMover {

        public static final GTBlockMover INSTANCE = new GTBlockMover();

        @Override
        public boolean canMove(World world, int x, int y, int z) {
            return world.getTileEntity(x, y, z) instanceof IGregTechTileEntity;
        }

        @Override
        public StandardBlock remove(World world, int x, int y, int z) {
            // Because GT uses this to call MTE.onRemoval() :doom:
            world.getBlock(x, y, z).getDrops(world, x, y, z, world.getBlockMetadata(x, y, z), 0);

            return super.remove(world, x, y, z);
        }

        @Override
        public void place(World world, int x, int y, int z, StandardBlock standardBlock) {
            super.place(world, x, y, z, standardBlock);

            TileEntity te = world.getTileEntity(x, y, z);

            if (te instanceof IGregTechTileEntity igte) {
                if (igte instanceof BaseMetaTileEntity bmte) {
                    bmte.setCableUpdateDelay(100);
                }

                IMetaTileEntity imte = igte.getMetaTileEntity();

                if (imte instanceof MTEPipeLaser laserPipe) {
                    laserPipe.updateNeighboringNetworks();
                }

                if (imte instanceof MTEPipeData dataPipe) {
                    dataPipe.updateNeighboringNetworks();
                }
            }

            if (te instanceof IIC2Enet enet) {
                enet.doEnetUpdate();
            }
        }
    }

    @Desugar
    private record FMPBlock(Block block, int meta, TileMultipart tile) {

    }

    private static class FMPBlockMover implements BlockMover<FMPBlock> {

        public static final FMPBlockMover INSTANCE = new FMPBlockMover();

        @Override
        public boolean canMove(World world, int x, int y, int z) {
            return world.getTileEntity(x, y, z) instanceof TileMultipart;
        }

        @Override
        public FMPBlock remove(World world, int x, int y, int z) {
            Block block = world.getBlock(x, y, z);
            int meta = world.getBlockMetadata(x, y, z);
            TileMultipart te = (TileMultipart) world.getTileEntity(x, y, z);

            world.setTileEntity(x, y, z, null);
            world.setBlockToAir(x, y, z);

            return new FMPBlock(block, meta, te);
        }

        @Override
        public void place(World world, int x, int y, int z, FMPBlock fmpBlock) {
            world.setBlock(x, y, z, fmpBlock.block, fmpBlock.meta, 3);

            fmpBlock.tile.xCoord = x;
            fmpBlock.tile.yCoord = y;
            fmpBlock.tile.zCoord = z;

            fmpBlock.tile.validate();
            world.setTileEntity(x, y, z, fmpBlock.tile);

            fmpBlock.tile.onMoved();

            world.markBlockForUpdate(x, y, z);
            world.func_147451_t(x, y, z);
            fmpBlock.tile.markDirty();
            fmpBlock.tile.markRender();
            MultipartHelper.sendDescPacket(world, fmpBlock.tile);
        }
    }
}
