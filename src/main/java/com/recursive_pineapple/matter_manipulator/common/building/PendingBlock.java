package com.recursive_pineapple.matter_manipulator.common.building;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import com.recursive_pineapple.matter_manipulator.common.building.BlockAnalyzer.IBlockApplyContext;
import com.recursive_pineapple.matter_manipulator.MMMod;
import com.recursive_pineapple.matter_manipulator.common.compat.BlockProperty;
import com.recursive_pineapple.matter_manipulator.common.compat.BlockPropertyRegistry;
import com.recursive_pineapple.matter_manipulator.common.compat.Orientation;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.Location;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.Transform;
import com.recursive_pineapple.matter_manipulator.common.utils.LazyBlock;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * This represents a block in the world.
 * It stores everything the building algorithm needs to know to place a block.
 */
public class PendingBlock extends Location {

    public ImmutableBlockSpec spec;

    public TileAnalysisResult tileData;
    public int renderOrder, buildOrder;

    public PendingBlock(int worldId, int x, int y, int z, @NotNull ImmutableBlockSpec spec) {
        super(worldId, x, y, z);
        this.spec = spec;
    }

    private PendingBlock() {}

    /**
     * Clears this block's info but not its position.
     */
    public PendingBlock reset() {
        this.spec = null;
        this.tileData = null;
        this.renderOrder = 0;
        this.buildOrder = 0;

        return this;
    }

    public PendingBlock setBlock(ImmutableBlockSpec spec) {
        reset();

        this.spec = spec;

        return this;
    }

    public PendingBlock setBlock(Block block, int metadata) {
        reset();

        this.spec = new BlockSpec().setObject(block, metadata);

        return this;
    }

    public PendingBlock setBlock(ItemStack stack) {
        reset();

        this.spec = new BlockSpec().setObject(stack);

        return this;
    }

    public PendingBlock setOrders(int renderOrder, int buildOrder) {
        this.renderOrder = renderOrder;
        this.buildOrder = buildOrder;

        return this;
    }

    public Block getBlock() {
        return spec.getBlock();
    }

    public Item getItem() {
        return spec.getItem();
    }
    
    public ItemStack getStack() {
        ItemStack stack = spec.getStack();

        if (tileData != null) stack.setTagCompound(tileData.getItemTag());

        return stack;
    }

    public String getDisplayName() {
        return getStack().getDisplayName() + (tileData == null ? "" : tileData.getItemDetails());
    }

    public boolean shouldBeSkipped() {
        return spec.shouldBeSkipped();
    }

    public static final LazyBlock AE_BLOCK_CABLE = new LazyBlock(Mods.AppliedEnergistics2, "tile.BlockCableBus");

    public boolean isFree() {
        Block block = getBlock();

        if (block == Blocks.air) return true;

        if (AE_BLOCK_CABLE.matches(spec)) return true;
        
        return false;
    }

    public PendingBlock clone(boolean shallow) {
        PendingBlock dup = new PendingBlock();

        dup.worldId = worldId;
        dup.x = x;
        dup.y = y;
        dup.z = z;
        dup.spec = spec;
        dup.tileData = shallow ? tileData : tileData == null ? null : tileData.clone();
        dup.renderOrder = renderOrder;
        dup.buildOrder = buildOrder;

        return dup;
    }

    public void transform(Transform transform) {
        EnumMap<CopyableProperty, String> p = new EnumMap<>(CopyableProperty.class);

        for (CopyableProperty property : CopyableProperty.VALUES) {
            String value = spec.getProperty(property);
            if (value != null && !value.isEmpty()) p.put(property, value);
        }

        transform(p, CopyableProperty.FACING, transform);
        transform(p, CopyableProperty.FORWARD, transform);
        transform(p, CopyableProperty.UP, transform);
        transform(p, CopyableProperty.LEFT, transform);

        if (p.containsKey(CopyableProperty.TOP) && transform.apply(ForgeDirection.UP) == ForgeDirection.DOWN) {
            String value = p.get(CopyableProperty.TOP);
            p.put(CopyableProperty.TOP, "true".equals(value) ? "false" : "true");
        }

        if (p.containsKey(CopyableProperty.ROTATION)) {
            try {
                int rotation = Integer.parseInt(p.get(CopyableProperty.ROTATION));

                Vector3f v = new Vector3f(0, 0, 1)
                    .rotateAxis(rotation * (float)Math.PI * 2f / 360f, 0, 1, 0)
                    .mulTransposeDirection(transform.getRotation());

                rotation = MathHelper.floor_double(Math.atan2(v.x, v.z) * 360d / Math.PI / 2d + 0.5);
                rotation = (rotation % 360 + 360) % 360;

                p.put(CopyableProperty.ROTATION, Integer.toString(rotation));
            } catch (NumberFormatException e) {
                MMMod.LOG.error("could not transform rotation", e);
            }
        }

        if (p.containsKey(CopyableProperty.ORIENTATION)) {
            try {
                Orientation o = Orientation.valueOf(p.get(CopyableProperty.ORIENTATION).toUpperCase());

                o = Orientation.getOrientation(
                    transform.apply(o.a),
                    transform.apply(o.b));

                p.put(CopyableProperty.ORIENTATION, o.name().toLowerCase());
            } catch (Exception e) {
                MMMod.LOG.error("could not transform orientation", e);
            }
        }

        spec = spec.withProperties(p.isEmpty() ? null : p);

        if (tileData != null) {
            tileData.transform(transform);
        }
    }

