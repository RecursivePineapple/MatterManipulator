package matter_manipulator.core.block_spec;

public enum ApplyResult {
    DidNothing,
    NotApplicable,
    /// Did an action that could be performed by a wrench (rotated, etc). Will show wrench particles.
    Wrenched,
    /// Did a generic action (will play an enderman portal sound).
    DidSomething,
    /// The operation was valid, but failed for some reason (insufficient resources, illegal block location, etc).
    Retry,
    /// An unrecoverable error occured and the block is in an undefined state. No further operations will be performed.
    Error,
    ;
}
