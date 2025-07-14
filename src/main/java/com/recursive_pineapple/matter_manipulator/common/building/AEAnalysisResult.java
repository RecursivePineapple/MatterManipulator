package com.recursive_pineapple.matter_manipulator.common.building;

import static com.recursive_pineapple.matter_manipulator.common.utils.MMUtils.nullIfUnknown;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.networking.IGridHost;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AEColor;
import appeng.helpers.ICustomNameObject;
import appeng.parts.automation.UpgradeInventory;
import appeng.parts.p2p.PartP2PTunnelNormal;
import appeng.tile.AEBaseTile;
import appeng.tile.networking.TileCableBus;
import appeng.util.Platform;
import appeng.util.SettingsFrom;

import com.google.gson.JsonElement;
import com.recursive_pineapple.matter_manipulator.common.building.BlockAnalyzer.IBlockApplyContext;
import com.recursive_pineapple.matter_manipulator.common.building.providers.IItemProvider;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.Transform;
import com.recursive_pineapple.matter_manipulator.common.utils.BigItemStack;
import com.recursive_pineapple.matter_manipulator.common.utils.ItemId;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;

public class AEAnalysisResult implements ITileAnalysisIntegration {

    public AEColor mAEColour = null;
    public ForgeDirection mAEUp = null, mAEForward = null;
    public JsonElement mAEConfig = null;
    public PortableItemStack[] mAEUpgrades = null;
    public String mAECustomName = null;
    public AEPartData[] mAEParts = null;
    public InventoryAnalysis mAECells = null;
    public InventoryAnalysis mAEPatterns = null;

    public static final ForgeDirection[] ALL_DIRECTIONS = ForgeDirection.values();

    private static final AEAnalysisResult NO_OP = new AEAnalysisResult();

    public static AEAnalysisResult analyze(TileEntity te) {
        if (te instanceof IGridHost) {
            AEAnalysisResult result = new AEAnalysisResult(te);

            return result.equals(NO_OP) ? null : result;
        } else {
            return null;
        }
    }

    public AEAnalysisResult() {}

    public AEAnalysisResult(TileEntity te) {

        // check if the tile is an ae tile and store its facing info + config
        if (te instanceof AEBaseTile ae) {
            mAEUp = nullIfUnknown(ae.getUp());
            mAEForward = nullIfUnknown(ae.getForward());
            mAEConfig = MMUtils.toJsonObject(ae.downloadSettings(SettingsFrom.MEMORY_CARD));
        }

        if (te instanceof IColorableTile colorable) {
            mAEColour = colorable.getColor();
        }

        // check if the tile has a custom name
        if (te instanceof ICustomNameObject customName && !(te instanceof TileCableBus)) {
            mAECustomName = customName.hasCustomName() ? customName.getCustomName() : null;
        }

        // check if the tile has AE inventories
        if (te instanceof ISegmentedInventory segmentedInventory) {
            if (segmentedInventory.getInventoryByName("upgrades") instanceof UpgradeInventory upgrades) {
                mAEUpgrades = MMUtils.fromInventory(upgrades);
            }

            IInventory cells = segmentedInventory.getInventoryByName("cells");
            if (cells != null) {
                mAECells = InventoryAnalysis.fromInventory(cells, false);
            }

            IInventory patterns = segmentedInventory.getInventoryByName("patterns");
            if (patterns != null) {
                mAEPatterns = InventoryAnalysis.fromInventory(patterns, false);
            }
        }

        // check all sides for parts (+UNKNOWN for cables)
        if (te instanceof IPartHost partHost) {
            mAEParts = new AEPartData[AEAnalysisResult.ALL_DIRECTIONS.length];

            for (ForgeDirection dir : AEAnalysisResult.ALL_DIRECTIONS) {
                IPart part = partHost.getPart(dir);

                if (part != null) mAEParts[dir.ordinal()] = new AEPartData(part);
            }
        }
    }

