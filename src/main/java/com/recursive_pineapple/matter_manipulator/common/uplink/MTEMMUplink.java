package com.recursive_pineapple.matter_manipulator.common.uplink;

import static com.recursive_pineapple.matter_manipulator.common.structure.MMCasings.AdvancedIridiumPlatedMachineCasing;
import static com.recursive_pineapple.matter_manipulator.common.structure.MMCasings.MatterGenerationCoil;
import static com.recursive_pineapple.matter_manipulator.common.structure.MMCasings.RadiantNaquadahAlloyCasing;
import static gregtech.api.enums.GTValues.AuthorPineapple;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.ExoticEnergy;
import static gregtech.api.enums.HatchElement.InputHatch;
import static gregtech.api.enums.HatchElement.Maintenance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import gregtech.api.enums.Materials;
import gregtech.api.enums.StructureError;
import gregtech.api.enums.Textures.BlockIcons.CustomIcon;
import gregtech.api.enums.TierEU;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEExtendedPowerMultiBlockBase;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.recipe.maps.FuelBackend;
import gregtech.api.render.TextureFactory;
import gregtech.api.structure.IStructureInstance;
import gregtech.api.structure.IStructureProvider;
import gregtech.api.structure.StructureWrapper;
import gregtech.api.structure.StructureWrapperInstanceInfo;
import gregtech.api.structure.StructureWrapperTooltipBuilder;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import gregtech.api.util.IGTHatchAdder;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.shutdown.ShutDownReason;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;

import com.google.common.collect.ImmutableList;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.recursive_pineapple.matter_manipulator.common.building.IPseudoInventory;
import com.recursive_pineapple.matter_manipulator.common.items.MMItemList;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.Location;
import com.recursive_pineapple.matter_manipulator.common.networking.Messages;
import com.recursive_pineapple.matter_manipulator.common.structure.CasingGTFrames;
import com.recursive_pineapple.matter_manipulator.common.utils.BigFluidStack;
import com.recursive_pineapple.matter_manipulator.common.utils.BigItemStack;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;

import it.unimi.dsi.fastutil.Pair;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import tectech.thing.metaTileEntity.hatch.MTEHatchEnergyMulti;

public class MTEMMUplink extends MTEExtendedPowerMultiBlockBase<MTEMMUplink> implements ISurvivalConstructable, IUplinkMulti, IStructureProvider<MTEMMUplink> {

    private static final long BASE_PLASMA_EU_COST = 131_072;

    private long pendingPlasmaEU = 0;
    private long address = 0;

    private final ArrayList<MTEMMUplinkMEHatch> uplinkHatches = new ArrayList<>();

    protected final StructureWrapper<MTEMMUplink> structure;
    protected final StructureWrapperInstanceInfo<MTEMMUplink> structureInstanceInfo;

    private static CasingGTFrames TRINIUM_FRAMES, NAQ_ALLOY_FRAMES;

    public MTEMMUplink(final int aID, final String aName, final String aNameRegional) {
        super(aID, aName, aNameRegional);

        TRINIUM_FRAMES = CasingGTFrames.forMaterial(Materials.Trinium);
        NAQ_ALLOY_FRAMES = CasingGTFrames.forMaterial(Materials.NaquadahAlloy);

        structure = new StructureWrapper<>(this);
        structureInstanceInfo = null;

        structure.loadStructure();
    }

    protected MTEMMUplink(MTEMMUplink prototype) {
        super(prototype.mName);

        structure = prototype.structure;
        structureInstanceInfo = new StructureWrapperInstanceInfo<>(structure);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEMMUplink(this);
    }

    // #region Structure

    @Override
    public String[][] getDefinition() {
        return Structures.UPLINK;
    }

    @Override
    public IStructureInstance<MTEMMUplink> getStructureInstance() {
        return structureInstanceInfo;
    }

