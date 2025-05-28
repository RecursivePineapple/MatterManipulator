package com.recursive_pineapple.matter_manipulator.common.items;

import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public enum MMUpgrades {

    Power(IDMetaItem.UpgradePower, 0),
    //
    ;

    public static final Int2ObjectMap<MMUpgrades> UPGRADES_BY_META = new Int2ObjectOpenHashMap<>();
    public static final Int2ObjectMap<MMUpgrades> UPGRADES_BY_BIT = new Int2ObjectOpenHashMap<>();

    static {
        for (MMUpgrades upgrade : MMUpgrades.values()) {
            UPGRADES_BY_META.put(upgrade.id, upgrade);
            UPGRADES_BY_BIT.put(upgrade.bit, upgrade);
        }
    }

    public final int id;
    public final int bit;

    MMUpgrades(IDMetaItem id, int bit) {
        this.id = id.ID;
        this.bit = bit;
    }

    public ItemStack getStack() {
        return new ItemStack(MMItems.META_ITEM, 1, id);
    }
}
