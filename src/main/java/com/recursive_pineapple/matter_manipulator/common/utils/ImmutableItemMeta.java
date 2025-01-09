package com.recursive_pineapple.matter_manipulator.common.utils;

import static com.recursive_pineapple.matter_manipulator.common.utils.MMValues.W;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface ImmutableItemMeta {

    public Item getItem();

    public int getMeta();

    public default Block getBlock() {
        return Block.getBlockFromItem(getItem());
    }

    public default boolean matches(ItemStack stack) {
        if (stack == null) return false;

        int meta = Items.feather.getDamage(stack);
        return getItem() == stack.getItem() && (getMeta() == meta || getMeta() == W || meta == W);
    }
}
