package matter_manipulator.core.fluid;

import java.util.Objects;

import javax.annotation.Nonnegative;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

/// A [ImmutableFluidStack] implementation that's meant to be passed to the various insert methods.
public class InsertionFluidStack implements ImmutableFluidStack {

    private boolean useImmutable = false;
    private FluidStack stack;
    private ImmutableFluidStack immutable;
    @Nonnegative
    private int amountToInsert;

    /// Must be initialized later
    public InsertionFluidStack() {

    }

    public InsertionFluidStack(@NotNull FluidStack stack) {
        set(stack, stack.amount);
    }

    public InsertionFluidStack(@NotNull FluidStack stack, @Nonnegative int amountToInsert) {
        set(stack, amountToInsert);
    }

    public InsertionFluidStack(@NotNull ImmutableFluidStack stack) {
        set(stack, stack.getCount());
    }

    public InsertionFluidStack(@NotNull ImmutableFluidStack stack, @Nonnegative int amountToInsert) {
        set(stack, amountToInsert);
    }

    public InsertionFluidStack set(FluidStack stack) {
        this.useImmutable = false;
        this.stack = Objects.requireNonNull(stack, "stack cannot be null");
        this.amountToInsert = Math.max(0, stack.amount);
        return this;
    }

    public InsertionFluidStack set(FluidStack stack, int amount) {
        this.useImmutable = false;
        this.stack = Objects.requireNonNull(stack, "stack cannot be null");
        this.amountToInsert = Math.max(0, amount);
        return this;
    }

    public InsertionFluidStack set(ImmutableFluidStack stack) {
        this.useImmutable = true;
        this.immutable = Objects.requireNonNull(stack, "stack cannot be null");
        this.amountToInsert = Math.max(0, stack.getCount());
        return this;
    }

    public InsertionFluidStack set(ImmutableFluidStack stack, int amount) {
        this.useImmutable = true;
        this.immutable = Objects.requireNonNull(stack, "stack cannot be null");
        this.amountToInsert = Math.max(0, amount);
        return this;
    }

    public InsertionFluidStack set(int amount) {
        this.amountToInsert = Math.max(0, amount);
        return this;
    }

    public InsertionFluidStack decrement(int amount) {
        this.amountToInsert = Math.max(0, amountToInsert - amount);
        return this;
    }

    @Override
    public int getCount() {
        return amountToInsert;
    }

    @Override
    public Fluid getFluid() {
        return useImmutable ? immutable.getFluid() : stack.getFluid();
    }

    @Override
    public NBTTagCompound getTag() {
        return useImmutable ? immutable.getTag() : stack.tag;
    }
}
