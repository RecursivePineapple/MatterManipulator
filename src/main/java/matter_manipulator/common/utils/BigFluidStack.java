package matter_manipulator.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import cpw.mods.fml.common.Optional.Method;

import appeng.api.storage.data.IAEFluidStack;
import appeng.util.item.AEFluidStack;

import matter_manipulator.common.utils.Mods.Names;

public class BigFluidStack {

    public Fluid fluid;
    public long amount;
    public int meta;
    public NBTTagCompound tag;

    public transient FluidId id;

    public BigFluidStack() {}

    private BigFluidStack(FluidStack stack) {
        this.fluid = stack.getFluid();
        this.amount = stack.amount;
        this.tag = MMUtils.copy(stack.tag);
    }

    private BigFluidStack(FluidId id, long amount) {
        this(id.getFluidStack());
        setStackSize(amount);
    }

    public FluidId getId() {
        if (id == null) {
            id = FluidId.createWithCopy(fluid, null, tag);
        }

        return id;
    }

    public FluidStack getFluidStack() {
        FluidStack stack = new FluidStack(fluid, amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount);
        stack.tag = MMUtils.copy(tag);
        return stack;
    }

    @Method(modid = Names.APPLIED_ENERGISTICS2)
    public IAEFluidStack getAEFluidStack() {
        return Objects.requireNonNull(AEFluidStack.create(getFluidStack())).setStackSize(amount);
    }

    public static BigFluidStack create(FluidStack stack) {
        if (stack == null) return null;

        return new BigFluidStack(stack);
    }

    public static BigFluidStack create(FluidId id, long amount) {
        if (id == null) return null;

        return new BigFluidStack(id, amount);
    }

    @Method(modid = Names.APPLIED_ENERGISTICS2)
    public static BigFluidStack create(IAEFluidStack stack) {
        if (stack == null) return null;

        return create(stack.getFluidStack()).setStackSize(stack.getStackSize());
    }

    public FluidStack remove(int amount) {
        if (this.amount < amount) {
            FluidStack stack = new FluidStack(fluid, (int) this.amount);
            stack.tag = MMUtils.copy(tag);
            this.amount = 0;
            return stack;
        } else {
            FluidStack stack = new FluidStack(fluid, amount);
            stack.tag = MMUtils.copy(tag);
            this.amount -= amount;
            return stack;
        }
    }

    public BigFluidStack removeBig(long amount) {
        long toRemove = Math.min(this.amount, amount);

        BigFluidStack stack = copy().setStackSize(toRemove);
        this.amount -= toRemove;

        return stack;
    }

    public BigFluidStack incStackSize(long amount) {
        this.amount += amount;
        return this;
    }

    public BigFluidStack decStackSize(long amount) {
        this.amount -= amount;
        return this;
    }

    public List<FluidStack> toStacks() {
        List<FluidStack> stack = new ArrayList<>();

        while (this.amount > 0) {
            stack.add(remove(Integer.MAX_VALUE));
        }

        return stack;
    }

    public BigFluidStack copy() {
        BigFluidStack out = new BigFluidStack();

        out.fluid = fluid;
        out.amount = amount;
        out.meta = meta;
        out.tag = tag == null ? null : (NBTTagCompound) tag.copy();

        return out;
    }

    public BigFluidStack setStackSize(long amount) {
        this.amount = amount;
        return this;
    }

    public long getStackSize() {
        return amount;
    }
}
