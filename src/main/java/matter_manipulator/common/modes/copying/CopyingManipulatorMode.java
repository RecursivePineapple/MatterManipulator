package matter_manipulator.common.modes.copying;

import static matter_manipulator.common.utils.MCUtils.BLUE;
import static matter_manipulator.common.utils.MCUtils.GRAY;
import static matter_manipulator.common.utils.MCUtils.processFormatStacks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Contract;
import org.joml.Vector3i;

import com.cleanroommc.modularui.api.drawable.IKey;
import matter_manipulator.Tags;
import matter_manipulator.client.gui.BranchableRadialMenu;
import matter_manipulator.client.rendering.ModeRenderer;
import matter_manipulator.client.rendering.modes.CopyingModeRenderer;
import matter_manipulator.common.building.StandardBuild;
import matter_manipulator.common.modes.CopyableMode;
import matter_manipulator.common.modes.PasteableMode;
import matter_manipulator.common.modes.ResettableMode;
import matter_manipulator.common.modes.copying.CopyingConfig.PendingAction;
import matter_manipulator.common.networking.MMPacketBuffer;
import matter_manipulator.common.utils.DataUtils;
import matter_manipulator.common.utils.math.Location;
import matter_manipulator.common.utils.math.Transform;
import matter_manipulator.core.analysis.BlockAnalyzer;
import matter_manipulator.core.analysis.BlockAnalyzer.RegionAnalysis;
import matter_manipulator.core.building.PendingBlock;
import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.modes.ManipulatorMode;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.util.Coroutine;

