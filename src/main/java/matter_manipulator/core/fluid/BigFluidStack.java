package matter_manipulator.core.fluid;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import lombok.SneakyThrows;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.resources.Resource;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.ResourceStack.LongResourceStack;
import matter_manipulator.core.resources.ResourceTrait;
import matter_manipulator.core.resources.fluid.FluidResource;

public class BigFluidStack implements FluidStackLike, LongResourceStack {

    @Getter
    @NotNull
    public final FluidId id;
    @Getter
    public long amount;

    private BigFluidStack(FluidStack stack) {
        this.id = FluidId.create(stack);
        this.amount = stack.amount;
    }

    public BigFluidStack(FluidId id, long amount) {
        this(id.getFluidStack());
        setAmountLong(amount);
    }

    public static BigFluidStack create(FluidStack stack) {
        if (stack == null) return null;

        return new BigFluidStack(stack);
    }

    public static BigFluidStack create(FluidId id, long amount) {
        if (id == null) return null;

        return new BigFluidStack(id, amount);
    }

    public FluidStack remove(int amount) {
        if (this.amount < amount) {
            FluidStack stack = id.getFluidStack((int) this.amount);
            this.amount = 0;
            return stack;
        } else {
            FluidStack stack = id.getFluidStack(amount);
            this.amount -= amount;
            return stack;
        }
    }

    public BigFluidStack removeBig(long amount) {
        long toRemove = Math.min(this.amount, amount);

        BigFluidStack stack = clone();
        stack.setAmountLong(toRemove);
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

    @SneakyThrows
    @Override
    public BigFluidStack clone() {
        return (BigFluidStack) super.clone();
    }

    @Override
    public Fluid getFluid() {
        return id.getFluid();
    }

    @Override
    public NBTTagCompound getTag() {
        return id.getTag();
    }

    @Override
    public long getAmountLong() {
        return amount;
    }

    @Override
    public void setAmountLong(long amount) {
        this.amount = amount;
    }

    @Override
    public boolean hasTrait(ResourceTrait trait) {
        return switch (trait) {
            case LongAmount -> true;
            default -> false;
        };
    }

    @Override
    public Resource<?> getResource() {
        return FluidResource.FLUIDS;
    }

    @Override
    public @NotNull Localized getName() {
        return new Localized("mm.misc.fluidstack", toStack(1));
    }

    @Override
    public FluidId getIdentity() {
        return id;
    }

    @Override
    public boolean isSameType(ResourceStack other) {
        if (!(other instanceof FluidStackLike fluid)) return false;

        return matches(fluid);
    }

    @Override
    public ResourceStack emptyCopy() {
        return new BigFluidStack(id, 0);
    }

    @Override
    public boolean isEmpty() {
        return amount <= 0;
    }
}
