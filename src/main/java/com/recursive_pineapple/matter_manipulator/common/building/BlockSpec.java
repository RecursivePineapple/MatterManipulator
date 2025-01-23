package com.recursive_pineapple.matter_manipulator.common.building;

import static com.recursive_pineapple.matter_manipulator.common.utils.MMValues.W;
import static com.recursive_pineapple.matter_manipulator.common.utils.Mods.ArchitectureCraft;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

import com.google.gson.annotations.SerializedName;
import com.recursive_pineapple.matter_manipulator.common.compat.BlockProperty;
import com.recursive_pineapple.matter_manipulator.common.compat.BlockPropertyRegistry;
import com.recursive_pineapple.matter_manipulator.common.utils.ItemId;
import com.recursive_pineapple.matter_manipulator.common.utils.LazyBlock;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods.Names;

public class BlockSpec implements ImmutableBlockSpec {

    public static final UniqueIdentifier AIR_ID = new UniqueIdentifier("minecraft:air");

    @SerializedName("b")
    private boolean isBlock;
    @SerializedName("id")
    private UniqueIdentifier objectId;

    /**
     * The item metadata.
     * Block => item: {@link Block#damageDropped(int)}
     * Item => block: {@link Item#getMetadata(int)}
     */
    @SerializedName("m")
    private int metadata;

    @SerializedName("p")
    public Map<CopyableProperty, String> properties;

    @com.recursive_pineapple.matter_manipulator.asm.Optional(Names.ARCHITECTURE_CRAFT)
    @SerializedName("a")
    public ArchitectureCraftAnalysisResult arch;

    private transient Block block;
    private transient Optional<Item> item;
    private transient Optional<ItemId> itemId;
    private transient Optional<ItemStack> stack;

    public BlockSpec() {
        reset();
    }

    private BlockSpec reset() {
        isBlock = true;
        objectId = AIR_ID;
        metadata = 0;
        properties = null;
        block = null;
        item = null;
        itemId = null;
        stack = null;
        if (ArchitectureCraft.isModLoaded()) arch = null;

        return this;
    }

    public BlockSpec setObject(Block block, int meta) {
        reset();
        this.isBlock = true;
        this.objectId = GameRegistry.findUniqueIdentifierFor(block);
        this.metadata = block.damageDropped(meta);
        this.block = block;

        return this;
    }

    public BlockSpec setObject(ItemStack stack) {
        reset();
        this.isBlock = false;
        this.objectId = GameRegistry.findUniqueIdentifierFor(stack.getItem());
        this.metadata = Items.feather.getDamage(stack);

        return this;
    }

    public BlockSpec setObject(Item item, int meta) {
        reset();
        this.isBlock = false;
        this.objectId = GameRegistry.findUniqueIdentifierFor(item);
        this.metadata = meta;

        return this;
    }

    public BlockSpec populate() {
        if (isBlock) {
            block = GameRegistry.findBlock(objectId.modId, objectId.name);
            item = Optional.ofNullable(MMUtils.getItemFromBlock(block, metadata));
        } else {
            item = Optional.of(GameRegistry.findItem(objectId.modId, objectId.name));
            block = MMUtils.getBlockFromItem(item.get(), item.get().getMetadata(metadata));
        }

        return this;
    }

    @Override
    public UniqueIdentifier getObjectId() {
        return objectId;
    }

    @Override
    public Block getBlock() {
        if (block == null) populate();

        return block;
    }

    @Override
    public Item getItem() {
        if (item == null) populate();

        return item.orElse(null);
    }

    @Override
    public ItemId getItemId() {
        if (itemId == null) {
            if (item == null) populate();

            if (item.isPresent()) {
                itemId = Optional.of(ItemId.create(item.get(), metadata, null));
            } else {
                itemId = Optional.empty();
            }
        }

        return itemId.orElse(null);
    }

    @Override
    public int getMeta() {
        return metadata;
    }

    private static final LazyBlock DYSON_CASING = new LazyBlock(Mods.GalaxySpace, "dysonswarmparts", W);

