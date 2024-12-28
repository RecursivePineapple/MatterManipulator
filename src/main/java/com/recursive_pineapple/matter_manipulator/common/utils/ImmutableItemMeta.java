package com.recursive_pineapple.matter_manipulator.common.utils;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public interface ImmutableItemMeta {
    
    public Item getItem();
    public int getMeta();

    public default Block getBlock() {
        return Block.getBlockFromItem(getItem());
    }
}
