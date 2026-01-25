package matter_manipulator.common.modes;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Contract;

import com.cleanroommc.modularui.api.drawable.IKey;
import matter_manipulator.Tags;
import matter_manipulator.client.gui.BranchableRadialMenu;
import matter_manipulator.client.rendering.ModeRenderer;
import matter_manipulator.client.rendering.modes.GeometryModeRenderer;
import matter_manipulator.common.building.StandardBuild;
import matter_manipulator.common.modes.GeometryConfig.PendingAction;
import matter_manipulator.common.modes.GeometryConfig.Shape;
import matter_manipulator.common.networking.MMPacketBuffer;
import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.modes.ManipulatorMode;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.util.Coroutine;

public class GeometryManipulatorMode implements ManipulatorMode<GeometryConfig, StandardBuild> {

    @Override
    public ResourceLocation getModeID() {
        return new ResourceLocation(Tags.MODID, "geometry");
    }

    @Override
    public String getLocalizedName() {
        return "Geometry";
    }

    @Override
    public ModeRenderer<GeometryConfig, StandardBuild> getRenderer(ManipulatorContext context) {
        return new GeometryModeRenderer();
    }

    @Override
    public void addTooltipInfo(ManipulatorContext context, List<String> lines) {
        GeometryConfig config = loadConfig(context.getState().getActiveModeConfigStorage());

        lines.add("Shape: " + config.shape);

        lines.add("A: " + config.a);
        lines.add("B: " + config.b);
        lines.add("C: " + config.c);

        lines.add("Faces: " + config.faces);
        lines.add("Edges: " + config.edges);
        lines.add("Corners: " + config.corners);
        lines.add("Volumes: " + config.volumes);
    }

    @Override
    public void addMenuItems(ManipulatorContext context, BranchableRadialMenu menu) {
        GeometryConfig config = loadConfig(context.getState().getActiveModeConfigStorage());

        menu.branch()
            .label(IKey.str("Set Shape"))
            .pipe(shapes -> {
                for (Shape shape : Shape.values()) {
                    shapes.option()
                        .label(IKey.str(shape.getLocalizedName()))
                        .onClicked(() -> {
                            config.shape = shape;
                            saveConfig(context.getState().getActiveModeConfigStorage(), config);
                        })
                        .done();
                }
            })
            .done();
    }

    @Override
    public Optional<GeometryConfig> onPickBlock(GeometryConfig geometryConfig, ManipulatorContext context) {
        return Optional.empty();
    }

    @Override
    public Optional<GeometryConfig> onRightClick(GeometryConfig geometryConfig, ManipulatorContext context) {
        if (geometryConfig.action != null) {
            Optional<GeometryConfig> result = geometryConfig.action.process(geometryConfig, context, false);

            if (result.isPresent()) {
                return result;
            }
        } else {
            var hit = context.getHitResult();

            if (hit != null) {
                geometryConfig.action = PendingAction.MARK_A;
                geometryConfig.a = null;
                geometryConfig.b = null;
                geometryConfig.c = null;

                return geometryConfig.action.process(geometryConfig, context, false);
            }
        }

        return Optional.empty();
    }

    @Override
    public GeometryConfig loadConfig(IDataStorage storage) {
        return storage.getSandbox(getModeID()).load(GeometryConfig.class);
    }

    @Override
    public void saveConfig(IDataStorage storage, GeometryConfig geometryConfig) {
        storage.getSandbox(getModeID()).save(geometryConfig);
    }

    @Override
    public void write(MMPacketBuffer buffer, StandardBuild buildable) {

    }

    @Override
    public StandardBuild read(MMPacketBuffer buffer) {
        return null;
    }

    @Contract(mutates = "param2")
    @Override
    public Coroutine<StandardBuild> startAnalysis(GeometryConfig config, ManipulatorContext context) {
        if (!config.shape.canRender(config.a, config.b, config.c)) {
            return ctx -> ctx.stop(new StandardBuild(new ArrayDeque<>()));
        }

        return config.shape.getBlocks(config, context);
    }
}
