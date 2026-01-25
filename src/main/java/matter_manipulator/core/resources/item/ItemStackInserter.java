package matter_manipulator.core.resources.item;

import javax.annotation.Nonnegative;

import matter_manipulator.core.item.ImmutableItemStack;

/// Something that inserts item stacks. These are ephemeral objects similar to [ItemStackIterator]s, except they are
/// meant to be kept over several operations. They can operate across several ticks, but their state may get
/// progressively more out of sync with the world. Calling [#reset()] updates an inserter's state to match the world.
/// Even while out of sync, inserters must behave sanely - items cannot be voided or generated, and inserter operations
/// must never throw an exception. Doing nothing when [#store(ImmutableItemStack)] is called is acceptable.
public interface ItemStackInserter {

    default void reset() {

    }

    @Nonnegative
    int store(ImmutableItemStack stack);
}
