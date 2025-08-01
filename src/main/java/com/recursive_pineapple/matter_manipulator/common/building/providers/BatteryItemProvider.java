package com.recursive_pineapple.matter_manipulator.common.building.providers;

import java.util.Arrays;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.OreDictionary;

import com.gtnewhorizon.gtnhlib.util.data.ImmutableItemMeta;
import com.gtnewhorizon.gtnhlib.util.data.ItemMeta;
import com.recursive_pineapple.matter_manipulator.common.building.IPseudoInventory;
import com.recursive_pineapple.matter_manipulator.common.utils.BigItemStack;

import org.jetbrains.annotations.Nullable;

public class BatteryItemProvider implements IItemProvider {

    public ImmutableItemMeta battery;

    public BatteryItemProvider() {}

    public static BatteryItemProvider fromStack(ItemStack stack) {
        if (stack == null) return null;

        boolean isBattery = false;

        for (int oreId : OreDictionary.getOreIDs(stack)) {
            if (OreDictionary.getOreName(oreId).startsWith("battery")) {
                isBattery = true;
                break;
            }
        }

        if (!isBattery) return null;

        BatteryItemProvider provider = new BatteryItemProvider();

        provider.battery = new ItemMeta(stack.getItem(), Items.feather.getDamage(stack));

        return provider;
    }

    @Override
    public @Nullable ItemStack getStack(IPseudoInventory inv, boolean consume) {
        ItemStack stack = new ItemStack(battery.getItem(), 1, battery.getItemMeta());

        if (!consume) return stack;

        var result = inv.tryConsumeItems(Arrays.asList(BigItemStack.create(stack)), IPseudoInventory.CONSUME_FUZZY);

        if (!result.leftBoolean()) return null;

        return result.right().get(0).getItemStack();
    }

    @Override
    public IItemProvider clone() {
        BatteryItemProvider provider = new BatteryItemProvider();

        provider.battery = new ItemMeta(battery.getItem(), battery.getItemMeta());

        return provider;
    }
}
