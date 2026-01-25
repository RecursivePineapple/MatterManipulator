package matter_manipulator.core.item;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.oredict.OreDictionary;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.Hash.Strategy;
import matter_manipulator.common.utils.MCUtils;
import matter_manipulator.common.utils.hash.Fnv1a32;
import matter_manipulator.common.utils.items.ItemUtils;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.resources.ResourceIdentity.IntResourceIdentity;
import matter_manipulator.core.resources.ResourceIdentity.LongResourceIdentity;
import matter_manipulator.core.resources.ResourceIdentityTrait;
import matter_manipulator.core.resources.item.ItemResourceIdentity;
import matter_manipulator.core.resources.item.ItemStackWrapper;

@SuppressWarnings("unused")
public final class ItemId implements ItemResourceIdentity, IntResourceIdentity, LongResourceIdentity {

    private final Item item;
    private final int meta;
    private final NBTTagCompound tag, caps;

    public ItemId(Item item, int meta, NBTTagCompound tag, NBTTagCompound caps) {
        this.item = item;
        this.meta = meta;
        this.tag = tag;
        this.caps = caps;
    }

    @Override
    public boolean hasTrait(ResourceIdentityTrait trait) {
        return switch (trait) {
            case IntAmount, LongAmount -> true;
            default -> false;
        };
    }

    @Override
    public Localized getName() {
        return null;
    }

    @Override
    public @NotNull Item getItem() {
        return item;
    }

    @Override
    public int getItemMeta() {
        return meta;
    }

    @Override
    public NBTTagCompound getTag() {
        return tag;
    }

    @Override
    public NBTTagCompound getCapTag() {
        return caps;
    }

    @Override
    public ItemStackWrapper createStackInt(int amount) {
        return new ItemStackWrapper(this.toStack(amount));
    }

    @Override
    public BigItemStack createStackLong(long amount) {
        return this.toBigStack(amount);
    }

    /// Matches [ItemStack#writeToNBT(NBTTagCompound)].
    public static ItemId create(NBTTagCompound tag) {
        return new ItemId(
                Item.REGISTRY.getObject(new ResourceLocation(tag.getString("id"))),
                tag.getInteger("Damage"),
                tag.hasKey("tag", NBT.TAG_COMPOUND) ? tag.getCompoundTag("tag").copy() : null,
                tag.hasKey("ForgeCaps", NBT.TAG_COMPOUND) ? tag.getCompoundTag("ForgeCaps").copy() : null);
    }

    public NBTTagCompound write(NBTTagCompound tag) {
        tag.setInteger("id", Item.getIdFromItem(this.item));
        tag.setInteger("Damage", this.meta);
        if (this.tag != null) tag.setTag("tag", this.tag.copy());
        if (this.caps != null) tag.setTag("ForgeCaps", this.caps.copy());

        return tag;
    }

    public static ItemId create(ItemStack stack) {
        return create(stack.getItem(), ItemUtils.getStackMeta(stack), stack.getTagCompound(), ItemUtils.getCapTag(stack));
    }

    public static ItemId create(Item item, int metaData, @Nullable NBTTagCompound tag, @Nullable NBTTagCompound caps) {
        return new ItemId(item, metaData, MCUtils.copy(tag), MCUtils.copy(caps));
    }

    public static ItemId createAsWildcard(ItemStack stack) {
        return new ItemId(stack.getItem(), OreDictionary.WILDCARD_VALUE, stack.getTagCompound(), ItemUtils.getCapTag(stack));
    }

    public static ItemId createAsWildcardWithoutNBT(ItemStack stack) {
        return create(stack.getItem(), OreDictionary.WILDCARD_VALUE, null, null);
    }

    public static ItemId createWithoutNBT(ItemStack stack) {
        return new ItemId(stack.getItem(), ItemUtils.getStackMeta(stack), null, null);
    }

    public static ItemId createNoCopy(Item item, int metaData, @Nullable NBTTagCompound nbt, @Nullable NBTTagCompound caps) {
        return new ItemId(item, metaData, nbt, caps);
    }

