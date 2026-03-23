package matter_manipulator.core.fluid;

import javax.annotation.Nonnegative;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

/// Something that extracts fluid stacks. These are ephemeral objects similar to [FluidStackIterator]s, except they are
/// meant to be kept over several operations. They can operate across several ticks, but their state may get
/// progressively more out of sync with the world. Calling [#reset()] updates an extractor's state to match the world.
/// Even while out of sync, extractors must behave sanely - fluids cannot be voided or generated, and extractor
/// operations must never throw an exception. If [#reset()] has not been called recently, falsely doing nothing when
/// [#pull(FluidStackPredicate , int)] is called is acceptable.
public interface FluidStackExtractor {

    default void reset() {

    }

    @Nullable FluidStack pull(@Nullable FluidStackPredicate filter, @Nonnegative int amount);
}
