package com.recursive_pineapple.matter_manipulator.common.utils;

import static com.recursive_pineapple.matter_manipulator.common.utils.MMValues.W;

import java.util.Objects;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.registry.GameRegistry;

public class LazyItem extends Lazy<ImmutableItemMeta> {

    public final Mods mod;
    public final String itemName;

    public LazyItem(Mods mod, String itemName, int meta) {
        super(() -> {
            if (!mod.isModLoaded()) return null;

            Item item = GameRegistry.findItem(mod.ID, itemName);

            Objects.requireNonNull(item, "could not find item: " + mod.ID + ":" + itemName);

            return new ItemMeta(item, meta);
        });

        this.mod = mod;
        this.itemName = itemName;
    }

    public LazyItem(Mods mod, String itemName) {
        this(mod, itemName, 0);
    }

    public boolean isLoaded() {
        return mod.isModLoaded();
    }

    public boolean matches(Item other, int metaOther) {
        if (!isLoaded()) return false;

        ImmutableItemMeta bm = get();

        if (bm == null) return false;

        return bm.getItem() == other && (bm.getMeta() == metaOther || bm.getMeta() == W || metaOther == W);
    }

    public boolean matches(ItemStack stack) {
        if (stack == null) return false;

        return matches(stack.getItem(), Items.feather.getDamage(stack));
    }
}
