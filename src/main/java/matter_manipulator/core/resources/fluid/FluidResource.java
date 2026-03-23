package matter_manipulator.core.resources.fluid;

import matter_manipulator.core.resources.Resource;

public class FluidResource implements Resource<FluidResourceProvider> {

    public static final FluidResource FLUIDS = new FluidResource();

    private FluidResource() { }

}
