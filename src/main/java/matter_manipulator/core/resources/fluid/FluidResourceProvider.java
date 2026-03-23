package matter_manipulator.core.resources.fluid;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.ApiStatus.Internal;

import matter_manipulator.core.fluid.FluidStackIO;
import matter_manipulator.core.fluid.FluidStackPredicate;
import matter_manipulator.core.fluid.InsertionFluidStack;
import matter_manipulator.core.meta.MetaMap;
import matter_manipulator.core.resources.ResourceProvider;

@Internal
public class FluidResourceProvider implements ResourceProvider<IntFluidResourceStack> {

    private final MetaMap meta = new MetaMap();
    private final FluidStackIO[] ios;

    public FluidResourceProvider(FluidStackIO[] ios) {
        this.ios = ios;

        for (FluidStackIO io : ios) {
            io.setMetaContainer(meta);
        }
    }

    @Override
    public FluidResourceProviderFactory getFactory() {
        return FluidResourceProviderFactory.INSTANCE;
    }

    @Override
    public boolean canExtract(IntFluidResourceStack request) {
        meta.clear();

        long amount = 0;

        FluidStackPredicate predicate = FluidStackPredicate.matches(request);

        for (FluidStackIO io : ios) {
            amount += io.getStoredAmount(predicate).orElse(0);
        }

        return amount >= request.getAmountInt();
    }

    @Override
    public IntFluidResourceStack extract(IntFluidResourceStack request) {
        meta.clear();

        IntFluidResourceStack out = request.emptyCopy();

        FluidStackPredicate predicate = FluidStackPredicate.matches(request);

        for (FluidStackIO io : ios) {
            FluidStack result = io.pull(predicate, request.getAmountInt() - out.getAmountInt());

            if (result != null) {
                out.setAmountInt(out.getAmountInt() + result.amount);
            }
        }

        return out;
    }

    @Override
    public IntFluidResourceStack insert(IntFluidResourceStack stack) {
        meta.clear();

        InsertionFluidStack insert = new InsertionFluidStack(stack.toStack(stack.getAmountInt()));

        for (FluidStackIO io : ios) {
            insert.set(io.store(insert));

            if (insert.isEmpty()) break;
        }

        return insert.isEmpty() ? IntFluidResourceStack.EMPTY : new FluidStackWrapper(insert.toStack());
    }
}
