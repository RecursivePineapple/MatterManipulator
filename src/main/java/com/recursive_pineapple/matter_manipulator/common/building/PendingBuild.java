package com.recursive_pineapple.matter_manipulator.common.building;

import static com.recursive_pineapple.matter_manipulator.common.utils.MMUtils.sendErrorToPlayer;
import static com.recursive_pineapple.matter_manipulator.common.utils.Mods.GregTech;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.gtnewhorizon.gtnhlib.util.CoordinatePacker;
import com.recursive_pineapple.matter_manipulator.MMMod;
import com.recursive_pineapple.matter_manipulator.asm.Optional;
import com.recursive_pineapple.matter_manipulator.common.building.BlockAnalyzer.IBlockApplyContext;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.PendingBlock;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator.ManipulatorTier;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState.PlaceMode;
import com.recursive_pineapple.matter_manipulator.common.networking.SoundResource;
import com.recursive_pineapple.matter_manipulator.common.utils.BigItemStack;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods.Names;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * Handles all building logic.
 */
public class PendingBuild extends AbstractBuildable {

    private LinkedList<PendingBlock> pendingBlocks;
    private LongSet visited = new LongArraySet();

    public PendingBuild(EntityPlayer player, MMState state, ManipulatorTier tier,
        LinkedList<PendingBlock> pendingBlocks) {
        super(player, state, tier);
        this.pendingBlocks = pendingBlocks;
    }

