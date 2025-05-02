package com.recursive_pineapple.matter_manipulator.common.building;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;

import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.common.covers.Cover;

/**
 * Contains all GT cover analysis data.
 */
public class CoverData {

    public PortableItemStack cover;
    public int coverID;
    public NBTTagCompound coverData;

    public CoverData() {}

    public CoverData(PortableItemStack cover, int coverID, NBTTagCompound coverData) {
        this.cover = cover;
        this.coverID = coverID;
        this.coverData = coverData;
    }

    public ItemStack getCoverStack() {
        return cover.toStack();
    }

    @Override
    public CoverData clone() {
        CoverData dup = new CoverData();

        dup.cover = cover.clone();
        dup.coverID = coverID;
        dup.coverData = (NBTTagCompound) coverData.copy();

        return dup;
    }

    /**
     * Converts a CoverInfo into a CoverData.
     *
     * @return The CoverData, or null if there's no cover.
     */
    public static CoverData fromMachine(ICoverable coverable, ForgeDirection dir) {
        if (!coverable.hasCoverAtSide(dir)) return null;

        Cover cover = coverable.getCoverAtSide(dir);

        return new CoverData(
            PortableItemStack.withNBT(coverable.getCoverItemAtSide(dir)),
            cover.getCoverID(),
            cover.writeToNBT(new NBTTagCompound())
        );
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cover == null) ? 0 : cover.hashCode());
        result = prime * result + coverID;
        result = prime * result + ((coverData == null) ? 0 : coverData.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        CoverData other = (CoverData) obj;
        if (cover == null) {
            if (other.cover != null) return false;
        } else if (!cover.equals(other.cover)) return false;
        if (other.coverID != coverID) return false;
        if (coverData == null) {
            if (other.coverData != null) return false;
        } else if (!coverData.equals(other.coverData)) return false;
        return true;
    }

}
