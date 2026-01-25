package matter_manipulator.core.context;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import matter_manipulator.common.utils.MathUtils;
import matter_manipulator.common.utils.math.Location;

public interface ManipulatorContext extends StackManipulatorContext {

    World getWorld();

    EntityPlayer getRealPlayer();

    @Nullable
    default RayTraceResult getHitResult() {
        return MathUtils.getHitResult(getRealPlayer());
    }

    @NotNull
    default Location getLookedAtBlock() {
        return new Location(getWorld(), MathUtils.getLookedAtBlock(getRealPlayer()));
    }
}
