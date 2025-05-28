package com.recursive_pineapple.matter_manipulator.common.utils;

import static com.recursive_pineapple.matter_manipulator.common.utils.MMValues.W;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import com.recursive_pineapple.matter_manipulator.common.building.BlockSpec;

public interface ImmutableBlockMeta {

    Block getBlock();

    int getMeta();

    default BlockSpec asSpec() {
        return new BlockSpec().setObject(getBlock(), getMeta());
    }

    default Item getItem() {
        return Item.getItemFromBlock(getBlock());
    }

    default boolean matches(Block block, int meta) {
        return getBlock() == block && (getMeta() == meta || getMeta() == W || meta == W);
    }
}
