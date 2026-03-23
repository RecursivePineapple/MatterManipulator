package matter_manipulator.core.fluid;

import javax.annotation.Nonnegative;

/// Something that inserts fluid stacks. These are ephemeral objects similar to [FluidStackIterator]s, except they are
/// meant to be kept over several operations. They can operate across several ticks, but their state may get
/// progressively more out of sync with the world. Calling [#reset()] updates an inserter's state to match the world.
/// Even while out of sync, inserters must behave sanely - fluids cannot be voided or generated, and inserter operations
/// must never throw an exception. Doing nothing when [#store(ImmutableFluidStack)] is called is acceptable.
public interface FluidStackInserter {

    default void reset() {

    }

    @Nonnegative
    int store(ImmutableFluidStack stack);
}