    @Override
    public int getBlockMeta() {
        if (DYSON_CASING.isLoaded() && DYSON_CASING.get().getBlock() == this.getBlock()) return metadata;

        return getItem() == null ? 0 : getItem().getMetadata(getMeta());
    }

    private static final MethodHandle CREATE_STACKED_BLOCK = MMUtils
        .exposeMethod(Block.class, MethodType.methodType(ItemStack.class, int.class), "createStackedBlock", "func_149644_j", "j");

    @Override
    public ItemStack getStack() {
        if (this.stack == null) {
            ItemStack stack = null;

            if (isBlock) {
                try {
                    stack = (ItemStack) CREATE_STACKED_BLOCK.invoke(getBlock(), metadata);
                } catch (Throwable t) {
                    throw new RuntimeException("Could not invoke " + CREATE_STACKED_BLOCK, t);
                }
            }

            if (stack == null || stack.getItem() == null) {
                Item item = getItem();

                if (item == null) {
                    this.stack = Optional.empty();
                    return null;
                }

                if (item.getHasSubtypes()) {
                    stack = new ItemStack(item, 1, metadata);
                } else {
                    stack = new ItemStack(item, 1, 0);
                }
            }

            if (stack != null && stack.getItem() != null) {
                this.stack = Optional.of(stack);
            } else {
                this.stack = Optional.empty();
            }

            if (this.stack.isPresent()) {
                NBTTagCompound tag = new NBTTagCompound();

                if (ArchitectureCraft.isModLoaded() && arch != null) arch.getItemTag(tag);

                this.stack.get().setTagCompound(tag.hasNoTags() ? null : tag);
            }
        }

        return ItemStack.copyItemStack(this.stack.orElse(null));
    }

    @Override
    public PendingBlock instantiate(int worldId, int x, int y, int z) {
        PendingBlock pendingBlock = new PendingBlock(worldId, x, y, z, this);

        if (ArchitectureCraft.isModLoaded() && arch != null) {
            pendingBlock.arch = arch.clone();
        }

        return pendingBlock;
    }

    public BlockSpec clone() {
        BlockSpec dup = new BlockSpec();

        dup.isBlock = isBlock;
        dup.objectId = objectId;
        dup.metadata = metadata;
        dup.properties = properties;
        dup.block = block;
        dup.item = item;
        dup.itemId = itemId;
        dup.stack = stack;
        if (ArchitectureCraft.isModLoaded()) dup.arch = arch == null ? null : arch.clone();

        return dup;
    }

    @Override
    public String getProperty(CopyableProperty property) {
        if (properties == null) return null;

        return properties.get(property);
    }

    @Override
    public BlockSpec withProperties(Map<CopyableProperty, String> properties) {
        if (Objects.equals(properties, this.properties)) return this;

        BlockSpec spec = clone();

        spec.properties = properties == null || properties.isEmpty() ? null : new EnumMap<>(properties);

        return spec;
    }

    private String getItemDetails() {
        List<String> details = new ArrayList<>(0);

        if (ArchitectureCraft.isModLoaded() && arch != null) arch.getItemDetails(details);

        return details.isEmpty() ? "" : String.format(" (%s)", String.join(", ", details));
    }

