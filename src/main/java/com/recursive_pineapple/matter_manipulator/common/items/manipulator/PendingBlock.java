package com.recursive_pineapple.matter_manipulator.common.items.manipulator;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Comparator;
import java.util.Optional;

import com.recursive_pineapple.matter_manipulator.common.building.InteropConstants;
import com.recursive_pineapple.matter_manipulator.common.building.TileAnalysisResult;
import com.recursive_pineapple.matter_manipulator.common.utils.Lazy;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods.Names;

import appeng.api.AEApi;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

/**
 * This represents a block in the world.
 * It stores everything the building algorithm needs to know to place a block.
 */
public class PendingBlock extends Location {

    public UniqueIdentifier blockId;
    public int metadata;

    /**
     * Some extra metadata that isn't saved in the item, but needs to be applied when the block is placed.
     * Currently this is only used for slabs, since their metadata is a combination of their item and whether they're on the top or bottom.
     */
    public int flags;

    public TileAnalysisResult tileData;
    // various sort orders, one is for drawing hints and one is for the build order
    public int renderOrder, buildOrder;

    private transient Item item;
    private transient Block block;
    private transient Optional<ItemStack> stack;

    private static int counter = 0;
    public static final int SLAB_TOP = 0b1 << counter++;

    public PendingBlock(int worldId, int x, int y, int z, ItemStack block) {
        super(worldId, x, y, z);
        setBlock(block);
    }

    public PendingBlock(int worldId, int x, int y, int z, ItemStack block, int renderOrder, int buildOrder) {
        this(worldId, x, y, z, block);
        this.renderOrder = renderOrder;
        this.buildOrder = buildOrder;
    }

    public PendingBlock(int worldId, int x, int y, int z, Block block, int meta) {
        super(worldId, x, y, z);
        setBlock(block, meta);
    }

    public PendingBlock(int worldId, int x, int y, int z, Block block, int meta, int flags) {
        this(worldId, x, y, z, block, meta);
        this.flags = flags;
    }

    private PendingBlock() {}

    /**
     * Clears this block's block but not its position.
     */
    public PendingBlock reset() {
        this.block = null;
        this.item = null;
        this.stack = null;
        this.blockId = null;
        this.metadata = 0;

        return this;
    }

    public PendingBlock setBlock(Block block, int metadata) {
        reset();

        this.blockId = GameRegistry.findUniqueIdentifierFor(block == null ? Blocks.air : block);
        this.metadata = metadata;

        return this;
    }

    /**
     * If the item in the stack isn't an ItemBlock, this just resets the stored block.
     */
    public PendingBlock setBlock(ItemStack stack) {
        reset();

        Optional<Block> block = Optional.ofNullable(stack)
            .map(ItemStack::getItem)
            .map(Block::getBlockFromItem);

        if (block.isPresent()) {
            this.block = block.get();
            this.item = (ItemBlock) Item.getItemFromBlock(block.get());
            this.blockId = GameRegistry.findUniqueIdentifierFor(block.get());
            this.metadata = this.item != null && this.item.getHasSubtypes() ? Items.feather.getDamage(stack) : 0;
        }

        return this;
    }

    public Block getBlock() {
        if (block == null) {
            block = blockId == null ? Blocks.air : GameRegistry.findBlock(blockId.modId, blockId.name);

            if (block == null) block = Blocks.air;
        }

        return block;
    }

    public Item getItem() {
        if (item == null) {
            Block block = getBlock();

            if (block != null) {
                item = MMUtils.getItemFromBlock(block, metadata);
            }
        }

        return item;
    }

    private static final MethodHandle CREATE_STACKED_BLOCK = MMUtils.exposeMethod(Block.class, MethodType.methodType(ItemStack.class, int.class), "createStackedBlock", "func_149644_j", "j");

