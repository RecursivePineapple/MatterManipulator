package com.recursive_pineapple.matter_manipulator.mixin.mixins.late;

import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.recursive_pineapple.matter_manipulator.mixin.interfaces.BlockFrameBoxExt;
import gregtech.api.metatileentity.BaseMetaPipeEntity;
import gregtech.common.blocks.BlockFrameBox;

@Mixin(BlockFrameBox.class)
public abstract class MixinBlockFrameBox implements BlockFrameBoxExt {

    @Shadow(remap = false)
    private BaseMetaPipeEntity spawnFrameEntity(World worldIn, int x, int y, int z) {
        return null;
    }

    @Override
    public BaseMetaPipeEntity spawnFrameEntityExt(World worldIn, int x, int y, int z) {
        return this.spawnFrameEntity(worldIn, x, y, z);
    }
}