    public String getDisplayName() {
        return (getStack() == null ? Blocks.air.getLocalizedName() : getStack().getDisplayName()) + getItemDetails();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isBlock ? 1231 : 1237);
        result = prime * result + ((objectId == null) ? 0 : objectId.hashCode());
        result = prime * result + metadata;
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        if (ArchitectureCraft.isModLoaded()) {
            result = prime * result + ((arch == null) ? 0 : arch.hashCode());
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BlockSpec other = (BlockSpec) obj;
        if (isBlock != other.isBlock) return false;
        if (objectId == null) {
            if (other.objectId != null) return false;
        } else if (!objectId.equals(other.objectId)) return false;
        if (metadata != other.metadata) return false;
        if (properties == null) {
            if (other.properties != null) return false;
        } else if (!properties.equals(other.properties)) return false;
        if (ArchitectureCraft.isModLoaded()) {
            if (arch == null) {
                if (other.arch != null) return false;
            } else if (!arch.equals(other.arch)) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "BlockSpec [isBlock=" + isBlock
            + ", objectId="
            + objectId
            + ", metadata="
            + metadata
            + ", properties="
            + properties
            + (ArchitectureCraft.isModLoaded() ? ", arch=" + arch : "")
            + ", block="
            + block
            + ", item="
            + item
            + ", itemId="
            + itemId
            + ", stack="
            + stack
            + "]";
    }

    public static boolean contains(Collection<BlockSpec> specs, BlockSpec spec) {
        for (BlockSpec candidate : specs) {
            if (candidate != null && candidate.equals(spec)) return true;
        }

        return false;
    }

    public static boolean contains(Collection<BlockSpec> specs, ItemStack stack) {
        for (BlockSpec candidate : specs) {
            if (candidate != null && candidate.matches(stack)) return true;
        }

        return false;
    }

    public static BlockSpec fromPickBlock(World world, EntityPlayer player, MovingObjectPosition hit) {
        if (hit == null || hit.typeOfHit != MovingObjectType.BLOCK) return new BlockSpec();

        return fromBlock(null, world, hit.blockX, hit.blockY, hit.blockZ);
    }

    public static BlockSpec fromBlock(BlockSpec pooled, World world, int x, int y, int z) {
        return fromBlock(pooled, world, x, y, z, world.getBlock(x, y, z), world.getBlockMetadata(x, y, z));
    }

    public static BlockSpec fromBlock(BlockSpec pooled, World world, int x, int y, int z, Block block, int blockMeta) {
        BlockSpec spec = pooled != null ? pooled.reset() : new BlockSpec();

        spec.isBlock = true;
        spec.objectId = GameRegistry.findUniqueIdentifierFor(block);
        spec.block = block;

        if (!MMUtils.isFree(block, blockMeta)) {
            @Nullable
            Item item = MMUtils.getItemFromBlock(block, blockMeta);

            if (item == null) { return new BlockSpec().setObject(Blocks.air, 0); }

            if (block != Blocks.wall_sign && block != Blocks.standing_sign) {
                block = MMUtils.getBlockFromItem(item, item.getMetadata(blockMeta));
            }

            int itemMeta = block.getDamageValue(world, x, y, z);

            spec.metadata = itemMeta;
            spec.item = Optional.ofNullable(item);
            spec.itemId = item == null ? Optional.empty() : Optional.of(ItemId.create(item, itemMeta, null));
        } else {
            spec.metadata = 0;
            spec.item = Optional.empty();
            spec.itemId = Optional.empty();
            spec.stack = Optional.empty();
        }

        if (Mods.ArchitectureCraft.isModLoaded()) {
            spec.arch = ArchitectureCraftAnalysisResult.analyze(world.getTileEntity(x, y, z));
        }

        Map<String, BlockProperty<?>> properties = new HashMap<>();
        BlockPropertyRegistry.getProperties(world, x, y, z, properties);

        if (!properties.isEmpty()) {
            EnumMap<CopyableProperty, String> values = new EnumMap<>(CopyableProperty.class);

            for (CopyableProperty name : CopyableProperty.VALUES) {
                BlockProperty<?> property = properties.get(name.toString());

                if (property == null) continue;

                values.put(name, property.getValueAsString(world, x, y, z));
            }

            if (!values.isEmpty()) spec.properties = values;
        }

        return spec;
    }

    public static final ImmutableBlockSpec AIR = air();

    public static BlockSpec air() {
        BlockSpec spec = new BlockSpec();

        spec.block = Blocks.air;
        spec.item = Optional.empty();
        spec.itemId = Optional.empty();
        spec.stack = Optional.empty();

        return spec;
    }

    public static ImmutableBlockSpec choose(List<BlockSpec> specs, Random rng) {
        if (specs == null || specs.isEmpty()) return AIR;

        ImmutableBlockSpec spec = MMUtils.choose(specs, rng);

        return spec == null ? AIR : spec;
    }
}
