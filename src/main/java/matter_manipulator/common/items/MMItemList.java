package matter_manipulator.common.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum MMItemList {

    Cluster,

    MK0,
    MK1,
    MK2,
    MK3,

    HologramProjector,

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

    public ItemStack stack;

    public void set(ItemStack stack) {
        this.stack = stack;
    }

    public void set(Item item) {
        stack = new ItemStack(item, 1);
    }

    public void set(Item item, int meta) {
        stack = new ItemStack(item, 1, meta);
    }

    public ItemStack get() {
        return stack.copy();
    }

    public ItemStack get(int amount) {
        ItemStack copy = stack.copy();
        copy.setCount(amount);
        return copy;
    }
}
