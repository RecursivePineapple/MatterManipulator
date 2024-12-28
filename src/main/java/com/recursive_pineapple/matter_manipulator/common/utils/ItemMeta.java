package com.recursive_pineapple.matter_manipulator.common.utils;

import net.minecraft.item.Item;

public class ItemMeta implements ImmutableItemMeta {

    public Item item;
    public int meta;

    public ItemMeta() {}

    public ItemMeta(Item item, int meta) {
        this.item = item;
        this.meta = meta;
    }

    @Override
    public Item getItem() {
        return item;
    }

    @Override
    public int getMeta() {
        return meta;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void setMeta(int meta) {
        this.meta = meta;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((item == null) ? 0 : item.hashCode());
        result = prime * result + meta;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ItemMeta other = (ItemMeta) obj;
        if (item == null) {
            if (other.item != null) return false;
        } else if (!item.equals(other.item)) return false;
        if (meta != other.meta) return false;
        return true;
    }

    @Override
    public String toString() {
        return "ItemMeta [item=" + item + ", meta=" + meta + "]";
    }
}
