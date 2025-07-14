package com.recursive_pineapple.matter_manipulator.common.building;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.gtnhlib.util.CoordinatePacker;
import com.recursive_pineapple.matter_manipulator.MMMod;
import com.recursive_pineapple.matter_manipulator.common.building.BlockAnalyzer.IBlockApplyContext;
import com.recursive_pineapple.matter_manipulator.common.building.providers.IItemProvider;
import com.recursive_pineapple.matter_manipulator.common.compat.BlockProperty;
import com.recursive_pineapple.matter_manipulator.common.compat.BlockPropertyRegistry;
import com.recursive_pineapple.matter_manipulator.common.compat.Orientation;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.Location;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.Transform;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * This represents a block in the world.
 * It stores everything the building algorithm needs to know to place a block.
 */
public class PendingBlock extends Location {

    public ImmutableBlockSpec spec;

    public ITileAnalysisIntegration gt;
    public ITileAnalysisIntegration ae;
    public ITileAnalysisIntegration arch;
    public ITileAnalysisIntegration mp;

    public InventoryAnalysis inventory = null;

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
        this.gt = null;
        this.ae = null;
        this.arch = null;
        this.inventory = null;
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

    private List<ITileAnalysisIntegration> getIntegrations() {
        List<ITileAnalysisIntegration> list = new ArrayList<>();

        if (gt != null) list.add(gt);
        if (ae != null) list.add(ae);
        if (arch != null) list.add(arch);
        // if (mp != null) list.add(mp);

        return list;
    }

    private String getItemDetails() {
        List<String> details = new ArrayList<>(0);

        spec.getItemDetails(details);

        for (var analysis : getIntegrations()) {
            analysis.getItemDetails(details);
        }

        return details.isEmpty() ? "" : String.format(" (%s)", String.join(", ", details));
    }

    public ItemStack getStack() {
        ItemStack stack = spec.getStack();

        if (stack == null) return null;

        NBTTagCompound tag = stack.getTagCompound() != null ? stack.getTagCompound() : new NBTTagCompound();

        for (var analysis : getIntegrations()) {
            analysis.getItemTag(tag);
        }

        stack.setTagCompound(tag.hasNoTags() ? null : tag);

        return stack;
    }

    /**
     * Get the required items for a block that exists in the world
     *
     * @return True when this result can be applied to the tile, false otherwise
     */
    public boolean getRequiredItemsForExistingBlock(IBlockApplyContext context) {
        TileEntity te = context.getTileEntity();

        for (var analysis : getIntegrations()) {
            if (!analysis.getRequiredItemsForExistingBlock(context)) return false;
        }

        if (this.inventory != null && te instanceof IInventory inventory) {
            this.inventory.apply(context, inventory, true, true);
        }

        return true;
    }

    /**
     * Get the required items for a block that doesn't exist
     *
     * @return True if this tile result is valid, false otherwise
     */
    public boolean getRequiredItemsForNewBlock(IBlockApplyContext context) {
        for (var analysis : getIntegrations()) {
            if (!analysis.getRequiredItemsForNewBlock(context)) return false;
        }

        if (this.inventory != null) {
            for (IItemProvider item : this.inventory.mItems) {
                if (item != null) {
                    item.getStack(context, true);
                }
            }
        }

        return true;
    }

    public String getDisplayName() {
        return getStack().getDisplayName() + getItemDetails();
    }

    public boolean isFree() {
        return spec.isFree();
    }

    public PendingBlock clone() {
        PendingBlock dup = new PendingBlock();

        dup.worldId = worldId;
        dup.x = x;
        dup.y = y;
        dup.z = z;
        dup.spec = spec;
        if (gt != null) dup.gt = gt.clone();
        if (ae != null) dup.ae = ae.clone();
        if (arch != null) dup.arch = arch.clone();
        if (mp != null) dup.mp = mp.clone();
        if (inventory != null) dup.inventory = inventory.clone();
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
                    .rotateAxis(rotation * (float) Math.PI * 2f / 360f, 0, 1, 0)
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
                    transform.apply(o.b)
                );

