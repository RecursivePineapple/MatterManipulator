package matter_manipulator.common.modes.shapes;

import static matter_manipulator.common.utils.MathUtils.signum;

import java.util.ArrayList;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import org.joml.Vector3i;

import matter_manipulator.common.utils.MathUtils;
import matter_manipulator.core.building.PendingBlock;

public class GeometryModeCylinder {


    public static ArrayList<PendingBlock> iterateCylinder(GeometryBlockPalette palette, World world, Vector3i posA, Vector3i posB, Vector3i posC) {
        Vector3i b2 = MathUtils.pinToPlanes(posA, posB);
        Vector3i height = MathUtils.pinToLine(posA, b2, posC).sub(posA);

        Vector3i delta = new Vector3i(b2).sub(posA);

        delta.x += signum(delta.x);
        delta.y += signum(delta.y);
        delta.z += signum(delta.z);

        // The deltas for each dimension (A/B/Height)
        int dA = 0, dB = 0, dH = 0;
        // Used to determine the final block position
        Vector3i vecA, vecB, vecH;

        // Calculate the delta vectors for each axis
        // This is kinda cursed and I don't really understand it anymore, so good luck changing it
        switch (delta.minComponent()) {
            case 0 -> {
                dA = delta.y;
                dB = delta.z;
                dH = height.x;
                vecA = new Vector3i(0, signum(delta.y), 0);
                vecB = new Vector3i(0, 0, signum(delta.z));
                vecH = new Vector3i(signum(height.x), 0, 0);
            }
            case 1 -> {
                dA = delta.x;
                dB = delta.z;
                dH = height.y;
                vecA = new Vector3i(signum(delta.x), 0, 0);
                vecB = new Vector3i(0, 0, signum(delta.z));
                vecH = new Vector3i(0, signum(height.y), 0);
            }
            case 2 -> {
                dA = delta.x;
                dB = delta.y;
                dH = height.z;
                vecA = new Vector3i(signum(delta.x), 0, 0);
                vecB = new Vector3i(0, signum(delta.y), 0);
                vecH = new Vector3i(0, 0, signum(height.z));
            }
            default -> {
                throw new AssertionError();
            }
        }

        int absA = Math.abs(dA);
        int absB = Math.abs(dB);
        int absH = Math.abs(dH) + 1; // I have no idea why this +1 is needed

        float rA = absA / 2f;
        float rB = absB / 2f;

        boolean[][][] present = new boolean[absA + 2][absH + 2][absB + 2];

        ArrayList<PendingBlock> blocks = new ArrayList<>();

        // Generate the blocks in A,B,H space
        // At this point, x=A, z=B, and y=H
        for (int a = 0; a < absA; a++) {
            for (int b = 0; b < absB; b++) {
                double distance = Math.pow((a - rA + 0.5) / rA, 2.0) + Math.pow((b - rB + 0.5) / rB, 2.0);

                if (distance <= 1) {
                    for (int h = 0; h < absH; h++) {
                        PendingBlock block = new PendingBlock(world, a, h, b, palette.volumes());

                        present[a + 1][h + 1][b + 1] = true;
                        blocks.add(block);
                    }
                }
            }
        }

        // Check the adjacent blocks for each block and determine whether the block should be a volume, edge, or face
        for (PendingBlock block : blocks) {
            int adj = 0;

            for (EnumFacing dir : EnumFacing.VALUES) {
                if (present[block.x + 1 + dir.getXOffset()][block.y + 1 + dir.getYOffset()][block.z + 1 + dir.getZOffset()]) {
                    adj |= 1 << dir.ordinal();
                }
            }

            // I know this looks :ConcerningRead: but this is just an easy way to check which blocks are adjacent to
            // this one

            // If this block is missing an adjacent block, it's not a volume
            if (adj != 0b111111) {
                // If this block is missing one of the N/S/E/W blocks, it's an edge (the surface)
                if ((adj & 0b111100) == 0b111100) {
                    block.spec = palette.edges();
                } else {
                    // Otherwise, it's a face (top & bottom)
                    block.spec = palette.faces();
                }
            }
        }

        // Transform the positions of each block from relative A,B,H space into absolute X,Y,Z space
        for (PendingBlock block : blocks) {
            int a = block.x, b = block.z, h = block.y;

            // Why, yes, that is an integer matrix
            block.x = a * vecA.x + b * vecB.x + h * vecH.x + posA.x;
            block.y = a * vecA.y + b * vecB.y + h * vecH.y + posA.y;
            block.z = a * vecA.z + b * vecB.z + h * vecH.z + posA.z;
        }

        return blocks;
    }
}
