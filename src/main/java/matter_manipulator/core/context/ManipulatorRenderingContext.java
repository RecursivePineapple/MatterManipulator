package matter_manipulator.core.context;

import java.util.Collection;
import java.util.List;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import matter_manipulator.common.utils.math.VoxelAABB;
import matter_manipulator.core.color.ImmutableColor;
import matter_manipulator.core.misc.BuildFeedback;

public interface ManipulatorRenderingContext extends ManipulatorContext {

    List<BuildFeedback> getSortedFeedback();

    Collection<BuildFeedback> getFeedback();

    BuildFeedback getFeedback(BlockPos pos);

    void markNeedsHintDraw();
    void clearHints();

    void drawRulers(BlockPos pos, ImmutableColor color);
    void drawBox(AxisAlignedBB aabb, ImmutableColor color);
    void drawBox(VoxelAABB aabb, ImmutableColor color);

}