    @Override
    public void tryPlaceBlocks(ItemStack stack, EntityPlayer player) {
        List<PendingBlock> toPlace = new ArrayList<>(tier.placeSpeed);

        Integer lastChunkX = null, lastChunkZ = null;
        int shuffleCount = 0;

        World world = player.worldObj;

        PendingBuildApplyContext applyContext = new PendingBuildApplyContext(stack);

        // check every pending block that's left
        while (toPlace.size() < tier.placeSpeed && pendingBlocks.size() > 0) {
            PendingBlock next = pendingBlocks.getFirst();

            int x = next.x, y = next.y, z = next.z;

            int chunkX = x >> 4;
            int chunkZ = z >> 4;

            long coord = CoordinatePacker.pack(x, y, z);

            if (!visited.add(coord)) {
                MMMod.LOG.warn("Tried to place block twice! " + next);
                pendingBlocks.removeFirst();
                continue;
            }

            // if this block's chunk isn't loaded, ignore it completely
            if (!Objects.equals(chunkX, lastChunkX) || !Objects.equals(chunkZ, lastChunkZ)) {
                if (!world.getChunkProvider()
                    .chunkExists(chunkX, chunkZ)) {
                    pendingBlocks.removeFirst();
                    continue;
                } else {
                    lastChunkX = chunkX;
                    lastChunkZ = chunkZ;
                }
            }

            // if this block is protected, ignore it completely and print a warning
            if (!isEditable(world, x, y, z)) {
                pendingBlocks.removeFirst();
                continue;
            }

            // if this block is different from the last one, stop checking blocks
            // since the pending blocks are sorted by their contained block, this is usually true
            if (!toPlace.isEmpty() && !PendingBlock.isSameBlock(next, toPlace.get(0))) {
                break;
            }

            PendingBlock existing = PendingBlock.fromBlock(world, x, y, z);

            if (next.getBlock() == Blocks.air && existing.getBlock() == Blocks.air) {
                pendingBlocks.removeFirst();
                continue;
            }

            // if the existing block is the same as the one we're trying to place, just apply its tile data
            if (PendingBlock.isSameBlock(next, existing)) {
                PendingBlock block = pendingBlocks.removeFirst();

                boolean didSomething = false;

                if (block.getItem() != null && !block.getItem()
                    .getHasSubtypes()) {
                    if (world.getBlockMetadata(block.x, block.y, block.z) != block.metadata) {
                        world.setBlockMetadataWithNotify(block.x, block.y, block.z, block.metadata, 3);
                        didSomething = true;
                    }
                }

                if (block.getBlock() instanceof BlockSlab) {
                    int meta = world.getBlockMetadata(x, y, z);
                    boolean isUp = (meta & 8) != 0;
                    boolean shouldBeUp = (block.flags & PendingBlock.SLAB_TOP) != 0;

                    if (isUp != shouldBeUp) {
                        if (shouldBeUp) {
                            meta |= 8;
                        } else {
                            meta &= 7;
                        }

                        world.setBlockMetadataWithNotify(x, y, z, meta, 3);
                        didSomething = true;
                    }
                }

                if (block.tileData != null && supportsConfiguring()) {
                    applyContext.pendingBlock = block;
                    block.tileData.apply(applyContext);
                    didSomething = true;
                }

                if (didSomething) {
                    playSound(world, x, y, z, SoundResource.MOB_ENDERMEN_PORTAL);
                }

                continue;
            }

            Block existingBlock = existing == null ? Blocks.air : existing.getBlock();

            // checks if the existing block is removable
            boolean canPlace = switch (state.config.removeMode) {
                case NONE -> existingBlock.isAir(world, x, y, z);
                case REPLACEABLE -> existingBlock.isReplaceable(world, x, y, z);
                case ALL -> true;
            };

            canPlace &= existingBlock.getBlockHardness(world, x, y, z) >= 0;

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
            if (!existingBlock.isAir(world, x, y, z)) {
                if (!tier.hasCap(ItemMatterManipulator.ALLOW_REMOVING)) {
                    pendingBlocks.removeFirst();
                    continue;
                }

                if (!tryConsumePower(stack, existing)) {
                    MMUtils.sendErrorToPlayer(player, "Matter Manipulator ran out of EU.");
                    break;
                }

                removeBlock(world, x, y, z, existingBlock, existing == null ? 0 : existing.metadata);
            }

            // check block dependencies for things like levers
            // if we can't place this block, shuffle it to the back of the list
            if (!next.getBlock()
                .canPlaceBlockAt(world, next.x, next.y, next.z)) {
                pendingBlocks.addLast(pendingBlocks.removeFirst());
                shuffleCount++;

                // if we've shuffled every block, then we'll never be able to place any of them
                if (shuffleCount > pendingBlocks.size()) {
                    break;
                } else {
                    continue;
                }
            }

            if (!tryConsumePower(stack, next)) {
                MMUtils.sendErrorToPlayer(player, "Matter Manipulator ran out of EU.");
                break;
            }

            toPlace.add(pendingBlocks.removeFirst());
        }

        // check if we could place any blocks
        if (toPlace.isEmpty()) {
            if (!pendingBlocks.isEmpty()) {
                MMUtils.sendErrorToPlayer(player, "Could not place " + pendingBlocks.size() + " remaining blocks.");
            } else {
                MMUtils.sendInfoToPlayer(player, "Finished placing blocks.");
            }

            actuallyGivePlayerStuff();
            playSounds();
            return;
        }

        PendingBlock first = toPlace.get(0);

        ItemStack consumed = null;

        // if the block we're placing isn't free (ae cable busses) we need to consume it
        if (!first.isFree()) {
            consumed = first.toStack();
        }

        if (consumed != null) {
            consumed.stackSize *= toPlace.size();

            List<BigItemStack> extracted = tryConsumeItems(Arrays.asList(new BigItemStack(consumed)), CONSUME_PARTIAL)
                .right();

            ItemStack extractedStack = extracted.size() == 1 ? extracted.get(0)
                .getItemStack() : null;

            int extractedAmount = extractedStack == null ? 0 : extractedStack.stackSize;

            // if we only consumed some of the blocks, only place as many as we got
            if (extractedAmount < consumed.stackSize) {
                MMUtils.sendErrorToPlayer(
                    player,
                    "Could not find item, the corresponding blocks will be skipped: " + first.getDisplayName()
                        + " x "
                        + (consumed.stackSize - extractedAmount));

                toPlace = toPlace.subList(0, extractedAmount);
            }
        }

        int initialItems = consumed == null ? 0 : consumed.stackSize;

        for (PendingBlock pending : toPlace) {
            int x = pending.x;
            int y = pending.y;
            int z = pending.z;

            playSound(world, x, y, z, SoundResource.MOB_ENDERMEN_PORTAL);

            Block block = pending.getBlock();
            Item item = pending.getItem();

            if (item == null) continue;

            int metadata = item.getHasSubtypes() ? item.getMetadata(pending.metadata) : pending.metadata;

            if (world.getBlock(x, y, z) == block && world.getBlockMetadata(x, y, z) == metadata) {
                // somehow the block already exists, despite us checking to make sure that this shouldn't happen
                // just to be safe, we only consume the item when we actually place something
                if (pending.tileData != null && supportsConfiguring()) {
                    applyContext.pendingBlock = pending;
                    pending.tileData.apply(applyContext);
                }
    
                world.notifyBlockOfNeighborChange(x, y, z, Blocks.air);
                continue;
            } else {
                if (consumed != null) consumed.stackSize--;
            }

            ItemStack stackToPlace = pending.toStack();

            if (item instanceof ItemBlock itemBlock) {
                itemBlock.placeBlockAt(
                    stackToPlace,
                    player,
                    player.worldObj,
                    x,
                    y,
                    z,
                    getDefaultPlaceSide(stackToPlace).ordinal(),
                    0,
                    0,
                    0,
                    metadata);
            } else {
                if (!world.setBlock(x, y, z, block, metadata, 3)) {
                    continue;
                }

                if (world.getBlock(x, y, z) == block) {
                    block.onBlockPlacedBy(world, x, y, z, player, stack);
                    block.onPostBlockPlaced(world, x, y, z, metadata);
                }
            }

            if (!item.getHasSubtypes()) {
                world.setBlockMetadataWithNotify(x, y, z, metadata, 3);
            }

            if (block instanceof BlockSlab) {
                if ((pending.flags & PendingBlock.SLAB_TOP) != 0) {
                    world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z) | 8, 3);
                } else {
                    world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z) & 7, 3);
                }
            }

            if (pending.tileData != null && supportsConfiguring()) {
                applyContext.pendingBlock = pending;
                pending.tileData.apply(applyContext);
            }

            world.notifyBlockOfNeighborChange(x, y, z, Blocks.air);
        }

        if (consumed != null && consumed.stackSize < 0) {
            // somehow consumed too many items
            MMMod.LOG.error("Consumed too many items! " + consumed.getDisplayName() + "; expected to consume " + initialItems + ", but consumed " + (initialItems - consumed.stackSize));
            sendErrorToPlayer(player, "Consumed too many items! There's a bug in the building code! (" + consumed.getDisplayName() + ")");
        }

        if (consumed != null && consumed.stackSize > 0) {
            // extra stuff left over somehow
            givePlayerItems(consumed);
            MMMod.LOG.error("Didn't consume enough items! " + consumed.getDisplayName() + "; expected to consume " + initialItems + ", but consumed " + (initialItems - consumed.stackSize));
            sendErrorToPlayer(player, "Didn't consume enough items! There's a bug in the building code! (" + consumed.getDisplayName() + ")");
        }

        actuallyGivePlayerStuff();
        playSounds();
    }

    private boolean supportsConfiguring() {
        // self-explanitory
        if (tier.hasCap(ItemMatterManipulator.ALLOW_CONFIGURING)) return true;

        // lower tiers support cables, but not copying
        // since exchanging or placing cables requires configuring, we need to return true for these two
        if (state.config.placeMode == PlaceMode.EXCHANGING) return true;
        if (state.config.placeMode == PlaceMode.CABLES) return true;

        return false;
    }

    private ForgeDirection getDefaultPlaceSide(ItemStack stack) {
        if (Mods.GregTech.isModLoaded() && MMState.isGTCable(stack)) {
            return ForgeDirection.UNKNOWN;
        }

        return ForgeDirection.NORTH;
    }

    private class PendingBuildApplyContext implements IBlockApplyContext {

        public static final double EU_PER_ACTION = 8192;

        private EntityPlayer fakePlayer;

        public ItemStack manipulatorItemStack;
        public PendingBlock pendingBlock;

        public PendingBuildApplyContext(ItemStack manipulatorItemStack) {
            this.manipulatorItemStack = manipulatorItemStack;
        }

        @Override
        public EntityPlayer getFakePlayer() {
            if (fakePlayer == null) {
                fakePlayer = new FakePlayer(
                    (WorldServer) PendingBuild.this.player.worldObj,
                    PendingBuild.this.player.getGameProfile());
            }

            return fakePlayer;
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
                EU_PER_ACTION * complexity);
        }

        @Override
        public Pair<Boolean, List<BigItemStack>> tryConsumeItems(List<BigItemStack> items, int flags) {
            return PendingBuild.this.tryConsumeItems(items, flags);
        }

        @Override
        public void givePlayerItems(ItemStack... items) {
            PendingBuild.this.givePlayerItems(items);
        }

        @Override
        public void givePlayerFluids(FluidStack... fluids) {
            PendingBuild.this.givePlayerFluids(fluids);
        }

        @Override
        public void warn(String message) {
            String blockName = null;

            if (pendingBlock.isInWorld(player.worldObj)) {
                if (GregTech.isModLoaded()) blockName = getGTBlockName(pendingBlock);

                if (blockName == null) {
                    blockName = PendingBlock.fromBlock(player.worldObj, pendingBlock.x, pendingBlock.y, pendingBlock.z)
                        .toStack()
                        .getDisplayName();
                }
            }

            MMUtils.sendWarningToPlayer(
                player,
                String.format(
                    "Warning at block %d, %d, %d%s: %s",
                    pendingBlock.x,
                    pendingBlock.y,
                    pendingBlock.z,
                    blockName != null ? " (" + blockName + ")" : "",
                    message));
        }

        @Override
        public void error(String message) {

            String blockName = null;

            if (pendingBlock.isInWorld(player.worldObj)) {
                if (GregTech.isModLoaded()) blockName = getGTBlockName(pendingBlock);

                if (blockName == null) {
                    blockName = PendingBlock.fromBlock(player.worldObj, pendingBlock.x, pendingBlock.y, pendingBlock.z)
                        .toStack()
                        .getDisplayName();
                }
            }

            MMUtils.sendErrorToPlayer(
                player,
                String.format(
                    "Error at block %d, %d, %d%s: %s",
                    pendingBlock.x,
                    pendingBlock.y,
                    pendingBlock.z,
                    blockName != null ? " (" + blockName + ")" : "",
                    message));
        }
    }

    @Optional(Names.GREG_TECH)
    private String getGTBlockName(PendingBlock pendingBlock) {
        if (player.worldObj.getTileEntity(
            pendingBlock.x,
            pendingBlock.y,
            pendingBlock.z) instanceof IGregTechTileEntity igte) {
            IMetaTileEntity imte = igte.getMetaTileEntity();
            if (imte != null) {
                return imte.getLocalName();
            }
        }

        return null;
    }
}
