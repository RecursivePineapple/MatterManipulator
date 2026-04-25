package matter_manipulator.common.inventory_adapter;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

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

public class ItemHandlerInventoryAdapterFactory implements InventoryAdapterFactory<IntItemResourceStack> {

    @Override
    @Nullable
    public InventoryAdapter<IntItemResourceStack> getAdapter(@NotNull TileEntity te, @Nullable EnumFacing side) {
        IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);

        if (handler == null) return null;

        return new ItemHandlerInventoryAdapter(handler);
    }

    @Desugar
    public record ItemHandlerInventoryAdapter(IItemHandler handler) implements InventoryAdapter<IntItemResourceStack> {

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
            return IntIterators.pour(IntIterators.fromTo(0, handler.getSlots()));
        }

        @Override
        public boolean canExtract(int slot) {
            return handler.getStackInSlot(slot).isEmpty() || !handler.extractItem(slot, 1, true)
                .isEmpty();
        }

        @Override
        public boolean canInsert(int slot, IntItemResourceStack stack) {
            var stack2 = stack.toStack();

            if (!handler.isItemValid(slot, stack2)) return false;

            int rejected = handler.insertItem(slot, stack2, true).getCount();

            return rejected == 0 || rejected != stack.getAmountInt();
        }

        @Override
        public IntItemResourceStack getStackInSlot(int slot) {
            return new ItemStackWrapper(handler.getStackInSlot(slot));
        }

        @Override
        public IntItemResourceStack extract(int slot) {
            return new ItemStackWrapper(handler.extractItem(slot, Integer.MAX_VALUE, false));
        }

        @Override
        public IntItemResourceStack insert(int slot, IntItemResourceStack stack) {
            var stack2 = stack.toStack();

            if (!handler.isItemValid(slot, stack2)) return stack;
            if (!handler.getStackInSlot(slot).isEmpty()) return stack;

            if (handler.insertItem(slot, stack2, true).isEmpty()) {
                return new ItemStackWrapper(handler.insertItem(slot, stack2, false));
            } else {
                return stack;
            }
        }
    }
}
