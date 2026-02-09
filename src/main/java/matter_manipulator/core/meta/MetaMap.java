package matter_manipulator.core.meta;

import java.util.HashMap;
import java.util.Optional;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
public class MetaMap implements MetadataContainer {

    private final HashMap<MetaKey, Object> values = new HashMap<>();

    private static final Object MISSING = new Object();

    @Nullable
    @Override
    public <T> T getMetaValue(MetaKey<T> key) {
        Object value = values.getOrDefault(key, MISSING);

        // Hack so that we don't have to do an extra map operation to check if the key was missing
        if (value == MISSING) {
            value = null;

            Optional def = key.getDefault();

            if (def.isPresent()) {
                value = def.get();
                values.put(key, value);
            }
        }

        return key.cast(value);
    }

    @Nullable
    @Override
    public <T> T getRequiredMetaValue(MetaKey<T> key) {
        Object value = values.getOrDefault(key, MISSING);

        // Hack so that we don't have to do an extra map operation to check if the key was missing
        if (value == MISSING) {
            value = null;

            Optional def = key.getDefault();

            if (!def.isPresent()) {
                throw new IllegalStateException("Expected meta key " + key + " to be present, but it was not");
            }

            value = def.get();
            values.put(key, value);
        }

        return key.cast(value);
    }

    @Override
    public boolean containsMetaValue(MetaKey<?> key) {
        return values.containsKey(key);
    }

    @Override
    public <T> T removeMetaValue(MetaKey<T> key) {
        return key.cast(values.remove(key));
    }

    @Override
    public <T> void putMetaValue(MetaKey<T> key, T value) {
        values.put(key, value);
    }
}
