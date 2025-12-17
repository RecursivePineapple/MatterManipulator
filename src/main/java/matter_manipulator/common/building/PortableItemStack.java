package com.recursive_pineapple.matter_manipulator.common.building;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

import com.google.gson.annotations.SerializedName;
import com.recursive_pineapple.matter_manipulator.common.building.providers.IItemProvider;

/**
 * An item stack that can be moved between worlds without id shifting.
 */
public class PortableItemStack implements IItemProvider {

    @SerializedName("id")
    public UniqueIdentifier item;
    @SerializedName("m")
    public Integer metadata;
    @SerializedName("a")
    public Integer amount;
    @SerializedName("nbt")
    public NBTTagCompound nbt;

    public transient Item itemRef;
    public transient Block blockRef;
    public transient ItemStack itemStack;

    public PortableItemStack() {}

    public PortableItemStack(Item item, int metadata) {
        this.item = GameRegistry.findUniqueIdentifierFor(item);
        this.metadata = metadata == 0 ? null : metadata;
        this.amount = null;
    }

    public PortableItemStack(Item item, int metadata, NBTTagCompound nbt) {
        this.item = GameRegistry.findUniqueIdentifierFor(item);
        this.metadata = metadata == 0 ? null : metadata;
        this.amount = null;
        this.nbt = nbt;
    }

    public PortableItemStack(ItemStack stack) {
        item = GameRegistry.findUniqueIdentifierFor(stack.getItem());
        metadata = Items.feather.getDamage(stack);
        if (metadata == 0) metadata = null;
        amount = stack.stackSize == 1 ? null : stack.stackSize;
    }

    public static PortableItemStack withoutStackSize(ItemStack stack) {
        stack = stack.copy();
        stack.stackSize = 1;
        return new PortableItemStack(stack);
    }

    public static PortableItemStack withNBT(ItemStack stack) {
        PortableItemStack portable = new PortableItemStack(stack);
        portable.nbt = stack.getTagCompound() == null ? null : (NBTTagCompound) stack.getTagCompound().copy();
        return portable;
    }

    public int getMeta() {
        return metadata == null ? 0 : metadata.intValue();
    }

    public Item getItem() {
        if (blockRef == null) {
            itemRef = GameRegistry.findItem(item.modId, item.name);
        }

        return itemRef;
    }

    public ItemStack toStack() {
        if (itemStack == null) {
            Item item = getItem();

            if (item == null) return null;

            itemStack = new ItemStack(
                item,
                amount == null ? 1 : amount,
                metadata == null ? 0 : metadata
            );

            if (nbt != null) {
                itemStack.setTagCompound((NBTTagCompound) nbt.copy());
            }
        }

        return itemStack.copy();
    }

    public Block getBlock() {
        if (blockRef == null) {
            blockRef = GameRegistry.findBlock(item.modId, item.name);
        }

        return blockRef;
    }

    @Override
    public ItemStack getStack(IPseudoInventory inv, boolean consume) {
        ItemStack stack = toStack();

        if (!consume) { return stack; }

        if (!inv.tryConsumeItems(stack)) { return null; }

        return stack;
    }

    @Override
    public String toString() {
        return toStack().getDisplayName() + (amount == null || amount == 1 ? "" : " x " + amount);
    }

    @Override
    public PortableItemStack clone() {
        PortableItemStack dup = new PortableItemStack();

        dup.item = item;
        dup.metadata = metadata;
        dup.amount = amount;
        dup.nbt = nbt == null ? null : (NBTTagCompound) nbt.copy();
        dup.itemRef = itemRef;
        dup.blockRef = blockRef;
        dup.itemStack = itemStack;

        return dup;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((item == null) ? 0 : item.hashCode());
        result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
        result = prime * result + ((amount == null) ? 0 : amount.hashCode());
        result = prime * result + ((nbt == null) ? 0 : nbt.hashCode());
        result = prime * result + ((itemStack == null) ? 0 : itemStack.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        PortableItemStack other = (PortableItemStack) obj;
        if (item == null) {
            if (other.item != null) return false;
        } else if (!item.equals(other.item)) return false;
        if (metadata == null) {
            if (other.metadata != null) return false;
        } else if (!metadata.equals(other.metadata)) return false;
        if (amount == null) {
            if (other.amount != null) return false;
        } else if (!amount.equals(other.amount)) return false;
        if (nbt == null) {
            if (other.nbt != null) return false;
        } else if (!nbt.equals(other.nbt)) return false;
        if (itemStack == null) {
            if (other.itemStack != null) return false;
        } else if (!itemStack.equals(other.itemStack)) return false;
        return true;
    }
}
