package matter_manipulator.core.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.joml.Vector3i;

import com.github.bsideup.jabel.Desugar;
import matter_manipulator.GlobalMMConfig.DebugConfig;
import matter_manipulator.MMMod;
import matter_manipulator.common.block_spec.StandardBlockSpec;
import matter_manipulator.common.context.AnalysisContextImpl;
import matter_manipulator.common.utils.MathUtils;
import matter_manipulator.common.utils.math.Location;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.building.PendingBlock;
import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.util.Coroutine;

public class BlockAnalyzer {

    @Desugar
    public record RegionAnalysis(Vector3i deltas, List<PendingBlock> blocks) { }

    public static Coroutine<RegionAnalysis> analyzeRegion(ManipulatorContext context, Location a, Location b) {
        World world = context.getWorld();

        if (!Location.areCompatible(a, b) || !a.isInWorld(world)) {
            return Coroutine.finished(new RegionAnalysis(new Vector3i(), Collections.emptyList()));
        }

        long pre = DebugConfig.debug ? System.nanoTime() : 0;

        Vector3i deltas = MathUtils.getRegionDeltas(a, b);

        List<PendingBlock> analysis = new ArrayList<>();

        Iterator<BlockPos> locations = MathUtils.getBlocksInBB(a, deltas).iterator();

        AnalysisContextImpl analysisContext = new AnalysisContextImpl(context);

        return ctx -> {
            int i = 0;

            while (locations.hasNext()) {
                if (i++ % 10 == 0 && ctx.shouldYield()) return;

                var pos = locations.next();

                analysisContext.setPos(pos);

                IBlockSpec spec = StandardBlockSpec.fromWorld(analysisContext);

                analysis.add(new PendingBlock(world, pos.getX() - a.x, pos.getY() - a.y, pos.getZ() - a.z, spec));
            }

            if (DebugConfig.debug) {
                long post = System.nanoTime();

                MMMod.LOG.info("Analysis took {} ms", (post - pre) / 1e6);
            }

            ctx.stop(new RegionAnalysis(deltas, analysis));
        };
    }
}
