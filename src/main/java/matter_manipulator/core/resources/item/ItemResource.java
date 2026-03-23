package matter_manipulator.core.resources.item;

import matter_manipulator.core.resources.Resource;

public class ItemResource implements Resource<ItemResourceProvider> {

    public static final ItemResource ITEMS = new ItemResource();

    private ItemResource() { }

}
