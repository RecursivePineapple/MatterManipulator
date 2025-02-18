package com.recursive_pineapple.matter_manipulator.common.utils;

import static com.recursive_pineapple.matter_manipulator.common.utils.MMValues.W;
import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;
import static net.minecraftforge.common.util.Constants.NBT.TAG_INT;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ItemId {

    public static ItemId create(NBTTagCompound tag) {
        return new AutoValue_ItemId(
            Item.getItemById(tag.getShort("item")),
            tag.getShort("meta"),
            tag.hasKey("tag", TAG_COMPOUND) ? tag.getCompoundTag("tag") : null,
            tag.hasKey("stackSize", TAG_INT) ? tag.getInteger("stackSize") : null
        );
    }

    /**
     * This method copies NBT, as it is mutable.
     */
    public static ItemId create(ItemStack itemStack) {
        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt != null) {
            nbt = (NBTTagCompound) nbt.copy();
        }

        return new AutoValue_ItemId(itemStack.getItem(), Items.feather.getDamage(itemStack), nbt, null);
    }

    /**
     * This method copies NBT, as it is mutable.
     */
    public static ItemId createWithStackSize(ItemStack itemStack) {
        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt != null) {
            nbt = (NBTTagCompound) nbt.copy();
        }

        return new AutoValue_ItemId(
            itemStack.getItem(),
            Items.feather.getDamage(itemStack),
            nbt,
            itemStack.stackSize
        );
    }

    /**
     * This method copies NBT, as it is mutable.
     */
    public static ItemId create(Item item, int metaData, @Nullable NBTTagCompound nbt) {
        if (nbt != null) {
            nbt = (NBTTagCompound) nbt.copy();
        }
        return new AutoValue_ItemId(item, metaData, nbt, null);
    }

    /**
     * This method copies NBT, as it is mutable.
     */
    public static ItemId create(
        Item item,
        int metaData,
        @Nullable NBTTagCompound nbt,
        @Nullable Integer stackSize
    ) {
        if (nbt != null) {
            nbt = (NBTTagCompound) nbt.copy();
        }
        return new AutoValue_ItemId(item, metaData, nbt, stackSize);
    }

    /**
     * This method stores metadata as wildcard and NBT as null.
     */
    public static ItemId createAsWildcard(ItemStack itemStack) {
        return new AutoValue_ItemId(itemStack.getItem(), W, null, null);
    }

    public static ItemId createAsWildcardWithNBT(ItemStack itemStack) {
        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt != null) {
            nbt = (NBTTagCompound) nbt.copy();
        }
        return new AutoValue_ItemId(itemStack.getItem(), W, nbt, null);
    }

    /**
     * This method stores NBT as null.
     */
    public static ItemId createWithoutNBT(ItemStack itemStack) {
        return new AutoValue_ItemId(itemStack.getItem(), Items.feather.getDamage(itemStack), null, null);
    }

    /**
     * This method does not copy NBT in order to save time. Make sure not to mutate it!
     */
    public static ItemId createNoCopy(ItemStack itemStack) {
        return new AutoValue_ItemId(
            itemStack.getItem(),
            Items.feather.getDamage(itemStack),
            itemStack.getTagCompound(),
            null
        );
    }

    /**
     * This method does not copy NBT in order to save time. Make sure not to mutate it!
     */
    public static ItemId createNoCopyWithStackSize(ItemStack itemStack) {
        return new AutoValue_ItemId(
            itemStack.getItem(),
            Items.feather.getDamage(itemStack),
            itemStack.getTagCompound(),
            itemStack.stackSize
        );
    }

    /**
     * This method does not copy NBT in order to save time. Make sure not to mutate it!
     */
    public static ItemId createNoCopy(Item item, int metaData, @Nullable NBTTagCompound nbt) {
        return new AutoValue_ItemId(item, metaData, nbt, null);
    }

    protected abstract Item item();

    protected abstract int metaData();

    @Nullable
    protected abstract NBTTagCompound nbt();

    @Nullable
    protected abstract Integer stackSize();

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setShort("item", (short) Item.getIdFromItem(item()));
        tag.setShort("meta", (short) metaData());
        if (nbt() != null) tag.setTag("tag", nbt());
        Integer s = stackSize();
        if (s != null) tag.setInteger("stackSize", s);
        return tag;
    }

    @Nonnull
    public ItemStack getItemStack() {
        ItemStack itemStack = new ItemStack(item(), 1, metaData());
        NBTTagCompound nbt = nbt();
        itemStack.setTagCompound(nbt == null ? null : (NBTTagCompound) nbt.copy());
        return itemStack;
    }

    @Nonnull
    public ItemStack getItemStack(int stackSize) {
        ItemStack itemStack = new ItemStack(item(), stackSize, metaData());
        NBTTagCompound nbt = nbt();
        itemStack.setTagCompound(nbt == null ? null : (NBTTagCompound) nbt.copy());
        return itemStack;
    }

    public boolean isSameAs(ItemStack stack) {
        if (stack == null) return false;
        if (item() != stack.getItem()) return false;
        if (metaData() != Items.feather.getDamage(stack)) return false;
        if (!Objects.equals(nbt(), stack.getTagCompound())) return false;
        
        return true;
    }
}
