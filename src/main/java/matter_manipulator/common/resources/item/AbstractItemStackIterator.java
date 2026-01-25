package matter_manipulator.common.resources.item;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import matter_manipulator.common.utils.items.FastImmutableItemStack;
import matter_manipulator.core.item.ImmutableItemStack;
import matter_manipulator.core.resources.item.ItemStackIterator;

public abstract class AbstractItemStackIterator implements ItemStackIterator {

    public static final int[] NO_SLOTS = new int[0];

    private final Int2IntFunction slots;

    private int i = 0, last = 0;

    private final FastImmutableItemStack pooled = new FastImmutableItemStack(ItemStack.EMPTY);

    protected AbstractItemStackIterator(Int2IntFunction slots) {
        this.slots = slots;
    }

    protected AbstractItemStackIterator(int count) {
        this(new Int2IntFunction() {

            @Override
            public int size() {
                return count;
            }

            @Override
            public int get(int key) {
                return key;
            }
        });
    }

    protected AbstractItemStackIterator(int[] slots) {
        this(new Int2IntFunction() {

            @Override
            public int size() {
                return slots.length;
            }

            @Override
            public int get(int key) {
                return slots[key];
            }
        });
    }

    protected AbstractItemStackIterator(int[] a, int[] b) {
        this(intersect(a, b));
    }

    public static int[] intersect(int[] a, int[] b) {
        if (a == null && b == null) return NO_SLOTS;
        if (a == null || b == null) return a == null ? b : a;

        IntLinkedOpenHashSet a2 = new IntLinkedOpenHashSet(a);
        IntLinkedOpenHashSet b2 = new IntLinkedOpenHashSet(b);

        IntArrayList out = new IntArrayList();

        a2.forEach(i -> {
            if (b2.contains(i)) {
                out.add(i);
            }
        });

        return out.toIntArray();
    }

    /**
     * @see IInventory#getStackInSlot(int)
     */
    protected abstract ItemStack getStackInSlot(int slot);

    protected boolean canAccess(ItemStack stack, int slot) {
        return true;
    }

    @Override
    public boolean hasNext() {
        return i < slots.size();
    }

    @Override
    public @NotNull ImmutableItemStack next() {
        last = i++;

        pooled.set(getStackInSlot(slots.get(last)));
        return pooled.isEmpty() || !canAccess(pooled.getStack(), slots.get(last)) ? ImmutableItemStack.EMPTY : pooled;
    }

    @Override
    public boolean hasPrevious() {
        return i > 0;
    }

    @Override
    public @NotNull ImmutableItemStack previous() {
        last = --i;

        pooled.set(getStackInSlot(slots.get(last)));
        return pooled.isEmpty() || !canAccess(pooled.getStack(), slots.get(last)) ? ImmutableItemStack.EMPTY : pooled;
    }

    @Override
    public int nextIndex() {
        return slots.get(last + 1);
    }

    @Override
    public int previousIndex() {
        return slots.get(last - 1);
    }

    @Override
    public boolean rewind() {
        i = 0;

        return true;
    }

    protected final int getCurrentSlot() {
        return slots.get(last);
    }
}
