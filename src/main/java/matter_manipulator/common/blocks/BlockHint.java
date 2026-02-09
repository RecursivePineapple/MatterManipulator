package matter_manipulator.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import matter_manipulator.Tags;

public class BlockHint extends Block {

    public BlockHint(String name) {
        super(Material.ROCK);

        setRegistryName(Tags.MODID, name);
        setTranslationKey(name);
    }
}
