package matter_manipulator.common.utils.math;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.joml.Vector3i;
import org.joml.Vector3ic;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a location in a world.
 * Can probably be improved, but it's not a big problem yet since these aren't meant to be kept around for very
 * long.
 */
@EqualsAndHashCode(callSuper = true)
public class Location extends Vector3i {

    @Getter
    @Setter
    public int worldId;

    public Location() {

    }

    public Location(int worldId, int x, int y, int z) {
        super(x, y, z);
        this.worldId = worldId;
    }

    public Location(@Nonnull World world, int x, int y, int z) {
        this(world.provider.getDimension(), x, y, z);
    }

    public Location(@Nonnull World world, Vector3i v) {
        this(world, v.x, v.y, v.z);
    }

    @Override
    public String toString() {
        return String.format("X=%,d Y=%,d Z=%,d", x, y, z);
    }

    public BlockPos toPos() {
        return new BlockPos(x, y, z);
    }

    public boolean isInWorld(@Nonnull World world) {
        return world.provider.getDimension() == worldId;
    }

    public int distanceTo2(Vector3ic other) {
        int dx = x - other.x();
        int dy = y - other.y();
        int dz = z - other.z();
        return dx * dx + dy * dy + dz * dz;
    }

    public double distanceTo(Location other) {
        return Math.sqrt(distanceTo2(other));
    }

    public World getWorld() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return getWorldClient();
        } else {
            return DimensionManager.getWorld(this.worldId);
        }
    }

    @SideOnly(Side.CLIENT)
    private World getWorldClient() {
        World world = Minecraft.getMinecraft().world;

        return world.provider.getDimension() == this.worldId ? world : null;
    }

    public Location offset(EnumFacing dir) {
        this.x += dir.getXOffset();
        this.y += dir.getYOffset();
        this.z += dir.getZOffset();
        return this;
    }

    public Location offset(int dx, int dy, int dz) {
        this.x += dx;
        this.y += dy;
        this.z += dz;
        return this;
    }

    public int getChunkX() {
        return x >> 4;
    }

    public int getChunkY() {
        return y >> 4;
    }

    public int getChunkZ() {
        return z >> 4;
    }

    public Location clone() {
        return new Location(worldId, x, y, z);
    }

    /**
     * Checks if two locations are compatible (in the same world).
     */
    public static boolean areCompatible(Location a, Location b) {
        if (a == null || b == null) return false;

        return a.worldId == b.worldId;
    }

    /**
     * Checks if three locations are compatible (in the same world).
     */
    public static boolean areCompatible(Location a, Location b, Location c) {
        if (a == null || b == null || c == null) return false;

        if (a.worldId != b.worldId) return false;
        return a.worldId == c.worldId;
    }
}
