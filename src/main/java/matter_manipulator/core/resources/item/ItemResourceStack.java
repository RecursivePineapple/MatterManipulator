package matter_manipulator.core.resources.item;

import matter_manipulator.core.item.ItemId;
import matter_manipulator.core.item.ItemStackLike;
import matter_manipulator.core.resources.ResourceStack;

public interface ItemResourceStack extends ResourceStack, ItemStackLike {

    @Override
    default ItemResource getResource() {
        return ItemResource.ITEMS;
    }

    @Override
    default ItemId getIdentity() {
        return ItemId.create(getItem(), getItemMeta(), getTag(), getCapTag());
    }

    @Override
    ItemResourceStack emptyCopy();
}
