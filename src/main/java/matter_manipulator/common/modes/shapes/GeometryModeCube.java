package matter_manipulator.common.modes.shapes;

import java.util.ArrayList;

import net.minecraft.world.World;

import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.building.PendingBlock;

public class GeometryModeCube {

    public static ArrayList<PendingBlock> iterateCube(
        GeometryBlockPalette palette,
        World world,
        int minX,
        int minY,
        int minZ,
        int maxX,
        int maxY,
        int maxZ
    ) {
        ArrayList<PendingBlock> blocks = new ArrayList<>();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    int insideCount = 0;

                    if (x > minX && x < maxX) insideCount++;
                    if (y > minY && y < maxY) insideCount++;
                    if (z > minZ && z < maxZ) insideCount++;

                    IBlockSpec spec = switch (insideCount) {
                        case 0 -> palette.corners();
                        case 1 -> palette.edges();
                        case 2 -> palette.faces();
                        case 3 -> palette.volumes();
                        default -> IBlockSpec.AIR;
                    };

                    blocks.add(new PendingBlock(world, x, y, z, spec));
                }
            }
        }

        return blocks;
    }

}
