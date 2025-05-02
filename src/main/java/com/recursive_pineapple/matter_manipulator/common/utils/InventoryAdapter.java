package com.recursive_pineapple.matter_manipulator.common.utils;

import static com.recursive_pineapple.matter_manipulator.common.utils.Mods.GregTech;

import javax.annotation.Nullable;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.util.ForgeDirection;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.IHasInventory;
import gregtech.api.metatileentity.implementations.MTEBasicBatteryBuffer;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.metatileentity.implementations.MTEHatchOutputBus;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.common.tileentities.machines.MTEHatchOutputBusME;
import gregtech.common.tileentities.machines.MTEHatchOutputME;

import com.google.common.collect.ImmutableList;
import com.recursive_pineapple.matter_manipulator.MMMod;
import com.recursive_pineapple.matter_manipulator.asm.Optional;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods.Names;

import tectech.thing.metaTileEntity.hatch.MTEHatchRack;

public enum InventoryAdapter {

    GTUnrestricted {

        @Override
        public boolean canHandle(IInventory inv) {
            return GregTech.isModLoaded() && canHandleImpl(inv);
        }

        @Optional(Names.GREG_TECH)
        private boolean canHandleImpl(IInventory inv) {
            if (inv instanceof IGregTechTileEntity igte) {
                IMetaTileEntity imte = igte.getMetaTileEntity();

                if (imte instanceof MTEHatchOutputBusME) return true;
                if (imte instanceof MTEHatchOutputME) return true;
                if (imte instanceof MTEHatchRack) return true;
                if (imte instanceof MTEMultiBlockBase) return true;
                if (imte instanceof MTEBasicBatteryBuffer) return true;
            }

            return false;
        }
    },

    GTNoop {

        @Override
        public boolean canHandle(IInventory inv) {
            return GregTech.isModLoaded() && canHandleImpl(inv);
        }

        @Optional(Names.GREG_TECH)
        private boolean canHandleImpl(IInventory inv) {
            if (inv instanceof IGregTechTileEntity igte) {
                if (igte.isDead()) return false;

                IMetaTileEntity imte = igte.getMetaTileEntity();

                if (imte == null) return false;

                if (imte.getClass() == MTEHatchOutputBus.class) return true;
                if (imte.getClass() == MTEHatchInputBus.class) return true;
            }

            return false;
        }

        @Override
        @Optional(Names.GREG_TECH)
        public boolean isValidSlot(IInventory inv, int slot) {
            return false;
        }

        @Override
        @Optional(Names.GREG_TECH)
        public boolean canExtract(IInventory inv, int slot) {
            return false;
        }

        @Override
        @Optional(Names.GREG_TECH)
        public boolean canInsert(IInventory inv, int slot, ItemStack stack) {
            return false;
        }

        @Override
        @Optional(Names.GREG_TECH)
        public boolean insert(IInventory inv, int slot, ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack extract(IInventory inv, int slot) {
            return null;
        }
    },

    GT {

        @Override
        public boolean canHandle(IInventory inv) {
            return GregTech.isModLoaded() && canHandleImpl(inv);
        }

        @Optional(Names.GREG_TECH)
        private boolean canHandleImpl(IInventory inv) {
            return inv instanceof IHasInventory;
        }

        @Override
        @Optional(Names.GREG_TECH)
        public boolean isValidSlot(IInventory inv, int slot) {
            return ((IHasInventory) inv).isValidSlot(slot);
        }

        @Override
        @Optional(Names.GREG_TECH)
        public boolean canExtract(IInventory inv, int slot) {
            return ((IHasInventory) inv).canExtractItem(slot, inv.getStackInSlot(slot), ForgeDirection.UNKNOWN.ordinal());
        }

        @Override
        @Optional(Names.GREG_TECH)
        public boolean canInsert(IInventory inv, int slot, ItemStack stack) {
            return ((IHasInventory) inv).canInsertItem(slot, stack, ForgeDirection.UNKNOWN.ordinal());
        }

        @Override
        @Optional(Names.GREG_TECH)
        public boolean insert(IInventory inv, int slot, ItemStack stack) {
            return ((IHasInventory) inv).addStackToSlot(slot, stack);
        }
    },
    SIDED {

        @Override
        public boolean canHandle(IInventory inv) {
            return inv instanceof ISidedInventory;
        }

        @Override
        public boolean canExtract(IInventory inv, int slot) {
            return ((ISidedInventory) inv).canExtractItem(slot, inv.getStackInSlot(slot), ForgeDirection.UNKNOWN.ordinal());
        }

        @Override
        public boolean canInsert(IInventory inv, int slot, ItemStack stack) {
            return ((ISidedInventory) inv).canInsertItem(slot, stack, ForgeDirection.UNKNOWN.ordinal());
        }
    },
    SIMPLE,
    ;

    public static final ImmutableList<InventoryAdapter> ADAPTERS = ImmutableList.copyOf(values());

    public boolean canHandle(IInventory inv) {
        return inv != null;
    }

    public boolean isValidSlot(IInventory inv, int slot) {
        return slot >= 0 && slot < inv.getSizeInventory();
    }

    public boolean canExtract(IInventory inv, int slot) {
        return inv.isItemValidForSlot(slot, inv.getStackInSlot(slot));
    }

    public boolean canInsert(IInventory inv, int slot, ItemStack stack) {
        return inv.isItemValidForSlot(slot, stack);
    }

    public ItemStack extract(IInventory inv, int slot) {
        ItemStack stack = inv.getStackInSlot(slot);

        inv.setInventorySlotContents(slot, null);

        return stack;
    }

    public boolean insert(IInventory inv, int slot, ItemStack stack) {
        if (!inv.isItemValidForSlot(slot, stack)) return false;
        if (inv.getStackInSlot(slot) != null) return false;

        inv.setInventorySlotContents(slot, stack);

        return true;
    }

    public static @Nullable InventoryAdapter findAdapter(IInventory inv) {
        for (InventoryAdapter adapter : ADAPTERS) {
            if (adapter.canHandle(inv)) return adapter;
        }

        MMMod.LOG.error("could not apply inventory: no adapters support " + inv);

        return null;
    }
}
