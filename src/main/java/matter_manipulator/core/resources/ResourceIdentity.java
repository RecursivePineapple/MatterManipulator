package matter_manipulator.core.resources;

import matter_manipulator.core.i18n.Localized;

/// This must be implemented on an immutable class. It must contain all identifying information for its matching
/// [ResourceStack]. This includes fields like the item, the stack meta, any NBT, etc. It can be used to recreate a
/// [ResourceStack] exactly, given a matching amount.
/// <p />
/// This must properly implement [Object#equals(Object)] and [Object#hashCode()] because it may be used as the key in a
/// map.
public interface ResourceIdentity {

    /// Checks if this identity behaves a certain way.
    boolean hasTrait(ResourceIdentityTrait trait);

    Localized getName();

    /// The stack has an integer amount field.
    interface IntResourceIdentity extends ResourceIdentity {
        ResourceStack createStackInt(int amount);
    }

    /// The stack has a long amount field.
    interface LongResourceIdentity extends ResourceIdentity {
        ResourceStack createStackLong(long amount);
    }
}
