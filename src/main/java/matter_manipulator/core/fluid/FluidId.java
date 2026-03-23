package matter_manipulator.core.fluid;

import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.Hash.Strategy;
import matter_manipulator.common.utils.MCUtils;
import matter_manipulator.common.utils.hash.Fnv1a32;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.item.ImmutableItemMeta;
import matter_manipulator.core.resources.ResourceIdentity.IntResourceIdentity;
import matter_manipulator.core.resources.ResourceIdentity.LongResourceIdentity;
import matter_manipulator.core.resources.ResourceIdentityTrait;
import matter_manipulator.core.resources.fluid.FluidResourceIdentity;
import matter_manipulator.core.resources.fluid.FluidStackWrapper;

@Desugar
public record FluidId(Fluid fluid, NBTTagCompound tag) implements FluidResourceIdentity, IntResourceIdentity,
    LongResourceIdentity {

    @Override
    public Fluid getFluid() {
        return fluid;
    }

    @Override
    public NBTTagCompound getTag() {
        return tag;
    }

    @Override
    public FluidStackWrapper createStackInt(int amount) {
        return new FluidStackWrapper(getFluidStack(amount));
    }

    @Override
    public BigFluidStack createStackLong(long amount) {
        return new BigFluidStack(this, amount);
    }

    @Override
    public boolean hasTrait(ResourceIdentityTrait trait) {
        return false;
    }

    @Override
    public Localized getName() {
        return new Localized("mm.misc.fluidstack", toStack(1));
    }

    public static FluidId create(NBTTagCompound tag) {
        return new FluidId(
            FluidRegistry.getFluid(tag.getString("FluidName")),
            tag.hasKey("Tag", TAG_COMPOUND) ? tag.getCompoundTag("Tag") : null
        );
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("FluidName", fluid().getName());
        if (tag() != null) tag.setTag("Tag", tag());
        return tag;
    }

    public static FluidId create(FluidStack fluidStack) {
        return create(fluidStack.getFluid(), fluidStack.tag);
    }

    public static FluidId create(Fluid fluid) {
        return create(fluid, null);
    }

    public static FluidId create(Fluid fluid, @Nullable NBTTagCompound tag) {
        return new FluidId(fluid, MCUtils.copy(tag));
    }

    /**
     * This method does not copy the NBT tag.
     */
    public static FluidId createNoCopy(Fluid fluid, @Nullable NBTTagCompound tag) {
        return new FluidId(fluid, tag);
    }

    @Nonnull
    public FluidStack getFluidStack() {
        NBTTagCompound tag = tag();
        return new FluidStack(fluid(), 1, tag != null ? tag.copy() : null);
    }

    @Nonnull
    public FluidStack getFluidStack(int amount) {
        NBTTagCompound tag = tag();
        return new FluidStack(fluid(), amount, tag != null ? tag.copy() : null);
    }

    /**
     * A hash strategy that only checks the item and metadata.
     */
    public static final Strategy<FluidId> FLUID_STRATEGY = new Strategy<>() {

        @Override
        public int hashCode(FluidId o) {
            int hash = Fnv1a32.initialState();

            if (o != null) {
                hash = Fnv1a32.hashStep(hash, Objects.hashCode(o.fluid));
            }

            return hash;
        }

        @Override
        public boolean equals(FluidId a, FluidId b) {
            if (a == b) return true;
            if (a == null || b == null) return false;

            return a.fluid() == b.fluid();
        }
    };

    /**
     * A hash strategy that checks the item, metadata, and nbt.
     */
    public static final Strategy<FluidId> FLUID_NBT_STRATEGY = new Strategy<>() {

        @Override
        public int hashCode(FluidId o) {
            int hash = Fnv1a32.initialState();

            if (o != null) {
                hash = Fnv1a32.hashStep(hash, Objects.hashCode(o.fluid));
                hash = Fnv1a32.hashStep(hash, Objects.hashCode(o.tag));
            }

            return hash;
        }

        @Override
        public boolean equals(FluidId a, FluidId b) {
            if (a == b) return true;
            if (a == null || b == null) return false;

            return a.fluid == b.fluid && Objects.equals(a.tag, b.tag);
        }
    };

    /**
     * A hash strategy that only checks the item and metadata.
     */
    public static final Strategy<FluidStack> STACK_FLUID_STRATEGY = new Strategy<>() {

        @Override
        public int hashCode(FluidStack o) {
            int hash = Fnv1a32.initialState();

            if (o != null) {
                hash = Fnv1a32.hashStep(hash, Objects.hashCode(o.getFluid()));
            }

            return hash;
        }

        @Override
        public boolean equals(FluidStack a, FluidStack b) {
            if (a == b) return true;
            if (a == null || b == null) return false;

            return a.getFluid() == b.getFluid();
        }
    };

    /**
     * A hash strategy that checks the fluid and nbt.
     */
    public static final Strategy<FluidStack> STACK_FLUID_NBT_STRATEGY = new Strategy<>() {

        @Override
        public int hashCode(FluidStack o) {
            int hash = Fnv1a32.initialState();

            if (o != null) {
                hash = Fnv1a32.hashStep(hash, Objects.hashCode(o.getFluid()));
                hash = Fnv1a32.hashStep(hash, Objects.hashCode(o.tag));
            }

            return hash;
        }

        @Override
        public boolean equals(FluidStack a, FluidStack b) {
            if (a == b) return true;
            if (a == null || b == null) return false;

            if (a.getFluid() != b.getFluid()) return false;
            return Objects.equals(a.tag, b.tag);
        }
    };

    private static Fluid getGenericFluid(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Fluid fluid) return fluid;
        if (obj instanceof FluidStack stack) return stack.getFluid();
        if (obj instanceof ImmutableFluidStack stack) return stack.getFluid();

        throw new IllegalArgumentException("Cannot extract fluid from object: " + obj);
    }

    private static NBTTagCompound getGenericTag(Object obj) {
        if (obj == null) return null;
        if (obj instanceof FluidStack stack) return stack.tag;
        // Includes FluidId
        if (obj instanceof ImmutableFluidStack stack) return stack.getTag();

        throw new IllegalArgumentException("Cannot extract fluid tag from object: " + obj);
    }

    /// A hash strategy that only checks the item and metadata. Works with [FluidStack], [FluidId], [ImmutableItemMeta],
    /// and [ImmutableFluidStack] - equivalent objects that represent the same 'stack' will have the same hash and equal
    /// each other.
    public static final Strategy<Object> GENERIC_FLUID_STRATEGY = new Strategy<>() {

        @Override
        public int hashCode(Object o) {
            int hash = Fnv1a32.initialState();

            if (o != null) {
                hash = Fnv1a32.hashStep(hash, Objects.hashCode(getGenericFluid(o)));
            }

            return hash;
        }

        @Override
        public boolean equals(Object a, Object b) {
            if (a == b) return true;
            if (a == null || b == null) return false;

            return getGenericFluid(a) == getGenericFluid(b);
        }
    };

    /// A hash strategy that checks the item, metadata, and tag. Works with [FluidStack], [FluidId], [ImmutableItemMeta],
    /// and [ImmutableFluidStack] - equivalent objects that represent the same 'stack' will have the same hash and equal
    /// each other.
    public static final Strategy<Object> GENERIC_FLUID_NBT_STRATEGY = new Strategy<>() {

        @Override
        public int hashCode(Object o) {
            int hash = Fnv1a32.initialState();

            if (o != null) {
                hash = Fnv1a32.hashStep(hash, Objects.hashCode(getGenericFluid(o)));
                hash = Fnv1a32.hashStep(hash, Objects.hashCode(getGenericTag(o)));
            }

            return hash;
        }

        @Override
        public boolean equals(Object a, Object b) {
            if (a == b) return true;
            if (a == null || b == null) return false;

            if (getGenericFluid(a) != getGenericFluid(b)) return false;
            return Objects.equals(getGenericTag(a), getGenericTag(b));
        }
    };
}
