package matter_manipulator.core.fluid;

import java.util.OptionalInt;
import java.util.OptionalLong;

import javax.annotation.Nonnegative;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import matter_manipulator.MMMod;
import matter_manipulator.core.meta.MetadataContainer;
import matter_manipulator.core.resources.IteratorUsage;
import matter_manipulator.core.resources.fluid.FluidResourceProvider;

public interface FluidStackIO {

    /// Sets the meta container for this IO. This is used for inter-IO communication, in case two IOs need to cooperate.
    /// The container is cleared prior to all [FluidResourceProvider] operations.
    default void setMetaContainer(MetadataContainer container) {

    }

    /// Creates an iterator builder for the Fluids in this source. Must return [FluidStackIterator#EMPTY] if iterators are
    /// not supported. Modifying any backing inventories while this iterator exists (without going through the iterator)
    /// is undefined behaviour, but it should never duplicate or delete Fluids.
    @NotNull FluidStackIteratorBuilder iterator();

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    OptionalInt ZERO_INT = OptionalInt.of(0);
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    OptionalLong ZERO_LONG = OptionalLong.of(0);

    default FluidStackExtractor createExtractor() {
        return this::pull;
    }

    @Nullable
    default FluidStack pull(@Nullable FluidStackPredicate filter, @Nonnegative int amount) {
        FluidStackIterator iter = iterator().setFluidFilter(filter).setUsage(IteratorUsage.Extract).build();

        long sum = 0;

        FluidStack result = null;

        while (iter.hasNext()) {
            ImmutableFluidStack stack = iter.next();

            if (stack.isEmpty()) continue;
            if (result != null && !stack.matches(result)) continue;

            sum += stack.getCount();

            if (result == null) {
                result = stack.toStack(0);
            }
        }

        if (!iter.rewind()) {
            iter = iterator().setFluidFilter(filter).setUsage(IteratorUsage.Extract).build();
        }

        if (sum < amount || result == null) return null;

        int remaining = amount;

        while (iter.hasNext() && remaining > 0) {
            ImmutableFluidStack stack = iter.next();

            if (stack.isEmpty()) continue;
            if (!stack.matches(result)) continue;

            FluidStack extracted = iter.extract(remaining, false);

            if (!FluidUtils.isEmpty(extracted)) {
                if (result.isFluidEqual(extracted)) {
                    result.amount += extracted.amount;
                    remaining -= extracted.amount;
                } else {
                    MMMod.LOG.error("FluidStackIO.pull() has misbehaved! extract() returned a stack that was supposed to be something else! Trying to reinsert the stack - Fluids may be voided. Expected={} Actual={} Iterator={}", result, extracted, iter);
                    store(new InsertionFluidStack(extracted));
                }
            }
        }

        if (result.amount < amount) {
            MMMod.LOG.error("FluidStackIO.pull() has misbehaved! The iterator reported having more Fluids than could be extracted! Reported amount={} Extracted={} Iterator={}", sum, result, iter);
            store(new InsertionFluidStack(result));
            result = null;
        }

        return result;
    }

    default FluidStackInserter createInserter() {
        return this::store;
    }

    default int store(ImmutableFluidStack stack) {
        FluidStackPredicate filter = FluidStackPredicate.matches(stack);

        FluidStackIterator iter = iterator().setFluidFilter(filter).setUsage(IteratorUsage.Insert).build();

        InsertionFluidStack insertion = new InsertionFluidStack(stack);

        while (iter.hasNext() && !insertion.isEmpty()) {
            ImmutableFluidStack slot = iter.next();

            if (!slot.isEmpty() && !slot.matches(stack)) continue;

            insertion.set(iter.insert(insertion, false));
        }

        if (insertion.isEmpty()) return 0;

        if (!iter.rewind()) {
            iter = iterator().setFluidFilter(filter).setUsage(IteratorUsage.Insert).build();
        }

        while (iter.hasNext() && !insertion.isEmpty()) {
            ImmutableFluidStack slot = iter.next();

            if (!slot.isEmpty()) continue;

            insertion.set(iter.insert(insertion, false));
        }

        return insertion.getCount();
    }

    default OptionalLong getStoredAmount(@Nullable FluidStackPredicate filter) {
        FluidStackIterator iter = iterator().setFluidFilter(filter).setUsage(IteratorUsage.None).build();

        long sum = 0;

        while (iter.hasNext()) {
            ImmutableFluidStack stack = iter.next();

            if (stack.isEmpty()) continue;

            sum += stack.getCount();
        }

        return sum == 0 ? ZERO_LONG : OptionalLong.of(sum);
    }
}
