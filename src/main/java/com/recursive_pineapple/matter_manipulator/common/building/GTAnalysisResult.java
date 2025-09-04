package com.recursive_pineapple.matter_manipulator.common.building;

import static com.recursive_pineapple.matter_manipulator.common.utils.MMUtils.nullIfUnknown;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import gregtech.api.covers.CoverRegistry;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.VoidingMode;
import gregtech.api.interfaces.IConfigurationCircuitSupport;
import gregtech.api.interfaces.IDataCopyable;
import gregtech.api.interfaces.IMEConnectable;
import gregtech.api.interfaces.metatileentity.IConnectable;
import gregtech.api.interfaces.metatileentity.IFluidLockable;
import gregtech.api.interfaces.metatileentity.IItemLockable;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEBasicMachine;
import gregtech.api.metatileentity.implementations.MTEFluidPipe;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.metatileentity.implementations.MTEHatchOutput;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.common.covers.Cover;
import gregtech.common.tileentities.machines.multi.MTEIntegratedOreFactory;

import appeng.helpers.ICustomNameObject;

import com.google.gson.JsonElement;
import com.gtnewhorizon.structurelib.alignment.IAlignment;
import com.gtnewhorizon.structurelib.alignment.IAlignmentProvider;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.alignment.enumerable.Flip;
import com.recursive_pineapple.matter_manipulator.MMMod;
import com.recursive_pineapple.matter_manipulator.common.building.BlockAnalyzer.IBlockApplyContext;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.Transform;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;

import gtnhlanth.common.beamline.MTEBeamlinePipe;
import lombok.SneakyThrows;
import tectech.thing.metaTileEntity.hatch.MTEHatchDynamoTunnel;
import tectech.thing.metaTileEntity.hatch.MTEHatchEnergyTunnel;
import tectech.thing.metaTileEntity.multi.base.TTMultiblockBase;
import tectech.thing.metaTileEntity.pipe.MTEPipeData;
import tectech.thing.metaTileEntity.pipe.MTEPipeLaser;

public class GTAnalysisResult implements ITileAnalysisIntegration {

    public byte mConnections = 0;
    public byte mGTColour = -1;
    public ForgeDirection mGTFront = null, mGTMainFacing = null;
    public short mGTFlags = 0;
    public ExtendedFacing mGTFacing = null;
    public CoverData[] mCovers = null;
    public byte mStrongRedstone = 0;
    public String mGTCustomName = null;
    public byte mGTGhostCircuit = 0;
    public PortableItemStack mGTItemLock = null;
    public String mGTFluidLock = null;
    public int mGTMode = 0;
    public JsonElement mGTData = null;
    public double[] mTTParams = null;
    public int mAmperes = 0;
    public byte mFluidPipeRestriction = 0;

    private static int counter = 0;
    private static final short GT_MACHINE_ENABLED = (short) (0b1 << counter++);
    private static final short GT_BASIC_IO_PUSH_ITEMS = (short) (0b1 << counter++);
    private static final short GT_BASIC_IO_PUSH_FLUIDS = (short) (0b1 << counter++);
    private static final short GT_BASIC_IO_DISABLE_FILTER = (short) (0b1 << counter++);
    private static final short GT_BASIC_IO_DISABLE_MULTISTACK = (short) (0b1 << counter++);
    private static final short GT_BASIC_IO_INPUT_FROM_OUTPUT_SIDE = (short) (0b1 << counter++);
    private static final short GT_INPUT_BUS_NO_SORTING = (short) (0b1 << counter++);
    private static final short GT_INPUT_BUS_NO_LIMITING = (short) (0b1 << counter++);
    private static final short GT_INPUT_BUS_NO_FILTERING = (short) (0b1 << counter++);
    private static final short GT_MULTI_PROTECT_ITEMS = (short) (0b1 << counter++);
    private static final short GT_MULTI_PROTECT_FLUIDS = (short) (0b1 << counter++);
    private static final short GT_MULTI_BATCH_MODE = (short) (0b1 << counter++);
    private static final short GT_MULTI_INPUT_SEPARATION = (short) (0b1 << counter++);
    private static final short GT_MULTI_RECIPE_LOCK = (short) (0b1 << counter++);
    private static final short GT_ME_CONNECT_ALL_SIDES = (short) (0b1 << counter++);

