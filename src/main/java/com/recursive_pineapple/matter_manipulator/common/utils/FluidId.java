package com.recursive_pineapple.matter_manipulator.common.utils;

import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;
import static net.minecraftforge.common.util.Constants.NBT.TAG_INT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class FluidId {

    public static FluidId create(NBTTagCompound tag) {
        return new AutoValue_FluidId(
            FluidRegistry.getFluid(tag.getString("FluidName")),
            tag.hasKey("Tag", TAG_COMPOUND) ? tag.getCompoundTag("Tag") : null,
            tag.hasKey("Amount", TAG_INT) ? tag.getInteger("Amount") : null);
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("FluidName", fluid().getName());
        if (nbt() != null) tag.setTag("Tag", nbt());
        Integer amount = amount();
        if (amount != null) tag.setInteger("Amount", amount);
        return tag;
    }

    public static FluidId create(FluidStack fluidStack) {
        return createWithCopy(fluidStack.getFluid(), null, fluidStack.tag);
    }

    public static FluidId createWithAmount(FluidStack fluidStack) {
        return createWithCopy(fluidStack.getFluid(), (Integer) fluidStack.amount, fluidStack.tag);
    }

    public static FluidId create(Fluid fluid) {
        return createNoCopy(fluid, null, null);
    }

    public static FluidId createWithCopy(Fluid fluid, Integer amount, @Nullable NBTTagCompound nbt) {
        if (nbt != null) {
            nbt = (NBTTagCompound) nbt.copy();
        }
        return new AutoValue_FluidId(fluid, nbt, amount);
    }

    /**
     * This method does not copy the NBT tag.
     */
    public static FluidId createNoCopy(Fluid fluid, Integer amount, @Nullable NBTTagCompound nbt) {
        return new AutoValue_FluidId(fluid, nbt, amount);
    }

    protected abstract Fluid fluid();

    @Nullable
    protected abstract NBTTagCompound nbt();

    @Nullable
    protected abstract Integer amount();

    @Nonnull
    public FluidStack getFluidStack() {
        NBTTagCompound nbt = nbt();
        return new FluidStack(fluid(), 1, nbt != null ? (NBTTagCompound) nbt.copy() : null);
    }

    @Nonnull
    public FluidStack getFluidStack(int amount) {
        NBTTagCompound nbt = nbt();
        return new FluidStack(fluid(), amount, nbt != null ? (NBTTagCompound) nbt.copy() : null);
    }
}
