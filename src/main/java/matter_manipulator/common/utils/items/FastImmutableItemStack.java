package matter_manipulator.common.utils.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import matter_manipulator.core.item.ImmutableItemStack;
import matter_manipulator.mixin.mixins.minecraft.AccessorItemStack;

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
        this.capTag = ((AccessorItemStack) (Object) stack).mm$getCapabilities().serializeNBT();
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
