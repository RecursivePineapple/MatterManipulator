package matter_manipulator.core.inventory_adapter;

import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.ints.IntList;
import matter_manipulator.core.context.BlockPlacingContext;

public interface InventoryAdapter {

    boolean validate(BlockPlacingContext context);

    IntList getSlots();

    boolean canExtract(int slot);

    boolean canInsert(int slot, ItemStack stack);

    ItemStack getStackInSlot(int slot);

    ItemStack extract(int slot);

    ItemStack insert(int slot, ItemStack stack);
}
