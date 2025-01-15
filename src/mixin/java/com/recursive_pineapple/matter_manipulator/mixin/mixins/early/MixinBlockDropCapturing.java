package com.recursive_pineapple.matter_manipulator.mixin.mixins.early;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import com.recursive_pineapple.matter_manipulator.mixin.BlockCaptureDrops;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Block.class)
public abstract class MixinBlockDropCapturing implements BlockCaptureDrops {

    @Shadow(remap = false)
    protected abstract List<ItemStack> captureDrops(boolean start);

    @Override
    public void captureDrops() {
        captureDrops(true);
    }

    @Override
    public List<ItemStack> stopCapturingDrops() {
        return captureDrops(false);
    }
}
