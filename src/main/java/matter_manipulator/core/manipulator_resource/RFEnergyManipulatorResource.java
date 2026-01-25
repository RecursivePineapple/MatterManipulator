package matter_manipulator.core.manipulator_resource;

import static matter_manipulator.common.utils.MCUtils.formatNumbers;

import java.util.List;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import matter_manipulator.common.items.ManipulatorTier;
import matter_manipulator.common.utils.MCUtils;
import matter_manipulator.common.utils.MathUtils;
import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.persist.IStateSandbox;

public class RFEnergyManipulatorResource implements EnergyManipulatorResource, IEnergyStorage {

    private final ManipulatorTier tier;
    private final IStateSandbox state;

    public RFEnergyManipulatorResource(ManipulatorTier tier, IStateSandbox state) {
        this.tier = tier;
        this.state = state;
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY;
    }

    @Override
    public <T> @Nullable T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(this);
        }

        return null;
    }

    @Override
    public double extract(double amount) {
        JsonElement value = state.getValue();

        double stored = value != null ? value.getAsDouble() : 0;

        if (stored < amount) {
            return 0;
        }

        stored -= amount;

        state.setValue(new JsonPrimitive(stored));

        return amount;
    }

    @Override
    public double getStored() {
        JsonElement value = state.getValue();

        return value != null ? value.getAsDouble() : 0;
    }

    @Override
    public double getCapacity() {
        return switch (tier) {
            case Tier0 -> 10_000_000d;
            case Tier1 -> 100_000_000d;
            case Tier2 -> 10_00_000_000d;
            case Tier3 -> 10_000_000_000d;
        };
    }

    @Override
    public void addManipulatorTooltipInfo(ManipulatorContext context, List<String> lines) {
        lines.add(
            TextFormatting.AQUA
                + MCUtils.translate(
                "mm.tooltip.energy.rf",
                formatNumbers(MathUtils.clamp(getStored(), 0d, getCapacity())),
                formatNumbers(getCapacity()))
                + TextFormatting.GRAY);

    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        double stored = getStored();
        double capacity = getCapacity();

        int remaining = (int) Math.max(0, capacity - stored);

        int accepted = Math.min(MathHelper.ceil(remaining), maxReceive);

        if (accepted <= 0) return 0;

        if (!simulate) {
            state.setValue(new JsonPrimitive(Math.min(capacity, stored + accepted)));
        }

        return accepted;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        double stored = getStored();

        int extractable = Math.min(maxExtract, (int) stored);

        if (!simulate) {
            state.setValue(new JsonPrimitive(Math.max(0, stored - extractable)));
        }

        return extractable;
    }

    @Override
    public int getEnergyStored() {
        double stored = getStored();
        double capacity = getCapacity();

        if (stored >= capacity) return Integer.MAX_VALUE;
        if (stored > 0 && stored < capacity) return Integer.MAX_VALUE / 2;
        return 0;
    }

    @Override
    public int getMaxEnergyStored() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}
