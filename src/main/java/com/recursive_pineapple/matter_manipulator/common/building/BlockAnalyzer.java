package com.recursive_pineapple.matter_manipulator.common.building;

import static com.recursive_pineapple.matter_manipulator.common.utils.MMUtils.sendErrorToPlayer;
import static com.recursive_pineapple.matter_manipulator.common.utils.MMUtils.sendWarningToPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.recursive_pineapple.matter_manipulator.GlobalMMConfig.DebugConfig;
import com.recursive_pineapple.matter_manipulator.MMMod;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.Location;
import com.recursive_pineapple.matter_manipulator.common.utils.BigFluidStack;
import com.recursive_pineapple.matter_manipulator.common.utils.BigItemStack;
import com.recursive_pineapple.matter_manipulator.common.utils.FluidId;
import com.recursive_pineapple.matter_manipulator.common.utils.ItemId;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;

import org.joml.Vector3i;

import it.unimi.dsi.fastutil.booleans.BooleanObjectImmutablePair;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

public class BlockAnalyzer {

    private BlockAnalyzer() {}

    /**
     * Analyzes a region.
     * The returned PendingBlocks are relative to zero.
     * The calling code must add another location (for example coord C) to get the real positions.
     */
    public static RegionAnalysis analyzeRegion(World world, Location a, Location b, boolean checkTiles) {
        if (a == null || b == null || world.provider.dimensionId != a.worldId || a.worldId != b.worldId) return null;

        long pre = System.nanoTime();

        RegionAnalysis analysis = new RegionAnalysis();

        Vector3i deltas = MMUtils.getRegionDeltas(a, b);
        analysis.deltas = deltas;

        analysis.blocks = new ArrayList<>();

        for (Vector3i voxel : MMUtils.getBlocksInBB(a, deltas)) {
            BlockSpec spec = BlockSpec.fromBlock(null, world, voxel.x, voxel.y, voxel.z);

            if (spec.skipWhenCopying()) {
                continue;
            }

            PendingBlock pending = spec.instantiate(world, voxel.x, voxel.y, voxel.z);

            if (checkTiles) {
                pending.analyze(world.getTileEntity(voxel.x, voxel.y, voxel.z), PendingBlock.ANALYZE_ALL & ~PendingBlock.ANALYZE_ARCH);
            }

            pending.x -= a.x;
            pending.y -= a.y;
            pending.z -= a.z;

            analysis.blocks.add(pending);
        }

        long post = System.nanoTime();

        if (DebugConfig.debug) {
            MMMod.LOG.info("Analysis took " + (post - pre) / 1e6 + " ms");
        }

        return analysis;
    }

    public static class RegionAnalysis {

        public Vector3i deltas;
        public List<PendingBlock> blocks;
    }

    /**
     * The context within which analysis results are applied.
     * Contains everything needed to apply a TileAnalysisResult.
     */

    public static interface IBlockApplyContext extends IPseudoInventory {

        public World getWorld();

        public int getX();

        public int getY();

        public int getZ();

        public TileEntity getTileEntity();

        public EntityPlayer getRealPlayer();

        public boolean tryApplyAction(double complexity);

        public void warn(String message);

        public void error(String message);
    }

    /**
     * A fake apply context that tracks which items were consumed.
     */
    private static class BlockItemCheckContext implements IBlockApplyContext {

        public World world;
        public int x, y, z;
        public EntityPlayer player;

        public Object2LongOpenHashMap<ItemId> requiredItems = new Object2LongOpenHashMap<>();

        public Object2LongOpenHashMap<ItemId> storedItems = new Object2LongOpenHashMap<>();
        public Object2LongOpenHashMap<FluidId> storedFluids = new Object2LongOpenHashMap<>();

