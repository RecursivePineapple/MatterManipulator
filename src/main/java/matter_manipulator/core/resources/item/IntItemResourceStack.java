package matter_manipulator.core.resources.item;

import net.minecraft.item.ItemStack;

import matter_manipulator.core.resources.ResourceStack.IntResourceStack;

public interface IntItemResourceStack extends ItemResourceStack, IntResourceStack {

    IntItemResourceStack EMPTY = new ItemStackWrapper(ItemStack.EMPTY);

    @Override
    IntItemResourceStack emptyCopy();

    default ItemStack toStack() {
        return toStack(getAmountInt());
    }

    @Override
    default IntItemResourceStack copy() {
        return (IntItemResourceStack) ItemResourceStack.super.copy();
    }

    @Override
    default IntItemResourceStack multipliedCopy(int mult) {
        return (IntItemResourceStack) ItemResourceStack.super.multipliedCopy(mult);
    }
}
