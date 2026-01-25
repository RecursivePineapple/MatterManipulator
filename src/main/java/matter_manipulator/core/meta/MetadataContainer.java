package matter_manipulator.core.meta;

import javax.annotation.Nullable;

public interface MetadataContainer {

    /// Gets the metadata value for the given key, or null if it does not exist.
    @Nullable
    <T> T getMetaValue(MetaKey<T> key);

    /// Gets the metadata value for the given key. If this container does not have an entry for the key, and the key
    /// does not have a default value, this method will throw an [IllegalStateException].
    /// This method may return null, if that value is stored in the container.
    @Nullable
    <T> T getRequiredMetaValue(MetaKey<T> key);

    /// Checks if this container contains an entry for the given key. Does not return true if the entry is missing but
    /// the key has a default value.
    boolean containsMetaValue(MetaKey<?> key);

    /// Removes the entry for the given key and returns it, if one is present in the container. Does not return true if
    /// the entry is missing but the key has a default value.
    <T> T removeMetaValue(MetaKey<T> key);

    /// Inserts a value into this metadata container. The value is strongly referenced and will not be deallocated.
    <T> void put(MetaKey<T> key, T value);
}
