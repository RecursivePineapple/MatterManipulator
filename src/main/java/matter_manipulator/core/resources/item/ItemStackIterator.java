package matter_manipulator.core.resources.item;

import java.util.ListIterator;

import javax.annotation.Nonnegative;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import matter_manipulator.core.item.ImmutableItemStack;

/// An iterator over something that contains [ItemStack]s. Any further details beyond the method specifications outlined
/// below are undefined.
public interface ItemStackIterator extends ListIterator<ImmutableItemStack> {

    ItemStackIterator EMPTY = new EmptyIterator();

    @Override
    default void set(ImmutableItemStack immutableItemStack) {
        throw new UnsupportedOperationException("Cannot insert items into an ItemStackIterator");
    }

    @Override
    default void add(ImmutableItemStack immutableItemStack) {
        throw new UnsupportedOperationException("Cannot insert items into an ItemStackIterator");
    }

    @Override
    default void remove() {
        throw new UnsupportedOperationException(
            "Cannot remove items from an ItemStackIterator via remove(); use extract()");
    }

    /// Extracts items from the current index. May return fewer items than were originally reported for this index.
    ///
    /// @param amount The amount to extract
    /// @param forced When true, all inventory slot validation should be ignored.
    /// @return The extracted stack, or [ItemStack#EMPTY].
    @NotNull
    ItemStack extract(int amount, boolean forced);

    /// Inserts items into the current index. Returns the number of items that could not be inserted.
    ///
    /// @param forced When true, all inventory slot validation should be ignored.
    @Nonnegative
    int insert(ImmutableItemStack stack, boolean forced);

    /// Rewinds this iterator to the first stack, if possible. When an iterator is rewound, the prior index can be
    /// discarded and [#hasPrevious()] may return false. Implementations are free to implement this properly, but there
    /// is no guarantee that this behaviour is supported.
    ///
    /// @return True when the rewind was successful, false otherwise.
    boolean rewind();

    /// Returns the prior stack, if any, and decrement to the previous position.
    /// Must return [ImmutableItemStack#EMPTY] for empty slots.
    @Override
    @NotNull
    ImmutableItemStack previous();

    /// Returns the current stack, if any, and increments to the next position.
    /// Must return [ImmutableItemStack#EMPTY] for empty slots.
    @Override
    @NotNull
    ImmutableItemStack next();

    class EmptyIterator implements ItemStackIterator {

        @Override
        public @NotNull ItemStack extract(int amount, boolean forced) {
            return ItemStack.EMPTY;
        }

        @Nonnegative
        @Override
        public int insert(ImmutableItemStack stack, boolean forced) {
            return stack.getCount();
        }

        @Override
        public boolean rewind() {
            return false;
        }

        @Override
        public @NotNull ImmutableItemStack previous() {
            return ImmutableItemStack.EMPTY;
        }

        @Override
        public int nextIndex() {
            return 0;
        }

        @Override
        public int previousIndex() {
            return 0;
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public @NotNull ImmutableItemStack next() {
            return ImmutableItemStack.EMPTY;
        }

        @Override
        public boolean hasPrevious() {
            return false;
        }
    }
}
