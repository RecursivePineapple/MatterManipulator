package matter_manipulator.core.context;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockAnalysisContext extends ManipulatorContext {

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

    default IBlockState getBlockState() {
        return getWorld().getBlockState(getPos());
    }
}
