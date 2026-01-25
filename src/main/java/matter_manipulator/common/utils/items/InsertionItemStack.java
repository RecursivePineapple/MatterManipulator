package matter_manipulator.common.utils.items;

import java.util.Objects;

import javax.annotation.Nonnegative;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

import matter_manipulator.core.item.ImmutableItemStack;
import matter_manipulator.mixin.mixins.minecraft.AccessorItemStack;

/// A [ImmutableItemStack] implementation that's meant to be passed to the various insert methods.
public class InsertionItemStack implements ImmutableItemStack {

    private boolean useImmutable = false;
    private ItemStack stack;
    private NBTTagCompound capTag;
    private ImmutableItemStack immutable;
    @Nonnegative
    private int amountToInsert;

    /// Must be initialized later
    public InsertionItemStack() {

    }

    public InsertionItemStack(@NotNull ItemStack stack) {
        set(stack, stack.getCount());
    }

    public InsertionItemStack(@NotNull ItemStack stack, @Nonnegative int amountToInsert) {
        set(stack, amountToInsert);
    }

    public InsertionItemStack(@NotNull ImmutableItemStack stack) {
        set(stack, stack.getCount());
    }

    public InsertionItemStack(@NotNull ImmutableItemStack stack, @Nonnegative int amountToInsert) {
        set(stack, amountToInsert);
    }

    public InsertionItemStack set(ItemStack stack) {
        this.useImmutable = false;
        this.stack = Objects.requireNonNull(stack, "stack cannot be null");
        this.amountToInsert = Math.max(0, stack.getCount());
        this.capTag = ((AccessorItemStack) (Object) stack).mm$getCapabilities().serializeNBT();
        return this;
    }

    public InsertionItemStack set(ItemStack stack, int amount) {
        this.useImmutable = false;
        this.stack = Objects.requireNonNull(stack, "stack cannot be null");
        this.amountToInsert = Math.max(0, amount);
        this.capTag = ((AccessorItemStack) (Object) stack).mm$getCapabilities().serializeNBT();
        return this;
    }

    public InsertionItemStack set(ImmutableItemStack stack) {
        this.useImmutable = true;
        this.immutable = Objects.requireNonNull(stack, "stack cannot be null");
        this.amountToInsert = Math.max(0, stack.getCount());
        return this;
    }

    public InsertionItemStack set(ImmutableItemStack stack, int amount) {
        this.useImmutable = true;
        this.immutable = Objects.requireNonNull(stack, "stack cannot be null");
        this.amountToInsert = Math.max(0, amount);
        return this;
    }

    public InsertionItemStack set(int amount) {
        this.amountToInsert = Math.max(0, amount);
        return this;
    }

    public InsertionItemStack decrement(int amount) {
        this.amountToInsert = Math.max(0, amountToInsert - amount);
        return this;
    }

    @Override
    public int getCount() {
        return amountToInsert;
    }

    @Override
    public @NotNull Item getItem() {
        return Objects.requireNonNull(useImmutable ? immutable.getItem() : stack.getItem(), "item cannot be null");
    }

    @Override
    public int getItemMeta() {
        return useImmutable ? immutable.getItemMeta() : ItemUtils.getStackMeta(stack);
    }

    @Override
    public NBTTagCompound getTag() {
        return useImmutable ? immutable.getTag() : stack.getTagCompound();
    }

    @Override
    public NBTTagCompound getCapTag() {
        return useImmutable ? immutable.getCapTag() : capTag;
    }
}
