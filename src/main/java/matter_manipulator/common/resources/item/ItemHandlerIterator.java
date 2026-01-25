package matter_manipulator.common.resources.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;

import matter_manipulator.common.utils.items.FastImmutableItemStack;
import matter_manipulator.core.item.ImmutableItemStack;
import matter_manipulator.core.resources.item.ItemStackPredicate;

/// An inventory iterator for a standard inventory. Performs all item slots validation. When the side is null,
/// canExtractItem and canInsertItem are skipped - only [IItemHandler#isItemValid(int, ItemStack)] is checked.
public class ItemHandlerIterator extends AbstractItemStackIterator {

    protected final IItemHandler inv;
    protected final ItemStackPredicate filter;

    private final FastImmutableItemStack pooled = new FastImmutableItemStack();

    public ItemHandlerIterator(IItemHandler inv, ItemStackPredicate filter) {
        super(inv.getSlots());

        this.inv = inv;
        this.filter = filter;
    }

    @Override
    protected ItemStack getStackInSlot(int slot) {
        return inv.getStackInSlot(slot);
    }

    @Override
    protected boolean canAccess(ItemStack stack, int slot) {
        return canExtract(stack, slot) || canInsert(stack, slot);
    }

    protected boolean canExtract(ItemStack stack, int slot) {
        ItemStack stack2 = inv.extractItem(slot, 1, true);
        return !stack2.isEmpty() && (filter == null || filter.test(pooled.set(stack2)));
    }

    protected boolean canInsert(ItemStack stack, int slot) {
        return inv.isItemValid(slot, stack) && (filter == null || filter.test(pooled.set(stack)));
    }

    @Override
    public @NotNull ItemStack extract(int amount, boolean forced) {
        int slotIndex = getCurrentSlot();

        ItemStack inSlot = getStackInSlot(slotIndex);

        if (inSlot.isEmpty()) return ItemStack.EMPTY;
        if (!forced && !canExtract(inSlot, slotIndex)) return ItemStack.EMPTY;

        return inv.extractItem(slotIndex, amount, false);
    }

    @Override
    public int insert(ImmutableItemStack stack, boolean forced) {
        if (stack.isEmpty()) return 0;

        int slotIndex = getCurrentSlot();

        ItemStack inSlot = getStackInSlot(slotIndex);

        if (!inSlot.isEmpty() && !stack.matches(inSlot)) {
            return stack.getCount();
        }

        ItemStack partialCopy = stack.toStackFast();

        if (!forced && !canInsert(partialCopy, slotIndex)) {
            return stack.getCount();
        }

        return inv.insertItem(slotIndex, partialCopy.copy(), false).getCount();
    }
}
