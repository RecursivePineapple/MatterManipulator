package matter_manipulator.core.resources.item;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ItemStackIteratorBuilder {

    /// Sets the filter for this iterator. The iterator must respect the given filter by only returning stacks that
    /// match it. Note that the iterator may still return empty stacks.
    /// Using this method is heavily recommended because it will enable the backend to only query specific stacks in
    /// some situations. If this iterator is drawing from something like an AE system it will only check the handful of
    /// slots that match the predicate, instead of checking every item. Note that this optimization relies on
    /// [ItemStackPredicate#getStacks()] returning a valid collection. If this is not the case, then this method can be
    /// elided.
    ItemStackIteratorBuilder setItemFilter(@Nullable ItemStackPredicate filter);

    /// Instead of checking real items, this iterator should only contain any patterns contained by the parent item
    /// source. Extractions will only be possible while generating an MM plan, in which case the extracted items will be
    /// recorded as plan inputs.
    /// <p/>
    /// Whether extractions succeed is up to the implementation. If it has some way to estimate how many patterns are
    /// craftable, it should return that many items. Otherwise, requests may be completely fulfilled.
    ItemStackIteratorBuilder iteratePatterns(boolean onlyPatterns);

    /// Gives a usage hint to iterator implementations. Implementations may avoid expensive operations if iterator
    /// consumers only need to insert or extract.
    /// Implementations are free to throw an exception if an unexpected insert or extract is performed. I.E. if a
    /// consumer sets the usage to [#Extract] and tries to insert an item, the iterator implementation may throw an
    /// exception, void the item, or behave normally.
    /// Iterator implementations use [Usage#Both] as the default.
    ItemStackIteratorBuilder setUsage(@NotNull Usage usage);

    ItemStackIterator build();

    enum Usage {
        Both,
        Extract,
        Insert,
        /// No extractions or insertions will be done - the iterator will only be used to check the contents of the
        /// inventory.
        None;
    }

    EmptyItemStackIteratorBuilder EMPTY = new EmptyItemStackIteratorBuilder();

    class EmptyItemStackIteratorBuilder implements ItemStackIteratorBuilder {

        @Override
        public ItemStackIteratorBuilder setItemFilter(@Nullable ItemStackPredicate filter) {
            return this;
        }

        @Override
        public ItemStackIteratorBuilder iteratePatterns(boolean onlyPatterns) {
            return this;
        }

        @Override
        public ItemStackIteratorBuilder setUsage(@NotNull Usage usage) {
            return this;
        }

        @Override
        public ItemStackIterator build() {
            return ItemStackIterator.EMPTY;
        }
    }
}
