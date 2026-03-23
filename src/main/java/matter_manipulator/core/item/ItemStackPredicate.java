package matter_manipulator.core.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import matter_manipulator.common.utils.DataUtils;
import matter_manipulator.core.resources.Resource;

@FunctionalInterface
public interface ItemStackPredicate extends Predicate<ImmutableItemStack> {

    boolean test(ImmutableItemStack stack);

    /// Gets any stacks that this predicate will match against, if possible. This is an optimization for inventories
    /// that maintain a resource map (such as AE). There is no guarantee that the resources passed to [#test(Resource)]
    /// match one of the resources returned from this method.
    /// If this method returns non-null, [#test(ItemStack)] may not scan the inventory. In this case, only matches
    /// for the returned stacks will be tested.
    @Nullable
    default Collection<ItemStackLike> getStacks() {
        return null;
    }

    default @NotNull ItemStackPredicate and(ItemStackPredicate other) {
        Objects.requireNonNull(other);

        return new ItemStackPredicate() {

            private List<ItemStackLike> stacks;

            @Override
            public boolean test(ImmutableItemStack t) {
                return ItemStackPredicate.this.test(t) && other.test(t);
            }

            @Override
            public @Nullable Collection<ItemStackLike> getStacks() {
                if (this.stacks != null) return this.stacks;

                Collection<ItemStackLike> a = ItemStackPredicate.this.getStacks();
                Collection<ItemStackLike> b = other.getStacks();

                if (a == null && b == null) return null;
                if (a == null || b == null) return a == null ? b : a;

                this.stacks = new ArrayList<>(a.size() + b.size());

                this.stacks.addAll(a);
                this.stacks.addAll(b);

                return this.stacks;
            }
        };
    }

    default @NotNull ItemStackPredicate negate() {
        // There isn't a way to implement getStacks here, so just use a lambda
        return t -> !ItemStackPredicate.this.test(t);
    }

    default @NotNull ItemStackPredicate or(ItemStackPredicate other) {
        Objects.requireNonNull(other);

        return new ItemStackPredicate() {

            @Override
            public boolean test(ImmutableItemStack t) {
                return ItemStackPredicate.this.test(t) || other.test(t);
            }

            @Override
            public @Nullable Collection<ItemStackLike> getStacks() {
                Collection<ItemStackLike> a = ItemStackPredicate.this.getStacks();
                Collection<ItemStackLike> b = other.getStacks();

                if (a == null && b == null) return null;
                if (a == null || b == null) return a == null ? b : a;

                ArrayList<ItemStackLike> stacks = new ArrayList<>(a.size() + b.size());

                stacks.addAll(a);
                stacks.addAll(b);

                return stacks;
            }
        };
    }

    static @NotNull ItemStackPredicate not(ItemStackPredicate target) {
        Objects.requireNonNull(target);
        return target.negate();
    }

    static @NotNull ItemStackPredicate and(ItemStackPredicate a, ItemStackPredicate b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        return a.and(b);
    }

    static ItemStackPredicate matches(ItemStackLike test) {
        if (test == null) return all();

        return new ItemStackPredicate() {

            @Override
            public boolean test(ImmutableItemStack stack) {
                if (stack == null) return false;

                return test.matches(stack);
            }

            private List<ItemStackLike> list;

            @Override
            public List<ItemStackLike> getStacks() {
                if (list == null) list = Collections.singletonList(test);

                return list;
            }
        };
    }

    static ItemStackPredicate oredict(String name, boolean checkNBT) {
        List<ItemStackLike> ores = DataUtils.mapToList(OreDictionary.getOres(name, false), FastImmutableItemStack::new);

        ObjectOpenCustomHashSet<Object> stacks = new ObjectOpenCustomHashSet<>(
            ores,
            checkNBT ? ItemId.GENERIC_ITEM_META_NBT_STRATEGY : ItemId.GENERIC_ITEM_META_STRATEGY);

        return new ItemStackPredicate() {

            @Override
            public boolean test(ImmutableItemStack stack) {
                return stacks.contains(stack);
            }

            @Override
            public Collection<ItemStackLike> getStacks() {
                return ores;
            }
        };
    }

    default @NotNull ItemStackPredicate withAmount(int size) {
        return withAmount(size, size);
    }

    default @NotNull ItemStackPredicate withAmount(int min, int max) {
        return new ItemStackPredicate() {

            @Override
            public boolean test(ImmutableItemStack stack) {
                if (stack.getCount() < min || stack.getCount() > max) return false;

                return ItemStackPredicate.this.test(stack);
            }

            @Override
            public @Nullable Collection<ItemStackLike> getStacks() {
                return ItemStackPredicate.this.getStacks();
            }
        };
    }

    static ItemStackPredicate all() {
        return x -> true;
    }
}
