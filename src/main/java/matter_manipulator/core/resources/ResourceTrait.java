package matter_manipulator.core.resources;

import matter_manipulator.core.resources.ResourceStack.IntResourceStack;
import matter_manipulator.core.resources.ResourceStack.LongResourceStack;

public enum ResourceTrait {
    /// The resource stack implements [IntResourceStack], meaning it cannot support >2.1 billion amounts.
    IntAmount,
    /// The resource stack implements [LongResourceStack], meaning it supports >2.1 billion amounts via a [Long] amount.
    LongAmount,
}
