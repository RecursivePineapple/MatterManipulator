package matter_manipulator.common.modes.shapes;

import java.util.ArrayList;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import matter_manipulator.core.building.PendingBlock;

public class GeometryModeSphere {

    public static ArrayList<PendingBlock> iterateSphere(
        GeometryBlockPalette palette,
        World world,
        int minX,
        int minY,
        int minZ,
        int maxX,
        int maxY,
        int maxZ
    ) {
        int sx = maxX - minX + 1;
        int sy = maxY - minY + 1;
        int sz = maxZ - minZ + 1;

        double rx = sx / 2.0;
        double ry = sy / 2.0;
        double rz = sz / 2.0;

        boolean[][][] present = new boolean[sx + 2][sy + 2][sz + 2];

        ArrayList<PendingBlock> blocks = new ArrayList<>();

        for (int x = 0; x < sx; x++) {
            for (int y = 0; y < sy; y++) {
                for (int z = 0; z < sz; z++) {
                    // the ternaries here check whether the given axis is 1, in which case this is a circle and not a
                    // sphere
                    // spotless:off
                    double distance = Math.sqrt(
                        (rx > 1 ? Math.pow((x - rx + 0.5) / rx, 2.0) : 0) +
                        (ry > 1 ? Math.pow((y - ry + 0.5) / ry, 2.0) : 0) +
                        (rz > 1 ? Math.pow((z - rz + 0.5) / rz, 2.0) : 0)
                    );
                    // spotless:on

                    if (distance <= 1) {
                        PendingBlock block = new PendingBlock(world, x + minX, y + minY, z + minZ, palette.volumes());

                        present[x + 1][y + 1][z + 1] = true;
                        blocks.add(block);
                    }
                }
            }
        }

        ArrayList<EnumFacing> directions = new ArrayList<>();

        if (rx > 1) {
            directions.add(EnumFacing.EAST);
            directions.add(EnumFacing.WEST);
        }

        if (ry > 1) {
            directions.add(EnumFacing.UP);
            directions.add(EnumFacing.DOWN);
        }

        if (rz > 1) {
            directions.add(EnumFacing.NORTH);
            directions.add(EnumFacing.SOUTH);
        }

        for (PendingBlock block : blocks) {
            for (EnumFacing dir : directions) {
                if (!present[block.x - minX + 1 + dir.getXOffset()][block.y - minY + 1 + dir.getYOffset()][block.z - minZ + 1 + dir.getZOffset()]) {
                    block.spec = palette.faces();
                    break;
                }
            }
        }

        return blocks;
    }

}
