package matter_manipulator.common.resources.item;

import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;

import matter_manipulator.core.resources.item.ItemStackIO;
import matter_manipulator.core.resources.item.ItemStackIterator;
import matter_manipulator.core.resources.item.ItemStackIteratorBuilder;
import matter_manipulator.core.resources.item.ItemStackPredicate;

public class ItemHandlerItemStackIO implements ItemStackIO {

    public final IItemHandler inv;

    public ItemHandlerItemStackIO(IItemHandler inv) {
        this.inv = inv;
    }

    @Override
    public @NotNull ItemStackIteratorBuilder iterator() {
        return new IteratorBuilder(inv);
    }

    protected static class IteratorBuilder implements ItemStackIteratorBuilder {

        private final IItemHandler inv;
        private ItemStackPredicate filter;
        private boolean onlyPatterns;

        public IteratorBuilder(IItemHandler inv) {
            this.inv = inv;
        }

        @Override
        public ItemStackIteratorBuilder setItemFilter(ItemStackPredicate filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public ItemStackIteratorBuilder iteratePatterns(boolean onlyPatterns) {
            // ignored
            return this;
        }

        @Override
        public ItemStackIteratorBuilder setUsage(@NotNull Usage usage) {
            // ignored
            return this;
        }

        @Override
        public ItemStackIterator build() {
            return new ItemHandlerIterator(inv, filter);
        }
    }
}
