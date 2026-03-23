package matter_manipulator.core.resources.item;

import net.minecraft.item.ItemStack;

import matter_manipulator.core.resources.ResourceStack.IntResourceStack;

public interface IntItemResourceStack extends ItemResourceStack, IntResourceStack {

    IntItemResourceStack EMPTY = new ItemStackWrapper(ItemStack.EMPTY);

    @Override
    IntItemResourceStack emptyCopy();
}
