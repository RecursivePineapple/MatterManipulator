package matter_manipulator.common.modes.shapes;

import matter_manipulator.core.block_spec.IBlockSpec;

public interface GeometryBlockPalette {

    IBlockSpec corners();
    IBlockSpec edges();
    IBlockSpec faces();
    IBlockSpec volumes();

}
