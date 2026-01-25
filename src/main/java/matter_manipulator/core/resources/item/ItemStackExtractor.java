package matter_manipulator.core.resources.item;

import javax.annotation.Nonnegative;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/// Something that extracts item stacks. These are ephemeral objects similar to [ItemStackIterator]s, except they are
/// meant to be kept over several operations. They can operate across several ticks, but their state may get
/// progressively more out of sync with the world. Calling [#reset()] updates an extractor's state to match the world.
/// Even while out of sync, extractors must behave sanely - items cannot be voided or generated, and extractor
/// operations must never throw an exception. If [#reset()] has not been called recently, falsely doing nothing when
/// [#pull(ItemStackPredicate, int)] is called is acceptable.
public interface ItemStackExtractor {

    default void reset() {

    }

    @NotNull
    ItemStack pull(@Nullable ItemStackPredicate filter, @Nonnegative int amount);
}