    private static final GTAnalysisResult NO_OP = new GTAnalysisResult();

    public static GTAnalysisResult analyze(TileEntity te) {
        if (te instanceof IGregTechTileEntity igte) {
            GTAnalysisResult result = new GTAnalysisResult(igte);

            return result.equals(NO_OP) ? null : result;
        } else {
            return null;
        }
    }

    public GTAnalysisResult() {}

    public GTAnalysisResult(IGregTechTileEntity igte) {
        IMetaTileEntity mte = igte.getMetaTileEntity();

        // save the colour
        if (igte.getColorization() != -1) mGTColour = igte.getColorization();

        if (igte.isAllowedToWork()) mGTFlags |= GT_MACHINE_ENABLED;

        // if the machine is a singleblock, store its data
        if (mte instanceof MTEBasicMachine basicMachine) {
            mGTMainFacing = basicMachine.mMainFacing;

            if (basicMachine.mItemTransfer) mGTFlags |= GT_BASIC_IO_PUSH_ITEMS;
            if (basicMachine.mFluidTransfer) mGTFlags |= GT_BASIC_IO_PUSH_FLUIDS;
            if (basicMachine.mDisableFilter) mGTFlags |= GT_BASIC_IO_DISABLE_FILTER;
            if (basicMachine.mDisableMultiStack) mGTFlags |= GT_BASIC_IO_DISABLE_MULTISTACK;
            if (basicMachine.mAllowInputFromOutputSide) mGTFlags |= GT_BASIC_IO_INPUT_FROM_OUTPUT_SIDE;
        }

        // if the machine is a pipe/cable/etc, store its connections
        if (mte instanceof IConnectable connectable && shouldMutateConnections(connectable)) {
            byte con = 0;

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                if (connectable.isConnectedAtSide(dir)) {
                    con |= dir.flag;
                }
            }

            mConnections = con;
        }

        if (mte instanceof MTEFluidPipe fluidPipe) {
            mFluidPipeRestriction = fluidPipe.mDisableInput;
        }

        // if the machine is alignable (basically everything) store its facing directly or extended alignment
        if (mte instanceof IAlignmentProvider provider) {
            IAlignment alignment = provider.getAlignment();

            mGTFacing = alignment != null ? alignment.getExtendedFacing() : null;
        } else {
            mGTFront = nullIfUnknown(igte.getFrontFacing());
        }

        CoverData[] covers = new CoverData[6];
        boolean hasCover = false;
        byte strongRedstone = 0;

