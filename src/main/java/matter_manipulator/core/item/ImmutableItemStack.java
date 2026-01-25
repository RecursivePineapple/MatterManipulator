package matter_manipulator.core.item;

import net.minecraft.item.ItemStack;

import matter_manipulator.common.utils.items.FastImmutableItemStack;

/**
 * An immutable version of {@link ItemStack} for situations where ItemStacks should never be modified.
 */
public interface ImmutableItemStack extends ItemStackLike {

    ImmutableItemStack EMPTY = new FastImmutableItemStack(ItemStack.EMPTY);

    int getCount();

    default boolean isEmpty() {
        return getCount() <= 0;
    }

    default ItemStack toStack() {
        if (isEmpty()) return ItemStack.EMPTY;

        return toStack(getCount());
    }

    default ImmutableItemStack copy() {
        return new FastImmutableItemStack(toStack());
    }

    /// Creates an ItemStack that matches this object, without copying the NBT (use with caution!).
    default ItemStack toStackFast() {
        return toStackFast(getCount());
    }
}
