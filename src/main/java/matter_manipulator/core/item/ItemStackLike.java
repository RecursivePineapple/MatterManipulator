package matter_manipulator.core.item;

import java.util.Objects;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import matter_manipulator.common.utils.MCUtils;
import matter_manipulator.common.utils.items.ItemUtils;

/// Something that acts like an ItemStack. Does not include amounts, use [ImmutableItemStack] for that instead.
public interface ItemStackLike extends ImmutableItemMeta {

    NBTTagCompound getTag();

    NBTTagCompound getCapTag();

    default ItemStack toStack(int amount) {
        int meta = getItemMeta();

        ItemStack stack = new ItemStack(getItem(), amount, meta == OreDictionary.WILDCARD_VALUE ? 0 : meta, MCUtils.copy(getCapTag()));

        stack.setTagCompound(MCUtils.copy(getTag()));

        return stack;
    }

    default BigItemStack toBigStack(long amount) {
        int meta = getItemMeta();

        return new BigItemStack(ItemId.create(getItem(), meta == OreDictionary.WILDCARD_VALUE ? 0 : meta, MCUtils.copy(getTag()), MCUtils.copy(getCapTag())), amount);
    }

    /// Creates an [ItemStack] that matches this object, without copying the NBT (use with caution!).
    default ItemStack toStackFast(int amount) {
        int meta = getItemMeta();

        ItemStack stack = new ItemStack(getItem(), amount, meta == OreDictionary.WILDCARD_VALUE ? 0 : meta, MCUtils.copy(getCapTag()));

        stack.setTagCompound(getTag());

        return stack;
    }

    /// Creates a [BigItemStack] that matches this object, without copying the NBT (use with caution!).
    default BigItemStack toBigStackFast(long amount) {
        int meta = getItemMeta();

        return new BigItemStack(ItemId.create(getItem(), meta == OreDictionary.WILDCARD_VALUE ? 0 : meta, getTag(), getCapTag()), amount);
    }

    default boolean matches(ItemStack stack) {
        if (stack == null) return false;

        if (getItem() != stack.getItem()) return false;
        if (getItemMeta() == OreDictionary.WILDCARD_VALUE) return true;
        if (ItemUtils.getStackMeta(stack) == OreDictionary.WILDCARD_VALUE) return true;
        if (getItemMeta() != ItemUtils.getStackMeta(stack)) return false;
        if (!Objects.equals(getTag(), stack.getTagCompound())) return false;

        return Objects.equals(getCapTag(), ItemUtils.getCapTag(stack));
    }

    default boolean matches(ItemStackLike stack) {
        if (stack == null) return false;

        if (getItem() != stack.getItem()) return false;
        if (getItemMeta() == OreDictionary.WILDCARD_VALUE) return true;
        if (stack.getItemMeta() == OreDictionary.WILDCARD_VALUE) return true;
        if (getItemMeta() != stack.getItemMeta()) return false;
        if (!Objects.equals(getTag(), stack.getTag())) return false;

        return Objects.equals(getCapTag(), stack.getCapTag());
    }
}
