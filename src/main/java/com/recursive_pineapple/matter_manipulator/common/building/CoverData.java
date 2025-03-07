package com.recursive_pineapple.matter_manipulator.common.building;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;

import net.minecraftforge.common.util.ForgeDirection;

import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.util.ISerializableObject;
import gregtech.common.covers.Cover;
import gregtech.common.covers.CoverBehaviorBase;

import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;

import lombok.SneakyThrows;

/**
 * Contains all GT cover analysis data.
 */
public class CoverData {

    public PortableItemStack cover;
    public NBTBase coverData;
    public Integer tickRateAddition;

    public CoverData() {}

    public CoverData(PortableItemStack cover, NBTBase coverData, int tickRateAddition) {
        this.cover = cover;
        this.coverData = coverData;
        this.tickRateAddition = tickRateAddition == 0 ? null : tickRateAddition;
    }

    public ItemStack getCoverStack() {
        return cover.toStack();
    }

    private static final MethodHandle COVER_LOAD_FROM_NBT = MMUtils.exposeMethod(
        CoverBehaviorBase.class,
        MethodType.methodType(ISerializableObject.class, NBTBase.class),
        "loadFromNbt"
    );

    @SneakyThrows
    public ISerializableObject getCoverData(Cover cover) {
        return (ISerializableObject) COVER_LOAD_FROM_NBT.invokeExact((CoverBehaviorBase<?>) cover, coverData);
    }

    @Override
    public CoverData clone() {
        CoverData dup = new CoverData();

        dup.cover = cover.clone();
        dup.coverData = coverData.copy();
        dup.tickRateAddition = tickRateAddition;

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
            cover.getCoverData().saveDataToNBT(),
            cover.getTickRateAddition()
        );
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cover == null) ? 0 : cover.hashCode());
        result = prime * result + ((coverData == null) ? 0 : coverData.hashCode());
        result = prime * result + ((tickRateAddition == null) ? 0 : tickRateAddition.hashCode());
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
        if (coverData == null) {
            if (other.coverData != null) return false;
        } else if (!coverData.equals(other.coverData)) return false;
        if (tickRateAddition == null) {
            if (other.tickRateAddition != null) return false;
        } else if (!tickRateAddition.equals(other.tickRateAddition)) return false;
        return true;
    }

}
