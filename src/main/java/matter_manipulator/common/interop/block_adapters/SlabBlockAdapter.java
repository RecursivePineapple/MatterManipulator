package matter_manipulator.common.interop.block_adapters;

import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;

import matter_manipulator.core.interop.BlockAdapter;
import matter_manipulator.core.item.ItemStackLike;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.ItemStackWrapper;
import matter_manipulator.mixin.mixins.minecraft.AccessorItemSlab;

public class SlabBlockAdapter implements BlockAdapter {

    @Override
    public boolean canAdapt(IBlockState state) {
        return state.getBlock() instanceof BlockSlab;
    }

    @Override
    public boolean canAdapt(ResourceStack stack) {
        if (!(stack instanceof ItemStackLike item)) return false;

        return item.getItem() instanceof ItemSlab;
    }

    @Override
    public ResourceStack getResourceForm(IBlockState state) {
        return new ItemStackWrapper(new ItemStack(
            state.getBlock().getItemDropped(state, ThreadLocalRandom.current(), 0),
            state.getBlock().quantityDropped(ThreadLocalRandom.current()),
            state.getBlock().damageDropped(state)));
    }

    @Override
    public IBlockState getBlockForm(ResourceStack stack) {
        if (!(stack instanceof ItemStackLike item)) return null;

        AccessorItemSlab slab = (AccessorItemSlab) item.getItem();

        Block block = ResourceStack.getStackAmount(stack) > 1 ? slab.getDoubleSlab() : slab.getSingleSlab();

        //noinspection deprecation
        return block.getStateFromMeta(item.getItem().getMetadata(item.toStackFast(1).getMetadata()));
    }
}
