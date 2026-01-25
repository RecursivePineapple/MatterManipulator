package matter_manipulator.core.building;

import java.util.List;

/// An [IBuildable] that builds a list of [PendingBlock]s.
public interface IPendingBlockBuildable extends IBuildable {

    List<PendingBlock> getPendingBlocks();

}
