package matter_manipulator.common.modes;

import matter_manipulator.core.context.ManipulatorContext;

public interface PasteableMode {

    boolean onPastePressed(ManipulatorContext context);

}
