package matter_manipulator.core.fluid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import matter_manipulator.core.resources.Resource;

@FunctionalInterface
public interface FluidStackPredicate extends Predicate<ImmutableFluidStack> {

    boolean test(ImmutableFluidStack stack);

    /// Gets any stacks that this predicate will match against, if possible. This is an optimization for inventories
    /// that maintain a resource map (such as AE). There is no guarantee that the resources passed to [#test(Resource)]
    /// match one of the resources returned from this method.
    /// If this method returns non-null, [#test(FluidStack)] may not scan the inventory. In this case, only matches
    /// for the returned stacks will be tested.
    @Nullable
    default Collection<FluidStackLike> getStacks() {
        return null;
    }

    default @NotNull FluidStackPredicate and(FluidStackPredicate other) {
        Objects.requireNonNull(other);

        return new FluidStackPredicate() {

            private List<FluidStackLike> stacks;

            @Override
            public boolean test(ImmutableFluidStack t) {
                return FluidStackPredicate.this.test(t) && other.test(t);
            }

            @Override
            public @Nullable Collection<FluidStackLike> getStacks() {
                if (this.stacks != null) return this.stacks;

                Collection<FluidStackLike> a = FluidStackPredicate.this.getStacks();
                Collection<FluidStackLike> b = other.getStacks();

                if (a == null && b == null) return null;
                if (a == null || b == null) return a == null ? b : a;

                this.stacks = new ArrayList<>(a.size() + b.size());

                this.stacks.addAll(a);
                this.stacks.addAll(b);

                return this.stacks;
            }
        };
    }

    default @NotNull FluidStackPredicate negate() {
        // There isn't a way to implement getStacks here, so just use a lambda
        return t -> !FluidStackPredicate.this.test(t);
    }

    default @NotNull FluidStackPredicate or(FluidStackPredicate other) {
        Objects.requireNonNull(other);

        return new FluidStackPredicate() {

            @Override
            public boolean test(ImmutableFluidStack t) {
                return FluidStackPredicate.this.test(t) || other.test(t);
            }

            @Override
            public @Nullable Collection<FluidStackLike> getStacks() {
                Collection<FluidStackLike> a = FluidStackPredicate.this.getStacks();
                Collection<FluidStackLike> b = other.getStacks();

                if (a == null && b == null) return null;
                if (a == null || b == null) return a == null ? b : a;

                ArrayList<FluidStackLike> stacks = new ArrayList<>(a.size() + b.size());

                stacks.addAll(a);
                stacks.addAll(b);

                return stacks;
            }
        };
    }

    static @NotNull FluidStackPredicate not(FluidStackPredicate target) {
        Objects.requireNonNull(target);
        return target.negate();
    }

    static @NotNull FluidStackPredicate and(FluidStackPredicate a, FluidStackPredicate b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        return a.and(b);
    }

    static FluidStackPredicate matches(FluidStackLike test) {
        if (test == null) return all();

        return new FluidStackPredicate() {

            @Override
            public boolean test(ImmutableFluidStack stack) {
                if (stack == null) return false;

                return test.matches(stack);
            }

            private List<FluidStackLike> list;

            @Override
            public List<FluidStackLike> getStacks() {
                if (list == null) list = Collections.singletonList(test);

                return list;
            }
        };
    }

    default @NotNull FluidStackPredicate withAmount(int size) {
        return withAmount(size, size);
    }

    default @NotNull FluidStackPredicate withAmount(int min, int max) {
        return new FluidStackPredicate() {

            @Override
            public boolean test(ImmutableFluidStack stack) {
                if (stack.getCount() < min || stack.getCount() > max) return false;

                return FluidStackPredicate.this.test(stack);
            }

            @Override
            public @Nullable Collection<FluidStackLike> getStacks() {
                return FluidStackPredicate.this.getStacks();
            }
        };
    }

    static FluidStackPredicate all() {
        return x -> true;
    }
}
