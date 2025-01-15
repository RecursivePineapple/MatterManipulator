package com.recursive_pineapple.matter_manipulator.mixin;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public interface BlockCaptureDrops {
    
    public void captureDrops();

    public List<ItemStack> stopCapturingDrops();

    public static void captureDrops(Block block) {
        if (block instanceof BlockCaptureDrops drops) drops.captureDrops();
    }

    public static List<ItemStack> stopCapturingDrops(Block block) {
        if (block instanceof BlockCaptureDrops drops) {
            return drops.stopCapturingDrops();
        } else {
            return Collections.emptyList();
        }
    }
}
