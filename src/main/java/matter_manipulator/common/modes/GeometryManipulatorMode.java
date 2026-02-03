package matter_manipulator.common.modes;

import static matter_manipulator.common.utils.MCUtils.BLUE;
import static matter_manipulator.common.utils.MCUtils.GRAY;
import static matter_manipulator.common.utils.MCUtils.processFormatStacks;

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
import matter_manipulator.common.block_spec.StandardBlockSpec;
import matter_manipulator.common.building.StandardBuild;
import matter_manipulator.common.context.AnalysisContextImpl;
import matter_manipulator.common.modes.GeometryConfig.PendingAction;
import matter_manipulator.common.modes.GeometryConfig.Shape;
import matter_manipulator.common.networking.MMPacketBuffer;
import matter_manipulator.common.utils.MCUtils;
import matter_manipulator.core.block_spec.IBlockSpec;
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
    public GeometryConfig getPreviewConfig(GeometryConfig geometryConfig, ManipulatorContext context) {
        if (geometryConfig.action != null) {
            Optional<GeometryConfig> result = geometryConfig.action.process(geometryConfig, context, true);

            if (result.isPresent()) {
                return result.get();
            }
        }

        return geometryConfig;
    }

    @Override
    public void addTooltipInfo(ManipulatorContext context, List<String> lines) {
        GeometryConfig config = loadConfig(context.getState().getActiveModeConfigStorage());

        addTooltipLine(lines, "Shape: ", config.shape);

        addTooltipLine(lines, "A: ", config.a);
        addTooltipLine(lines, "B: ", config.b);
        if (config.shape.needsC()) {
            addTooltipLine(lines, "C: ", config.c);
        }

        addTooltipLine(lines, "Faces: ", config.faces().getDisplayName());
        addTooltipLine(lines, "Edges: ", config.edges().getDisplayName());
        addTooltipLine(lines, "Corners: ", config.corners().getDisplayName());
        addTooltipLine(lines, "Volumes: ", config.volumes().getDisplayName());
    }

    private static void addTooltipLine(List<String> lines, String name, Object value) {
        lines.add(GRAY + name + processFormatStacks(BLUE + value));
    }

    @Override
    public void addMenuItems(ManipulatorContext context, BranchableRadialMenu menu) {
        GeometryConfig config = loadConfig(context.getState().getActiveModeConfigStorage());

        menu.branch()
            .label(IKey.str("Set Shape"))
            .pipe(shapes -> {
                for (Shape shape : Shape.values()) {
                    shapes.option()
                        .label(IKey.str(shape.toString()))
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
        var hit = context.getHitResult();

        IBlockSpec selected = IBlockSpec.AIR;

        if (hit != null) {
            AnalysisContextImpl analysisContext = new AnalysisContextImpl(context);
            analysisContext.setPos(hit.getBlockPos());
            selected = StandardBlockSpec.fromWorld(analysisContext);
        }

        geometryConfig.volumes = selected;

        if (context.isRemote()) {
            MCUtils.sendInfoToPlayer(context.getRealPlayer(), MCUtils.translate("mm.info.set", "volumes", selected.getDisplayName().toString()));
        }

        return Optional.of(geometryConfig);
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
