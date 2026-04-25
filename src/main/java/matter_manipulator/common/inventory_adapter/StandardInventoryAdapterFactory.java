package matter_manipulator.common.inventory_adapter;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntList;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.inventory_adapter.InventoryAdapter;
import matter_manipulator.core.inventory_adapter.InventoryAdapterFactory;
import matter_manipulator.core.resources.Resource;
import matter_manipulator.core.resources.item.IntItemResourceStack;
import matter_manipulator.core.resources.item.ItemResource;
import matter_manipulator.core.resources.item.ItemStackWrapper;

public class StandardInventoryAdapterFactory implements InventoryAdapterFactory<IntItemResourceStack> {

    @Override
    public InventoryAdapter<IntItemResourceStack> getAdapter(@NotNull TileEntity te, @Nullable EnumFacing side) {
        if (!(te instanceof IInventory inv)) return null;

        return new InvInventoryAdapter(inv);
    }

    @Desugar
    private record InvInventoryAdapter(IInventory inv) implements InventoryAdapter<IntItemResourceStack> {

        @Override
        public Resource<?> getResource() {
            return ItemResource.ITEMS;
        }

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
        public boolean canInsert(int slot, IntItemResourceStack stack) {
            return inv.isItemValidForSlot(slot, stack.toStackFast(stack.getAmountInt()));
        }

        @Override
        public IntItemResourceStack getStackInSlot(int slot) {
            return new ItemStackWrapper(inv.getStackInSlot(slot));
        }

        @Override
        public IntItemResourceStack extract(int slot) {
            ItemStack stack = inv.getStackInSlot(slot);

            inv.setInventorySlotContents(slot, ItemStack.EMPTY);

            return new ItemStackWrapper(stack);
        }

        @Override
        public IntItemResourceStack insert(int slot, IntItemResourceStack stack) {
            var stack2 = stack.toStackFast(stack.getAmountInt());

            if (!inv.isItemValidForSlot(slot, stack2)) return stack;
            if (!inv.getStackInSlot(slot).isEmpty()) return stack;

            int max = Math.min(inv.getInventoryStackLimit(), stack2.getMaxStackSize());
            int xfer = Math.min(max, stack.getAmountInt());

            inv.setInventorySlotContents(slot, stack.toStack(xfer));
            stack.setAmountInt(stack.getAmountInt() - xfer);

            return stack;
        }
    }
}
