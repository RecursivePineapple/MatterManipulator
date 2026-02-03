package matter_manipulator.common.building;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import matter_manipulator.MMMod;
import matter_manipulator.common.block_spec.StandardBlockSpec;
import matter_manipulator.common.context.AnalysisContextImpl;
import matter_manipulator.common.items.ItemMatterManipulator;
import matter_manipulator.common.networking.SoundResource;
import matter_manipulator.common.utils.MCUtils;
import matter_manipulator.common.utils.world.ProxiedWorld;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.building.IPendingBlockBuildable;
import matter_manipulator.core.building.PendingBlock;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.resources.ResourceStack;

public class StandardBuild implements IPendingBlockBuildable {

    public final ArrayDeque<PendingBlock> pendingBlocks;
    private final ObjectOpenHashSet<BlockPos> visited = new ObjectOpenHashSet<>();
    private boolean done = false;

    public StandardBuild(ArrayDeque<PendingBlock> pendingBlocks) {
        this.pendingBlocks = pendingBlocks;
    }

    @Override
    public List<PendingBlock> getPendingBlocks() {
        return new ArrayList<>(pendingBlocks);
    }

    @Override
    public void onBuildTick(BlockPlacingContext context) {
        int placeSpeed = context.getPlaceSpeed();

        Integer lastChunkX = null, lastChunkZ = null;
        int shuffleCount = 0;

        World world = context.getWorld();
        ProxiedWorld proxiedWorld = new ProxiedWorld(world);

        ArrayDeque<PendingBlock> toPlace = new ArrayDeque<>();

        AnalysisContextImpl analysisContext = new AnalysisContextImpl(context);

        // check every pending block that's left
        while (toPlace.size() < placeSpeed && !pendingBlocks.isEmpty()) {
            PendingBlock next = pendingBlocks.getFirst();

            int x = next.x, y = next.y, z = next.z;
            BlockPos pos = next.toPos();

            context.setTarget(pos, next.spec);

            if (!next.spec.isValid()) {
                pendingBlocks.removeFirst();
                context.error(new Localized("mm.info.error.unplaceable_block", next.spec.getBlockState().toString()));
                continue;
            }

            int chunkX = x >> 4;
            int chunkZ = z >> 4;

            // if this block's chunk isn't loaded, ignore it completely
            if (!Objects.equals(chunkX, lastChunkX) || !Objects.equals(chunkZ, lastChunkZ)) {
                if (world.getChunkProvider().getLoadedChunk(chunkX, chunkZ) == null) {
                    pendingBlocks.removeFirst();
                    continue;
                } else {
                    lastChunkX = chunkX;
                    lastChunkZ = chunkZ;
                }
            }

            if (world.isOutsideBuildHeight(pos)) {
                pendingBlocks.removeFirst();
                continue;
            }

            // If this block is protected, ignore it completely and print a warning because it'll
//            if (!isEditable(world, x, y, z)) {
//                pendingBlocks.removeFirst();
//                continue;
//            }

            // if this block is different from the last one, stop checking blocks
            // since the pending blocks are sorted by their contained block, this is usually true
            if (!toPlace.isEmpty()) {
                ResourceStack firstResource = toPlace.getFirst().spec.getResource();
                ResourceStack nextResource = next.spec.getResource();

                if (!firstResource.isSameType(nextResource)) break;
            }

            analysisContext.setPos(pos);
            StandardBlockSpec existing = StandardBlockSpec.fromWorld(analysisContext);

            if (next.spec.isAir() && world.isAirBlock(pos)) {
                pendingBlocks.removeFirst();
                continue;
            }

            // Check if the existing block is removable
//            boolean canPlace = switch (state.config.removeMode) {
//                case NONE -> existing.getBlock().isAir(world, x, y, z);
//                case REPLACEABLE -> existing.getBlock().isReplaceable(world, x, y, z);
//                case ALL -> true;
//            };

            boolean canPlace = existing.getBlockState().getBlockHardness(world, pos) >= 0;

            // We don't want to remove these even though they'll never be placed because we want to see how many blocks
            // couldn't be placed
            if (!canPlace) {
                pendingBlocks.addLast(pendingBlocks.removeFirst());
                shuffleCount++;

                if (shuffleCount > pendingBlocks.size()) {
                    break;
                } else {
                    continue;
                }
            }

            // if there's an existing block then remove it if possible
            if (!world.isAirBlock(pos)) {
                if (!context.hasCapability(ItemMatterManipulator.ALLOW_REMOVING)) {
                    pendingBlocks.removeFirst();
                    continue;
                }
            }

            proxiedWorld.overrides.clear();
            proxiedWorld.setBlockState(pos, Blocks.AIR.getDefaultState());

            // Check block dependencies for things like levers
            // If we can't place this block, shuffle it to the back of the list
            if (!next.spec.getBlockState().getBlock().canPlaceBlockAt(proxiedWorld, pos)) {
                pendingBlocks.addLast(pendingBlocks.removeFirst());
                shuffleCount++;

                // if we've shuffled every block, then we'll never be able to place any of them
                if (shuffleCount > pendingBlocks.size()) {
                    break;
                } else {
                    continue;
                }
            }

            if (!visited.add(pos)) {
                MMMod.LOG.warn("Tried to place block twice! {}", next);
                pendingBlocks.removeFirst();
                continue;
            }

            toPlace.add(next);
            pendingBlocks.remove();
        }

        if (toPlace.isEmpty()) {
            if (pendingBlocks.isEmpty()) {
                MCUtils.sendInfoToPlayer(context.getRealPlayer(), MCUtils.translate("mm.info.finished_placing"));
            } else {
                MCUtils.sendErrorToPlayer(context.getRealPlayer(), MCUtils.translate("mm.info.error.could_not_place", pendingBlocks.size()));
            }

            this.done = true;

            return;
        }

        while (!toPlace.isEmpty()) {
            PendingBlock pendingBlock = toPlace.getFirst();

            BlockPos pos = pendingBlock.toPos();

            context.setTarget(pos, pendingBlock.spec);

            analysisContext.setPos(pos);
            IBlockSpec existing = StandardBlockSpec.fromWorld(analysisContext);

            if (!existing.isAir()) {
                ResourceStack pendingResource = pendingBlock.spec.getResource();
                ResourceStack existingResource = existing.getResource();

                if (!pendingResource.isSameType(existingResource)) {
                    context.removeBlock();

                    if (!world.isAirBlock(pos)) {
                        pendingBlocks.add(pendingBlock);
                        visited.remove(pos);

                        context.error(new Localized("mm.info.error.could_not_remove"));
                        continue;
                    }
                }
            }

            EnumSet<ApplyResult> result = EnumSet.noneOf(ApplyResult.class);

            if (world.isAirBlock(pos)) {
                result.add(pendingBlock.spec.place(context));
            }

            if (!result.contains(ApplyResult.Error)) {
                result.addAll(pendingBlock.spec.update(context));
            }

            if (result.contains(ApplyResult.DidSomething)) {
                context.playSound(SoundResource.MOB_ENDERMEN_PORTAL);
            }

            if (result.contains(ApplyResult.Retry)) {
                pendingBlocks.add(pendingBlock);
                visited.remove(pos);
            }

            toPlace.removeFirst();
        }

        for (PendingBlock pendingBlock : toPlace) {
            pendingBlocks.add(pendingBlock);
            visited.remove(pendingBlock.toPos());
        }
    }

    @Override
    public void onStop(BlockPlacingContext context) {

    }

    @Override
    public boolean isDone() {
        return done;
    }
}
