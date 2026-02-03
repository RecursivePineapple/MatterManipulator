package matter_manipulator.core.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import matter_manipulator.common.utils.MathUtils;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.ResourceStack.LongResourceStack;
import matter_manipulator.core.resources.ResourceTrait;
import matter_manipulator.core.resources.item.ItemResourceStack;

@SuppressWarnings("unused")
public class BigItemStack implements ItemResourceStack, LongResourceStack {

    @Getter
    public final ItemId id;
    @Getter
    public long stackSize;

    private BigItemStack(ItemStack stack) {
        this.id = ItemId.create(stack);
        this.stackSize = stack.getCount();
    }

    public BigItemStack(ItemId id, long amount) {
        this.id = id;
        this.stackSize = amount;
    }

    @Override
    public @NotNull Item getItem() {
        return id.getItem();
    }

    @Override
    public int getItemMeta() {
        return id.getItemMeta();
    }

    @Override
    public NBTTagCompound getTag() {
        return this.id.getTag();
    }

    @Override
    public NBTTagCompound getCapTag() {
        return this.id.getCapTag();
    }

    @Override
    public boolean hasTrait(ResourceTrait trait) {
        return switch (trait) {
            case LongAmount -> true;
            default -> false;
        };
    }

    @Override
    public @NotNull Localized getName() {
        return null;
    }

    @Override
    public ItemId getIdentity() {
        return this.id;
    }

    @Override
    public boolean isSameType(ResourceStack other) {
        if (!(other instanceof ItemStackLike item)) return false;

        return matches(item);
    }

    @Override
    public ItemResourceStack emptyCopy() {
        return new BigItemStack(id, 0);
    }

    @Override
    public boolean isEmpty() {
        return stackSize <= 0;
    }

    @Override
    public long getAmountLong() {
        return stackSize;
    }

    @Override
    public void setAmountLong(long amount) {
        this.stackSize = Math.max(0, amount);
    }

    public ItemStack split(int amount) {
        amount = Math.min(MathUtils.longToInt(stackSize), amount);

        shrink(amount);

        return toStack(amount);
    }

    public BigItemStack toBigStack() {
        return copy();
    }

    @Override
    public BigItemStack toBigStack(long amount) {
        return copy().setStackSize(amount);
    }

    public BigItemStack splitBig(long amount) {
        amount = Math.min(stackSize, amount);

        shrink(amount);

        return this.toBigStack(amount);
    }

    public BigItemStack grow(long stackSize) {
        this.stackSize += stackSize;
        return this;
    }

    public BigItemStack shrink(long stackSize) {
        this.stackSize -= stackSize;
        return this;
    }

    public List<ItemStack> toStacks() {
        List<ItemStack> stack = new ArrayList<>();

        while (this.stackSize > 0) {
            stack.add(split(Integer.MAX_VALUE));
        }

        return stack;
    }

    public List<ItemStack> toStacks(int stackSize) {
        List<ItemStack> stack = new ArrayList<>();

        while (this.stackSize > 0) {
            stack.add(split(stackSize));
        }

        return stack;
    }

    public BigItemStack copy() {
        return new BigItemStack(id, this.stackSize);
    }

    public BigItemStack setStackSize(long stackSize) {
        this.stackSize = stackSize;
        return this;
    }

    public static BigItemStack create(ItemStack stack) {
        if (stack == null) return null;

        return new BigItemStack(stack);
    }

    public static BigItemStack create(ItemId id, long amount) {
        if (id == null) return null;

        return new BigItemStack(id, amount);
    }
}
