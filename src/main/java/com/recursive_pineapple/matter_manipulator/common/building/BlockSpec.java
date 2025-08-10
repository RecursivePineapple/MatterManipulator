package com.recursive_pineapple.matter_manipulator.common.building;

import static com.recursive_pineapple.matter_manipulator.common.utils.Mods.ArchitectureCraft;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.recursive_pineapple.matter_manipulator.MMMod;
import com.recursive_pineapple.matter_manipulator.common.compat.BlockProperty;
import com.recursive_pineapple.matter_manipulator.common.compat.BlockPropertyRegistry;
import com.recursive_pineapple.matter_manipulator.common.compat.IntrinsicProperty;
import com.recursive_pineapple.matter_manipulator.common.utils.ItemId;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods.Names;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

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

    @SerializedName("i")
    @Nullable
    public Map<String, JsonElement> intrinsicProperties;

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
        intrinsicProperties = null;
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
        this.metadata = stack.itemDamage;

        List<IntrinsicProperty> props = new ArrayList<>();

        BlockPropertyRegistry.getIntrinsicProperties(stack, props);

        if (!props.isEmpty()) {
            this.intrinsicProperties = new Object2ObjectOpenHashMap<>();

            for (IntrinsicProperty prop : props) {
                intrinsicProperties.put(prop.getName(), prop.getValue(stack));
            }
        }

        return this;
    }

    public BlockSpec setObject(Item item, int meta) {
        reset();
        this.isBlock = false;
        this.objectId = GameRegistry.findUniqueIdentifierFor(item);
        this.metadata = meta;

        return this;
    }

    public void populate() {
        if (isBlock) {
            block = GameRegistry.findBlock(objectId.modId, objectId.name);
            item = Optional.ofNullable(MMUtils.getItemFromBlock(block, metadata));
        } else {
            item = Optional.of(GameRegistry.findItem(objectId.modId, objectId.name));
            block = MMUtils.getBlockFromItem(item.get(), item.get().getMetadata(metadata));
        }
    }

    @Override
    public UniqueIdentifier getObjectId() {
        return objectId;
    }

    @Override
    public @NotNull Block getBlock() {
        if (block == null) populate();

        return block;
    }

    @Override
    public @NotNull Item getItem() {
        ItemStack stack = toStack(1);

        // noinspection DataFlowIssue
        return stack == null ? null : stack.getItem();
    }

    @Override
    public int getItemMeta() {
        ItemStack stack = toStack(1);

        return stack == null ? 0 : stack.itemDamage;
    }

    @Override
    public int getBlockMeta() {
        // noinspection ConstantValue
        return getItem() == null ? 0 : getItem().getMetadata(getItemMeta());
    }

    @Override
    public ItemStack toStack(int amount) {
        if (this.stack == null) {
            ItemStack stack = null;

            if (isBlock) {
                stack = getBlock().createStackedBlock(metadata);
            }

            if (stack == null || stack.getItem() == null) {
                if (this.item == null) populate();

                Item item = this.item.orElse(null);

                // noinspection ConstantValue
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

            // noinspection ConstantValue
            if (stack != null && stack.getItem() != null) {
                this.stack = Optional.of(stack);
            } else {
                this.stack = Optional.empty();
            }

            if (this.stack.isPresent()) {
                ItemStack stack2 = this.stack.get();

                NBTTagCompound tag = new NBTTagCompound();
                stack2.setTagCompound(tag);

                if (ArchitectureCraft.isModLoaded() && arch != null) arch.getItemTag(tag);

                if (intrinsicProperties != null) {
                    Map<String, IntrinsicProperty> props = new Object2ObjectOpenHashMap<>();
                    BlockPropertyRegistry.getIntrinsicProperties(stack, props);

                    for (var e : intrinsicProperties.entrySet()) {
                        IntrinsicProperty prop = props.get(e.getKey());

                        if (prop == null) {
                            MMMod.LOG.warn(
                                "Tried to set intrinsic property '{}' on an item, but the property is not registered to that item. Value='{}' Item='{}'",
                                e.getKey(),
                                e.getValue(),
                                stack.toString(),
                                new Exception()
                            );
                            continue;
                        }

                        prop.setValue(stack, e.getValue());
                    }
                }

                tag = stack2.getTagCompound();

                if (tag != null && tag.hasNoTags()) stack2.setTagCompound(null);
            }
        }

        ItemStack out = ItemStack.copyItemStack(this.stack.orElse(null));
        if (out != null) out.stackSize *= amount;
        return out;
    }

    public void getItemDetails(List<String> details) {
        Map<String, IntrinsicProperty> props = new Object2ObjectOpenHashMap<>();
        BlockPropertyRegistry.getIntrinsicProperties(toStack(1), props);

        if (intrinsicProperties != null) {
            for (var e : intrinsicProperties.entrySet()) {
                IntrinsicProperty prop = props.get(e.getKey());

                if (prop == null) continue;

                prop.getItemDetails(details, e.getValue());
            }
        }
    }

    @Override
    public PendingBlock instantiate(int worldId, int x, int y, int z) {
        PendingBlock pendingBlock = new PendingBlock(worldId, x, y, z, this);

        if (ArchitectureCraft.isModLoaded() && arch != null) {
            pendingBlock.arch = arch.clone();
        }

        return pendingBlock;
    }

    @Override
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

        getItemDetails(details);

        if (ArchitectureCraft.isModLoaded() && arch != null) arch.getItemDetails(details);

        return details.isEmpty() ? "" : String.format(" (%s)", String.join(", ", details));
    }

    public String getDisplayName() {
        return (toStack(1) == null ? Blocks.air.getLocalizedName() : toStack(1).getDisplayName()) + getItemDetails();
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof BlockSpec blockSpec)) return false;

        return isBlock == blockSpec.isBlock && metadata == blockSpec.metadata &&
            getBlock() == blockSpec.getBlock() &&
            Objects.equals(objectId, blockSpec.objectId) &&
            Objects.equals(properties, blockSpec.properties) &&
            (!Mods.ArchitectureCraft.isModLoaded() || Objects.equals(arch, blockSpec.arch)) &&
            Objects.equals(intrinsicProperties, blockSpec.intrinsicProperties);
    }

    @Override
    public int hashCode() {
        int result = Boolean.hashCode(isBlock);
        result = 31 * result + objectId.hashCode();
        result = 31 * result + metadata;
        result = 31 * result + Objects.hashCode(properties);
        if (Mods.ArchitectureCraft.isModLoaded()) {
            result = 31 * result + Objects.hashCode(arch);
        }
        result = 31 * result + Objects.hashCode(intrinsicProperties);
        result = 31 * result + getBlock().hashCode();
        return result;
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

        @Nullable
        Item item = MMUtils.getItemFromBlock(block, blockMeta);

        if (!InteropConstants.isFree(block, blockMeta)) {
            if (item == null) return BlockSpec.air();

            if (block != Blocks.wall_sign && block != Blocks.standing_sign) {
                block = MMUtils.getBlockFromItem(item, item.getMetadata(blockMeta));
            }
        }

        int itemMeta = block.getDamageValue(world, x, y, z);

        spec.metadata = itemMeta;
        spec.item = Optional.ofNullable(item);
        spec.itemId = item == null ? Optional.empty() : Optional.of(ItemId.create(item, itemMeta, null));

        if (Mods.ArchitectureCraft.isModLoaded()) {
            spec.arch = ArchitectureCraftAnalysisResult.analyze(world.getTileEntity(x, y, z));
        }

        Map<String, BlockProperty<?>> properties = new Object2ObjectOpenHashMap<>();
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

        List<IntrinsicProperty> intrinsicProperties = new ArrayList<>();
        BlockPropertyRegistry.getIntrinsicProperties(world, x, y, z, intrinsicProperties);

        if (!intrinsicProperties.isEmpty()) {
            spec.intrinsicProperties = new Object2ObjectOpenHashMap<>();

            for (IntrinsicProperty prop : intrinsicProperties) {
                spec.intrinsicProperties.put(prop.getName(), prop.getValue(world, x, y, z));
            }
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
}
