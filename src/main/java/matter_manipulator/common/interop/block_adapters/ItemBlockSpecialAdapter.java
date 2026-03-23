package matter_manipulator.common.interop.block_adapters;

import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlockSpecial;
import net.minecraft.item.ItemStack;

import matter_manipulator.core.interop.BlockAdapter;
import matter_manipulator.core.item.ItemStackLike;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.ItemStackWrapper;

public class ItemBlockSpecialAdapter implements BlockAdapter {

    @Override
    public boolean canAdapt(IBlockState state) {
        return state.getBlock().getItemDropped(state, ThreadLocalRandom.current(), 0) instanceof ItemBlockSpecial;
    }

    @Override
    public boolean canAdapt(ResourceStack resource) {
        return resource instanceof ItemStackLike item && item.getItem() instanceof ItemBlockSpecial;
    }

    @Override
    public ResourceStack getResourceForm(IBlockState state) {
        return new ItemStackWrapper(new ItemStack(
            state.getBlock().getItemDropped(state, ThreadLocalRandom.current(), 0),
            state.getBlock().quantityDropped(ThreadLocalRandom.current()),
            state.getBlock().damageDropped(state)));
    }

    @Override
    public IBlockState getBlockForm(ResourceStack resource) {
        if (!(resource instanceof ItemStackLike itemStack)) return null;

        ItemBlockSpecial item = (ItemBlockSpecial) itemStack.getItem();

        int meta = item.getMetadata(itemStack.toStackFast(1).getMetadata());

        //noinspection deprecation
        return item.getBlock().getStateFromMeta(meta);
    }
}
