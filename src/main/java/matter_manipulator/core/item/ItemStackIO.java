package matter_manipulator.core.item;

import java.util.OptionalInt;

import javax.annotation.Nonnegative;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import matter_manipulator.MMMod;
import matter_manipulator.common.utils.MathUtils;
import matter_manipulator.core.meta.MetadataContainer;
import matter_manipulator.core.resources.IteratorUsage;
import matter_manipulator.core.resources.item.ItemResourceProvider;

public interface ItemStackIO {

    /// Sets the meta container for this IO. This is used for inter-IO communication, in case two IOs need to cooperate.
    /// The container is cleared prior to all [ItemResourceProvider] operations.
    default void setMetaContainer(MetadataContainer container) {

    }

    /// Creates an iterator builder for the items in this source. Must return [ItemStackIterator#EMPTY] if iterators are
    /// not supported. Modifying any backing inventories while this iterator exists (without going through the iterator)
    /// is undefined behaviour, but it should never duplicate or delete items.
    @NotNull ItemStackIteratorBuilder iterator();

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    OptionalInt ZERO = OptionalInt.of(0);

    default ItemStackExtractor createExtractor() {
        return this::pull;
    }

    @NotNull
    default ItemStack pull(@Nullable ItemStackPredicate filter, @Nonnegative int amount) {
        ItemStackIterator iter = iterator().setItemFilter(filter).setUsage(IteratorUsage.Extract).build();

        long sum = 0;

        ItemId resultFilter = null;

        while (iter.hasNext()) {
            ImmutableItemStack stack = iter.next();

            if (stack.isEmpty()) continue;
            if (resultFilter != null && !stack.matches(resultFilter)) continue;

            sum += stack.getCount();

            if (resultFilter == null) {
                resultFilter = ItemId.create(stack);
            }
        }

        if (sum < amount || resultFilter == null) return ItemStack.EMPTY;

        if (!iter.rewind()) {
            iter = iterator().setItemFilter(filter).setUsage(IteratorUsage.Extract).build();
        }

        int remaining = amount;

        ItemStack result = ItemStack.EMPTY;

        while (iter.hasNext() && remaining > 0) {
            ImmutableItemStack stack = iter.next();

            if (stack.isEmpty()) continue;
            if (!stack.matches(resultFilter)) continue;

            ItemStack extracted = iter.extract(remaining, false);

            if (!extracted.isEmpty()) {
                if (resultFilter.matches(extracted)) {
                    if (result.isEmpty()) {
                        result = extracted;
                    } else {
                        result.grow(extracted.getCount());
                    }

                    remaining -= extracted.getCount();
                } else {
                    MMMod.LOG.error("ItemStackIO.pull() has misbehaved! extract() returned a stack that was supposed to be something else! Trying to reinsert the stack - items may be voided. Expected={} Actual={} Iterator={}", resultFilter, extracted, iter);
                    store(new InsertionItemStack(extracted));
                }
            }
        }

        if (result.getCount() < amount) {
            MMMod.LOG.error("ItemStackIO.pull() has misbehaved! The iterator reported having more items than could be extracted! Reported amount={} Extracted={} Iterator={}", sum, result, iter);
            store(new InsertionItemStack(result));
            result = ItemStack.EMPTY;
        }

        return result;
    }

    default ItemStackInserter createInserter() {
        return this::store;
    }

    default int store(ImmutableItemStack stack) {
        ItemStackPredicate filter = ItemStackPredicate.matches(stack);

        ItemStackIterator iter = iterator().setItemFilter(filter).setUsage(IteratorUsage.Insert).build();

        InsertionItemStack insertion = new InsertionItemStack(stack);

        while (iter.hasNext() && !insertion.isEmpty()) {
            ImmutableItemStack slot = iter.next();

            if (!slot.isEmpty() && !slot.matches(stack)) continue;

            insertion.set(iter.insert(insertion, false));
        }

        if (insertion.isEmpty()) return 0;

        if (!iter.rewind()) {
            iter = iterator().setItemFilter(filter).setUsage(IteratorUsage.Insert).build();
        }

        while (iter.hasNext() && !insertion.isEmpty()) {
            ImmutableItemStack slot = iter.next();

            if (!slot.isEmpty()) continue;

            insertion.set(iter.insert(insertion, false));
        }

        return insertion.getCount();
    }

    default OptionalInt getStoredAmount(@Nullable ItemStackPredicate filter) {
        ItemStackIterator iter = iterator().setItemFilter(filter).setUsage(IteratorUsage.None).build();

        long sum = 0;

        while (iter.hasNext()) {
            ImmutableItemStack stack = iter.next();

            if (stack.isEmpty()) continue;

            sum += stack.getCount();
        }

        return sum == 0 ? ZERO : OptionalInt.of(MathUtils.longToInt(sum));
    }
}