        @Override
        public World getWorld() {
            return world;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public int getZ() {
            return z;
        }

        @Override
        public TileEntity getTileEntity() {
            return world.getTileEntity(x, y, z);
        }

        @Override
        public EntityPlayer getRealPlayer() {
            return player;
        }

        @Override
        public boolean tryApplyAction(double complexity) {
            return true;
        }

        @Override
        public BooleanObjectImmutablePair<List<BigItemStack>> tryConsumeItems(List<BigItemStack> items, int flags) {
            boolean simulate = (flags & CONSUME_SIMULATED) != 0;
            boolean fuzzy = (flags & CONSUME_FUZZY) != 0;

            if ((flags & CONSUME_REAL_ONLY) != 0) return BooleanObjectImmutablePair.of(false, new ArrayList<>());

            List<BigItemStack> extractedItems = new ArrayList<>();

            for (BigItemStack req : items) {
                if (req.getStackSize() == 0) {
                    continue;
                }

                if (!fuzzy) {
                    long amtInPending = storedItems.getLong(req.getId());

                    long toRemove = Math.min(amtInPending, req.getStackSize());

                    if (toRemove > 0) {
                        extractedItems.add(req.copy().setStackSize(toRemove));
                        amtInPending -= toRemove;
                        req.decStackSize(toRemove);

                        if (!simulate) {
                            if (amtInPending == 0) {
                                storedItems.removeLong(req.getId());
                            } else {
                                storedItems.put(req.getId(), amtInPending);
                            }
                        }
                    }
                } else {
                    var iter = storedItems.object2LongEntrySet()
                        .iterator();

                    while (iter.hasNext()) {
                        var e = iter.next();

                        if (e.getLongValue() == 0) {
                            continue;
                        }

                        ItemStack stack = e.getKey()
                            .getItemStack();

                        if (stack.getItem() != req.getItem()) {
                            continue;
                        }

                        if (stack.getHasSubtypes() && Items.feather.getDamage(stack) != req.getItemDamage()) {
                            continue;
                        }

                        long amtInPending = e.getLongValue();
                        long toRemove = Math.min(amtInPending, req.getStackSize());

                        if (toRemove > 0) {
                            extractedItems.add(req.copy().setStackSize(toRemove));
                            amtInPending -= toRemove;
                            req.decStackSize(toRemove);

                            if (!simulate) {
                                if (amtInPending == 0) {
                                    iter.remove();
                                } else {
                                    e.setValue(amtInPending);
                                }
                            }
                        }
                    }
                }

                ItemId id;

                if (fuzzy && !req.getItem().getHasSubtypes()) {
                    id = ItemId.createAsWildcard(req.getItemStack());
                } else {
                    id = ItemId.create(req.getItemStack());
                }

                requiredItems.addTo(id, req.getStackSize());
            }

            return BooleanObjectImmutablePair.of(true, items);
        }

        @Override
        public void givePlayerItems(List<BigItemStack> items) {
            for (BigItemStack item : items) {
                storedItems.addTo(item.getId(), item.stackSize);
            }
        }

        @Override
        public void givePlayerFluids(List<BigFluidStack> fluids) {
            for (BigFluidStack fluid : fluids) {
                storedFluids.addTo(fluid.getId(), fluid.amount);
            }
        }

        @Override
        public void warn(String message) {
            sendWarningToPlayer(
                player,
                StatCollector.translateToLocalFormatted(
                    "mm.info.warning",
                    x,
                    y,
                    z,
                    message
                )
            );
        }

        @Override
        public void error(String message) {
            sendErrorToPlayer(
                player,
                StatCollector.translateToLocalFormatted(
                    "mm.info.error",
                    x,
                    y,
                    z,
                    message
                )
            );
        }
    }

    public static class RequiredItemAnalysis {

        public Map<ItemId, Long> requiredItems;
        public Map<ItemId, Long> storedItems;
        public Map<FluidId, Long> storedFluids;
    }

    /**
     * Gets the required items for a build
     *
     * @param fromScratch When true, existing blocks will be ignored.
     * @return
     */
    public static RequiredItemAnalysis getRequiredItemsForBuild(
        EntityPlayer player,
        List<PendingBlock> blocks,
        boolean fromScratch
    ) {
        BlockItemCheckContext context = new BlockItemCheckContext();
        context.player = player;
        context.world = player.getEntityWorld();

        BlockSpec pooled = new BlockSpec();

        for (PendingBlock block : blocks) {
            if (block.isInWorld(context.world)) {
                boolean isNew = true;

                if (!fromScratch && !context.world.isAirBlock(block.x, block.y, block.z)) {
                    BlockSpec.fromBlock(pooled, context.world, block.x, block.y, block.z);

                    if (pooled.isEquivalent(block.spec)) {
                        isNew = false;
                    }
                }

                if (isNew && !block.isFree()) {
                    context.tryConsumeItems(block.getStack());
                }

                context.x = block.x;
                context.y = block.y;
                context.z = block.z;

                if (isNew) {
                    block.getRequiredItemsForNewBlock(context);
                } else {
                    block.getRequiredItemsForExistingBlock(context);
                }
            }
        }

        RequiredItemAnalysis analysis = new RequiredItemAnalysis();
        analysis.requiredItems = context.requiredItems;
        analysis.storedItems = context.storedItems;
        analysis.storedFluids = context.storedFluids;

        return analysis;
    }
}
