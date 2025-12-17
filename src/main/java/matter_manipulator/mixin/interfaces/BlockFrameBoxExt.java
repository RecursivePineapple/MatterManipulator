package com.recursive_pineapple.matter_manipulator.mixin.interfaces;

import net.minecraft.world.World;

import gregtech.api.metatileentity.BaseMetaPipeEntity;

public interface BlockFrameBoxExt {

    BaseMetaPipeEntity spawnFrameEntityExt(World worldIn, int x, int y, int z);

}
