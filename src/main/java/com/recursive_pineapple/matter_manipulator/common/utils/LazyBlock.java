package com.recursive_pineapple.matter_manipulator.common.utils;

import java.util.Objects;

import net.minecraft.block.Block;

import cpw.mods.fml.common.registry.GameRegistry;

import com.recursive_pineapple.matter_manipulator.common.building.ImmutableBlockSpec;

public class LazyBlock extends Lazy<ImmutableBlockMeta> {

    public final Mods mod;
    public final String blockName;

    public LazyBlock(Mods mod, String blockName, int meta) {
        super(() -> {
            if (!mod.isModLoaded()) return null;

            Block block = GameRegistry.findBlock(mod.ID, blockName);

            Objects.requireNonNull(block, "could not find block: " + mod.ID + ":" + blockName);

            return new BlockMeta(block, meta);
        });

        this.mod = mod;
        this.blockName = blockName;
    }

    public LazyBlock(Mods mod, String blockName) {
        this(mod, blockName, 0);
    }

    public boolean isLoaded() {
        return mod.isModLoaded();
    }

    public boolean matches(Block other, int metaOther) {
        if (!isLoaded()) return false;

        ImmutableBlockMeta bm = get();

        return bm == null ? false : bm.matches(other, metaOther);
    }

    public boolean matches(ImmutableBlockSpec spec) {
        return matches(spec.getBlock(), spec.getBlockMeta());
    }
}
