package matter_manipulator.core.resources.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

import matter_manipulator.common.utils.items.ItemUtils;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.item.ItemId;
import matter_manipulator.core.item.ItemStackLike;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.ResourceStack.IntResourceStack;
import matter_manipulator.core.resources.ResourceTrait;

public class ItemStackWrapper implements ItemResourceStack, IntResourceStack {

    public ItemStack stack;

    public ItemStackWrapper(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public boolean hasTrait(ResourceTrait trait) {
        return switch (trait) {
            case IntAmount -> true;
            default -> false;
        };
    }

    @Override
    public @NotNull Localized getName() {
        return new Localized("mm.misc.itemstack", toStack(1));
    }

    @Override
    public ItemId getIdentity() {
        return ItemId.create(stack);
    }

    @Override
    public boolean isSameType(ResourceStack other) {
        if (!(other instanceof ItemStackLike item)) return false;

        return matches(item);
    }

    @Override
    public ItemStackWrapper emptyCopy() {
        return new ItemStackWrapper(ItemUtils.copyWithAmount(this.stack, 0));
    }

    @Override
    public int getAmountInt() {
        return stack.getCount();
    }

    @Override
    public void setAmountInt(int amount) {
        stack.setCount(Math.max(0, amount));
    }

    @Override
    public boolean isEmpty() {
        return stack == ItemStack.EMPTY || stack.getCount() <= 0;
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
    public NBTTagCompound getTag() {
        return stack.getTagCompound();
    }

    @Override
    public NBTTagCompound getCapTag() {
        return ItemUtils.getCapTag(stack);
    }
}
