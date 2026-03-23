package matter_manipulator.core.fluid;

import net.minecraftforge.fluids.FluidStack;

public class FluidUtils {

    public static boolean isEmpty(FluidStack stack) {
        return stack == null || stack.getFluid() == null || stack.amount <= 0;
    }

    public static FluidStack copyWithAmount(FluidStack stack, int amount) {
        if (isEmpty(stack)) return null;

        stack = stack.copy();

        stack.amount = amount;

        return stack;
    }
}
