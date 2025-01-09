package com.recursive_pineapple.matter_manipulator.common.items.manipulator;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.joml.Vector3i;

/**
 * Represents a location in a world.
 * Can probably be improved, but it's not a big problem yet since these aren't meant to be kept around for very
 * long.
 */
public class Location {

    public int worldId;
    public int x, y, z;

    public Location() {

    }

    public Location(int worldId, int x, int y, int z) {
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Location(@Nonnull World world, int x, int y, int z) {
        this(world.provider.dimensionId, x, y, z);
    }

    public Location(@Nonnull World world, Vector3i v) {
        this(world, v.x, v.y, v.z);
    }

    @Override
    public String toString() {
        return String.format("X=%,d Y=%,d Z=%,d", x, y, z);
    }

    public Vector3i toVec() {
        return new Vector3i(x, y, z);
    }

    public boolean isInWorld(@Nonnull World world) {
        return world.provider.dimensionId == worldId;
    }

    public int distanceTo2(Location other) {
        int dx = x - other.x;
        int dy = y - other.y;
        int dz = z - other.z;
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
        World world = Minecraft.getMinecraft().theWorld;

        return world.provider.dimensionId == this.worldId ? world : null;
    }

    public Location offset(ForgeDirection dir) {
        this.x += dir.offsetX;
        this.y += dir.offsetY;
        this.z += dir.offsetZ;
        return this;
    }

    public Location offset(int dx, int dy, int dz) {
        this.x += dx;
        this.y += dy;
        this.z += dz;
        return this;
    }

    public Location clone() {
        return new Location(worldId, x, y, z);
    }

    /**
     * Checks if two locations are compatible (in the same world).
     */
    public static boolean areCompatible(Location a, Location b) {
        if (a == null || b == null) return false;

        if (a.worldId != b.worldId) return false;

        return true;
    }

    /**
     * Checks if three locations are compatible (in the same world).
     */
    public static boolean areCompatible(Location a, Location b, Location c) {
        if (a == null || b == null || c == null) return false;

        if (a.worldId != b.worldId) return false;
        if (a.worldId != c.worldId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + worldId;
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + z;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Location other = (Location) obj;
        if (worldId != other.worldId) return false;
        if (x != other.x) return false;
        if (y != other.y) return false;
        if (z != other.z) return false;
        return true;
    }
}
