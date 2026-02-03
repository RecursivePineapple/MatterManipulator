package matter_manipulator.core.resources;

import org.jetbrains.annotations.NotNull;

import matter_manipulator.core.i18n.Localized;

/// A resource. This should be implemented on the stack class itself. For existing classes, this interface should be
/// implemented on a wrapper class.
/// <p />
/// The core interface does not include any stack size methods. This is handled by [IntResourceStack] and
/// [LongResourceStack]. One of these interfaces must be implemented for a stack class, but it is valid to implement
/// both at the same time. Note that calling the methods on these interfaces without first calling
/// `hasTrait(ResourceTrait.IntAmount)` or `hasTrait(ResourceTrait.LongAmount)` is undefined behaviour and may cause
/// exceptions to be thrown. The trait system exists so that consumers of the API do not have to infer how the stack
/// behaves by inspecting its super classes.
public interface ResourceStack {

    /// Checks if this stack behaves a certain way.
    boolean hasTrait(ResourceTrait trait);

    /// Gets the [Resource] for this stack.
    Resource<?> getResource();

    @NotNull
    Localized getName();

    /// Gets an immutable object that contains all identifying information for this stack. See [ResourceIdentity]'s
    /// header comment for more info.
    ResourceIdentity getIdentity();

    /// Checks if the identity of this stack is the same as the provided stack.
    boolean isSameType(ResourceStack other);

    /// Creates a copy of this stack with an amount of zero
    ResourceStack emptyCopy();

    default ResourceStack multipliedCopy(int mult) {
        ResourceStack out = emptyCopy();

        if (hasTrait(ResourceTrait.LongAmount)) {
            // Prefer longs since they have a lower chance of overflowing
            ((LongResourceStack) out).setAmountLong(((LongResourceStack) this).getAmountLong() * mult);
        } else if (hasTrait(ResourceTrait.IntAmount)) {
            ((IntResourceStack) out).setAmountInt(((IntResourceStack) this).getAmountInt() * mult);
        } else {
            throw new IllegalStateException("Resource " + this + " must have either the IntAmount or LongAmount resource trait.");
        }

        return out;
    }

    /// Checks if this stack contains an invalid resource, or its amount is <= 0.
    boolean isEmpty();

    /// The stack has an `int` amount field. The stack can store any amount between 0 to [Integer#MAX_VALUE]
    /// (inclusive). Implementations must clamp negative values to zero.
    interface IntResourceStack extends ResourceStack {
        int getAmountInt();
        void setAmountInt(int amount);
    }

    /// The stack has a `long` amount field. The stack can store any amount between 0 to [Long#MAX_VALUE]
    /// (inclusive). Implementations must clamp negative values to zero.
    interface LongResourceStack extends ResourceStack {
        long getAmountLong();
        void setAmountLong(long amount);
    }
}
