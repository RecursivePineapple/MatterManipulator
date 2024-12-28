package com.recursive_pineapple.matter_manipulator.common.utils;

import static com.recursive_pineapple.matter_manipulator.common.utils.MMValues.W;

import java.util.Objects;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

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

        if (bm == null) return false;

        return bm.getBlock() == other && (bm.getMeta() == metaOther || bm.getMeta() == W || metaOther == W);
    }
}
