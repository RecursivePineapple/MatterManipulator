package matter_manipulator.common.resources.item.ios;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.common.resources.item.AbstractItemStackIterator;
import matter_manipulator.common.utils.DataUtils;
import matter_manipulator.core.item.ImmutableItemStack;
import matter_manipulator.core.item.ItemStackIO;
import matter_manipulator.core.item.ItemStackIterator;
import matter_manipulator.core.item.ItemStackIteratorBuilder;
import matter_manipulator.core.item.ItemStackPredicate;
import matter_manipulator.core.resources.IteratorUsage;

public class FreeItemStackIOFactory implements ItemStackIO {

    @Override
    public @NotNull ItemStackIteratorBuilder iterator() {
        return new Builder();
    }

    @Override
    public @NotNull ItemStack pull(@Nullable ItemStackPredicate filter, int amount) {
        for (ImmutableItemStack stack : MMRegistriesInternal.FREE_ITEMS) {
            if (filter == null || filter.test(stack)) return stack.toStack(amount);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public int store(ImmutableItemStack request) {
        for (ImmutableItemStack stack : MMRegistriesInternal.FREE_ITEMS) {
            if (request.matches(stack)) return 0;
        }

        return request.getCount();
    }

    private static class Builder implements ItemStackIteratorBuilder {

        private ItemStackPredicate filter;
        private boolean onlyPatterns;

        @Override
        public ItemStackIteratorBuilder setItemFilter(@Nullable ItemStackPredicate filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public ItemStackIteratorBuilder iteratePatterns(boolean onlyPatterns) {
            this.onlyPatterns = onlyPatterns;
            return this;
        }

        @Override
        public ItemStackIteratorBuilder setUsage(@NotNull IteratorUsage usage) {
            return this;
        }

        @Override
        public ItemStackIterator build() {
            if (onlyPatterns) return ItemStackIterator.EMPTY;

            List<ImmutableItemStack> items = Arrays.asList(MMRegistriesInternal.FREE_ITEMS);

            if (filter != null) {
                items = DataUtils.filterList(items, filter);
            }

            final var items2 = items;

            return new AbstractItemStackIterator(items.size()) {

                @Override
                protected ItemStack getStackInSlot(int slot) {
                    return items2.get(slot).toStackFast();
                }

                @Override
                public @NotNull ItemStack extract(int amount, boolean forced) {
                    return items2.get(getCurrentSlot()).toStack(amount);
                }

                @Override
                public int insert(ImmutableItemStack stack, boolean forced) {
                    var current = items2.get(getCurrentSlot());

                    if (!stack.matches(current)) return stack.getCount();

                    return 0;
                }
            };
        }
    }
}
