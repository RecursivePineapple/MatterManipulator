package matter_manipulator.core.resources.fluid;

import matter_manipulator.core.fluid.FluidId;
import matter_manipulator.core.fluid.FluidStackLike;
import matter_manipulator.core.resources.ResourceStack;

public interface FluidResourceStack extends ResourceStack, FluidStackLike {

    @Override
    default FluidResource getResource() {
        return FluidResource.FLUIDS;
    }

    @Override
    default FluidId getIdentity() {
        return FluidId.create(getFluid(), getTag());
    }

    @Override
    FluidResourceStack emptyCopy();
}
