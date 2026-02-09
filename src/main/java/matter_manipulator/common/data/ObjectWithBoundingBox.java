package matter_manipulator.common.data;

import matter_manipulator.common.utils.math.VoxelAABB;

public interface ObjectWithBoundingBox {

    VoxelAABB getBoundingBox();

}
