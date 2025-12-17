package com.recursive_pineapple.matter_manipulator.common.items;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import gregtech.client.GTTooltipHandler;

import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods.Names;

public class MetaItem extends Item {

    public final String name;

    public final IIcon[] icons;
    public final IDMetaItem[] metaItems;

    public MetaItem(String name) {
        this.name = name;

        setHasSubtypes(true);
        setMaxDamage(0);

        GameRegistry.registerItem(this, name);

        int max = Arrays.stream(IDMetaItem.values()).mapToInt(x -> x.ID).max().getAsInt();

        icons = new IIcon[max + 1];
        metaItems = new IDMetaItem[max + 1];

        for (IDMetaItem id : IDMetaItem.values()) {
            metaItems[id.ID] = id;
            id.container.set(this, id.ID);
        }
    }

    @Override
    public final Item setUnlocalizedName(String aName) {
        return this;
    }

    @Override
    public final String getUnlocalizedName() {
        return name;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return "item." + name + "." + getDamage(stack);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> desc, boolean advancedTooltips) {
        super.addInformation(stack, player, desc, advancedTooltips);

        int meta = getDamage(stack);

        String descKey = "item." + name + "." + meta + ".desc";

        if (StatCollector.canTranslate(descKey)) {
            desc.add(StatCollector.translateToLocal(descKey));
        }

        MMUpgrades upgrade = MMUpgrades.UPGRADES_BY_META.get(meta);

        if (upgrade != null) {
            desc.add(StatCollector.translateToLocal("mm.upgrade.hint"));

            for (ItemMatterManipulator.ManipulatorTier tier : ItemMatterManipulator.ManipulatorTier.values()) {
                if (tier.allowedUpgrades.contains(upgrade)) {
                    desc.add("- " + tier.container.stack.getDisplayName());
                }
            }
        }

        if (Mods.GregTech.isModLoaded()) {
            IDMetaItem metaItem = MMUtils.getIndexSafe(metaItems, meta);

            String tooltip = metaItem != null ? getItemTier(metaItem) : null;

            if (tooltip != null) desc.add(tooltip);
        }
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    private static String getItemTier(IDMetaItem metaItem) {
        return switch (metaItem) {
            case PowerCore0 -> Tier.HV.tooltip.get();
            case ComputerCore0 -> Tier.HV.tooltip.get();
            case TeleporterCore0 -> Tier.HV.tooltip.get();
            case Frame0 -> Tier.HV.tooltip.get();
            case Lens0 -> Tier.HV.tooltip.get();
            case PowerCore1 -> Tier.IV.tooltip.get();
            case ComputerCore1 -> Tier.IV.tooltip.get();
            case TeleporterCore1 -> Tier.IV.tooltip.get();
            case Frame1 -> Tier.IV.tooltip.get();
            case Lens1 -> Tier.IV.tooltip.get();
            case PowerCore2 -> Tier.LuV.tooltip.get();
            case ComputerCore2 -> Tier.LuV.tooltip.get();
            case TeleporterCore2 -> Tier.LuV.tooltip.get();
            case Frame2 -> Tier.LuV.tooltip.get();
            case Lens2 -> Tier.LuV.tooltip.get();
            case PowerCore3 -> Tier.ZPM.tooltip.get();
            case ComputerCore3 -> Tier.ZPM.tooltip.get();
            case TeleporterCore3 -> Tier.ZPM.tooltip.get();
            case Frame3 -> Tier.ZPM.tooltip.get();
            case Lens3 -> Tier.ZPM.tooltip.get();
            case AEDownlink -> Tier.IV.tooltip.get();
            case QuantumDownlink -> Tier.ZPM.tooltip.get();
            case UpgradeBlank -> Tier.EV.tooltip.get();
            case UpgradePowerP2P -> Tier.UHV.tooltip.get();
            case UpgradePrototypeMining -> Tier.EV.tooltip.get();
            case UpgradeSpeed -> Tier.EV.tooltip.get();
            case UpgradePowerEff -> Tier.EV.tooltip.get();
            default -> null;
        };
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        for (IDMetaItem id : IDMetaItem.values()) {
            icons[id.ID] = iconRegister.registerIcon(Mods.MatterManipulator.getResourcePath("metaitem", Integer.toString(id.ID)));
        }
    }

    @Override
    public IIcon getIconFromDamage(int meta) {
        return MMUtils.getIndexSafe(icons, meta);
    }

    @Override
    public void getSubItems(Item self, CreativeTabs tab, List<ItemStack> subItems) {
        for (IDMetaItem id : IDMetaItem.values()) {
            subItems.add(new ItemStack(this, 1, id.ID));
        }
    }

    private enum Tier {

        ULV,
        LV,
        MV,
        HV,
        EV,
        IV,
        LuV,
        ZPM,
        UV,
        UHV,
        UEV,
        UIV,
        UMV,
        UXV,
        MAX,
        ERV;

        public final Supplier<String> tooltip;

        Tier() {
            if (Mods.GregTech.isModLoaded()) {
                tooltip = getForTier(name());
            } else {
                tooltip = () -> null;
            }
        }

        @Method(modid = Names.GREG_TECH_NH)
        private static Supplier<String> getForTier(String tier) {
            var t = GTTooltipHandler.Tier.valueOf(tier);

            return ReflectionHelper.getPrivateValue(GTTooltipHandler.Tier.class, t, "tooltip");
        }
    }
}
