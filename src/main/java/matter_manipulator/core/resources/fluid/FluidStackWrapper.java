package matter_manipulator.core.resources.fluid;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import matter_manipulator.core.fluid.FluidId;
import matter_manipulator.core.fluid.FluidStackLike;
import matter_manipulator.core.fluid.FluidUtils;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.ResourceTrait;

public class FluidStackWrapper implements IntFluidResourceStack {

    public FluidStack stack;

    public FluidStackWrapper(FluidStack stack) {
        this.stack = stack;
    }

    @Override
    public boolean hasTrait(ResourceTrait trait) {
        return switch (trait) {
            case IntAmount -> true;
            default -> false;
        };
    }

    @Override
    public @NotNull Localized getName() {
        return new Localized("mm.misc.fluidstack", toStack(1));
    }

    @Override
    public FluidId getIdentity() {
        return FluidId.create(stack);
    }

    @Override
    public boolean isSameType(ResourceStack other) {
        if (!(other instanceof FluidStackLike fluid)) return false;

        return matches(fluid);
    }

    @Override
    public FluidStackWrapper emptyCopy() {
        return new FluidStackWrapper(FluidUtils.copyWithAmount(this.stack, 0));
    }

    @Override
    public int getAmountInt() {
        return stack.amount;
    }

    @Override
    public FluidStackWrapper setAmountInt(int amount) {
        stack.amount = Math.max(0, amount);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return stack == null || stack.getFluid() == null || stack.amount <= 0;
    }

    @Override
    public Fluid getFluid() {
        return isEmpty() ? null : stack.getFluid();
    }

    @Override
    public NBTTagCompound getTag() {
        return stack.tag;
    }
}
