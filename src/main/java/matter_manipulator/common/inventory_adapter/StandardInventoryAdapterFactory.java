package matter_manipulator.common.inventory_adapter;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntList;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.inventory_adapter.InventoryAdapter;
import matter_manipulator.core.inventory_adapter.InventoryAdapterFactory;

public class StandardInventoryAdapterFactory implements InventoryAdapterFactory {

    @Override
    public InventoryAdapter getAdapter(@NotNull TileEntity te, @Nullable EnumFacing side) {
        if (!(te instanceof IInventory inv)) return null;

        return new InventoryAdapter() {

            @Override
            public boolean validate(BlockPlacingContext context) {
                return true;
            }

            @Override
            public IntList getSlots() {
                return IntIterators.pour(IntIterators.fromTo(0, inv.getSizeInventory()));
            }

            @Override
            public boolean canExtract(int slot) {
                return inv.isItemValidForSlot(slot, inv.getStackInSlot(slot));
            }

            @Override
            public boolean canInsert(int slot, ItemStack stack) {
                return inv.isItemValidForSlot(slot, stack);
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return inv.getStackInSlot(slot);
            }

            @Override
            public ItemStack extract(int slot) {
                ItemStack stack = inv.getStackInSlot(slot);

                inv.setInventorySlotContents(slot, ItemStack.EMPTY);

                return stack;
            }

            @Override
            public ItemStack insert(int slot, ItemStack stack) {
                if (!inv.isItemValidForSlot(slot, stack)) return stack;
                if (!inv.getStackInSlot(slot).isEmpty()) return stack;

                int max = Math.min(inv.getInventoryStackLimit(), stack.getMaxStackSize());

                inv.setInventorySlotContents(slot, stack);

                return ItemStack.EMPTY;
            }
        };
    }
}
