package matter_manipulator.client.rendering;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import org.joml.Vector3i;

import matter_manipulator.GlobalMMConfig.RenderingConfig;
import matter_manipulator.common.block_spec.StandardBlockSpec;
import matter_manipulator.common.context.AnalysisContextImpl;
import matter_manipulator.common.utils.data.Lazy;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.building.IPendingBlockBuildable;
import matter_manipulator.core.building.PendingBlock;
import matter_manipulator.core.color.ImmutableColor;
import matter_manipulator.core.color.RGBColor;
import matter_manipulator.core.context.ManipulatorRenderingContext;
import matter_manipulator.core.misc.BuildFeedback;
import matter_manipulator.core.resources.ResourceStack;

public class StandardModeRenderer<Config, Buildable extends IPendingBlockBuildable> implements ModeRenderer<Config, Buildable> {

    private static final ImmutableColor WHITE = RGBColor.fromRGB(0xE5F2FF);
    private static final ImmutableColor INFO = RGBColor.fromRGB(0x3e73ff);
    private static final ImmutableColor WARNING = RGBColor.fromRGB(0xFFAA00);
    private static final ImmutableColor ERROR = RGBColor.fromRGB(0xFF5555);

    public static final Lazy<IBlockSpec> STONE = new Lazy<>(() -> new StandardBlockSpec(Blocks.STONE.getDefaultState()));

    @Override
    public void renderOverlay(ManipulatorRenderingContext context, Config config, Buildable buildable) {

    }

    @Override
    public void emitHints(ManipulatorRenderingContext context, Config config, Buildable buildable) {
        OptionalInt maxRange = context.getMaxRange();

        int i = 0;

        EntityPlayer player = context.getRealPlayer();

        World world = player.world;

        Vector3i playerPos = new Vector3i(
            MathHelper.floor(player.posX),
            MathHelper.floor(player.posY),
            MathHelper.floor(player.posZ));

        Map<BlockPos, BuildFeedback> feedbackMap = new HashMap<>();

        for (BuildFeedback f : context.getFeedback()) {
            feedbackMap.put(f.pos(), f);
        }

        AnalysisContextImpl analysisContext = new AnalysisContextImpl(context);

        for (PendingBlock pendingBlock : buildable.getPendingBlocks()) {
            if (!pendingBlock.isInWorld(world)) continue;

            if (maxRange.isPresent()) {
                int dist2 = pendingBlock.distanceTo2(playerPos);

                if (dist2 > maxRange.getAsInt() * maxRange.getAsInt()) continue;
            }

            BlockPos pos = pendingBlock.toPos();

            if (pendingBlock.spec.isAir() && world.isAirBlock(pos)) continue;

            analysisContext.setPos(pos);
            StandardBlockSpec existing = StandardBlockSpec.fromWorld(analysisContext);

            ResourceStack pendingResource = pendingBlock.spec.getResource();
            ResourceStack existingResource = existing.getResource();

            if (pendingResource.isSameType(existingResource)) continue;

            if (++i > RenderingConfig.maxHints) break;

            ImmutableColor tint = WHITE;

            BuildFeedback feedback = feedbackMap.remove(pos);

            if (feedback != null) {
                tint = switch (feedback.severity()) {
                    case ERROR -> ERROR;
                    case WARNING -> WARNING;
                    case INFO -> INFO;
                };
            }

            if (pendingBlock.spec.isAir()) {
                MMHintRenderer.INSTANCE.addHint(
                    pendingBlock.x,
                    pendingBlock.y,
                    pendingBlock.z,
                    STONE.get(),
                    tint
                );
            } else {
                MMHintRenderer.INSTANCE.addHint(
                    pendingBlock.x,
                    pendingBlock.y,
                    pendingBlock.z,
                    pendingBlock.spec,
                    tint
                );
            }
        }

        feedbackMap.forEach((pos, feedback) -> {
            MMHintRenderer.INSTANCE.addHint(pos.getX(), pos.getY(), pos.getZ(), STONE.get(), WARNING);
        });
    }

    @Override
    public void reset(Config config, Buildable buildable) {

    }
}
