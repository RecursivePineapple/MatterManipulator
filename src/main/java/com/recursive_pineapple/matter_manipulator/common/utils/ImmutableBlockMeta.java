package com.recursive_pineapple.matter_manipulator.common.utils;

import static com.recursive_pineapple.matter_manipulator.common.utils.MMValues.W;

import com.recursive_pineapple.matter_manipulator.common.building.BlockSpec;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public interface ImmutableBlockMeta {
    
    public Block getBlock();
    public int getMeta();

    public default BlockSpec asSpec() {
        return new BlockSpec().setObject(getBlock(), getMeta());
    }

    public default Item getItem() {
        return Item.getItemFromBlock(getBlock());
    }

    public default boolean matches(Block block, int meta) {
        return getBlock() == block && (getMeta() == meta || getMeta() == W || meta == W);
    }
}
