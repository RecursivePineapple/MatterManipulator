package matter_manipulator.common.modes;

import matter_manipulator.core.context.ManipulatorContext;

public interface ResettableMode {

    boolean onResetPressed(ManipulatorContext context);

}
