package matter_manipulator.core.fluid;

import java.util.Objects;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import matter_manipulator.common.utils.MCUtils;
import matter_manipulator.core.item.BigItemStack;
import matter_manipulator.core.item.ImmutableItemStack;

/// Something that acts like a [FluidStack]. Does not include amounts, use [ImmutableItemStack] for that instead.
public interface FluidStackLike {

    Fluid getFluid();

    NBTTagCompound getTag();

    default FluidStack toStack(int amount) {
        FluidStack stack = new FluidStack(getFluid(), amount);

        stack.tag = MCUtils.copy(getTag());

        return stack;
    }

    default BigFluidStack toBigStack(long amount) {
        return new BigFluidStack(FluidId.create(getFluid(), MCUtils.copy(getTag())), amount);
    }

    /// Creates an [ItemStack] that matches this object, without copying the NBT (use with caution!).
    default FluidStack toStackFast(int amount) {
        FluidStack stack = new FluidStack(getFluid(), amount);

        stack.tag = getTag();

        return stack;
    }

    /// Creates a [BigItemStack] that matches this object, without copying the NBT (use with caution!).
    default BigFluidStack toBigStackFast(long amount) {
        return new BigFluidStack(FluidId.create(getFluid(), getTag()), amount);
    }

    default boolean matches(FluidStack stack) {
        if (stack == null) return false;

        if (getFluid() != stack.getFluid()) return false;
        return Objects.equals(getTag(), stack.tag);
    }

    default boolean matches(FluidStackLike stack) {
        if (stack == null) return false;

        if (getFluid() != stack.getFluid()) return false;
        return Objects.equals(getTag(), stack.getTag());
    }
}
