package com.recursive_pineapple.matter_manipulator.common.utils;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class BigItemStack {
    
    public Item item;
    public long amount;
    public int meta;
    public NBTTagCompound tag;

    public transient ItemId id;

    public BigItemStack(Item item, long amount) {
        this.item = item;
        this.amount = amount;
    }

    public BigItemStack(ItemStack stack) {
        this.item = stack.getItem();
        this.amount = stack.stackSize;
        this.meta = Items.feather.getDamage(stack);
        this.tag = stack.getTagCompound();
    }

    public ItemId getId() {
        if (id == null) {
            id = ItemId.create(item, meta, tag);
        }

        return id;
    }

    public ItemStack remove(int amount) {
        if (this.amount < amount) {
            ItemStack stack = new ItemStack(item, (int) this.amount, this.meta);
            stack.setTagCompound(tag == null ? null : (NBTTagCompound) tag.copy());
            this.amount = 0;
            return stack;
        } else {
            ItemStack stack = new ItemStack(item, amount, this.meta);
            stack.setTagCompound(tag == null ? null : (NBTTagCompound) tag.copy());
            this.amount -= amount;
            return stack;
        }
    }

    public List<ItemStack> toStacks() {
        List<ItemStack> stack = new ArrayList<>();

        while (this.amount > 0) {
            stack.add(remove(Integer.MAX_VALUE));
        }

        return stack;
    }

    public boolean hasSubtypes() {
        return item.getHasSubtypes();
    }
}