public class CopyingManipulatorMode implements ManipulatorMode<CopyingConfig, StandardBuild>, ResettableMode,
    CopyableMode, PasteableMode {

    @Override
    public ResourceLocation getModeID() {
        return new ResourceLocation(Tags.MODID, "copying");
    }

    @Override
    public String getLocalizedName() {
        return "Copying";
    }

    @Override
    public ModeRenderer<CopyingConfig, StandardBuild> getRenderer(ManipulatorContext context) {
        return new CopyingModeRenderer();
    }

    @Override
    public CopyingConfig getPreviewConfig(CopyingConfig config, ManipulatorContext context) {
        if (config.action != null) {
            Optional<CopyingConfig> result = config.action.process(config, context, true);

            if (result.isPresent()) {
                return result.get();
            }
        }

        return config;
    }

    @Override
    public void addTooltipInfo(ManipulatorContext context, List<String> lines) {
        CopyingConfig config = loadConfig(context.getState()
            .getActiveModeConfigStorage());

        addTooltipLine(lines, "Action: ", config.action);

        addTooltipLine(lines, "Copy A: ", config.copyA);
        addTooltipLine(lines, "Copy B: ", config.copyB);
        addTooltipLine(lines, "Paste: ", config.paste);

        addTooltipLine(lines, "Stack: ", config.stack);

        addTooltipLine(lines, "Forward: ", config.transform.forward);
        addTooltipLine(lines, "Up: ", config.transform.up);

        List<String> flip = new ArrayList<>();

        if (config.transform.flipX) flip.add("X");
        if (config.transform.flipY) flip.add("Y");
        if (config.transform.flipZ) flip.add("Z");

        addTooltipLine(lines, "Flip: ", flip.isEmpty() ? null : DataUtils.join(", ", flip));
    }

    private static void addTooltipLine(List<String> lines, String name, Object value) {
        lines.add(GRAY + name + processFormatStacks(BLUE + value));
    }

    @Override
    public void addMenuItems(ManipulatorContext context, BranchableRadialMenu menu) {
        // spotless:off
        menu.branch()
            .label(IKey.str("Mark Coord"))
            .option()
                .label(IKey.str("Copy"))
                .onClicked(() -> {
                    context.mutateConfig(this, c -> {
                        c.action = PendingAction.MARK_COPY_A;
                    });
                })
            .done()
            .option()
                .label(IKey.str("Paste"))
                .onClicked(() -> {
                    context.mutateConfig(this, c -> {
                        c.action = PendingAction.MARK_PASTE;
                    });
                })
            .done()
            .option()
                .label(IKey.str("Stack"))
                .onClicked(() -> {
                    context.mutateConfig(this, c -> {
                        c.action = PendingAction.MARK_STACK;
                    });
                })
            .done();
        // spotless:on
    }

    @Override
    public Optional<CopyingConfig> onPickBlock(CopyingConfig config, ManipulatorContext context) {
        return Optional.empty();
    }

    @Override
    public Optional<CopyingConfig> onRightClick(CopyingConfig config, ManipulatorContext context) {
        if (config.action != null) {
            Optional<CopyingConfig> result = config.action.process(config, context, false);

            if (result.isPresent()) {
                return result;
            }
        } else {
            var hit = context.getHitResult();

            if (hit != null) {
                config.action = PendingAction.MARK_COPY_A;
                config.copyA = null;
                config.copyB = null;
                config.paste = null;

                return config.action.process(config, context, false);
            }
        }

        return Optional.empty();
    }

    @Override
    public CopyingConfig loadConfig(IDataStorage storage) {
        return storage.getSandbox(getModeID())
            .load(CopyingConfig.class);
    }

    @Override
    public void saveConfig(IDataStorage storage, CopyingConfig config) {
        storage.getSandbox(getModeID())
            .save(config);
    }

    @Override
    public boolean needsSync() {
        return false;
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
    public Coroutine<StandardBuild> startAnalysis(CopyingConfig config, ManipulatorContext context) {
        final Location copyA = config.copyA;
        final Location copyB = config.copyB;
        final Location paste = config.paste;

        if (!Location.areCompatible(copyA, copyB) || paste == null) {
            return ctx -> ctx.stop(new StandardBuild(new ArrayDeque<>()));
        }

        Transform transform = config.transform.clone();
        transform.cacheRotation();

        Coroutine<RegionAnalysis> analyzer = BlockAnalyzer.analyzeRegion(context, copyA, copyB);

        return analyzer.then(analysis -> {
            Vector3i deltas = analysis.deltas();
            List<PendingBlock> blocks = analysis.blocks();

            // Apply rotation
            for (PendingBlock block : blocks) {
                Vector3i v = transform.apply(block);

                block.x = v.x;
                block.y = v.y;
                block.z = v.z;

                block.spec.transform(transform);
            }

            // Offset to the correct location (needs to be after rotating)
            for (PendingBlock block : blocks) {
                block.add(paste);
            }

            // Copy the blocks (stacking)
            if (config.stack != null) {
                int sx = config.stack.x;
                int sy = config.stack.y;
                int sz = config.stack.z;

                List<PendingBlock> base = new ArrayList<>(blocks);
                blocks.clear();

                for (int y = Math.min(sy, 0); y <= Math.max(sy, 0); y++) {
                    for (int z = Math.min(sz, 0); z <= Math.max(sz, 0); z++) {
                        for (int x = Math.min(sx, 0); x <= Math.max(sx, 0); x++) {
                            int dx = x * (deltas.x + (deltas.x < 0 ? -1 : 1));
                            int dy = y * (deltas.y + (deltas.y < 0 ? -1 : 1));
                            int dz = z * (deltas.z + (deltas.z < 0 ? -1 : 1));

                            Vector3i d = new Vector3i(dx, dy, dz);

                            transform.apply(d);

                            for (PendingBlock original : base) {
                                PendingBlock dup = original.clone();
                                dup.x += d.x;
                                dup.y += d.y;
                                dup.z += d.z;
                                blocks.add(dup);
                            }
                        }
                    }
                }
            }

            return Coroutine.finished(new StandardBuild(new ArrayDeque<>(blocks)));
        });
    }

    @Override
    public boolean onCopyPressed(ManipulatorContext context) {
        context.mutateConfig(this, config -> {
            config.action = PendingAction.MARK_COPY_A;
        });

        return true;
    }

    @Override
    public boolean onPastePressed(ManipulatorContext context) {
        context.mutateConfig(this, config -> {
            config.action = PendingAction.MARK_PASTE;
        });

        return true;
    }

    @Override
    public boolean onResetPressed(ManipulatorContext context) {
        context.mutateConfig(this, config -> {
            config.action = null;
            config.copyA = null;
            config.copyB = null;
            config.paste = null;
        });

        return true;
    }
}
