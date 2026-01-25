package matter_manipulator.common.utils;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeHooks;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import matter_manipulator.common.utils.math.Location;

public class MathUtils {

    public static int clamp(int val, int lo, int hi) {
        return Math.max(lo, Math.min(hi, val));
    }

    public static long clamp(long val, long lo, long hi) {
        return Math.max(lo, Math.min(hi, val));
    }

    public static float clamp(float val, float lo, float hi) {
        return Math.max(lo, Math.min(hi, val));
    }

    public static double clamp(double val, double lo, double hi) {
        return Math.max(lo, Math.min(hi, val));
    }

    public static int min(int first, int... rest) {
        for (int l : rest) {
            if (l < first) first = l;
        }

        return first;
    }

    public static long min(long first, long... rest) {
        for (long l : rest) {
            if (l < first) first = l;
        }

        return first;
    }

    public static int max(int first, int... rest) {
        for (int l : rest) {
            if (l > first) first = l;
        }

        return first;
    }

    public static long max(long first, long... rest) {
        for (long l : rest) {
            if (l > first) first = l;
        }

        return first;
    }

    public static int ceilDiv(int lhs, int rhs) {
        return (lhs + rhs - 1) / rhs;
    }

    public static int ceilDiv2(int lhs, int rhs) {
        int sign = signum(lhs) * signum(rhs);

        if (lhs == 0) return 0;
        if (rhs == 0) throw new ArithmeticException("/ by zero");

        lhs = Math.abs(lhs);
        rhs = Math.abs(rhs);

        int unsigned = 1 + (lhs - 1) / rhs;

        return unsigned * sign;
    }

    public static long ceilDiv(long lhs, long rhs) {
        return (lhs + rhs - 1) / rhs;
    }

    public static int signum(int i) {
        return Integer.compare(i, 0);
    }

    public static long signum(long x) {
        return x < 0 ? -1 : x > 0 ? 1 : 0;
    }

    public static Vector3i signum(Vector3i v) {
        v.x = signum(v.x);
        v.y = signum(v.y);
        v.z = signum(v.z);

        return v;
    }

    public static long ceilLong(double d) {
        long l = (long) d;
        return d > l ? l + 1 : l;
    }

