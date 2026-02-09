package matter_manipulator.common.interop.block_adapters;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import matter_manipulator.core.interop.BlockAdapter;
import matter_manipulator.core.item.ItemStackLike;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.ItemStackWrapper;

public class AirBlockAdapter implements BlockAdapter {

    @Override
    public boolean canAdapt(IBlockState state) {
        return state.getBlock() == Blocks.AIR;
    }

    @Override
    public boolean canAdapt(ResourceStack stack) {
        if (!(stack instanceof ItemStackLike item)) return false;

        return item.getItem() == Items.AIR;
    }

    @Override
    public ResourceStack getResourceForm(IBlockState state) {
        return new ItemStackWrapper(ItemStack.EMPTY);
    }

    @Override
    public IBlockState getBlockForm(ResourceStack stack) {
        return Blocks.AIR.getDefaultState();
    }
}
