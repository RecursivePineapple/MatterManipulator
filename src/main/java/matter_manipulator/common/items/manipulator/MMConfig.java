package com.recursive_pineapple.matter_manipulator.common.items.manipulator;

import javax.annotation.Nullable;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import com.recursive_pineapple.matter_manipulator.common.building.BlockSpec;
import com.recursive_pineapple.matter_manipulator.common.data.WeightedSpecList;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState.BlockRemoveMode;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState.BlockSelectMode;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState.PendingAction;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState.PlaceMode;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState.Shape;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class MMConfig {

    public PendingAction action;
    public BlockSelectMode blockSelectMode = BlockSelectMode.ALL;
    public BlockRemoveMode removeMode = BlockRemoveMode.ALL;
    public PlaceMode placeMode = PlaceMode.GEOMETRY;
    public Shape shape = Shape.LINE;

    public Location coordA, coordB, coordC;
    // these are used to determine which blocks are being moved
    // if any are non-null, then the corresponding block is being moved
    public Vector3i coordAOffset, coordBOffset, coordCOffset;

    public WeightedSpecList corners = new WeightedSpecList(BlockSpec.air());
    public WeightedSpecList edges = new WeightedSpecList(BlockSpec.air());
    public WeightedSpecList faces = new WeightedSpecList(BlockSpec.air());
    public WeightedSpecList volumes = new WeightedSpecList(BlockSpec.air());
    public BlockSpec cables = BlockSpec.air();

    /** These blocks are what gets removed when exchanging */
    public WeightedSpecList replaceWhitelist = new WeightedSpecList(BlockSpec.air());
    /** These blocks are what gets placed when exchanging */
    public WeightedSpecList replaceWith = new WeightedSpecList(BlockSpec.air());

    @Nullable
    public Transform transform;
    /** The array size in repetitions */
    public Vector3i arraySpan;

    public Location getCoordA(World world, Vector3i lookingAt) {
        if (coordAOffset == null) {
            return coordA;
        } else {
            return new Location(world, new Vector3i(lookingAt).add(coordAOffset));
        }
    }

    public Location getCoordB(World world, Vector3i lookingAt) {
        if (coordBOffset == null) {
            return coordB;
        } else {
            return new Location(world, new Vector3i(lookingAt).add(coordBOffset));
        }
    }

    public Location getCoordC(World world, Vector3i lookingAt) {
        if (coordCOffset == null) {
            return coordC;
        } else {
            return new Location(world, new Vector3i(lookingAt).add(coordCOffset));
        }
    }

    public static class VoxelAABB {

        public Vector3i origin, a, b;

        public VoxelAABB() {
            origin = new Vector3i();
            a = new Vector3i();
            b = new Vector3i();
        }

        public VoxelAABB(Vector3i a, Vector3i b) {
            this.origin = new Vector3i(a);
            this.a = new Vector3i(a);
            this.b = new Vector3i(b);
        }

        public Vector3i min() {
            return new Vector3i(a).min(b);
        }

        public Vector3i max() {
            return new Vector3i(a).max(b);
        }

        public VoxelAABB union(Vector3i v) {
            Vector3i min = min(), max = max();

            a.set(v)
                .min(min);
            b.set(v)
                .max(max);

            return this;
        }

        public VoxelAABB union(VoxelAABB other) {
            Vector3i min = min(), max = max();

            a.set(min)
                .min(other.min());
            b.set(max)
                .max(other.max());

            return this;
        }

        public VoxelAABB moveOrigin(Vector3i newOrigin) {
            b.sub(origin).add(newOrigin);
            a.sub(origin).add(newOrigin);
            origin.set(newOrigin);

            return this;
        }

        public VoxelAABB scale(int x, int y, int z) {
            int dirX = b.x < a.x ? -1 : 1;
            int dirY = b.y < a.y ? -1 : 1;
            int dirZ = b.z < a.z ? -1 : 1;

            Vector3i size = size();

            size.mul(x, y, z);
            size.mul(dirX, dirY, dirZ);
            size.add(origin);

            VoxelAABB other = clone();

            other.moveOrigin(size);

            union(other);

            return this;
        }

        public VoxelAABB clone() {
            VoxelAABB dup = new VoxelAABB();
            dup.origin = new Vector3i(origin);
            dup.a = new Vector3i(a);
            dup.b = new Vector3i(b);
            return dup;
        }

        public Vector3i span() {
            Vector3i min = min(), max = max();

            return new Vector3i(max.x - min.x, max.y - min.y, max.z - min.z);
        }

        public Vector3i size() {
            Vector3i min = min(), max = max();

            return new Vector3i(max.x - min.x + 1, max.y - min.y + 1, max.z - min.z + 1);
        }

        public AxisAlignedBB toBoundingBox() {
            Vector3i min = min(), max = max();

            return AxisAlignedBB.getBoundingBox(min.x, min.y, min.z, max.x + 1, max.y + 1, max.z + 1);
        }

        public String describe() {
            Vector3i size = size();

            return String.format(
                "dX=%,d dY=%,d dZ=%,d V=%,d",
                Math.abs(size.x),
                Math.abs(size.y),
                Math.abs(size.z),
                size.x * size.y * size.z
            );
        }
    }

    public Vector3i getArrayMult(
        World world,
        Location sourceA,
        Location sourceB,
        Location dest,
        Vector3i lookingAt
    ) {
        if (!Location.areCompatible(sourceA, sourceB, dest)) return new Vector3i(1);

        Vector3i array = new Vector3i(lookingAt).sub(dest.toVec());

        Vector3i delta = sourceB.toVec()
            .sub(sourceA.toVec());

        if (transform != null) {
            Vector3f v2 = new Vector3f(array).mulTransposeDirection(new Matrix4f(transform.getRotation()).invert());

            array.x = Math.round(v2.x);
            array.y = Math.round(v2.y);
            array.z = Math.round(v2.z);
        }

        array.x = delta.x == 0 ? array.x : Math.floorDiv(array.x, delta.x + (delta.x < 0 ? -1 : 1));
        array.y = delta.y == 0 ? array.y : Math.floorDiv(array.y, delta.y + (delta.y < 0 ? -1 : 1));
        array.z = delta.z == 0 ? array.z : Math.floorDiv(array.z, delta.z + (delta.z < 0 ? -1 : 1));

        return array;
    }

    public VoxelAABB getPasteVisualDeltas(World world, boolean transform) {
        if (!Location.areCompatible(coordA, coordB, coordC)) return null;
        if (world != null && coordA.worldId != world.provider.dimensionId) return null;

        VoxelAABB aabb = new VoxelAABB(coordA.toVec(), coordB.toVec());

        aabb.moveOrigin(coordC.toVec());

        if (arraySpan != null) {
            aabb.scale(arraySpan.x, arraySpan.y, arraySpan.z);
        }

        if (transform && this.transform != null) {
            this.transform.apply(aabb);
        }

        return aabb;
    }

    public VoxelAABB getCopyVisualDeltas(World world) {
        if (!Location.areCompatible(coordA, coordB)) return null;
        if (world != null && coordA.worldId != world.provider.dimensionId) return null;

        return new VoxelAABB(coordA.toVec(), coordB.toVec());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + ((blockSelectMode == null) ? 0 : blockSelectMode.hashCode());
        result = prime * result + ((removeMode == null) ? 0 : removeMode.hashCode());
        result = prime * result + ((placeMode == null) ? 0 : placeMode.hashCode());
        result = prime * result + ((shape == null) ? 0 : shape.hashCode());
        result = prime * result + ((coordA == null) ? 0 : coordA.hashCode());
        result = prime * result + ((coordB == null) ? 0 : coordB.hashCode());
        result = prime * result + ((coordC == null) ? 0 : coordC.hashCode());
        result = prime * result + ((coordAOffset == null) ? 0 : coordAOffset.hashCode());
        result = prime * result + ((coordBOffset == null) ? 0 : coordBOffset.hashCode());
        result = prime * result + ((coordCOffset == null) ? 0 : coordCOffset.hashCode());
        result = prime * result + ((corners == null) ? 0 : corners.hashCode());
        result = prime * result + ((edges == null) ? 0 : edges.hashCode());
        result = prime * result + ((faces == null) ? 0 : faces.hashCode());
        result = prime * result + ((volumes == null) ? 0 : volumes.hashCode());
        result = prime * result + ((cables == null) ? 0 : cables.hashCode());
        result = prime * result + ((replaceWhitelist == null) ? 0 : replaceWhitelist.hashCode());
        result = prime * result + ((replaceWith == null) ? 0 : replaceWith.hashCode());
        result = prime * result + ((transform == null) ? 0 : transform.hashCode());
        result = prime * result + ((arraySpan == null) ? 0 : arraySpan.hashCode());
        return result;
    }

    @SuppressWarnings("null")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        MMConfig other = (MMConfig) obj;
        if (action != other.action) return false;
        if (blockSelectMode != other.blockSelectMode) return false;
        if (removeMode != other.removeMode) return false;
        if (placeMode != other.placeMode) return false;
        if (shape != other.shape) return false;
        if (coordA == null) {
            if (other.coordA != null) return false;
        } else if (!coordA.equals(other.coordA)) return false;
        if (coordB == null) {
            if (other.coordB != null) return false;
        } else if (!coordB.equals(other.coordB)) return false;
        if (coordC == null) {
            if (other.coordC != null) return false;
        } else if (!coordC.equals(other.coordC)) return false;
        if (coordAOffset == null) {
            if (other.coordAOffset != null) return false;
        } else if (!coordAOffset.equals(other.coordAOffset)) return false;
        if (coordBOffset == null) {
            if (other.coordBOffset != null) return false;
        } else if (!coordBOffset.equals(other.coordBOffset)) return false;
        if (coordCOffset == null) {
            if (other.coordCOffset != null) return false;
        } else if (!coordCOffset.equals(other.coordCOffset)) return false;
        if (corners == null) {
            if (other.corners != null) return false;
        } else if (!corners.equals(other.corners)) return false;
        if (edges == null) {
            if (other.edges != null) return false;
        } else if (!edges.equals(other.edges)) return false;
        if (faces == null) {
            if (other.faces != null) return false;
        } else if (!faces.equals(other.faces)) return false;
        if (volumes == null) {
            if (other.volumes != null) return false;
        } else if (!volumes.equals(other.volumes)) return false;
        if (cables == null) {
            if (other.cables != null) return false;
        } else if (!cables.equals(other.cables)) return false;
        if (replaceWhitelist == null) {
            if (other.replaceWhitelist != null) return false;
        } else if (!replaceWhitelist.equals(other.replaceWhitelist)) return false;
        if (replaceWith == null) {
            if (other.replaceWith != null) return false;
        } else if (!replaceWith.equals(other.replaceWith)) return false;
        if (transform == null) {
            if (other.transform != null) return false;
        } else if (!transform.equals(other.transform)) return false;
        if (arraySpan == null) {
            if (other.arraySpan != null) return false;
        } else if (!arraySpan.equals(other.arraySpan)) return false;
        return true;
    }
}
