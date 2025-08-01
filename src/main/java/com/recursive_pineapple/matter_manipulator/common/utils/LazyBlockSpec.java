package com.recursive_pineapple.matter_manipulator.common.utils;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.registry.GameRegistry;

import com.gtnewhorizon.gtnhlib.util.data.BlockMeta;
import com.gtnewhorizon.gtnhlib.util.data.IMod;
import com.gtnewhorizon.gtnhlib.util.data.ImmutableBlockMeta;
import com.gtnewhorizon.gtnhlib.util.data.ImmutableItemMeta;
import com.gtnewhorizon.gtnhlib.util.data.ItemMeta;
import com.recursive_pineapple.matter_manipulator.common.building.BlockSpec;
import com.recursive_pineapple.matter_manipulator.common.building.ImmutableBlockSpec;

import org.jetbrains.annotations.NotNull;

public abstract class LazyBlockSpec implements ImmutableItemMeta, ImmutableBlockMeta {

    private volatile boolean loaded = false;

    protected boolean isBlock, present;

    protected volatile ImmutableItemMeta im;
    protected volatile ImmutableBlockMeta bm;

    public static LazyBlockSpec ofBlock(IMod mod, String blockName, int meta) {
        return new LazyBlockSpec() {

            {
                this.isBlock = true;
            }

            @Override
            protected void init() {
                present = mod.isModLoaded();

                if (!present) return;

                Block block = GameRegistry.findBlock(mod.getID(), blockName);
                Item item = Item.getItemFromBlock(block);

                this.bm = new BlockMeta(block, meta);
                this.im = new ItemMeta(item, block.damageDropped(meta));
            }
        };
    }

    public static LazyBlockSpec ofItem(IMod mod, String itemName, int meta) {
        return new LazyBlockSpec() {

            {
                this.isBlock = false;
            }

            @Override
            protected void init() {
                present = mod.isModLoaded();

                if (!present) return;

                Item item = GameRegistry.findItem(mod.getID(), itemName);
                Block block = Block.getBlockFromItem(item);

                this.im = new ItemMeta(item, meta);
                this.bm = new BlockMeta(block, item.getMetadata(meta));
            }
        };
    }

    public static LazyBlockSpec ofBlock(IMod mod, String blockName, int blockMeta, String itemName, int itemMeta) {
        return new LazyBlockSpec() {

            {
                this.isBlock = true;
            }

            @Override
            protected void init() {
                present = mod.isModLoaded();

                if (!present) return;

                bm = new BlockMeta(GameRegistry.findBlock(mod.getID(), blockName), blockMeta);
                im = new ItemMeta(GameRegistry.findItem(mod.getID(), itemName), itemMeta);
            }
        };
    }

    private void ensureInited() {
        if (!loaded) {
            init();
            loaded = true;
        }
    }

    protected abstract void init();

    @Override
    @SuppressWarnings("DataFlowIssue")
    public @NotNull Block getBlock() {
        ensureInited();

        if (!present) return null;

        return bm.getBlock();
    }

    @Override
    public int getBlockMeta() {
        ensureInited();

        if (!present) return 0;

        return bm.getBlockMeta();
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public @NotNull Item getItem() {
        ensureInited();

        if (!present) return null;

        return im.getItem();
    }

    @Override
    public int getItemMeta() {
        ensureInited();

        if (!present) return 0;

        return im.getItemMeta();
    }

    public BlockSpec toSpec() {
        ensureInited();

        if (!present) return null;

        if (isBlock) {
            return new BlockSpec().setObject(getBlock(), getBlockMeta());
        } else {
            return new BlockSpec().setObject(getItem(), getItemMeta());
        }
    }

    @Override
    public boolean matches(ItemStack stack) {
        ensureInited();

        return present && ImmutableItemMeta.super.matches(stack);
    }

    @Override
    public boolean matches(Item item, int meta) {
        ensureInited();

        return present && ImmutableItemMeta.super.matches(item, meta);
    }

    @Override
    public boolean matches(Block block, int meta) {
        ensureInited();

        return present && ImmutableBlockMeta.super.matches(block, meta);
    }

    public boolean matches(ImmutableBlockSpec spec) {
        ensureInited();

        if (!present) return false;

        if (isBlock) {
            return bm.matches(spec.getBlock(), spec.getBlockMeta());
        } else {
            return im.matches(spec.getItem(), spec.getItemMeta());
        }
    }
}
