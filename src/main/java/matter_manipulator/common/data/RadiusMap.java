package matter_manipulator.common.data;

import java.util.HashSet;

import matter_manipulator.common.data.RadiusMap.ValueList;
import matter_manipulator.common.utils.MathUtils;

public class RadiusMap<T extends ObjectWithRange & ObjectWithPosition> extends ChunkMap<ValueList<T>> {

    protected static class ValueList<T> extends HashSet<T> {
        public ValueList(int x, int z) {

        }
    }

    public void add(T obj) {
        int blockX = obj.getBlockX();
        int blockZ = obj.getBlockZ();
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        int radius = (obj.getBlockRadius() >> 4) + 1;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                ValueList<T> values = computeIfAbsent(chunkX + x, chunkZ + z, ValueList::new);

                values.add(obj);
            }
        }
    }

    public void remove(T obj) {
        int blockX = obj.getBlockX();
        int blockZ = obj.getBlockZ();
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        int radius = (obj.getBlockRadius() >> 4) + 1;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                ValueList<T> values = get(chunkX + x, chunkZ + z);

                if (values == null) continue;

                values.remove(obj);

                if (values.isEmpty()) {
                    remove(chunkX + x, chunkZ + z);
                }
            }
        }
    }

    public void get(int x, int y, int z, HashSet<T> values) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        ValueList<T> list = get(chunkX, chunkZ);

        if (list == null) return;

        if (list.isEmpty()) {
            remove(chunkX, chunkZ);
            return;
        }

        for (T value : list) {
            double radius2 = value.getBlockRadius();
            radius2 *= radius2;

            double dist2 = MathUtils.dot2(x - value.getBlockX(), y - value.getBlockY(), z - value.getBlockZ());

            if (dist2 > radius2) continue;

            values.add(value);
        }
    }
}
