package com.recursive_pineapple.matter_manipulator.common.compat;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

import com.google.gson.JsonElement;

/** A property which is kept when a block is destroyed. */
public interface IntrinsicProperty {

    String getName();

    boolean hasValue(ItemStack stack);

    boolean hasValue(IBlockAccess world, int x, int y, int z);

    JsonElement getValue(ItemStack stack);

    JsonElement getValue(IBlockAccess world, int x, int y, int z);

    void setValue(ItemStack stack, JsonElement value);

    void setValue(IBlockAccess world, int x, int y, int z, JsonElement value);

    default void getItemDetails(List<String> details, JsonElement value) {}
}
