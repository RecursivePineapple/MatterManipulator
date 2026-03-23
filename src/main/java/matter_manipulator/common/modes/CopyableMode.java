package matter_manipulator.common.modes;

import matter_manipulator.core.context.ManipulatorContext;

public interface CopyableMode {

    boolean onCopyPressed(ManipulatorContext context);

}
