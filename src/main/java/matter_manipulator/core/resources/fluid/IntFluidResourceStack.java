package matter_manipulator.core.resources.fluid;

import matter_manipulator.core.resources.ResourceStack.IntResourceStack;

public interface IntFluidResourceStack extends FluidResourceStack, IntResourceStack {

    IntFluidResourceStack EMPTY = new FluidStackWrapper(null);

    @Override
    IntFluidResourceStack emptyCopy();
}