    public static int longToInt(long l) {
        return l > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) l;
    }

    /**
     * Gets the standard vanilla hit result for a player.
     */
    public static RayTraceResult getHitResult(EntityPlayer player) {
        float reach = (float) player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();

        RayTraceResult hit = ForgeHooks.rayTraceEyes(player, reach + 1);

        return hit != null && hit.typeOfHit != Type.BLOCK ? null : hit;
    }

    public static Vec3d getPosition(EntityPlayer player, float partialTicks) {
        double d0 = player.prevPosX + (player.posX - player.prevPosX) * (double) partialTicks;
        double d1 = player.prevPosY + (player.posY - player.prevPosY) * (double) partialTicks + (player.getEyeHeight() - player.getDefaultEyeHeight());
        double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) partialTicks;
        return new Vec3d(d0, d1, d2);
    }

    /**
     * Gets the 'location' that the player is looking at.
     */
    public static Vector3i getLookedAtBlock(EntityPlayer player) {
        float reach = (float) player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() + 1;

        RayTraceResult hit = ForgeHooks.rayTraceEyes(player, reach);

        Vector3i target;

        if (hit != null && hit.typeOfHit == Type.BLOCK) {
            target = new Vector3i(hit.getBlockPos().getX(), hit.getBlockPos().getY(), hit.getBlockPos().getZ());

            if (!player.isSneaking()) {
                target.add(hit.sideHit.getXOffset(), hit.sideHit.getYOffset(), hit.sideHit.getZOffset());
            }
        } else {
            Vec3d startPos = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
            Vec3d endPos = startPos.add(new Vec3d(player.getLookVec().x * reach, player.getLookVec().y * reach, player.getLookVec().z * reach));

            target = new Vector3i(
                MathHelper.floor(endPos.x),
                MathHelper.floor(endPos.y),
                MathHelper.floor(endPos.z)
            );
        }

        return target;
    }

    /**
     * Calculates the delta x/y/z for the bounding box around a,b.
     * This is useful because a,a + deltas will always represent the same bounding box that's around a,b.
     */
    public static Vector3i getRegionDeltas(Location a, Location b) {
        if (a == null || b == null || a.worldId != b.worldId) return null;

        int x1 = a.x;
        int y1 = a.y;
        int z1 = a.z;
        int x2 = b.x;
        int y2 = b.y;
        int z2 = b.z;

        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        int maxZ = Math.max(z1, z2);

        int dX = (maxX - minX) * (minX < x1 ? -1 : 1);
        int dY = (maxY - minY) * (minY < y1 ? -1 : 1);
        int dZ = (maxZ - minZ) * (minZ < z1 ? -1 : 1);

        return new Vector3i(dX, dY, dZ);
    }

    /**
     * {@link #getRegionDeltas(Location, Location)} but with three params.
     */
    public static Vector3i getRegionDeltas(Location a, Location b, Location c) {
        if (a == null || b == null || c == null || a.worldId != b.worldId || a.worldId != c.worldId) return null;

        Vector3i max = new Vector3i(a).max(b).max(c);
        Vector3i min = new Vector3i(a).min(b).min(c);

        int dX = (max.x - min.x) * (min.x < a.x ? -1 : 1);
        int dY = (max.y - min.y) * (min.y < a.y ? -1 : 1);
        int dZ = (max.z - min.z) * (min.z < a.z ? -1 : 1);

        return new Vector3i(dX, dY, dZ);
    }

    /**
     * Converts deltas to an AABB.
     */
    public static AxisAlignedBB getBoundingBox(Location l, Vector3i deltas) {
        int minX = Math.min(l.x, l.x + deltas.x);
        int minY = Math.min(l.y, l.y + deltas.y);
        int minZ = Math.min(l.z, l.z + deltas.z);
        int maxX = Math.max(l.x, l.x + deltas.x) + 1;
        int maxY = Math.max(l.y, l.y + deltas.y) + 1;
        int maxZ = Math.max(l.z, l.z + deltas.z) + 1;

        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Gets all blocks contained in a bounding box.
     * This can certainly be improved but I couldn't get the Iterator version to work properly and this doesn't seem to
     * be a big problem.
     */
    public static List<Vector3i> getBlocksInBB(Location l, Vector3i deltas) {
        int minX = Math.min(l.x, l.x + deltas.x);
        int minY = Math.min(l.y, l.y + deltas.y);
        int minZ = Math.min(l.z, l.z + deltas.z);
        int maxX = Math.max(l.x, l.x + deltas.x) + 1;
        int maxY = Math.max(l.y, l.y + deltas.y) + 1;
        int maxZ = Math.max(l.z, l.z + deltas.z) + 1;

        int dX = maxX - minX;
        int dY = maxY - minY;
        int dZ = maxZ - minZ;

        List<Vector3i> blocks = new ArrayList<>();

        for (int y = 0; y < dY; y++) {
            for (int z = 0; z < dZ; z++) {
                for (int x = 0; x < dX; x++) {
                    blocks.add(new Vector3i(minX + x, minY + y, minZ + z));
                }
            }
        }

        return blocks;
    }

    public static <T, R> Function<T, R> exposeFieldGetterLambda(Class<? super T> clazz, String srgName) {
        final MethodHandle method = DataUtils.exposeFieldGetter(clazz, srgName);

        return instance -> {
            try {
                //noinspection unchecked
                return (R) method.invoke(instance);
            } catch (Throwable e) {
                throw new RuntimeException("Could not get field " + clazz.getName() + ":" + srgName, e);
            }
        };
    }

    /**
     * Pins a point to the axis planes around an origin.
     *
     * @return The pinned point
     */
    public static Vector3i pinToPlanes(Vector3i origin, Vector3i point) {
        int dX = Math.abs(point.x - origin.x);
        int dY = Math.abs(point.y - origin.y);
        int dZ = Math.abs(point.z - origin.z);

        int shortest = min(dX, dY, dZ);

        if (shortest == dX) {
            return new Vector3i(origin.x, point.y, point.z);
        } else if (shortest == dY) {
            return new Vector3i(point.x, origin.y, point.z);
        } else {
            return new Vector3i(point.x, point.y, origin.z);
        }
    }

    /**
     * Pins a point to the normal of the axis plane described by origin,b.
     *
     * @param origin The origin
     * @param b A point on an axis plane of origin
     * @param point The point to pin
     * @return The pinned point on the normal
     */
    public static Vector3i pinToLine(Vector3i origin, Vector3i b, Vector3i point) {
        return switch (new Vector3i(b).sub(origin).minComponent()) {
            case 0 -> new Vector3i(point.x, origin.y, origin.z);
            case 1 -> new Vector3i(origin.x, point.y, origin.z);
            case 2 -> new Vector3i(origin.x, origin.y, point.z);
            default -> throw new AssertionError();
        };
    }

    /**
     * Pins a point to the cardinal axes.
     */
    public static Vector3i pinToAxes(Vector3i origin, Vector3i point) {
        return switch (new Vector3i(point).sub(origin).maxComponent()) {
            case 0 -> new Vector3i(point.x, origin.y, origin.z);
            case 1 -> new Vector3i(origin.x, point.y, origin.z);
            case 2 -> new Vector3i(origin.x, origin.y, point.z);
            default -> throw new AssertionError();
        };
    }

    public static Vector3i copy(Vector3ic v) {
        return v == null ? null : new Vector3i(v);
    }

    public static Vector3f v(EnumFacing dir) {
        return new Vector3f(dir.getXOffset(), dir.getYOffset(), dir.getZOffset());
    }

    public static EnumFacing vprime(Vector3f dir) {
        return switch (dir.maxComponent()) {
            case 0 -> dir.x > 0 ? EnumFacing.EAST : EnumFacing.WEST;
            case 1 -> dir.y > 0 ? EnumFacing.UP : EnumFacing.DOWN;
            case 2 -> dir.z > 0 ? EnumFacing.SOUTH : EnumFacing.NORTH;
            default -> throw new AssertionError();
        };
    }

    public static Vector3i vi(EnumFacing dir) {
        return new Vector3i(dir.getXOffset(), dir.getYOffset(), dir.getZOffset());
    }

    public static EnumFacing vprime(Vector3i dir) {
        return switch (dir.maxComponent()) {
            case 0 -> dir.x > 0 ? EnumFacing.EAST : EnumFacing.WEST;
            case 1 -> dir.y > 0 ? EnumFacing.UP : EnumFacing.DOWN;
            case 2 -> dir.z > 0 ? EnumFacing.SOUTH : EnumFacing.NORTH;
            default -> throw new AssertionError();
        };
    }
}
