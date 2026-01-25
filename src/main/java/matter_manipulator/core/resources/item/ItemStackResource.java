package matter_manipulator.core.resources.item;

import matter_manipulator.core.resources.Resource;

public class ItemStackResource implements Resource<ItemStackResourceProvider> {

    public static final ItemStackResource ITEMS = new ItemStackResource();

    private ItemStackResource() { }

}
