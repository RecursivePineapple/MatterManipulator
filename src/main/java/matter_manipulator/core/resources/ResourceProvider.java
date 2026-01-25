package matter_manipulator.core.resources;

/// This is a minimal interface for resource I/O. Implementations are free to add extra methods to
/// subclasses/subinterfaces.
/// All operations performed on this interface or subclasses/subinterfaces must be atomic, and must be immediately
/// affect the world to avoid duplication or state sync glitches.
public interface ResourceProvider {

    /// Returns the factory that created this provider.
    ResourceProviderFactory<?> getFactory();

    /// Fallibly extract several sets of a resource from this provider. Each individual operation must be atomic, but
    /// the overarching extraction should be performed separately. That is, extracting 3 stacks of 4 stone must extract
    /// 0, 4, 8, or 12 stone. Implementations are free to merge requests to make this method call O(1), but care should
    /// be taken to avoid voiding or duplicating resources.
    long extract(ResourceStack request);

    /// Inserts a resource into this provider. This operation should accept as much of the stack as possible. Any extra
    /// will be voided due to the opaque nature of the stack.
    boolean insert(ResourceStack stack);
}
