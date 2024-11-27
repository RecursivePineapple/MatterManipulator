package com.recursive_pineapple.matter_manipulator.common.items;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import com.recursive_pineapple.matter_manipulator.common.utils.Mods;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods.Names;

import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.client.GTTooltipHandler;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

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
        return name + "." + getDamage(stack);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> desc, boolean advancedTooltips) {
        super.addInformation(stack, player, desc, advancedTooltips);

        if (Mods.GregTech.isModLoaded()) {
            IDMetaItem metaItem = metaItems[getDamage(stack)];

            String tooltip = metaItem != null ? getItemTier(metaItem) : null;

            if (tooltip != null) desc.add(tooltip);
        }
    }

    private static String getItemTier(IDMetaItem metaItem) {
        return switch(metaItem) {
            case MatterManipulatorPowerCore0 -> Tier.HV.tooltip.get();
            case MatterManipulatorComputerCore0 -> Tier.HV.tooltip.get();
            case MatterManipulatorTeleporterCore0 -> Tier.HV.tooltip.get();
            case MatterManipulatorFrame0 -> Tier.HV.tooltip.get();
            case MatterManipulatorLens0 -> Tier.HV.tooltip.get();
            case MatterManipulatorPowerCore1 -> Tier.IV.tooltip.get();
            case MatterManipulatorComputerCore1 -> Tier.IV.tooltip.get();
            case MatterManipulatorTeleporterCore1 -> Tier.IV.tooltip.get();
            case MatterManipulatorFrame1 -> Tier.IV.tooltip.get();
            case MatterManipulatorLens1 -> Tier.IV.tooltip.get();
            case MatterManipulatorPowerCore2 -> Tier.LuV.tooltip.get();
            case MatterManipulatorComputerCore2 -> Tier.LuV.tooltip.get();
            case MatterManipulatorTeleporterCore2 -> Tier.LuV.tooltip.get();
            case MatterManipulatorFrame2 -> Tier.LuV.tooltip.get();
            case MatterManipulatorLens2 -> Tier.LuV.tooltip.get();
            case MatterManipulatorPowerCore3 -> Tier.ZPM.tooltip.get();
            case MatterManipulatorComputerCore3 -> Tier.ZPM.tooltip.get();
            case MatterManipulatorTeleporterCore3 -> Tier.ZPM.tooltip.get();
            case MatterManipulatorFrame3 -> Tier.ZPM.tooltip.get();
            case MatterManipulatorLens3 -> Tier.ZPM.tooltip.get();
            case MatterManipulatorAEDownlink -> Tier.IV.tooltip.get();
            case MatterManipulatorQuantumDownlink -> Tier.ZPM.tooltip.get();
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
        return meta < 0 || meta >= icons.length ? null : icons[meta];
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

        private Tier() {
            if (Mods.GregTech.isModLoaded()) {
                tooltip = getForTier(name());
            } else {
                tooltip = () -> null;
            }
        }

        @Method(modid = Names.GREG_TECH)
        private static Supplier<String> getForTier(String tier) {
            var t = GTTooltipHandler.Tier.valueOf(tier);

            return ReflectionHelper.getPrivateValue(GTTooltipHandler.Tier.class, t, "tooltip");
        }
    }
}
