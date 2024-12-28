package com.recursive_pineapple.matter_manipulator.common.utils;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public interface ImmutableBlockMeta {
    
    public Block getBlock();
    public int getMeta();

    public default Item getItem() {
        return Item.getItemFromBlock(getBlock());
    }
}
