package matter_manipulator.common.items;

import net.minecraft.item.ItemStack;

import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public enum MMUpgrades {

    PowerP2P(IDMetaItem.UpgradePowerP2P, 0, 0),
    Mining(IDMetaItem.UpgradePrototypeMining, 1, ItemMatterManipulator.ALLOW_REMOVING),
    Speed(IDMetaItem.UpgradeSpeed, 2, 0),
    PowerEff(IDMetaItem.UpgradePowerEff, 3, 0),
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
    public final int providesCaps;

    MMUpgrades(IDMetaItem id, int bit, int providesCaps) {
        this.id = id.ID;
        this.bit = bit;
        this.providesCaps = providesCaps;
    }

    public ItemStack getStack() {
        return new ItemStack(MMItems.META_ITEM, 1, id);
    }
}
