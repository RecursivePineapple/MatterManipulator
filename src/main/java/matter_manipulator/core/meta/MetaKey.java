package matter_manipulator.core.meta;

import java.util.Optional;

/// A metadata key. This is used as the key in a map, so it must implement [#equals(Object)] and [#hashCode()].
/// Singleton metadata keys can elide these since the default implementation works fine in this scenario.
public interface MetaKey<T> {

    default T cast(Object value) {
        //noinspection unchecked
        return (T) value;
    }

    /// Gets the default value if it does not exist. This value should be a new reference, as it is immediately inserted
    /// into the [MetadataContainer].
    /// @return The default value, or [Optional#empty()] if this key does not have one.
    default Optional<T> getDefault() {
        return Optional.empty();
    }
}
