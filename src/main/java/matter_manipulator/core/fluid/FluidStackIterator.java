package matter_manipulator.core.fluid;

import java.util.ListIterator;

import javax.annotation.Nonnegative;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/// An iterator over something that contains [FluidStack]s. Any further details beyond the method specifications outlined
/// below are undefined.
public interface FluidStackIterator extends ListIterator<ImmutableFluidStack> {

    FluidStackIterator EMPTY = new EmptyIterator();

    @Override
    default void set(ImmutableFluidStack immutableFluidStack) {
        throw new UnsupportedOperationException("Cannot insert items into an FluidStackIterator");
    }

    @Override
    default void add(ImmutableFluidStack immutableFluidStack) {
        throw new UnsupportedOperationException("Cannot insert items into an FluidStackIterator");
    }

    @Override
    default void remove() {
        throw new UnsupportedOperationException(
            "Cannot remove items from an FluidStackIterator via remove(); use extract()");
    }

    /// Extracts items from the current index. May return fewer items than were originally reported for this index.
    ///
    /// @param amount The amount to extract
    /// @param forced When true, all inventory slot validation should be ignored.
    /// @return The extracted stack, or null if nothing was extracted.
    @Nullable FluidStack extract(int amount, boolean forced);

    /// Inserts items into the current index. Returns the number of items that could not be inserted.
    ///
    /// @param forced When true, all inventory slot validation should be ignored.
    @Nonnegative
    int insert(ImmutableFluidStack stack, boolean forced);

    /// Rewinds this iterator to the first stack, if possible. When an iterator is rewound, the prior index can be
    /// discarded and [#hasPrevious()] may return false. Implementations are free to implement this properly, but there
    /// is no guarantee that this behaviour is supported.
    ///
    /// @return True when the rewind was successful, false otherwise.
    boolean rewind();

    /// Returns the prior stack, if any, and decrement to the previous position.
    /// Must return [ImmutableFluidStack#EMPTY] for empty slots.
    @Override
    @NotNull
    ImmutableFluidStack previous();

    /// Returns the current stack, if any, and increments to the next position.
    /// Must return [ImmutableFluidStack#EMPTY] for empty slots.
    @Override
    @NotNull
    ImmutableFluidStack next();

    class EmptyIterator implements FluidStackIterator {

        @Override
        public @Nullable FluidStack extract(int amount, boolean forced) {
            return null;
        }

        @Nonnegative
        @Override
        public int insert(ImmutableFluidStack stack, boolean forced) {
            return stack.getCount();
        }

        @Override
        public boolean rewind() {
            return false;
        }

        @Override
        public @NotNull ImmutableFluidStack previous() {
            return ImmutableFluidStack.EMPTY;
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
        public @NotNull ImmutableFluidStack next() {
            return ImmutableFluidStack.EMPTY;
        }

        @Override
        public boolean hasPrevious() {
            return false;
        }
    }
}