    @Override
    public boolean apply(IBlockApplyContext ctx) {
        TileEntity te = ctx.getTileEntity();

        if (te instanceof IColorableTile colorable && mAEColour != null) {
            colorable.recolourBlock(ForgeDirection.NORTH, mAEColour, ctx.getRealPlayer());
        }

        // apply upgrades, cells, and patterns
        if (te instanceof ISegmentedInventory segmentedInventory) {
            if (segmentedInventory.getInventoryByName("upgrades") instanceof UpgradeInventory upgrades) {
                MMUtils.installUpgrades(ctx, upgrades, mAEUpgrades, true, false);
            }

            IInventory cells = segmentedInventory.getInventoryByName("cells");
            if (mAECells != null && cells != null) {
                mAECells.apply(ctx, cells, true, false);
            }

            IInventory patterns = segmentedInventory.getInventoryByName("patterns");
            if (mAEPatterns != null && patterns != null) {
                mAEPatterns.apply(ctx, patterns, true, false);
            }
        }

        // set ae tile orientation and config
        if (te instanceof AEBaseTile ae) {
            if (mAEUp != null && mAEForward != null) {
                ae.setOrientation(mAEForward, mAEUp);
            }

            if (mAEConfig != null) {
                ae.uploadSettings(SettingsFrom.MEMORY_CARD, (NBTTagCompound) MMUtils.toNbt(mAEConfig));
            }
        }

        // set ae tile custom name
        if (mAECustomName != null && te instanceof ICustomNameObject customName && !(te instanceof TileCableBus)) {
            customName.setCustomName(mAECustomName);
        }

        boolean success = true;

        // add/remove/update ae parts and cables
        if (te instanceof IPartHost partHost && mAEParts != null) {
            for (ForgeDirection dir : AEAnalysisResult.ALL_DIRECTIONS) {
                IPart part = partHost.getPart(dir);
                AEPartData expected = mAEParts[dir.ordinal()];

                ItemId actualItem = part == null ? null : ItemId.createWithoutNBT(part.getItemStack(PartItemStack.Break));

                ItemStack expectedStack = expected == null ? null : expected.getEffectivePartStack();
                ItemId expectedItem = expectedStack == null ? null : ItemId.createWithoutNBT(expectedStack);

                boolean isAttunable = part instanceof PartP2PTunnelNormal && expected != null && expected.isAttunable();

                // if the p2p is attunable (non-interface) then we don't need to remove it
                if (!isAttunable) {
                    // change the part into the proper version
                    if (actualItem != null && (expectedItem == null || !Objects.equals(actualItem, expectedItem))) {
                        if (expectedStack != null) {
                            var result = ctx.tryConsumeItems(Arrays.asList(BigItemStack.create(expectedStack)), IPseudoInventory.CONSUME_SIMULATED);

                            if (!result.leftBoolean()) {
                                ctx.warn("Could not extract item: " + expectedStack.getDisplayName());
                                continue;
                            }
                        }

                        removePart(ctx, partHost, dir, false);
                        actualItem = null;
                    }

                    if (actualItem == null && expectedItem != null) {
                        if (expectedStack != null && !partHost.canAddPart(expectedStack, dir)) {
                            ctx.error(
                                "Invalid location (" + MMUtils.getDirectionDisplayName(dir, true) + ") for part (" + expectedStack.getDisplayName() + ")"
                            );
                            continue;
                        }

                        if (!installPart(ctx, partHost, dir, expected, false)) {
                            success = false;
                            continue;
                        }
                    }
                }

                if (expected != null) {
                    if (!expected.updatePart(ctx, partHost, dir)) {
                        success = false;
                        continue;
                    }
                }

                Platform.notifyBlocksOfNeighbors(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord);
            }
        }

        return success;
    }

    private void removePart(IBlockApplyContext context, IPartHost partHost, ForgeDirection side, boolean simulate) {
        IPart part = partHost.getPart(side);

        if (part == null) return;

        List<ItemStack> drops = new ArrayList<>();

        part.getDrops(drops, false);

        context.givePlayerItems(
            drops.stream()
                .map(ItemStack::copy)
                .toArray(ItemStack[]::new)
        );

        ItemStack partStack = part.getItemStack(PartItemStack.Break)
            .copy();

        NBTTagCompound tag = partStack.getTagCompound();

        // manually clear the name
        if (tag != null) {
            tag.removeTag("display");

            if (tag.hasNoTags()) {
                partStack.setTagCompound(null);
            }
        }

        context.givePlayerItems(partStack);

        if (!simulate) partHost.removePart(side, false);
    }

