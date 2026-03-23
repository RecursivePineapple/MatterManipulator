package matter_manipulator.client.rendering.modes;

import matter_manipulator.client.rendering.MMRenderUtils;
import matter_manipulator.client.rendering.StandardModeRenderer;
import matter_manipulator.common.building.StandardBuild;
import matter_manipulator.common.modes.copying.CopyingConfig;
import matter_manipulator.common.utils.math.Location;
import matter_manipulator.core.color.ImmutableColor;
import matter_manipulator.core.context.ManipulatorRenderingContext;

public class CopyingModeRenderer extends StandardModeRenderer<CopyingConfig, StandardBuild> {

    @Override
    public void renderOverlay(ManipulatorRenderingContext context, CopyingConfig config, StandardBuild buildable) {
        super.renderOverlay(context, config, buildable);

        if (config.action != null) {
            ImmutableColor ruler = config.action.getRulerColor();

            if (ruler != null) {
                context.drawRulers(context.getLookedAtBlock().toPos(), ruler);
            }
        }

        if (Location.areCompatible(config.copyA, config.copyB)) {
            if (config.copyA.worldId == context.getWorld().provider.getDimension()) {
                context.drawBox(config.getCopyVisualDeltas(), MMRenderUtils.BLUE);
            }
        }

        if (Location.isInWorld(config.paste, context.getWorld())) {
            context.drawBox(config.getPasteVisualDeltas(), MMRenderUtils.ORANGE);
        }
    }
}
