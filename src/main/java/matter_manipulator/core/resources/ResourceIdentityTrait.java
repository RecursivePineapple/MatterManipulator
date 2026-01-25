package matter_manipulator.core.resources;

import matter_manipulator.core.resources.ResourceIdentity.IntResourceIdentity;
import matter_manipulator.core.resources.ResourceIdentity.LongResourceIdentity;

public enum ResourceIdentityTrait {
    /// The resource identity implements [IntResourceIdentity], meaning it cannot create stacks with >2.1 billion amounts.
    IntAmount,
    /// The resource identity implements [LongResourceIdentity], meaning it can create stacks with >2.1 billion amounts via a [Long] amount.
    LongAmount,
}
