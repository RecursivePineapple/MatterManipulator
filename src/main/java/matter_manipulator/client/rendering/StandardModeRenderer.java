package matter_manipulator.client.rendering;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.Vector3i;

import matter_manipulator.CommonProxy;
import matter_manipulator.GlobalMMConfig.RenderingConfig;
import matter_manipulator.common.block_spec.StandardBlockSpec;
import matter_manipulator.common.context.AnalysisContextImpl;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.common.utils.data.Lazy;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.building.IPendingBlockBuildable;
import matter_manipulator.core.building.PendingBlock;
import matter_manipulator.core.color.ImmutableColor;
import matter_manipulator.core.color.RGBColor;
import matter_manipulator.core.context.ManipulatorRenderingContext;
import matter_manipulator.core.interop.BlockAdapter;
import matter_manipulator.core.misc.BuildFeedback;

public class StandardModeRenderer<Config, Buildable extends IPendingBlockBuildable> implements ModeRenderer<Config, Buildable> {

    private static final ImmutableColor WHITE = RGBColor.fromRGB(0xE5F2FF);
    private static final ImmutableColor INFO = RGBColor.fromRGB(0x3e73ff);
    private static final ImmutableColor WARNING = RGBColor.fromRGB(0xFFAA00);
    private static final ImmutableColor ERROR = RGBColor.fromRGB(0xFF5555);

    public static final Lazy<IBlockSpec> HINT_BLANK = new Lazy<>(() -> new StandardBlockSpec(CommonProxy.HINT_BLANK.getDefaultState()));
    public static final Lazy<IBlockSpec> HINT_DOT = new Lazy<>(() -> new StandardBlockSpec(CommonProxy.HINT_DOT.getDefaultState()));
    public static final Lazy<IBlockSpec> HINT_WARNING = new Lazy<>(() -> new StandardBlockSpec(CommonProxy.HINT_WARNING.getDefaultState()));
    public static final Lazy<IBlockSpec> HINT_X = new Lazy<>(() -> new StandardBlockSpec(CommonProxy.HINT_X.getDefaultState()));

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

            if (pendingBlock.spec.equals(existing)) continue;

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

            IBlockState target = pendingBlock.spec.getBlockState();

            BlockAdapter adapter = MMRegistriesInternal.getBlockAdapter(target);

            if (adapter == null) {
                MMHintRenderer.INSTANCE.addHint(
                    pendingBlock.x,
                    pendingBlock.y,
                    pendingBlock.z,
                    HINT_WARNING.get(),
                    ERROR
                );

                continue;
            }

            IBlockState base = adapter.getBlockForm(adapter.getResourceForm(target));

            MutableObject<IBlockState> state = new MutableObject<>(base);

            MMRegistriesInternal.transformBlock(state, target, EnumSet.noneOf(ApplyResult.class));

            MMHintRenderer.INSTANCE.addHint(
                pendingBlock.x,
                pendingBlock.y,
                pendingBlock.z,
                state.getValue().getBlock() == Blocks.AIR ?
                    HINT_X.get() :
                    pendingBlock.spec.clone(state.getValue()),
                tint
            );
        }

        feedbackMap.forEach((pos, feedback) -> {
            ImmutableColor tint = WHITE;

            if (feedback != null) {
                tint = switch (feedback.severity()) {
                    case ERROR -> ERROR;
                    case WARNING -> WARNING;
                    case INFO -> INFO;
                };
            }

            MMHintRenderer.INSTANCE.addHint(pos.getX(), pos.getY(), pos.getZ(), HINT_WARNING.get(), tint);
        });
    }

    @Override
    public void reset(Config config, Buildable buildable) {

    }
}
