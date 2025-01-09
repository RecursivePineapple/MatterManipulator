package com.recursive_pineapple.matter_manipulator.common.uplink;

import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import com.google.common.collect.MapMaker;
import com.recursive_pineapple.matter_manipulator.common.building.IPseudoInventory;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.Location;
import com.recursive_pineapple.matter_manipulator.common.utils.BigFluidStack;
import com.recursive_pineapple.matter_manipulator.common.utils.BigItemStack;

import it.unimi.dsi.fastutil.Pair;

public interface IUplinkMulti {

    public boolean isActive();

    public Location getLocation();

    public UplinkState getState();

    @SideOnly(Side.CLIENT)
    public void setState(UplinkState state);

    /**
     * See {@link IPseudoInventory#tryConsumeItems(List, int)}
     */
    public Pair<UplinkStatus, List<BigItemStack>> tryConsumeItems(List<BigItemStack> requestedItems, boolean simulate, boolean fuzzy);

    /**
     * See {@link IPseudoInventory#givePlayerItems(ItemStack...)}
     */
    public UplinkStatus tryGivePlayerItems(List<BigItemStack> items);

    /**
     * See {@link IPseudoInventory#givePlayerFluids(FluidStack...)}
     */
    public UplinkStatus tryGivePlayerFluids(List<BigFluidStack> fluids);

    /**
     * Submits a new plan to the ME hatch.
     *
     * @param details Some extra details for the plan
     * @param autocraft When true, the plan will be automatically crafted
     */
    public void submitPlan(EntityPlayer submitter, String details, List<BigItemStack> requiredItems, boolean autocraft);

    /**
     * Clears any manual plans
     */
    public void clearManualPlans(EntityPlayer player);

    /**
     * Clears and auto plans and cancels their jobs
     */
    public void cancelAutoPlans(EntityPlayer player);

    /**
     * A weak-valued map containing all active uplinks
     */
    public static final Map<Long, IUplinkMulti> UPLINKS = new MapMaker().weakValues()
        .makeMap();

    public static IUplinkMulti getUplink(long address) {
        return UPLINKS.get(address);
    }
}
