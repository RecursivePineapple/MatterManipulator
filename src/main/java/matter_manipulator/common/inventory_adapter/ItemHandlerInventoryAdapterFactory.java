package matter_manipulator.common.inventory_adapter;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntList;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.inventory_adapter.InventoryAdapter;
import matter_manipulator.core.inventory_adapter.InventoryAdapterFactory;

public class ItemHandlerInventoryAdapterFactory implements InventoryAdapterFactory {

    @Override
    @Nullable
    public InventoryAdapter getAdapter(@NotNull TileEntity te, @Nullable EnumFacing side) {
        IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);

        if (handler == null) return null;

        return new InventoryAdapter() {

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
                return !handler.extractItem(slot, 1, true).isEmpty();
            }

            @Override
            public boolean canInsert(int slot, ItemStack stack) {
                return handler.isItemValid(slot, stack);
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return handler.getStackInSlot(slot);
            }

            @Override
            public ItemStack extract(int slot) {
                return handler.extractItem(slot, Integer.MAX_VALUE, false);
            }

            @Override
            public ItemStack insert(int slot, ItemStack stack) {
                if (!handler.isItemValid(slot, stack)) return stack;
                if (!handler.getStackInSlot(slot).isEmpty()) return stack;

                if (handler.insertItem(slot, stack, true).isEmpty()) {
                    return handler.insertItem(slot, stack, false);
                } else {
                    return stack;
                }
            }
        };
    }
}
