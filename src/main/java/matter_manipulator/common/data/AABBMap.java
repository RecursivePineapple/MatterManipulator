package matter_manipulator.common.data;

import java.util.HashSet;
import java.util.function.Consumer;

import matter_manipulator.common.data.AABBMap.ValueList;
import matter_manipulator.common.utils.math.VoxelAABB;

public class AABBMap<T extends ObjectWithBoundingBox> extends ChunkMap<ValueList<T>> {

    protected static class ValueList<T> extends HashSet<T> {
        public ValueList(int x, int z) {

        }
    }

    public void add(T obj) {
        VoxelAABB aabb = obj.getBoundingBox();

        int chunkX1 = aabb.minX() >> 4;
        int chunkZ1 = aabb.minZ() >> 4;

        int chunkX2 = aabb.maxX() >> 4;
        int chunkZ2 = aabb.maxZ() >> 4;

        for (int x = chunkX1; x <= chunkX2; x++) {
            for (int z = chunkZ1; z <= chunkZ2; z++) {
                ValueList<T> values = computeIfAbsent(x, z, ValueList::new);

                values.add(obj);
            }
        }
    }

    public void remove(T obj) {
        VoxelAABB aabb = obj.getBoundingBox();

        int chunkX1 = aabb.minX() >> 4;
        int chunkZ1 = aabb.minZ() >> 4;

        int chunkX2 = aabb.maxX() >> 4;
        int chunkZ2 = aabb.maxZ() >> 4;

        for (int x = chunkX1; x <= chunkX2; x++) {
            for (int z = chunkZ1; z <= chunkZ2; z++) {
                ValueList<T> values = get(x, z);

                if (values == null) continue;

                values.remove(obj);

                if (values.isEmpty()) {
                    remove(x, z);
                }
            }
        }
    }

    public void get(int x, int y, int z, Consumer<T> values) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        ValueList<T> list = get(chunkX, chunkZ);

        if (list == null) return;

        if (list.isEmpty()) {
            remove(chunkX, chunkZ);
            return;
        }

        for (T value : list) {
            if (value.getBoundingBox().contains(x, y, z)) {
                values.accept(value);
            }
        }
    }
}
