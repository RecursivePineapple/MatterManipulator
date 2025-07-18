package com.recursive_pineapple.matter_manipulator.common.building;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

import com.recursive_pineapple.matter_manipulator.common.utils.ImmutableItemMeta;
import com.recursive_pineapple.matter_manipulator.common.utils.ItemId;

public interface ImmutableBlockSpec extends ImmutableItemMeta {

    UniqueIdentifier getObjectId();

    Block getBlock();

    Item getItem();

    ItemId getItemId();

    int getMeta();

    int getBlockMeta();

    ItemStack getStack();

    PendingBlock instantiate(int worldId, int x, int y, int z);

    default PendingBlock instantiate(World world, int x, int y, int z) {
        return instantiate(world.provider.dimensionId, x, y, z);
    }

    String getProperty(CopyableProperty property);

    ImmutableBlockSpec withProperties(Map<CopyableProperty, String> properties);

    default boolean isEquivalent(ImmutableBlockSpec other) {
        return ItemStack.areItemStacksEqual(getStack(), other.getStack());
    }

    /** Returns true when this contains air. BlockSpecs may be air if an invalid block was analyzed. */
    default boolean isAir() {
        return getObjectId() == null || getBlock() == null || InteropConstants.isAir(getBlock(), getBlockMeta());
    }

    default boolean skipWhenCopying() {
        return InteropConstants.skipWhenCopying(getBlock(), getBlockMeta());
    }

    default boolean shouldDropItem() {
        return InteropConstants.shouldDropItem(getBlock(), getBlockMeta());
    }

    default boolean isFree() {
        return InteropConstants.isFree(getBlock(), getBlockMeta());
    }

    void getItemDetails(List<String> details);

    static Comparator<ImmutableBlockSpec> getComparator() {
        return Comparator.comparing(ImmutableBlockSpec::getStack, (a, b) -> {
            if (a == null && b != null) return -1;
            if (a != null && b == null) return 1;
            if (a == null && b == null) return 0;

            assert a.getItem() != null;
            assert b.getItem() != null;

            int result;

            result = String.CASE_INSENSITIVE_ORDER.compare(a.getItem().delegate.name(), b.getItem().delegate.name());
            if (result != 0) return result;

            result = Integer.compare(a.itemDamage, b.itemDamage);
            if (result != 0) return result;

            NBTTagCompound ta = a.getTagCompound();
            NBTTagCompound tb = b.getTagCompound();

            if (ta == null && tb != null) return -1;
            if (ta != null && tb == null) return 1;
            if (ta == null && tb == null) return 0;

            return Integer.compare(ta.hashCode(), tb.hashCode());
        });
    }
}
