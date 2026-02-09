package matter_manipulator.common.interop.block_adapters;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import matter_manipulator.core.interop.BlockAdapter;
import matter_manipulator.core.item.ItemStackLike;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.ItemStackWrapper;

public class RedstoneBlockAdapter implements BlockAdapter {

    @Override
    public boolean canAdapt(IBlockState state) {
        return state.getBlock() == Blocks.REDSTONE_WIRE;
    }

    @Override
    public boolean canAdapt(ResourceStack resource) {
        if (!(resource instanceof ItemStackLike stack)) return false;

        return stack.getItem() == Items.REDSTONE;
    }

    @Override
    public ResourceStack getResourceForm(IBlockState state) {
        return new ItemStackWrapper(new ItemStack(Items.REDSTONE, 1));
    }

    @Override
    public IBlockState getBlockForm(ResourceStack resource) {
        //noinspection DataFlowIssue
        return Blocks.REDSTONE_WIRE.getDefaultState();
    }
}
