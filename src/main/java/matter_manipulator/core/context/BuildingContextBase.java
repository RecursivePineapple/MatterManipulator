package matter_manipulator.core.context;

import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

public interface BuildingContextBase extends ManipulatorContext {

    BlockPos getPos();

    default int getX() {
        return getPos().getX();
    }

    default int getY() {
        return getPos().getY();
    }

    default int getZ() {
        return getPos().getZ();
    }

    default void playSound(SoundEvent sound) {
        playSound(getPos(), sound);
    }

    /// Queues a sound to be played at a specific spot.
    /// This mechanism finds the centre point for all played sounds of the same type and makes a single sound event so
    /// that several aren't played in the same tick.
    void playSound(BlockPos pos, SoundEvent sound);

    /// Returns true when the current build is a simulated build. This means that resources should be extracted as
    /// normal, but nothing should be placed or removed from the world. This is used to calculate which resources are
    /// needed for a build. The resource lists are then typically used to generate a plan.
    boolean isSimulation();
}
