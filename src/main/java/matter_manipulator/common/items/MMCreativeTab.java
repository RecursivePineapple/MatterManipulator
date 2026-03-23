package matter_manipulator.common.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class MMCreativeTab extends CreativeTabs {

    public static final MMCreativeTab INSTANCE = new MMCreativeTab();

    public MMCreativeTab() {
        super("matter-manipulator");
    }

    @Override
    public @NotNull ItemStack createIcon() {
        return MMItemList.MK3.toStack(1);
    }
}