                p.put(CopyableProperty.ORIENTATION, o.name().toLowerCase());
            } catch (Exception e) {
                MMMod.LOG.error("could not transform orientation", e);
            }
        }

        spec = spec.withProperties(p.isEmpty() ? null : p);

        for (var analysis : getIntegrations()) {
            analysis.transform(transform);
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
        class RefCell {

            public boolean didSomething = false;
        }

        RefCell ref = new RefCell();

        Map<String, BlockProperty<?>> properties = new Object2ObjectOpenHashMap<>();
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
                    context.error("Could not apply property " + property + ": " + e.getMessage());
                }
            }
        }

        for (var analysis : getIntegrations()) {
            if (!analysis.apply(context)) return false;
        }

        if (context.getTileEntity() instanceof IInventory inventory && this.inventory != null) {
            if (!this.inventory.apply(context, inventory, true, false)) { return false; }
        }

        world.notifyBlockOfNeighborChange(x, y, z, Blocks.air);

        return ref.didSomething;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((spec == null) ? 0 : spec.hashCode());
        result = prime * result + ((gt == null) ? 0 : gt.hashCode());
        result = prime * result + ((ae == null) ? 0 : ae.hashCode());
        result = prime * result + ((arch == null) ? 0 : arch.hashCode());
        result = prime * result + ((mp == null) ? 0 : mp.hashCode());
        result = prime * result + ((inventory == null) ? 0 : inventory.hashCode());
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
        if (gt == null) {
            if (other.gt != null) return false;
        } else if (!gt.equals(other.gt)) return false;
        if (ae == null) {
            if (other.ae != null) return false;
        } else if (!ae.equals(other.ae)) return false;
        if (arch == null) {
            if (other.arch != null) return false;
        } else if (!arch.equals(other.arch)) return false;
        if (mp == null) {
            if (other.mp != null) return false;
        } else if (!mp.equals(other.mp)) return false;
        if (inventory == null) {
            if (other.inventory != null) return false;
        } else if (!inventory.equals(other.inventory)) return false;
        if (renderOrder != other.renderOrder) return false;
        if (buildOrder != other.buildOrder) return false;
        return true;
    }

    /**
     * A comparator for sorting blocks prior to building.
     */
    public static Comparator<PendingBlock> getComparator() {
        return Comparator.comparingInt((PendingBlock b) -> b.buildOrder)
            .thenComparing(b -> b.spec, ImmutableBlockSpec.getComparator())
            .thenComparingInt(b -> b.x >> 4)
            .thenComparingInt(b -> b.z >> 4)
            .thenComparingLong(value -> CoordinatePacker.pack(value.x, value.y, value.z));
    }

    public static PendingBlock fromBlock(World world, int x, int y, int z) {
        return BlockSpec.fromBlock(null, world, x, y, z).instantiate(world, x, y, z);
    }

    private static int counter = 0;
    public static final int ANALYZE_GT = 0b1 << counter++;
    public static final int ANALYZE_AE = 0b1 << counter++;
    public static final int ANALYZE_ARCH = 0b1 << counter++;
    public static final int ANALYZE_MP = 0b1 << counter++;
    public static final int ANALYZE_INV = 0b1 << counter++;
    public static final int ANALYZE_ALL = -1;

    public PendingBlock analyze(TileEntity te, int flags) {
        if (te != null) {
            if ((flags & ANALYZE_GT) != 0 && Mods.GregTech.isModLoaded()) {
                this.gt = GTAnalysisResult.analyze(te);
            }

            if ((flags & ANALYZE_AE) != 0 && Mods.AppliedEnergistics2.isModLoaded()) {
                this.ae = AEAnalysisResult.analyze(te);
            }

            if ((flags & ANALYZE_ARCH) != 0 && Mods.ArchitectureCraft.isModLoaded()) {
                this.arch = ArchitectureCraftAnalysisResult.analyze(te);
            }

            // if ((flags & ANALYZE_MP) != 0 && Mods.ForgeMultipart.isModLoaded()) {
            // this.mp = MultipartAnalysisResult.analyze(te);
            // }

            if ((flags & ANALYZE_INV) != 0 && te instanceof IInventory inventory) {
                this.inventory = InventoryAnalysis.fromInventory(inventory, false);
            }
        }

        return this;
    }

    public PendingBlock migrate() {
        if (gt != null) gt.migrate();
        if (ae != null) ae.migrate();
        if (arch != null) arch.migrate();
        if (mp != null) mp.migrate();

        return this;
    }

    public static boolean areEquivalent(PendingBlock a, PendingBlock b) {
        ItemStack sa = a.getStack();
        ItemStack sb = b.getStack();

        if (sa == null && sb == null) {
            return a.spec.equals(b.spec);
        } else {
            return ItemStack.areItemStacksEqual(sa, sb);
        }
    }
}
