package matter_manipulator.core.item;

import net.minecraft.item.ItemStack;

public interface ItemStackSizeCalculator {

    int getSlotStackLimit(int slot, ItemStack stack);
}
