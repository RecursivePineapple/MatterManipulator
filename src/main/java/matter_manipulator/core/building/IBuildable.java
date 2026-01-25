package matter_manipulator.core.building;

import matter_manipulator.core.context.BlockPlacingContext;

/// Something that can be built.
public interface IBuildable {

    void onBuildTick(BlockPlacingContext context);
    void onStop(BlockPlacingContext context);
    boolean isDone();
}
