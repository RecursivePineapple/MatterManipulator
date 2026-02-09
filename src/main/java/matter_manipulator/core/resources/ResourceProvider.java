package matter_manipulator.core.resources;

/// This is a minimal interface for resource I/O. Implementations are free to add extra methods to
/// subclasses/subinterfaces for more granular or optimized operations.
/// All operations performed on this interface or subclasses/subinterfaces must be atomic, and must be immediately
/// affect the world to avoid duplication, voiding, or state sync glitches.
public interface ResourceProvider {

    /// Returns the factory that created this provider.
    ResourceProviderFactory<?> getFactory();

    /// Checks if the following stack could be extracted. Invalid setups (such as looped AE storage subnets) that report
    /// more items than are present are considered user error. False positives from this method are allowed for this
    /// reason.
    /// False positives should be avoided if possible as this method is used to determine if a block/part/etc can be
    /// immediately swapped with another block/part/etc. In this use-case, false positives will cause the existing block
    /// to be removed without a replacement, which make cause user-facing operations to misbehave (i.e. setups will have
    /// random blocks/parts/etc removed).
    boolean canExtract(ResourceStack request);

    /// Fallibly extracts a stack of a resource from this provider.
    boolean extract(ResourceStack request);

    /// Inserts a resource into this provider. This operation should accept as much of the stack as possible. Any extra
    /// will be voided due to the opaque nature of the stack.
    boolean insert(ResourceStack stack);
}