    private void transform(EnumMap<CopyableProperty, String> p, CopyableProperty prop, Transform transform) {
        String value = p.get(prop);

        if (value == null || value.isEmpty()) return;

        ForgeDirection dir;

        try {
            dir = ForgeDirection.valueOf(value.toUpperCase());
        } catch (Exception e) {
            MMMod.LOG.error("could not transform " + prop, e);
            return;
        }

        dir = transform.apply(dir);

        p.put(prop, dir.name().toLowerCase());
    }

    public boolean apply(IBlockApplyContext context, World world) {
        class RefCell { public boolean didSomething = false; }

        RefCell ref = new RefCell();

        Map<String, BlockProperty<?>> properties = new HashMap<>();
        BlockPropertyRegistry.getProperties(world, x, y, z, properties);

        for (CopyableProperty property : CopyableProperty.VALUES) {
            String value = spec.getProperty(property);

            if (value == null) continue;

            BlockProperty<?> prop = properties.get(property.toString());

            if (prop == null) continue;

            String existing = prop.getValueAsString(world, x, y, z);

            if (!existing.equals(value)) {
                ref.didSomething = true;

                try {
                    prop.setValueFromText(world, x, y, z, value);
                } catch (Exception e) {
                    context.error("could not apply property " + property + ": " + e.getMessage());
                }
            }
        }

        if (tileData != null) {
            tileData.apply(context);
            ref.didSomething = true;
        }

        world.notifyBlockOfNeighborChange(x, y, z, Blocks.air);

        return ref.didSomething;
    }

    @Override
    public String toString() {
        return "PendingBlock [spec=" + spec + ", tileData=" + tileData + ", renderOrder=" + renderOrder
                + ", buildOrder=" + buildOrder + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((spec == null) ? 0 : spec.hashCode());
        result = prime * result + ((tileData == null) ? 0 : tileData.hashCode());
        result = prime * result + renderOrder;
        result = prime * result + buildOrder;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        PendingBlock other = (PendingBlock) obj;
        if (spec == null) {
            if (other.spec != null) return false;
        } else if (!spec.equals(other.spec)) return false;
        if (tileData == null) {
            if (other.tileData != null) return false;
        } else if (!tileData.equals(other.tileData)) return false;
        if (renderOrder != other.renderOrder) return false;
        if (buildOrder != other.buildOrder) return false;
        return true;
    }

    /**
     * A comparator for sorting blocks prior to building.
     */
    public static Comparator<PendingBlock> getComparator() {
        return Comparator.comparingInt((PendingBlock b) -> b.buildOrder)
            .thenComparing(Comparator.comparing(b -> b.spec, ImmutableBlockSpec.getComparator()))
            .thenComparingLong(b -> {
                int chunkX = b.x >> 4;
                int chunkZ = b.z >> 4;

                return (long) chunkX | (long) (chunkZ << 32);
            })
            .thenComparing(b -> Objects.hashCode(b.tileData));
    }

    public static PendingBlock fromBlock(World world, int x, int y, int z) {
        PendingBlock pendingBlock = BlockSpec.fromBlock(null, world, x, y, z).instantiate(world, x, y, z);

        TileEntity te = world.getTileEntity(x, y, z);
        if (te != null) {
            pendingBlock.tileData = TileAnalysisResult.analyze(te);
        }

        return pendingBlock;
    }
}