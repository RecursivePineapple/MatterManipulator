package matter_manipulator.core.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;

/// An ImmutableItemStack backed by an ItemStack. This is meant to be allocated once, then modified and returned from an
/// API repeatedly.
public class FastImmutableItemStack implements ImmutableItemStack {

    @Getter
    protected ItemStack stack;
    protected NBTTagCompound capTag;

    public FastImmutableItemStack() {
        this(ItemStack.EMPTY);
    }

    public FastImmutableItemStack(ItemStack stack) {
        set(stack);
    }

    public FastImmutableItemStack set(@NotNull ItemStack stack) {
        this.stack = stack;
        this.capTag = ItemUtils.getCapTag(stack);
        return this;
    }

    @Override
    public @NotNull Item getItem() {
        return stack.getItem();
    }

    @Override
    public int getItemMeta() {
        return ItemUtils.getStackMeta(stack);
    }

    @Override
    public int getCount() {
        return stack.getCount();
    }

    @Override
    public NBTTagCompound getTag() {
        return stack.getTagCompound();
    }

    @Override
    public NBTTagCompound getCapTag() {
        return capTag;
    }
}
