package matter_manipulator.core.fluid;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;

/// An ImmutableItemStack backed by an ItemStack. This is meant to be allocated once, then modified and returned from an
/// API repeatedly.
public class FastImmutableFluidStack implements ImmutableFluidStack {

    @Getter
    @NotNull
    protected FluidStack stack;

    public FastImmutableFluidStack() {
        this(null);
    }

    public FastImmutableFluidStack(FluidStack stack) {
        set(stack);
    }

    public FastImmutableFluidStack set(@NotNull FluidStack stack) {
        this.stack = stack;
        return this;
    }

    @Override
    public int getCount() {
        return stack.amount;
    }

    @Override
    public Fluid getFluid() {
        return stack.getFluid();
    }

    @Override
    public NBTTagCompound getTag() {
        return stack.tag;
    }
}
