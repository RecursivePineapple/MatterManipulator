package matter_manipulator.client.rendering;

import matter_manipulator.core.building.IBuildable;
import matter_manipulator.core.context.ManipulatorRenderingContext;

public interface ModeRenderer<Config, Buildable extends IBuildable> {

    void renderOverlay(ManipulatorRenderingContext context, Config config, Buildable buildable);

    void emitHints(ManipulatorRenderingContext context, Config config, Buildable buildable);

    void reset(Config config, Buildable buildable);
}
