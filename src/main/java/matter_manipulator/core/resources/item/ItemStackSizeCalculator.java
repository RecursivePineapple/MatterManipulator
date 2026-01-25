package matter_manipulator.core.resources.item;

import net.minecraft.item.ItemStack;

public interface ItemStackSizeCalculator {

    int getSlotStackLimit(int slot, ItemStack stack);
}
