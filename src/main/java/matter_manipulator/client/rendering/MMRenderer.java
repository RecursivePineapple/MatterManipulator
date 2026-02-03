package matter_manipulator.client.rendering;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Type;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import matter_manipulator.MMMod;
import matter_manipulator.common.building.BuildStatusTracker;
import matter_manipulator.common.items.ItemMatterManipulator;
import matter_manipulator.common.state.MMState;
import matter_manipulator.common.utils.MCUtils;
import matter_manipulator.common.utils.math.VoxelAABB;
import matter_manipulator.core.building.IBuildable;
import matter_manipulator.core.color.ImmutableColor;
import matter_manipulator.core.context.ManipulatorRenderingContext;
import matter_manipulator.core.misc.BuildFeedback;
import matter_manipulator.core.modes.ManipulatorMode;

@SuppressWarnings({ "rawtypes", "unchecked" })
@SideOnly(Side.CLIENT)
public class MMRenderer {

    private static final long ANALYSIS_INTERVAL_MS = 10_000;

    public static final MMRenderer INSTANCE = new MMRenderer();

    private long lastAnalysisMS = 0;

    private ModeRenderer lastRenderer = null;
    private Object lastAnalyzedConfig = null;
    private IBuildable lastAnalysis = null;

    private long lastExceptionPrint = 0;

    private boolean needsHintDraw = false;
    private boolean needsAnalysis = false;

    private boolean wasInUse = false;

    private MMRenderer() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /// Just loads the class
    public static void init() {}

    public static void markNeedsReanalysis() {
        INSTANCE.needsAnalysis = true;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void renderSelection(RenderWorldLastEvent event) {
        try {
            renderSelectionImpl(event);
        } catch (Throwable t) {
            MMMod.LOG.error("Could not draw matter manipulator preview", t);

            long now = System.currentTimeMillis();
            if (now - lastExceptionPrint > 10_000) {
                MCUtils.sendErrorToPlayer(
                    Minecraft.getMinecraft().player,
                    "Could not draw preview due to a crash. Check the logs for more info. Building will not work - items may be voided if you try.");
                lastExceptionPrint = now;
            }
        }
    }

    /**
     * Renders the overlay.
     */
    private void renderSelectionImpl(RenderWorldLastEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);

        if (held != ItemStack.EMPTY && held.getItem() instanceof ItemMatterManipulator) {
            MMState state = ItemMatterManipulator.getState(held);

            RenderContextImpl context = new RenderContextImpl(held, state, event.getPartialTicks());

            ManipulatorMode mode = state.getActiveMode();

            if (mode != null) {
                Object config = mode.loadConfig(state.getActiveModeConfigStorage());

                config = mode.getPreviewConfig(config, context);

                ModeRenderer renderer = mode.getRenderer(context);

                if (!Objects.equals(renderer, lastRenderer) && lastRenderer != null) {
                    lastRenderer.reset(lastAnalyzedConfig, lastAnalysis);
                    needsAnalysis = true;
                }

                lastRenderer = renderer;

                if (System.currentTimeMillis() - lastAnalysisMS >= ANALYSIS_INTERVAL_MS) {
                    needsAnalysis = true;
                }

                if (needsAnalysis || lastAnalysis == null || !Objects.equals(config, lastAnalyzedConfig)) {
                    lastAnalysis = (IBuildable) mode.startAnalysis(config, context).get();
                    lastAnalyzedConfig = config;
                    lastAnalysisMS = System.currentTimeMillis();
                    needsAnalysis = false;
                    needsHintDraw = true;
                }

                BoxRenderer.INSTANCE.start();

                lastRenderer.renderOverlay(context, config, lastAnalysis);

                BoxRenderer.INSTANCE.finish();

                if (needsHintDraw) {
                    needsHintDraw = false;
                    MMHintRenderer.INSTANCE.reset();

                    lastRenderer.emitHints(context, config, lastAnalysis);
                }

                return;
            }
        }

        clear();
    }

    private void clear() {
        if (lastRenderer != null) {
            lastRenderer.reset(lastAnalyzedConfig, lastAnalysis);
        }

        lastAnalysisMS = 0;
        lastAnalyzedConfig = null;
        lastRenderer = null;
        lastAnalysis = null;

        needsHintDraw = false;
        needsAnalysis = false;

        MMHintRenderer.INSTANCE.reset();
    }

    @SubscribeEvent
    public void checkPlayerStoppedBuilding(PlayerTickEvent event) {
        if (event.side != Side.CLIENT) return;
        if (event.phase != Phase.END) return;
        if (event.type != Type.PLAYER) return;

        ItemStack inUse = event.player.getActiveItemStack();

        if (inUse != ItemStack.EMPTY) {
            if (inUse.getItem() instanceof ItemMatterManipulator) {
                wasInUse = true;
            }

            return;
        }

        if (wasInUse) {
            markNeedsReanalysis();
            wasInUse = false;
        }
    }

    private static class RenderContextImpl implements ManipulatorRenderingContext {

        public ItemStack manipulator;
        public MMState state;

        public final float partialTickTime;
        public boolean sorted = false;

        public RenderContextImpl(ItemStack manipulator, MMState state, float partialTickTime) {
            this.manipulator = manipulator;
            this.state = state;
            this.partialTickTime = partialTickTime;
        }

        @Override
        public World getWorld() {
            return Minecraft.getMinecraft().world;
        }

        @Override
        public EntityPlayer getRealPlayer() {
            return Minecraft.getMinecraft().player;
        }

        @Override
        public ItemStack getManipulator() {
            return manipulator;
        }

        @Override
        public MMState getState() {
            return state;
        }

        @Override
        public List<BuildFeedback> getSortedFeedback() {
            if (!sorted) {
                EntityPlayer player = getRealPlayer();

                double pX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTickTime;
                double pY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTickTime;
                double pZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTickTime;

                BuildStatusTracker.SORTED_FEEDBACK.sort(Comparator.comparingDouble(f -> f.pos().distanceSqToCenter(pX, pY, pZ)));
            }

            return BuildStatusTracker.SORTED_FEEDBACK;
        }

        @Override
        public Collection<BuildFeedback> getFeedback() {
            return BuildStatusTracker.FEEDBACK.values();
        }

        @Override
        public BuildFeedback getFeedback(BlockPos pos) {
            return BuildStatusTracker.FEEDBACK.get(pos);
        }

        @Override
        public void markNeedsHintDraw() {
            MMRenderer.INSTANCE.needsHintDraw = true;
        }

        @Override
        public void clearHints() {
            MMHintRenderer.INSTANCE.reset();
            MMRenderer.INSTANCE.needsHintDraw = false;
        }

        @Override
        public void drawRulers(BlockPos pos, ImmutableColor color) {
            color.makeActive();
            MMRenderUtils.drawRulers(getRealPlayer(), pos, false, partialTickTime);
        }

        @Override
        public void drawBox(AxisAlignedBB aabb, ImmutableColor color) {
            BoxRenderer.INSTANCE.drawAround(partialTickTime, aabb, color);
        }

        @Override
        public void drawBox(VoxelAABB aabb, ImmutableColor color) {
            BoxRenderer.INSTANCE.drawAround(partialTickTime, aabb.toBoundingBox().grow(0.01, 0.01, 0.01), color);
        }
    }
}
