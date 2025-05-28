package com.recursive_pineapple.matter_manipulator.common.building;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
        return getObjectId() == null || getBlock() == null || getBlock() == Blocks.air;
    }

    default boolean shouldBeSkipped() {
        return InteropConstants.shouldBeSkipped(getBlock(), getBlockMeta());
    }

    void getItemDetails(List<String> details);

    static Comparator<ImmutableBlockSpec> getComparator() {
        Comparator<UniqueIdentifier> blockId = Comparator.nullsFirst(
            Comparator.comparing((UniqueIdentifier id) -> id.modId)
                .thenComparing(id -> id.name)
        );

        return Comparator.nullsFirst(Comparator.comparing(ImmutableBlockSpec::getObjectId, blockId)).thenComparingInt(ImmutableBlockSpec::getMeta);
    }
}
