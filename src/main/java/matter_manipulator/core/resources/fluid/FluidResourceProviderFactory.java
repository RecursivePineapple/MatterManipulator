package matter_manipulator.core.resources.fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.fluid.FluidStackIO;
import matter_manipulator.core.persist.DataStorage;
import matter_manipulator.core.resources.ResourceProviderFactory;

public class FluidResourceProviderFactory implements ResourceProviderFactory<FluidResourceProvider> {

    public static final FluidResourceProviderFactory INSTANCE = new FluidResourceProviderFactory();

    private FluidResourceProviderFactory() { }

    @Override
    public FluidResource getResource() {
        return FluidResource.FLUIDS;
    }

    @Override
    public FluidResourceProvider createProvider(ManipulatorContext context) {
        List<FluidStackIO> ios = new ArrayList<>();

        DataStorage storage = context.getState().ioState;

        for (var factory : MMRegistriesInternal.FLUID_IO_FACTORIES.sorted()) {
            Optional<FluidStackIO> io = factory.getIO(context, storage);

            if (io.isPresent()) ios.add(io.get());
        }

        return new FluidResourceProvider(ios.toArray(new FluidStackIO[0]));
    }
}