    public ItemStack toStack() {
        if (stack == null) {
            Block block = getBlock();

            if (block == Blocks.air) {
                this.stack = Optional.empty();
                return null;
            }

            ItemStack stack = null;

            try {
                stack = (ItemStack) CREATE_STACKED_BLOCK.invoke(block, metadata);
            } catch (Throwable t) {
                throw new RuntimeException("Could not invoke " + CREATE_STACKED_BLOCK, t);
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

            if (tileData != null) {
                stack.setTagCompound(tileData.getItemTag());
            }

            this.stack = Optional.ofNullable(stack);
        }

        return stack.orElse(null);
    }

    public String getDisplayName() {
        return toStack().getDisplayName() + (tileData == null ? "" : tileData.getItemDetails());
    }

    public boolean shouldBeSkipped() {
        return InteropConstants.shouldBeSkipped(getBlock(), metadata);
    }

    @com.recursive_pineapple.matter_manipulator.asm.Optional(Names.APPLIED_ENERGISTICS2)
    public static Lazy<Block> AE_BLOCK_CABLE;

    static {
        if (Mods.AppliedEnergistics2.isModLoaded()) {
            AE_BLOCK_CABLE = new Lazy<>(() -> AEApi.instance()
                .definitions()
                .blocks()
                .multiPart()
                .maybeBlock()
                .get());
        }
    }

    public boolean isFree() {
        Block block = getBlock();

        if (block == Blocks.air) {
            return true;
        }

        if (Mods.AppliedEnergistics2.isModLoaded()) {
            if (block == AE_BLOCK_CABLE.get() && tileData != null) {
                return true;
            }
        }

        return false;
    }

    public PendingBlock clone() {
        PendingBlock dup = new PendingBlock();

        dup.worldId = worldId;
        dup.x = x;
        dup.y = y;
        dup.z = z;
        dup.blockId = blockId;
        dup.metadata = metadata;
        dup.flags = flags;
        dup.tileData = tileData;
        dup.renderOrder = renderOrder;
        dup.buildOrder = buildOrder;
        dup.item = item;
        dup.block = block;
        dup.stack = stack;

        return dup;
    }

    public static PendingBlock fromBlock(World world, int x, int y, int z, Block block, int meta) {
        Item item = MMUtils.getItemFromBlock(block, meta);

        if (item == null) {
            return new PendingBlock(world.provider.dimensionId, x, y, z, Blocks.air, 0);
        }

        block = MMUtils.getBlockFromItem(item, meta);

        meta = item.getHasSubtypes() ? block.getDamageValue(world, x, y, z) : meta;

        int flags = 0;

        if (block instanceof BlockSlab) {
            if ((world.getBlockMetadata(x, y, z) & 8) != 0) {
                flags |= SLAB_TOP;
            }
        }

        return new PendingBlock(world.provider.dimensionId, x, y, z, block, meta, flags);
    }

    /**
     * Creates a PendingBlock from an existing block in the world.
     */
    public static PendingBlock fromBlock(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);

        return fromBlock(world, x, y, z, block, meta);
    }

    /**
     * Creates a PendingBlock from a picked block.
     */
    public static PendingBlock fromPickBlock(World world, EntityPlayer player, MovingObjectPosition hit) {
        if (hit == null || hit.typeOfHit != MovingObjectType.BLOCK) return null;

        return fromBlock(world, hit.blockX, hit.blockY, hit.blockZ);
    }

    /**
     * Checks if two PendingBlocks contain the same Block.
     */
    public static boolean isSameBlock(PendingBlock a, PendingBlock b) {
        if (a == null || b == null) {
            return a == null && b == null;
        }

        if (!ItemStack.areItemStacksEqual(a.toStack(), b.toStack())) return false;

        if (a.getBlock() instanceof BlockSlab slabA && b.getBlock() instanceof BlockSlab slabB) {
            if (slabA.field_150004_a != slabB.field_150004_a) return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "PendingBlock [blockId=" + blockId
            + ", metadata="
            + metadata
            + ", tileData="
            + tileData
            + ", renderOrder="
            + renderOrder
            + ", buildOrder="
            + buildOrder
            + ", x="
            + x
            + ", y="
            + y
            + ", z="
            + z
            + ", worldId="
            + worldId
            + ", world="
            + getWorld()
            + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((blockId == null) ? 0 : blockId.hashCode());
        result = prime * result + metadata;
        result = prime * result + ((tileData == null) ? 0 : tileData.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        PendingBlock other = (PendingBlock) obj;
        if (blockId == null) {
            if (other.blockId != null) return false;
        } else if (!blockId.equals(other.blockId)) return false;
        if (metadata != other.metadata) return false;
        if (tileData == null) {
            if (other.tileData != null) return false;
        } else if (!tileData.equals(other.tileData)) return false;
        return true;
    }

    /**
     * A comparator for sorting blocks prior to building.
     */
    public static Comparator<PendingBlock> getComparator() {
        Comparator<UniqueIdentifier> blockId = Comparator.nullsFirst(
            Comparator.comparing((UniqueIdentifier id) -> id.modId)
                .thenComparing(id -> id.name));

        return Comparator.comparingInt((PendingBlock b) -> b.buildOrder)
            .thenComparing(Comparator.nullsFirst(Comparator.comparing(b -> b.blockId, blockId)))
            .thenComparingInt(b -> b.metadata)
            .thenComparingLong(b -> {
                int chunkX = b.x >> 4;
                int chunkZ = b.z >> 4;

                return chunkX | (chunkZ << 32);
            });
    }
}