    @Override
    public IStructureDefinition<MTEMMUplink> compile(String[][] definition) {
        structure.addCasing('A', AdvancedIridiumPlatedMachineCasing)
            .withHatches(1, 16, Arrays.asList(InputHatch, Energy, ExoticEnergy, Maintenance, UplinkHatchAdder.INSTANCE));
        structure.addCasing('B', NAQ_ALLOY_FRAMES);
        structure.addCasing('C', TRINIUM_FRAMES);
        structure.addCasing('D', MatterGenerationCoil);
        structure.addCasing('E', RadiantNaquadahAlloyCasing);

        return structure.buildStructure(definition);
    }

    @Override
    public IStructureDefinition<MTEMMUplink> getStructureDefinition() {
        return structure.structureDefinition;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        structure.construct(this, stackSize, hintsOnly);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        return structure.survivalConstruct(this, stackSize, elementBudget, env);
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        return structure.checkStructure(this);
    }

    @Override
    protected void validateStructure(Collection<StructureError> errors, NBTTagCompound context) {
        super.validateStructure(errors, context);

        structureInstanceInfo.validate(errors, context);
    }

    @Override
    protected void localizeStructureErrors(
        Collection<StructureError> errors,
        NBTTagCompound context,
        List<String> lines
    ) {
        super.localizeStructureErrors(errors, context, lines);

        structureInstanceInfo.localizeStructureErrors(errors, context, lines);
    }

    private enum UplinkHatchAdder implements IHatchElement<MTEMMUplink> {

        INSTANCE;

        @Override
        public List<? extends Class<? extends IMetaTileEntity>> mteClasses() {
            return Arrays.asList(MTEMMUplinkMEHatch.class);
        }

        @Override
        public IGTHatchAdder<? super MTEMMUplink> adder() {
            return (uplink, hatchTE, aBaseCasingIndex) -> {
                if (hatchTE == null || hatchTE.isDead()) return false;

                IMetaTileEntity aMetaTileEntity = hatchTE.getMetaTileEntity();

                if (aMetaTileEntity == null) return false;

                if (!(aMetaTileEntity instanceof MTEMMUplinkMEHatch uplinkHatch)) return false;

                uplink.uplinkHatches.add(uplinkHatch);
                uplinkHatch.updateTexture(aBaseCasingIndex);
                uplinkHatch.updateCraftingIcon(uplink.getMachineCraftingIcon());

                return true;
            };
        }

        @Override
        public long count(MTEMMUplink t) {
            return t.uplinkHatches.size();
        }
    }

    // #endregion

    // #region Misc boilerplate

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        StructureWrapperTooltipBuilder<MTEMMUplink> tt = new StructureWrapperTooltipBuilder<>(structure);

        String hatch = MMItemList.UplinkHatch.get(1).getDisplayName();

        tt.addMachineType("Matter Manipulator Quantum Uplink")
            .addInfo("Interdimensional and infinite range uplink for matter manipulators.")
            .addInfo("Allows manipulators to convert plans into AE patterns.")
            .addInfo("Connects directly to an ME system via a " + EnumChatFormatting.GOLD + hatch + EnumChatFormatting.GRAY + ".")
            .addSeparator()
            .addInfo("Consumes 1A ZPM while active.")
            .addInfo("Must be fed with plasma via an input hatch.")
            .addInfo("Transfers to/from the manipulator cost " + String.format("%,d", BASE_PLASMA_EU_COST) + " EU in plasma per item or per bucket.")
            .addInfo("Insert a compatible manipulator in the controller slot while the machine is running to bind it to the uplink.");

        tt.beginStructureBlock();
        tt.addController("Front Center");
        tt.addHatchNameOverride(UplinkHatchAdder.INSTANCE, hatch);
        tt.addAllCasingInfo(
            Arrays.asList(
                AdvancedIridiumPlatedMachineCasing,
                MatterGenerationCoil,
                TRINIUM_FRAMES,
                NAQ_ALLOY_FRAMES,
                RadiantNaquadahAlloyCasing
            ),
            null
        );

        tt.toolTipFinisher(AuthorPineapple);

