package matter_manipulator.core.resources;

/// This is a minimal interface for resource I/O. Implementations are free to add extra methods to
/// subclasses/subinterfaces for more granular or optimized operations.
/// All operations performed on this interface or subclasses/subinterfaces must immediately affect the world to avoid
/// duplication, voiding, or state sync glitches.
public interface ResourceProvider<R extends ResourceStack> {

    /// Returns the factory that created this provider.
    ResourceProviderFactory<?> getFactory();

    /// Checks if the following stack could be extracted. Invalid setups (such as looped AE storage subnets) that report
    /// more items than are present are considered user error. False positives from this method are allowed for this
    /// reason.
    /// False positives should be avoided if possible as this method is used to determine if a block/part/etc can be
    /// immediately swapped with another block/part/etc. In this use-case, false positives will cause the existing block
    /// to be removed without a replacement, which cause user-facing operations to misbehave (i.e. setups will have
    /// random blocks/parts/etc removed).
    boolean canExtract(R request);

    /// Fallibly extracts a stack of a resource from this provider. Returns the extracted stack. May return partial
    /// stacks.
    R extract(R request);

    /// Inserts a resource into this provider. Returns anything that was not inserted.
    R insert(R stack);
}