    private boolean installPart(
        IBlockApplyContext context,
        IPartHost partHost,
        ForgeDirection side,
        AEPartData partData,
        boolean simulate
    ) {
        ItemStack partStack = partData.getEffectivePartStack();

        if (!partHost.canAddPart(partStack, side)) { return false; }

        if (!context.tryConsumeItems(partStack)) {
            context.warn("Could not find " + partStack.getDisplayName());
            return false;
        }

        if (!simulate) {
            if (partHost.addPart(partStack, side, context.getRealPlayer()) == null) {
                context.givePlayerItems(partStack);
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean getRequiredItemsForExistingBlock(IBlockApplyContext context) {
        TileEntity te = context.getTileEntity();

        if (te instanceof ISegmentedInventory segmentedInventory) {
            if (mAEUpgrades != null && segmentedInventory.getInventoryByName("upgrades") instanceof UpgradeInventory upgrades) {
                MMUtils.installUpgrades(context, upgrades, mAEUpgrades, true, true);
            }

            IInventory cells = segmentedInventory.getInventoryByName("cells");
            if (mAECells != null && cells != null) {
                mAECells.apply(context, cells, true, true);
            }
        }

        if (mAEParts != null && te instanceof IPartHost partHost) {
            for (ForgeDirection dir : AEAnalysisResult.ALL_DIRECTIONS) {
                IPart part = partHost.getPart(dir);
                AEPartData expected = mAEParts[dir.ordinal()];

                ItemId actualItem = part == null ? null : ItemId.createWithoutNBT(part.getItemStack(PartItemStack.Break));
                ItemId expectedItem = expected == null ? null : ItemId.createWithoutNBT(expected.getEffectivePartStack());

                boolean isAttunable = part instanceof PartP2PTunnelNormal && expected != null && expected.isAttunable();

                if (!isAttunable) {
                    if ((expectedItem == null || !Objects.equals(actualItem, expectedItem)) && actualItem != null) {
                        removePart(context, partHost, dir, true);
                        actualItem = null;
                    }

                    if (actualItem == null && expectedItem != null) {
                        if (!installPart(context, partHost, dir, expected, true)) { return false; }
                    }
                }

                if (expected != null) {
                    if (!expected.getRequiredItemsForExistingPart(context, partHost, dir)) { return false; }
                }

                Platform.notifyBlocksOfNeighbors(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord);
            }
        }

        return true;
    }

    @Override
    public boolean getRequiredItemsForNewBlock(IBlockApplyContext context) {
        if (mAEUpgrades != null) {
            for (PortableItemStack upgrade : mAEUpgrades) {
                context.tryConsumeItems(upgrade.toStack());
            }
        }

        if (mAECells != null) {
            for (IItemProvider cell : mAECells.mItems) {
                if (cell != null) {
                    cell.getStack(context, true);
                }
            }
        }

        if (mAEParts != null) {
            for (ForgeDirection dir : AEAnalysisResult.ALL_DIRECTIONS) {
                AEPartData expected = mAEParts[dir.ordinal()];

                if (expected == null) continue;

                context.tryConsumeItems(expected.getEffectivePartStack());

                if (!expected.getRequiredItemsForNewPart(context)) { return false; }
            }
        }

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
        mAEUp = transform.apply(mAEUp);
        mAEForward = transform.apply(mAEForward);

        if (mAEParts != null) {
            AEPartData[] partsOut = new AEPartData[AEAnalysisResult.ALL_DIRECTIONS.length];

            int unknown = ForgeDirection.UNKNOWN.ordinal();

            for (int i = 0; i < partsOut.length; i++) {
                if (i == unknown) {
                    partsOut[unknown] = mAEParts[unknown];
                } else {
                    partsOut[transform.apply(AEAnalysisResult.ALL_DIRECTIONS[i])
                        .ordinal()] = mAEParts[i];
                }
            }

            mAEParts = partsOut;
        }
    }

    @Override
    public void migrate() {

    }

    @Override
    public AEAnalysisResult clone() {
        AEAnalysisResult dup = new AEAnalysisResult();

        dup.mAEColour = mAEColour;
        dup.mAEUp = mAEUp;
        dup.mAEForward = mAEForward;
        dup.mAEConfig = mAEConfig == null ? null : MMUtils.toJsonObject(MMUtils.toNbt(mAEConfig));
        dup.mAEUpgrades = mAEUpgrades == null ? null : MMUtils.mapToArray(mAEUpgrades, PortableItemStack[]::new, x -> x == null ? null : x.clone());
        dup.mAECustomName = mAECustomName;
        dup.mAEParts = mAEParts == null ? null : MMUtils.mapToArray(mAEParts, AEPartData[]::new, x -> x == null ? null : x.clone());
        dup.mAECells = mAECells == null ? null : mAECells.clone();
        dup.mAEPatterns = mAEPatterns == null ? null : mAEPatterns.clone();

        return dup;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mAEColour == null) ? 0 : mAEColour.hashCode());
        result = prime * result + ((mAEUp == null) ? 0 : mAEUp.hashCode());
        result = prime * result + ((mAEForward == null) ? 0 : mAEForward.hashCode());
        result = prime * result + ((mAEConfig == null) ? 0 : mAEConfig.hashCode());
        result = prime * result + Arrays.hashCode(mAEUpgrades);
        result = prime * result + ((mAECustomName == null) ? 0 : mAECustomName.hashCode());
        result = prime * result + Arrays.hashCode(mAEParts);
        result = prime * result + ((mAECells == null) ? 0 : mAECells.hashCode());
        result = prime * result + ((mAEPatterns == null) ? 0 : mAEPatterns.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AEAnalysisResult other = (AEAnalysisResult) obj;
        if (mAEColour != other.mAEColour) return false;
        if (mAEUp != other.mAEUp) return false;
        if (mAEForward != other.mAEForward) return false;
        if (mAEConfig == null) {
            if (other.mAEConfig != null) return false;
        } else if (!mAEConfig.equals(other.mAEConfig)) return false;
        if (!Arrays.equals(mAEUpgrades, other.mAEUpgrades)) return false;
        if (mAECustomName == null) {
            if (other.mAECustomName != null) return false;
        } else if (!mAECustomName.equals(other.mAECustomName)) return false;
        if (!Arrays.equals(mAEParts, other.mAEParts)) return false;
        if (mAECells == null) {
            if (other.mAECells != null) return false;
        } else if (!mAECells.equals(other.mAECells)) return false;
        if (mAEPatterns == null) {
            if (other.mAEPatterns != null) return false;
        } else if (!mAEPatterns.equals(other.mAEPatterns)) return false;
        return true;
    }
}
