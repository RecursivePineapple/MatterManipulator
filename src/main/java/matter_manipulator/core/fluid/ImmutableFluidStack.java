package matter_manipulator.core.fluid;

import net.minecraftforge.fluids.FluidStack;

/// An immutable version of [FluidStack] for situations where FluidStacks should never be modified.
public interface ImmutableFluidStack extends FluidStackLike {

    ImmutableFluidStack EMPTY = new FastImmutableFluidStack(null);

    int getCount();

    default boolean isEmpty() {
        return getFluid() == null || getCount() <= 0;
    }

    default FluidStack toStack() {
        if (isEmpty()) return null;

        return toStack(getCount());
    }

    default ImmutableFluidStack copy() {
        return new FastImmutableFluidStack(toStack());
    }

    /// Creates an ItemStack that matches this object, without copying the NBT (use with caution!).
    default FluidStack toStackFast() {
        return toStackFast(getCount());
    }
}
