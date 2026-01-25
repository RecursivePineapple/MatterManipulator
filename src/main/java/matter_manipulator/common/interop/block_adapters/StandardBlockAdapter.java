package matter_manipulator.common.interop.block_adapters;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import matter_manipulator.core.interop.interfaces.BlockAdapter;
import matter_manipulator.core.item.ItemStackLike;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.ItemStackWrapper;

public class StandardBlockAdapter implements BlockAdapter {

    @Override
    public boolean canAdapt(IBlockState state) {
        return true;
    }

    @Override
    public boolean canAdapt(ResourceStack stack) {
        if (!(stack instanceof ItemStackLike item)) return false;

        return item.getItem() instanceof ItemBlock;
    }

    @Override
    public ResourceStack getResourceForm(IBlockState state) {
        return new ItemStackWrapper(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)));
    }

    @Override
    public IBlockState getBlockForm(ResourceStack stack) {
        if (!(stack instanceof ItemStackLike item)) return null;

        Block block = ((ItemBlock) item.getItem()).getBlock();

        //noinspection deprecation
        return block.getStateFromMeta(item.getItem().getMetadata(item.toStackFast(1).getMetadata()));
    }
}
