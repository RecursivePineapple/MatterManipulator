package matter_manipulator.common.uplink;

import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.networking.storage.IStorageGrid;

import com.google.common.collect.MapMaker;
import matter_manipulator.asm.Optional;
import matter_manipulator.common.building.IPseudoInventory;
import matter_manipulator.common.items.manipulator.Location;
import matter_manipulator.common.utils.BigFluidStack;
import matter_manipulator.common.utils.BigItemStack;
import matter_manipulator.common.utils.Mods.Names;

import it.unimi.dsi.fastutil.Pair;

public interface IUplinkMulti {

    boolean isActive();

    Location getLocation();

    UplinkState getState();

    @SideOnly(Side.CLIENT)
    void setState(UplinkState state);

    /**
     * See {@link IPseudoInventory#tryConsumeItems(List, int)}
     */
    Pair<UplinkStatus, List<BigItemStack>> tryConsumeItems(List<BigItemStack> requestedItems, boolean simulate, boolean fuzzy);

    /**
     * See {@link IPseudoInventory#givePlayerItems(ItemStack...)}
     */
    UplinkStatus tryGivePlayerItems(List<BigItemStack> items);

    /**
     * See {@link IPseudoInventory#givePlayerFluids(FluidStack...)}
     */
    UplinkStatus tryGivePlayerFluids(List<BigFluidStack> fluids);

    @Optional(Names.APPLIED_ENERGISTICS2)
    IStorageGrid getStorageGrid();

    /**
     * Submits a new plan to the ME hatch.
     *
     * @param details Some extra details for the plan
     * @param autocraft When true, the plan will be automatically crafted
     */
    void submitPlan(EntityPlayer submitter, String details, List<BigItemStack> requiredItems, boolean autocraft);

    /**
     * Clears any manual plans
     */
    void clearManualPlans(EntityPlayer player);

    /**
     * Clears and auto plans and cancels their jobs
     */
    void cancelAutoPlans(EntityPlayer player);

    /**
     * Drains power from the uplink
     *
     * @param requested The wanted amount of power
     * @return The amount of power actually drained
     */
    double drainPower(double requested);

    /**
     * A weak-valued map containing all active uplinks
     */
    Map<Long, IUplinkMulti> UPLINKS = new MapMaker().weakValues()
        .makeMap();

    static IUplinkMulti getUplink(long address) {
        return UPLINKS.get(address);
    }
}