    public static ItemId createNoCopy(ItemStack stack) {
        return new ItemId(stack.getItem(), ItemUtils.getStackMeta(stack), stack.getTagCompound(), ItemUtils.getCapTag(stack));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ItemId itemId)) return false;

        return ITEM_META_NBT_STRATEGY.equals(this, itemId);
    }

    @Override
    public int hashCode() {
        return ITEM_META_NBT_STRATEGY.hashCode(this);
    }

    @Override
    public String toString() {
        return "ItemId[" + "item=" + item + ", " + "meta=" + meta + ", " + "tag=" + tag + ']';
    }

    /**
     * A hash strategy that only checks the item and metadata.
     */
    public static final Strategy<ItemId> ITEM_META_STRATEGY = new Strategy<>() {

        @Override
        public int hashCode(ItemId o) {
            int hash = Fnv1a32.initialState();

            if (o != null) {
                hash = Fnv1a32.hashStep(hash, Objects.hashCode(o.item));
                hash = Fnv1a32.hashStep(hash, o.meta);
            }

            return hash;
        }

        @Override
        public boolean equals(ItemId a, ItemId b) {
            if (a == b) return true;
            if (a == null || b == null) return false;

            return a.getItem() == b.getItem() && a.meta == b.meta;
        }
    };

    /**
     * A hash strategy that checks the item, metadata, and nbt.
     */
    public static final Strategy<ItemId> ITEM_META_NBT_STRATEGY = new Strategy<>() {

        @Override
        public int hashCode(ItemId o) {
            int hash = Fnv1a32.initialState();

            if (o != null) {
                hash = Fnv1a32.hashStep(hash, Objects.hashCode(o.item));
                hash = Fnv1a32.hashStep(hash, o.meta);
                hash = Fnv1a32.hashStep(hash, Objects.hashCode(o.tag));
            }

            return hash;
        }

        @Override
        public boolean equals(ItemId a, ItemId b) {
            if (a == b) return true;
            if (a == null || b == null) return false;

            return a.item == b.item && a.meta == b.meta && Objects.equals(a.tag, b.tag);
        }
    };

    /**
     * A hash strategy that only checks the item and metadata.
     */
    public static final Strategy<ItemStack> STACK_ITEM_META_STRATEGY = new Strategy<>() {

        @Override
        public int hashCode(ItemStack o) {
            int hash = Fnv1a32.initialState();

            if (o != null) {
                hash = Fnv1a32.hashStep(hash, Objects.hashCode(o.getItem()));
                hash = Fnv1a32.hashStep(hash, ItemUtils.getStackMeta(o));
            }

            return hash;
        }

        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            if (a == b) return true;
            if (a == null || b == null) return false;

            if (a.getItem() != b.getItem()) return false;
            return ItemUtils.getStackMeta(a) == ItemUtils.getStackMeta(b);
        }
    };

    /**
     * A hash strategy that checks the item, metadata, and nbt.
     */
    public static final Strategy<ItemStack> STACK_ITEM_META_NBT_STRATEGY = new Strategy<>() {

        @Override
        public int hashCode(ItemStack o) {
            int hash = Fnv1a32.initialState();

            if (o != null) {
                hash = Fnv1a32.hashStep(hash, Objects.hashCode(o.getItem()));
                hash = Fnv1a32.hashStep(hash, ItemUtils.getStackMeta(o));
                hash = Fnv1a32.hashStep(hash, Objects.hashCode(o.getTagCompound()));
            }

            return hash;
        }

        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            if (a == b) return true;
            if (a == null || b == null) return false;

            if (a.getItem() != b.getItem()) return false;
            if (ItemUtils.getStackMeta(a) != ItemUtils.getStackMeta(b)) return false;
            return Objects.equals(a.getTagCompound(), b.getTagCompound());
        }
    };

    private static Item getGenericItem(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Item item) return item;
        if (obj instanceof ItemStack stack) return stack.getItem();
        // Includes ImmutableItemStack and ItemId
        if (obj instanceof ImmutableItemMeta im) return im.getItem();

        throw new IllegalArgumentException("Cannot extract item from object: " + obj);
    }

    private static int getGenericMeta(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof ItemStack stack) return ItemUtils.getStackMeta(stack);
        // Includes ImmutableItemStack and ItemId
        if (obj instanceof ImmutableItemMeta im) return im.getItemMeta();

        throw new IllegalArgumentException("Cannot extract item metadata from object: " + obj);
    }

    private static NBTTagCompound getGenericTag(Object obj) {
        if (obj == null) return null;
        if (obj instanceof ItemStack stack) return stack.getTagCompound();
        // Includes ItemId
        if (obj instanceof ImmutableItemStack stack) return stack.getTag();

        throw new IllegalArgumentException("Cannot extract item metadata from object: " + obj);
    }

    /// A hash strategy that only checks the item and metadata. Works with [ItemStack], [ItemId], [ImmutableItemMeta],
    /// and [ImmutableItemStack] - equivalent objects that represent the same 'stack' will have the same hash and equal
    /// each other.
    public static final Strategy<Object> GENERIC_ITEM_META_STRATEGY = new Strategy<>() {

        @Override
        public int hashCode(Object o) {
            int hash = Fnv1a32.initialState();

            if (o != null) {
                hash = Fnv1a32.hashStep(hash, Objects.hashCode(getGenericItem(o)));
                hash = Fnv1a32.hashStep(hash, getGenericMeta(o));
            }

            return hash;
        }

        @Override
        public boolean equals(Object a, Object b) {
            if (a == b) return true;
            if (a == null || b == null) return false;

            if (getGenericItem(a) != getGenericItem(b)) return false;
            return getGenericMeta(a) == getGenericMeta(b);
        }
    };

    /// A hash strategy that checks the item, metadata, and tag. Works with [ItemStack], [ItemId], [ImmutableItemMeta],
    /// and [ImmutableItemStack] - equivalent objects that represent the same 'stack' will have the same hash and equal
    /// each other.
    public static final Strategy<Object> GENERIC_ITEM_META_NBT_STRATEGY = new Strategy<>() {

        @Override
        public int hashCode(Object o) {
            int hash = Fnv1a32.initialState();

            if (o != null) {
                hash = Fnv1a32.hashStep(hash, Objects.hashCode(getGenericItem(o)));
                hash = Fnv1a32.hashStep(hash, getGenericMeta(o));
                hash = Fnv1a32.hashStep(hash, Objects.hashCode(getGenericTag(o)));
            }

            return hash;
        }

        @Override
        public boolean equals(Object a, Object b) {
            if (a == b) return true;
            if (a == null || b == null) return false;

            if (getGenericItem(a) != getGenericItem(b)) return false;
            if (getGenericMeta(a) == getGenericMeta(b)) return false;
            return Objects.equals(getGenericTag(a), getGenericTag(b));
        }
    };
}