        return tt;
    }

    private static final CustomIcon ACTIVE_GLOW = new CustomIcon(
        Mods.MatterManipulator.getResourcePath("machines", "uplink", "OVERLAY_FRONT_ACTIVE_GLOW")
    );
    private static final CustomIcon IDLE_GLOW = new CustomIcon(
        Mods.MatterManipulator.getResourcePath("machines", "uplink", "OVERLAY_FRONT_IDLE_GLOW")
    );
    private static final CustomIcon OFF = new CustomIcon(
        Mods.MatterManipulator.getResourcePath("machines", "uplink", "OVERLAY_FRONT_OFF")
    );

    @Override
    public ITexture[] getTexture(
        IGregTechTileEntity baseMetaTileEntity,
        ForgeDirection side,
        ForgeDirection facing,
        int colorIndex,
        boolean active,
        boolean redstoneLevel
    ) {
        List<ITexture> textures = new ArrayList<>(3);

        textures.add(AdvancedIridiumPlatedMachineCasing.getCasingTexture());

        if (side == facing) {
            textures.add(
                TextureFactory.builder()
                    .addIcon(OFF)
                    .extFacing()
                    .build()
            );

            switch (getState()) {
                case OFF: {
                    break;
                }
                case IDLE: {
                    textures.add(
                        TextureFactory.builder()
                            .addIcon(IDLE_GLOW)
                            .extFacing()
                            .glow()
                            .build()
                    );
                    break;
                }
                case ACTIVE: {
                    textures.add(
                        TextureFactory.builder()
                            .addIcon(ACTIVE_GLOW)
                            .extFacing()
                            .glow()
                            .build()
                    );
                    break;
                }
            }
        }

        return textures.toArray(new ITexture[0]);
    }

    @SideOnly(Side.CLIENT)
    private UplinkState state;

    public UplinkState getState() {
        if (getBaseMetaTileEntity().isServerSide()) {
            if (getBaseMetaTileEntity().isActive()) {
                if (
                    uplinkHatches.stream()
                        .anyMatch(hatch -> hatch.hasAnyRequests())
                ) {
                    return UplinkState.ACTIVE;
                } else {
                    return UplinkState.IDLE;
                }
            } else {
                return UplinkState.OFF;
            }
        } else {
            if (state == null) state = UplinkState.OFF;

            return state;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setState(UplinkState state) {
        this.state = state;
        getBaseMetaTileEntity().issueTextureUpdate();
    }

    @Override
    public Location getLocation() {
        IGregTechTileEntity igte = getBaseMetaTileEntity();

        if (igte != null) {
            return new Location(
                igte.getWorld(),
                igte.getXCoord(),
                igte.getYCoord(),
                igte.getZCoord()
            );
        } else {
            return null;
        }
    }

    @Override
    public boolean isActive() {
        return getBaseMetaTileEntity() != null && getBaseMetaTileEntity().isActive();
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return true;
    }

    @Override
    public int getMaxEfficiency(ItemStack aStack) {
        return 10_000;
    }

    @Override
    public int getDamageToComponent(ItemStack aStack) {
        return 0;
    }

    @Override
    public boolean explodesOnComponentBreak(ItemStack aStack) {
        return false;
    }

    // #endregion

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);

        address = aNBT.getLong("address");
        pendingPlasmaEU = aNBT.getLong("plasmaEU");

        if (address == 0) address = newAddress();
    }

    @Override
    public void getWailaBody(
        ItemStack itemStack,
        List<String> currentTip,
        IWailaDataAccessor accessor,
        IWailaConfigHandler config
    ) {
        super.getWailaBody(itemStack, currentTip, accessor, config);
        currentTip.add(
            String.format(
                "Address: %x",
                accessor.getNBTData()
                    .getLong("address")
            )
        );
    }

    @Override
    public void getWailaNBTData(
        EntityPlayerMP player,
        TileEntity tile,
        NBTTagCompound tag,
        World world,
        int x,
        int y,
        int z
    ) {
        super.getWailaNBTData(player, tile, tag, world, x, y, z);
        tag.setLong("address", address);
    }

    @Override
    public void addAdditionalTooltipInformation(ItemStack stack, List<String> tooltip) {
        super.addAdditionalTooltipInformation(stack, tooltip);

        if (stack.getTagCompound() != null) {
            tooltip.add(
                String.format(
                    "Address: %x",
                    stack.getTagCompound()
                        .getLong("address")
                )
            );
        }
    }

    @Override
    public String[] getInfoData() {
        List<String> info = new ArrayList<>(Arrays.asList(super.getInfoData()));

        info.add(
            String.format(
                "Stored Plasma: %s%,d%s EU",
                EnumChatFormatting.YELLOW,
                pendingPlasmaEU,
                EnumChatFormatting.WHITE
            )
        );

        return info.toArray(new String[0]);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);

        aNBT.setLong("address", address);
        aNBT.setLong("plasmaEU", pendingPlasmaEU);
    }

    @Override
    public void setItemNBT(NBTTagCompound aNBT) {
        aNBT.setLong("address", address);
    }

    @Override
    public void initDefaultModes(NBTTagCompound aNBT) {
        address = aNBT == null ? newAddress() : aNBT.getLong("address");
    }

    private static long newAddress() {
        return (long) (Long.MAX_VALUE * Math.random());
    }

    private MTEMMUplinkMEHatch getMEHatch() {
        for (MTEMMUplinkMEHatch hatch : uplinkHatches) {
            if (hatch != null && hatch.isActive() && hatch.isPowered()) { return hatch; }
        }

        return null;
    }

    /**
     * See {@link IPseudoInventory#tryConsumeItems(List, int)}
     */
    @Override
    public Pair<UplinkStatus, List<BigItemStack>> tryConsumeItems(
        List<BigItemStack> requestedItems,
        boolean simulate,
        boolean fuzzy
    ) {
        MTEMMUplinkMEHatch hatch = getMEHatch();

        if (hatch == null) return Pair.of(UplinkStatus.NO_HATCH, null);

        IStorageGrid storage = hatch.getStorageGrid();

        if (storage == null) return Pair.of(UplinkStatus.AE_OFFLINE, null);

        IMEMonitor<IAEItemStack> itemInventory = storage.getItemInventory();

        if (itemInventory == null) return Pair.of(UplinkStatus.AE_OFFLINE, null);

        List<BigItemStack> out = new ArrayList<>();

        for (BigItemStack req : requestedItems) {
            if (req.getStackSize() == 0) {
                continue;
            }

            // spotless:off
            List<IAEItemStack> matches = fuzzy ?
                ImmutableList.copyOf(itemInventory.getStorageList().findFuzzy(req.getAEItemStack(), FuzzyMode.IGNORE_ALL)) :
                Arrays.asList(itemInventory.getStorageList().findPrecise(req.getAEItemStack()));
            // spotless:on

            for (IAEItemStack match : matches) {
                if (req.getStackSize() == 0) break;
                if (match == null) continue;
                if (match.getStackSize() == 0) continue;

                match = match.copy()
                    .setStackSize(req.getStackSize());

                if (!simulate) {
                    if (!consumePlasmaEU(req.getStackSize() * BASE_PLASMA_EU_COST)) { return Pair.of(UplinkStatus.NO_PLASMA, null); }
                }

                IAEItemStack result = itemInventory.extractItems(
                    match,
                    simulate ? Actionable.SIMULATE : Actionable.MODULATE,
                    hatch.getRequestSource()
                );

                if (result != null) {
                    out.add(BigItemStack.create(result));
                    req.setStackSize(req.getStackSize() - result.getStackSize());
                }
            }
        }

        return Pair.of(UplinkStatus.OK, out);
    }

    /**
     * See {@link IPseudoInventory#givePlayerItems(ItemStack...)}
     */
    @Override
    public UplinkStatus tryGivePlayerItems(List<BigItemStack> items) {
        MTEMMUplinkMEHatch hatch = getMEHatch();

        if (hatch == null) return UplinkStatus.NO_HATCH;

        IStorageGrid storage = hatch.getStorageGrid();

        if (storage == null) return UplinkStatus.AE_OFFLINE;

        IMEMonitor<IAEItemStack> itemInventory = storage.getItemInventory();

        if (itemInventory == null) return UplinkStatus.AE_OFFLINE;

        for (BigItemStack item : items) {
            if (item == null) continue;

            if (!consumePlasmaEU(item.getStackSize() * BASE_PLASMA_EU_COST)) { return UplinkStatus.NO_PLASMA; }

            IAEItemStack result = itemInventory.injectItems(item.getAEItemStack(), Actionable.MODULATE, hatch.getRequestSource());

            item.setStackSize(result == null ? 0 : result.getStackSize());
        }

        return UplinkStatus.OK;
    }

    /**
     * See {@link IPseudoInventory#givePlayerFluids(FluidStack...)}
     */
    @Override
    public UplinkStatus tryGivePlayerFluids(List<BigFluidStack> fluids) {
        MTEMMUplinkMEHatch hatch = getMEHatch();

        if (hatch == null) return UplinkStatus.NO_HATCH;

        IStorageGrid storage = hatch.getStorageGrid();

        if (storage == null) return UplinkStatus.AE_OFFLINE;

        IMEMonitor<IAEFluidStack> fluidInventory = storage.getFluidInventory();

        if (fluidInventory == null) return UplinkStatus.AE_OFFLINE;

        for (BigFluidStack fluid : fluids) {
            if (fluid == null) continue;

            if (!consumePlasmaEU(MMUtils.ceilDiv(fluid.getStackSize(), 1000) * BASE_PLASMA_EU_COST)) { return UplinkStatus.NO_PLASMA; }

            IAEFluidStack result = fluidInventory.injectItems(fluid.getAEFluidStack(), Actionable.MODULATE, hatch.getRequestSource());

            fluid.setStackSize(result == null ? 0 : result.getStackSize());
        }

        return UplinkStatus.OK;
    }

    public IStorageGrid getStorageGrid() {
        MTEMMUplinkMEHatch hatch = getMEHatch();

        if (hatch == null) return null;

        return hatch.getStorageGrid();
    }

    /**
     * Tries to consume plasma EU.
     * Converts plasma to EU as needed.
     */
    private boolean consumePlasmaEU(long euToConsume) {
        if (pendingPlasmaEU < euToConsume) {
            generatePlasmaEU(euToConsume - pendingPlasmaEU);
        }

        if (pendingPlasmaEU >= euToConsume) {
            pendingPlasmaEU -= euToConsume;

            return true;
        } else {
            return false;
        }
    }

    /**
     * Converts plasma in hatches to EU.
     */
    private void generatePlasmaEU(long euToGenerate) {
        FuelBackend fuels = RecipeMaps.plasmaFuels.getBackend();

        for (MTEHatchInput input : mInputHatches) {
            for (FluidTankInfo tank : input.getTankInfo(ForgeDirection.UNKNOWN)) {
                if (tank.fluid == null) continue;

                GTRecipe fuel = fuels.findFuel(tank.fluid);

                if (fuel != null) {
                    long euPerLitre = fuel.mSpecialValue;

                    int litresToConsume = (int) Math.min(Integer.MAX_VALUE, MMUtils.ceilDiv(euToGenerate, euPerLitre));

                    FluidStack toConsume = tank.fluid.copy();
                    toConsume.amount = litresToConsume;

                    FluidStack drained = input.drain(ForgeDirection.UNKNOWN, toConsume, true);

                    long generated = drained.amount * euPerLitre;
                    euToGenerate -= generated;
                    pendingPlasmaEU += generated;
                }

                if (euToGenerate <= 0) { return; }
            }
        }
    }

    /**
     * Submits a new plan to the ME hatch.
     *
     * @param details Some extra details for the plan
     * @param autocraft When true, the plan will be automatically crafted
     */
    @Override
    public void submitPlan(
        EntityPlayer submitter,
        String details,
        List<BigItemStack> requiredItems,
        boolean autocraft
    ) {
        MTEMMUplinkMEHatch hatch = getMEHatch();

        if (hatch != null) {
            String patternName = String.format(
                "%s's Manipulator Plan",
                submitter.getGameProfile()
                    .getName()
            );

            if (details != null && !details.isEmpty()) {
                patternName += " (" + details + ")";
            }

            hatch.addRequest(submitter, patternName, MMUtils.mapToList(requiredItems, BigItemStack::getAEItemStack), autocraft);

            MMUtils.sendInfoToPlayer(
                submitter,
                "Pushed a new virtual ME pattern to the uplink called '" + patternName + "'."
            );
        }
    }

    /**
     * Clears any manual plans
     */
    @Override
    public void clearManualPlans(EntityPlayer player) {
        MTEMMUplinkMEHatch hatch = getMEHatch();

        if (hatch != null) {
            hatch.clearManualPlans(player);
        }
    }

    /**
     * Clears and auto plans and cancels their jobs
     */
    @Override
    public void cancelAutoPlans(EntityPlayer player) {
        MTEMMUplinkMEHatch hatch = getMEHatch();

        if (hatch != null) {
            hatch.cancelAutoPlans(player);
        }
    }

    @Override
    public double drainPower(double requested) {
        double drained = 0;

        for (MTEHatch hatch : GTUtility.filterValidMTEs(mExoticEnergyHatches)) {
            if (!(hatch instanceof MTEHatchEnergyMulti exotic)) continue;

            // always keep 5 seconds worth of eu in the hatch to keep the uplink from powerfailing
            long toKeep = TierEU.RECIPE_ZPM * 20 * 5;

            long extractable = exotic.getEUVar() - toKeep;

            if (extractable <= 0) continue;

            long remaining = MMUtils.ceilLong(requested - drained);
            if (remaining <= 0) break;

            extractable = Math.min(extractable, remaining);

            exotic.setEUVar(exotic.getEUVar() - extractable);
            drained += extractable;
        }

        return drained;
    }

    @Override
    public boolean onRunningTick(ItemStack aStack) {
        if (aStack != null && aStack.getItem() instanceof ItemMatterManipulator manipulator) {
            manipulator.setUplinkAddress(aStack, address);
        }

        return super.onRunningTick(aStack);
    }

    private UplinkState lastState;

    private int stateCounter = 0;

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);

        if (aBaseMetaTileEntity.isServerSide() && aTick % 5 == 0) {
            UplinkState state = getState();

            stateCounter++;

            // if the state has changed or 10 seconds have passed, send an update to all nearby clients
            if (state != lastState || stateCounter > 10) {
                lastState = state;
                stateCounter = 0;
                sendUplinkStateUpdate();
            }
        }
    }

    @Override
    @Nonnull
    public CheckRecipeResult checkProcessing() {
        mMaxProgresstime = 20;
        mEUt = (int) -TierEU.RECIPE_ZPM;
        mEfficiency = 10_000;

        UPLINKS.put(address, this);

        return CheckRecipeResultRegistry.SUCCESSFUL;
    }

    @Override
    public void stopMachine(@Nonnull ShutDownReason reason) {
        super.stopMachine(reason);

        UPLINKS.remove(address);
        sendUplinkStateUpdate();
    }

    @Override
    public void onBlockDestroyed() {
        super.onBlockDestroyed();

        UPLINKS.remove(address);
    }

    private void sendUplinkStateUpdate() {
        IGregTechTileEntity igte = getBaseMetaTileEntity();

        if (igte != null) {
            Location l = new Location(
                igte.getWorld(),
                igte.getXCoord(),
                igte.getYCoord(),
                igte.getZCoord()
            );

            Messages.UpdateUplinkState.sendToPlayersAround(l, this);
        }
    }
}
