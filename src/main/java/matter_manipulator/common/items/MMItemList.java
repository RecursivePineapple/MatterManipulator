package matter_manipulator.common.items;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import matter_manipulator.core.item.ImmutableItemMeta;

public enum MMItemList implements ImmutableItemMeta {

    Cluster,

    MK0,
    MK1,
    MK2,
    MK3,

    HologramProjector,
    Wrench,

    Hologram,
    PowerCore0,
    ComputerCore0,
    TeleporterCore0,
    Frame0,
    Lens0,

    PowerCore1,
    ComputerCore1,
    TeleporterCore1,
    Frame1,
    Lens1,

    PowerCore2,
    ComputerCore2,
    TeleporterCore2,
    Frame2,
    Lens2,

    PowerCore3,
    ComputerCore3,
    TeleporterCore3,
    Frame3,
    Lens3,

    AEDownlink,
    QuantumDownlink,

    UplinkController,
    UplinkHatch,

    UpgradeBlank,
    UpgradePowerP2P,
    UpgradePrototypeMining,
    UpgradeSpeed,
    UpgradePowerEff,

    ;

    public ItemStack stack = ItemStack.EMPTY;

    public void set(ItemStack stack) {
        this.stack = stack;
    }

    public void set(Item item) {
        stack = new ItemStack(item, 1);
    }

    public void set(Item item, int meta) {
        stack = new ItemStack(item, 1, meta);
    }

    @Override
    public @NotNull Item getItem() {
        return stack.getItem();
    }

    @Override
    public int getItemMeta() {
        return Items.FEATHER.getDamage(stack);
    }
}
