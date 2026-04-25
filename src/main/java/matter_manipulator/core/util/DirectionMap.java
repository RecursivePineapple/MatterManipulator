package matter_manipulator.core.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import matter_manipulator.common.utils.DataUtils;

@SuppressWarnings("unchecked")
public class DirectionMap<T> extends AbstractMap<@Nullable EnumFacing, @NotNull T> {

    private final Object[] values = new Object[7];

    private SlowSet slowSet;
    private FastSet fastSet;

    @Override
    public @NotNull Set<Map.Entry<@Nullable EnumFacing, T>> entrySet() {
        if (slowSet == null) slowSet = new SlowSet();
        return slowSet;
    }

    public @NotNull Set<Map.Entry<@Nullable EnumFacing, T>> fastEntrySet() {
        if (fastSet == null) fastSet = new FastSet();
        return fastSet;
    }

    @Override
    public T get(Object key) {
        return (T) values[index((EnumFacing) key)];
    }

    @Override
    public T put(@Nullable EnumFacing key, T value) {
        final int index = index(key);
        final T old = (T) values[index];
        values[index] = value;
        return old;
    }

    @Override
    public T remove(Object key) {
        return put((EnumFacing) key, null);
    }

    @Override
    public boolean remove(Object key, Object value) {
        final int index = index((EnumFacing) key);
        if (Objects.equals(values[index], value)) {
            values[index] = null;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void forEach(BiConsumer<? super @Nullable EnumFacing, ? super T> action) {
        for (int index = 0; index < this.values.length; index++) {
            T value = (T) values[index];

            if (value == null) continue;

            action.accept(key(index), value);
        }
    }

    public void transform(Function<EnumFacing, EnumFacing> transform) {
        Object[] out = new Object[6];

        for (int i = 0; i < 6; i++) {
            out[index(transform.apply(key(i)))] = values[i];
        }

        System.arraycopy(out, 0, values, 0, 6);
    }

    public static int index(@Nullable EnumFacing key) {
        if (key == null) return 6;

        return key.ordinal();
    }

    public static EnumFacing key(int index) {
        if (index == 6) return null;

        return EnumFacing.VALUES[index];
    }

    private class Entry implements Map.Entry<@Nullable EnumFacing, T> {

        private EnumFacing key;

        public Entry(EnumFacing key) {
            this.key = key;
        }

        @Override
        public @Nullable EnumFacing getKey() {
            return key;
        }

        @Override
        public T getValue() {
            return (T) values[index(key)];
        }

        @Override
        public T setValue(T value) {
            final int index = index(key);
            final T old = (T) values[index];
            values[index] = value;
            return old;
        }
    }

    private class SlowSet extends AbstractSet<Map.Entry<EnumFacing, T>> {

        @Override
        public @NotNull Iterator<Map.Entry<EnumFacing, T>> iterator() {
            return new Iterator<>() {

                private int index;

                @Override
                public boolean hasNext() {
                    return index < 7;
                }

                @Override
                public Map.Entry<EnumFacing, T> next() {
                    if (!hasNext()) throw new NoSuchElementException();

                    return new DirectionMap<T>.Entry(key(index++));
                }

                @Override
                public void remove() {
                    values[index - 1] = null;
                }
            };
        }

        @Override
        public int size() {
            return DataUtils.countNonNulls(values);
        }
    }

    private class FastSet extends AbstractSet<Map.Entry<EnumFacing, T>> {

        private final DirectionMap<T>.Entry entry = new DirectionMap<T>.Entry(null);

        @Override
        public @NotNull Iterator<Map.Entry<EnumFacing, T>> iterator() {
            return new Iterator<>() {

                private int index;

                @Override
                public boolean hasNext() {
                    return index < 7;
                }

                @Override
                public Map.Entry<EnumFacing, T> next() {
                    if (!hasNext()) throw new NoSuchElementException();

                    entry.key = key(index++);

                    return entry;
                }

                @Override
                public void remove() {
                    values[index - 1] = null;
                }
            };
        }

        @Override
        public int size() {
            return DataUtils.countNonNulls(values);
        }
    }
}
