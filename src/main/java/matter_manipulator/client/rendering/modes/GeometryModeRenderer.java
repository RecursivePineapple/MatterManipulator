package matter_manipulator.client.rendering.modes;

import java.util.Optional;

import org.joml.Vector3i;

import matter_manipulator.client.rendering.MMRenderUtils;
import matter_manipulator.client.rendering.StandardModeRenderer;
import matter_manipulator.common.building.StandardBuild;
import matter_manipulator.common.modes.GeometryConfig;
import matter_manipulator.common.utils.MathUtils;
import matter_manipulator.common.utils.math.VoxelAABB;
import matter_manipulator.core.color.ImmutableColor;
import matter_manipulator.core.context.ManipulatorRenderingContext;

public class GeometryModeRenderer extends StandardModeRenderer<GeometryConfig, StandardBuild> {

    @Override
    public void renderOverlay(ManipulatorRenderingContext context, GeometryConfig config, StandardBuild buildable) {
        super.renderOverlay(context, config, buildable);

        if (config.action != null) {
            ImmutableColor ruler = config.action.getRulerColor();

            if (ruler != null) {
                context.drawRulers(context.getLookedAtBlock().toPos(), ruler);
            }

            Optional<GeometryConfig> result = config.action.process(config, context, true);

            if (result.isPresent()) {
                config = result.get();
            }
        }

        if (!config.shape.canRender(config.a, config.b, config.c)) {
            context.clearHints();
            return;
        }

        Vector3i a = MathUtils.copy(config.a);
        Vector3i b = MathUtils.copy(config.b);
        Vector3i c = MathUtils.copy(config.c);

        config.shape.pinCoordinates(a, b, c);

        VoxelAABB box = new VoxelAABB(a, b);

        if (config.shape.needsC()) {
            box.union(c);
        }

        context.drawBox(box, MMRenderUtils.BLUE);
    }

    @Override
    public int hashCode() {
        return GeometryModeRenderer.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GeometryModeRenderer;
    }
}
