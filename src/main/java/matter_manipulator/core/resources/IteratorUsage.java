package matter_manipulator.core.resources;

/// A hint for iterators, so that they can elide pointless operations. Note that specifying one usage hint, then doing
/// another operation (insert usage, with an extract operation) is undefined behaviour. All iterators must support
/// inventory polling regardless of this hint.
public enum IteratorUsage {
    /// The iterator will be used for insertions, extractions, and polling.
    Both,
    /// The iterator will only be used for extractions and polling.
    Extract,
    /// The iterator will only be used for insertions and polling.
    Insert,
    /// No extractions or insertions will be done - the iterator will only be used to check the contents of the
    /// inventory.
    None;
}
