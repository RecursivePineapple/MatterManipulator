package matter_manipulator.core.item;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

public class StackMapDiff {

    public Object2LongOpenHashMap<ItemId> added = new Object2LongOpenHashMap<>();
    public Object2LongOpenHashMap<ItemId> removed = new Object2LongOpenHashMap<>();
}
