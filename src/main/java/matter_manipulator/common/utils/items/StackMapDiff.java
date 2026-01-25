package matter_manipulator.common.utils.items;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import matter_manipulator.core.item.ItemId;

public class StackMapDiff {

    public Object2LongOpenHashMap<ItemId> added = new Object2LongOpenHashMap<>();
    public Object2LongOpenHashMap<ItemId> removed = new Object2LongOpenHashMap<>();
}