        // check each side for covers
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (igte.hasCoverAtSide(dir)) {
                covers[dir.ordinal()] = CoverData.fromMachine(igte, dir);
                hasCover = true;

                if (igte.getRedstoneOutputStrength(dir)) {
                    strongRedstone |= dir.flag;
                }
            }
        }

        if (hasCover) mCovers = covers;
        mStrongRedstone = strongRedstone;

        // check if the machine has a custom name
        if (mte instanceof ICustomNameObject customName && customName.hasCustomName()) {
            mGTCustomName = customName.getCustomName();
        }

        // check if the machine has a ghost circuit slot
        if (mte instanceof IConfigurationCircuitSupport ghostCircuit && ghostCircuit.allowSelectCircuit()) {
            ItemStack circuit = mte.getStackInSlot(ghostCircuit.getCircuitSlot());

            if (circuit == null || circuit.getItem() == null) {
                mGTGhostCircuit = 0;
            } else if (circuit.getItem() == ItemList.Circuit_Integrated.getItem()) {
                mGTGhostCircuit = (byte) Items.feather.getDamage(circuit);
            }
        }

        // check if the machine is an input bus
        if (mte instanceof MTEHatchInputBus inputBus) {
            if (inputBus.disableSort) mGTFlags |= GT_INPUT_BUS_NO_SORTING;
            if (inputBus.disableLimited) mGTFlags |= GT_INPUT_BUS_NO_LIMITING;
            if (inputBus.disableFilter) mGTFlags |= GT_INPUT_BUS_NO_FILTERING;
        }

        // check if the machine has a locked item
        if (mte instanceof IItemLockable lockable && lockable.acceptsItemLock() && lockable.getLockedItem() != null) {
            mGTItemLock = new PortableItemStack(lockable.getLockedItem());
        }

        // check if the machine is an output hatch
        if (mte instanceof MTEHatchOutput outputHatch) {
            mGTMode = outputHatch.getMode();
        }

        // check if the machine has a locked fluid
        if (mte instanceof IFluidLockable lockable && lockable.isFluidLocked()) {
            mGTFluidLock = lockable.getLockedFluidName();
        }

        // check if the machine is a multi and store its settings
        if (mte instanceof MTEMultiBlockBase multi) {
            if (multi instanceof MTEIntegratedOreFactory iof) {
                mGTMode = getIOFMode(iof);
            } else {
                mGTMode = multi.machineMode;
            }

            if (multi.getVoidingMode().protectFluid) mGTFlags |= GT_MULTI_PROTECT_FLUIDS;
            if (multi.getVoidingMode().protectItem) mGTFlags |= GT_MULTI_PROTECT_ITEMS;

            if (multi.isBatchModeEnabled()) mGTFlags |= GT_MULTI_BATCH_MODE;
            if (multi.isInputSeparationEnabled()) mGTFlags |= GT_MULTI_INPUT_SEPARATION;
            if (multi.isRecipeLockingEnabled()) mGTFlags |= GT_MULTI_RECIPE_LOCK;
        }

        // check if the machine can be copied with a data stick
        if (mte instanceof IDataCopyable copyable) {
            try {
                // There's no reason for this EntityPlayer parameter besides sending chat messages, so we just fail
                // if it actually needs the player.
                NBTTagCompound data = copyable.getCopiedData(null);

                if (data != null && !data.hasNoTags()) {
                    mGTData = MMUtils.toJsonObject(data);
                }
            } catch (Throwable t) {
                // Probably an NPE, but we're catching Throwable just to be safe
                MMMod.LOG.error("Could not copy IDataCopyable's data", t);
            }
        }

        if (mte instanceof TTMultiblockBase tt && tt.parametrization.hasInputs()) {
            mTTParams = tt.parametrization.getInputs();
        }

        if (mte instanceof MTEHatchEnergyTunnel hatch) {
            mAmperes = hatch.Amperes;
        }

        if (mte instanceof MTEHatchDynamoTunnel dynamo) {
            mAmperes = dynamo.Amperes;
        }

        if (mte instanceof IMEConnectable me && me.connectsToAllSides()) {
            mGTFlags |= GT_ME_CONNECT_ALL_SIDES;
        }
    }

    private static final MethodHandle GET_IOF_MODE = MMUtils
        .exposeFieldGetter(MTEIntegratedOreFactory.class, "sMode");

    @SneakyThrows
    private static int getIOFMode(MTEIntegratedOreFactory cal) {
        return (int) GET_IOF_MODE.invokeExact(cal);
    }

    private static final MethodHandle SET_IOF_MODE = MMUtils
        .exposeFieldSetter(MTEIntegratedOreFactory.class, "sMode");

    @SneakyThrows
    private static void setIOFMode(MTEIntegratedOreFactory cal, int mode) {
        SET_IOF_MODE.invokeExact(cal, mode);
    }

    @Override
    public boolean apply(IBlockApplyContext ctx) {
        TileEntity te = ctx.getTileEntity();

        if (te instanceof IGregTechTileEntity gte) {
            IMetaTileEntity mte = gte.getMetaTileEntity();

            gte.setColorization(mGTColour);

            if ((mGTFlags & GT_MACHINE_ENABLED) != 0) {
                gte.enableWorking();
            } else {
                gte.disableWorking();
            }

            if (mte instanceof MTEBasicMachine basicMachine) {
                if (mGTMainFacing != null) {
                    basicMachine.setMainFacing(mGTMainFacing);
                    // Stop MTEBasicMachine.doDisplayThings from overwriting the setFrontFacing call when the block is
                    // newly placed
                    basicMachine.mHasBeenUpdated = true;
                }

                basicMachine.mItemTransfer = (mGTFlags & GT_BASIC_IO_PUSH_ITEMS) != 0;
                basicMachine.mFluidTransfer = (mGTFlags & GT_BASIC_IO_PUSH_FLUIDS) != 0;
                basicMachine.mDisableFilter = (mGTFlags & GT_BASIC_IO_DISABLE_FILTER) != 0;
                basicMachine.mDisableMultiStack = (mGTFlags & GT_BASIC_IO_DISABLE_MULTISTACK) != 0;
                basicMachine.mAllowInputFromOutputSide = (mGTFlags & GT_BASIC_IO_INPUT_FROM_OUTPUT_SIDE) != 0;
            }

            // only (dis)connect sides that need to be updated
            if (mte instanceof IConnectable connectable && shouldMutateConnections(connectable)) {
                for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                    boolean shouldBeConnected = (mConnections & dir.flag) != 0;
                    if (connectable.isConnectedAtSide(dir) != shouldBeConnected) {
                        if (shouldBeConnected) {
                            connectable.connect(dir);
                        } else {
                            connectable.disconnect(dir);
                        }
                    }
                }
            }

            if (mte instanceof MTEFluidPipe fluidPipe) {
                fluidPipe.mDisableInput = mFluidPipeRestriction;
            }

            // set the machine's facing and alignment
            if (mte instanceof IAlignmentProvider provider) {
                IAlignment alignment = provider.getAlignment();

                if (mGTFacing != null && alignment != null) {

                    ExtendedFacing facing = mGTFacing;

                    // maintenance hatches can be rotated but not flipped
                    if (!alignment.isNewExtendedFacingValid(facing)) {
                        facing = ExtendedFacing.of(mGTFacing.getDirection(), mGTFacing.getRotation(), Flip.NONE);
                    }

                    if (alignment.isNewExtendedFacingValid(facing)) {
                        gte.setFrontFacing(facing.getDirection());
                        alignment.toolSetExtendedFacing(facing);
                    } else {
                        ctx.error("Could not set direction to '" + facing.getLocalizedName() + "'");
                    }
                }
            } else {
                if (mGTFront != null) {
                    gte.setFrontFacing(mGTFront);
                }
            }

            // install/remove/update the covers
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                CoverData expected = mCovers == null ? null : mCovers[dir.ordinal()];
                Cover actual = gte.getCoverAtSide(dir);

                if (actual == CoverRegistry.NO_COVER && expected != null) {
                    installCover(ctx, gte, dir, expected);
                } else if (actual != CoverRegistry.NO_COVER && expected == null) {
                    removeCover(ctx, gte, dir);
                } else if (actual != CoverRegistry.NO_COVER) {
                    if (!ItemStack.areItemStacksEqual(expected.getCoverStack(), gte.getCoverItemAtSide(dir))) {
                        removeCover(ctx, gte, dir);
                        installCover(ctx, gte, dir, expected);
                    } else if (!Objects.equals(actual.writeToNBT(new NBTTagCompound()), expected.coverData)) {
                        updateCover(ctx, gte, dir, expected);
                    }
                }

                // set the redstone strength
                gte.setRedstoneOutputStrength(dir, (mStrongRedstone & dir.flag) != 0);
            }

            // set the custom name
            if (mte instanceof ICustomNameObject customName && mGTCustomName != null) {
                customName.setCustomName(mGTCustomName);
            }

            // set the ghost circuit
            if (mte instanceof IConfigurationCircuitSupport ghostCircuit && ghostCircuit.allowSelectCircuit()) {
                ItemStack circuit = null;

                if (mGTGhostCircuit > 0) {
                    circuit = ItemList.Circuit_Integrated.getWithDamage(0, mGTGhostCircuit);
                }

                mte.setInventorySlotContents(ghostCircuit.getCircuitSlot(), circuit);
                mte.markDirty();
            }

            // set the various input bus options
            if (mte instanceof MTEHatchInputBus inputBus) {
                inputBus.disableSort = (mGTFlags & GT_INPUT_BUS_NO_SORTING) != 0;
                inputBus.disableLimited = (mGTFlags & GT_INPUT_BUS_NO_LIMITING) != 0;
                inputBus.disableFilter = (mGTFlags & GT_INPUT_BUS_NO_FILTERING) != 0;
            }

            // set the locked item
            if (mte instanceof IItemLockable lockable && lockable.acceptsItemLock()) {
                ItemStack lockedItem = mGTItemLock == null ? null : mGTItemLock.toStack();

                lockable.setLockedItem(lockedItem);
            }

            // set the output hatch mode
            if (mte instanceof MTEHatchOutput outputHatch) {
                outputHatch.mMode = (byte) mGTMode;
            }

            // set the locked fluid
            if (mte instanceof IFluidLockable lockable && lockable.isFluidLocked()) {
                lockable.setLockedFluidName(mGTFluidLock);
            }

            // set the various multi options
            if (mte instanceof MTEMultiBlockBase multi) {
                if (mte instanceof MTEIntegratedOreFactory iof) {
                    setIOFMode(iof, mGTMode);
                } else {
                    multi.machineMode = mGTMode;
                }

                if (multi.supportsVoidProtection()) {
                    boolean protectFluids = (mGTFlags & GT_MULTI_PROTECT_FLUIDS) != 0;
                    boolean protectItems = (mGTFlags & GT_MULTI_PROTECT_ITEMS) != 0;

                    VoidingMode voidingMode = null;

                    for (VoidingMode mode : VoidingMode.values()) {
                        if (mode.protectFluid == protectFluids && mode.protectItem == protectItems) {
                            voidingMode = mode;
                            break;
                        }
                    }

                    if (voidingMode != null) {
                        multi.setVoidingMode(voidingMode);
                    } else {
                        throw new RuntimeException(
                            "This should never happen. protectFluids=" + protectFluids
                                + ", protectItems="
                                + protectItems
                        );
                    }
                }

                if (multi.supportsBatchMode()) multi.setBatchMode((mGTFlags & GT_MULTI_BATCH_MODE) != 0);
                if (multi.supportsInputSeparation()) multi.setInputSeparation((mGTFlags & GT_MULTI_INPUT_SEPARATION) != 0);
                if (multi.supportsSingleRecipeLocking()) multi.setRecipeLocking((mGTFlags & GT_MULTI_RECIPE_LOCK) != 0);
            }

            // paste the data
            if (mte instanceof IDataCopyable copyable) {
                NBTTagCompound data = mGTData == null ? new NBTTagCompound() : (NBTTagCompound) MMUtils.toNbt(mGTData);

                try {
                    // There's no reason for this EntityPlayer parameter besides sending chat messages, so we just fail
                    // if it actually needs the player.
                    if (!copyable.pasteCopiedData(null, data)) { return false; }
                } catch (Throwable t) {
                    // Probably an NPE, but we're catching Throwable just to be safe
                    MMMod.LOG.error("Could not paste IDataCopyable's data", t);
                }
            }

            if (mTTParams != null && mTTParams.length == 20 && mte instanceof TTMultiblockBase tt) {
                tt.parametrization.setInputs(mTTParams);
            }

            if (mAmperes > 0 && mte instanceof MTEHatchEnergyTunnel hatch) {
                hatch.Amperes = MMUtils.clamp(mAmperes, 0, hatch.maxAmperes);
            }

            if (mAmperes > 0 && mte instanceof MTEHatchDynamoTunnel dynamo) {
                dynamo.Amperes = MMUtils.clamp(mAmperes, 0, dynamo.maxAmperes);
            }

            if (mte instanceof IMEConnectable me) {
                me.setConnectsToAllSides((mGTFlags & GT_ME_CONNECT_ALL_SIDES) != 0);
            }

            if (mte instanceof MTEPipeLaser laserPipe) {
                laserPipe.updateNeighboringNetworks();
            }

            if (mte instanceof MTEPipeData dataPipe) {
                dataPipe.updateNeighboringNetworks();
            }
        }

        return true;
    }

    private void removeCover(IBlockApplyContext context, IGregTechTileEntity gte, ForgeDirection side) {
        if (gte.hasCoverAtSide(side)) {
            context.givePlayerItems(gte.detachCover(side));
        }
    }

    private void installCover(IBlockApplyContext context, IGregTechTileEntity gte, ForgeDirection side, CoverData cover) {
        ItemStack stack = cover.getCoverStack();

        if (!canPlace(gte, side, cover)) {
            context.error("Was not allowed to put cover on " + side.name().toLowerCase() + "side: " + stack.getDisplayName());
            return;
        }

        if (!context.tryConsumeItems(stack)) {
            context.error("Could not find cover: " + stack.getDisplayName());
            return;
        }

        CoverRegistry.getCoverPlacer(stack).placeCover(context.getRealPlayer(), stack, gte, side);

        if (gte.getCoverAtSide(side).allowsCopyPasteTool()) {
            gte.updateAttachedCover(
                cover.coverID,
                side,
                cover.coverData
            );
        }
    }

    private void updateCover(
        IBlockApplyContext context,
        IGregTechTileEntity gte,
        ForgeDirection side,
        CoverData target
    ) {
        if (gte.hasCoverAtSide(side) && ItemStack.areItemStacksEqual(gte.getCoverItemAtSide(side), target.getCoverStack())) {
            Cover cover = gte.getCoverAtSide(side);
            if (cover.allowsCopyPasteTool()) {
                gte.updateAttachedCover(
                    target.coverID,
                    cover.getSide(),
                    target.coverData
                );
            }
        }
    }

    private boolean shouldMutateConnections(IConnectable conn) {
        if (conn instanceof MTEPipeLaser) return false;
        if (conn instanceof MTEPipeData) return false;
        if (conn instanceof MTEBeamlinePipe) return false;

        return true;
    }

    @Override
    public boolean getRequiredItemsForExistingBlock(IBlockApplyContext context) {
        TileEntity te = context.getTileEntity();

        if (te instanceof IGregTechTileEntity gte) {
            for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                CoverData target = mCovers == null ? null : mCovers[side.ordinal()];
                Cover actual = gte.getCoverAtSide(side);
                ItemStack actualItem = gte.getCoverItemAtSide(side);

                if (actual != CoverRegistry.NO_COVER && (target == null || !ItemStack.areItemStacksEqual(actualItem, target.getCoverStack()))) {
                    context.givePlayerItems(actualItem);
                    actual = null;
                }

                if (actual == null && target != null) {
                    if (canPlace(gte, side, target)) {
                        context.tryConsumeItems(target.getCoverStack());
                    }
                }
            }
        }

        return true;
    }

    @Override
    public boolean getRequiredItemsForNewBlock(IBlockApplyContext context) {
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            CoverData target = mCovers == null ? null : mCovers[side.ordinal()];

            if (target != null) {
                context.tryConsumeItems(target.getCoverStack());
            }
        }

        return true;
    }

    private boolean canPlace(IGregTechTileEntity gte, ForgeDirection side, CoverData cover) {
        ItemStack stack = cover.getCoverStack();

        if (!gte.getMetaTileEntity().allowCoverOnSide(side, stack)) return false;

        return true;
    }

    @Override
    public void getItemTag(NBTTagCompound tag) {

    }

    @Override
    public void getItemDetails(List<String> details) {

    }

    @Override
    public void transform(Transform transform) {
        mGTFront = transform.apply(mGTFront);
        mGTMainFacing = transform.apply(mGTMainFacing);
        mGTFacing = transform.apply(mGTFacing);

        if (mCovers != null) {
            CoverData[] coversOut = new CoverData[mCovers.length];

            for (int i = 0; i < coversOut.length; i++) {
                coversOut[transform.apply(ForgeDirection.VALID_DIRECTIONS[i])
                    .ordinal()] = mCovers[i];
            }

            mCovers = coversOut;
        }

        mConnections = transform.applyBits(mConnections);
        mStrongRedstone = transform.applyBits(mStrongRedstone);
        mFluidPipeRestriction = transform.applyBits(mFluidPipeRestriction);
    }

    @Override
    public void migrate() {
        mGTMode = 0;
        mTTParams = null;
        mAmperes = 0;
    }

    @Override
    public GTAnalysisResult clone() {
        GTAnalysisResult dup = new GTAnalysisResult();

        dup.mConnections = mConnections;
        dup.mGTColour = mGTColour;
        dup.mGTFront = mGTFront;
        dup.mGTMainFacing = mGTMainFacing;
        dup.mGTFlags = mGTFlags;
        dup.mGTFacing = mGTFacing;
        dup.mCovers = mCovers == null ? null : MMUtils.mapToArray(mCovers, CoverData[]::new, x -> x == null ? null : x.clone());
        dup.mStrongRedstone = mStrongRedstone;
        dup.mGTCustomName = mGTCustomName;
        dup.mGTGhostCircuit = mGTGhostCircuit;
        dup.mGTItemLock = mGTItemLock == null ? null : mGTItemLock.clone();
        dup.mGTFluidLock = mGTFluidLock;
        dup.mGTMode = mGTMode;
        dup.mGTData = mGTData == null ? null : MMUtils.toJsonObject(MMUtils.toNbt(mGTData));
        dup.mTTParams = mTTParams == null ? null : mTTParams.clone();
        dup.mAmperes = mAmperes;
        dup.mFluidPipeRestriction = mFluidPipeRestriction;

        return dup;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mConnections;
        result = prime * result + mGTColour;
        result = prime * result + ((mGTFront == null) ? 0 : mGTFront.hashCode());
        result = prime * result + ((mGTMainFacing == null) ? 0 : mGTMainFacing.hashCode());
        result = prime * result + mGTFlags;
        result = prime * result + ((mGTFacing == null) ? 0 : mGTFacing.hashCode());
        result = prime * result + Arrays.hashCode(mCovers);
        result = prime * result + mStrongRedstone;
        result = prime * result + ((mGTCustomName == null) ? 0 : mGTCustomName.hashCode());
        result = prime * result + mGTGhostCircuit;
        result = prime * result + ((mGTItemLock == null) ? 0 : mGTItemLock.hashCode());
        result = prime * result + ((mGTFluidLock == null) ? 0 : mGTFluidLock.hashCode());
        result = prime * result + mGTMode;
        result = prime * result + ((mGTData == null) ? 0 : mGTData.hashCode());
        result = prime * result + Arrays.hashCode(mTTParams);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        GTAnalysisResult other = (GTAnalysisResult) obj;
        if (mConnections != other.mConnections) return false;
        if (mGTColour != other.mGTColour) return false;
        if (mGTFront != other.mGTFront) return false;
        if (mGTMainFacing != other.mGTMainFacing) return false;
        if (mGTFlags != other.mGTFlags) return false;
        if (mGTFacing != other.mGTFacing) return false;
        if (!Arrays.equals(mCovers, other.mCovers)) return false;
        if (mStrongRedstone != other.mStrongRedstone) return false;
        if (mGTCustomName == null) {
            if (other.mGTCustomName != null) return false;
        } else if (!mGTCustomName.equals(other.mGTCustomName)) return false;
        if (mGTGhostCircuit != other.mGTGhostCircuit) return false;
        if (mGTItemLock == null) {
            if (other.mGTItemLock != null) return false;
        } else if (!mGTItemLock.equals(other.mGTItemLock)) return false;
        if (mGTFluidLock == null) {
            if (other.mGTFluidLock != null) return false;
        } else if (!mGTFluidLock.equals(other.mGTFluidLock)) return false;
        if (mGTMode != other.mGTMode) return false;
        if (mGTData == null) {
            if (other.mGTData != null) return false;
        } else if (!mGTData.equals(other.mGTData)) return false;
        if (!Arrays.equals(mTTParams, other.mTTParams)) return false;
        return true;
    }

}
