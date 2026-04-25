package matter_manipulator.core.analysis;

public enum ApplyMode {
    /// Update the inventory as normal.
    NORMAL,
    /// Update the inventory, but do not insert or extract items. Useful for phantom inventories.
    NO_IO,
    /// Mock updating the inventory, but don't actually modify it. Extracts and inserts items. Respects any existing
    /// items.
    SIMULATE_EXISTING,
    /// Mock updating the inventory, but don't actually modify it. Extracts and inserts items. Pretends existing items
    /// (in the inventory) do not exist.
    SIMULATE_EMPTY;

    public boolean doIO() {
        return this != NO_IO;
    }

    public boolean isSimulate() {
        return this == SIMULATE_EXISTING || this == SIMULATE_EMPTY;
    }

    public boolean clearExisting() {
        return this != SIMULATE_EMPTY;
    }
}
