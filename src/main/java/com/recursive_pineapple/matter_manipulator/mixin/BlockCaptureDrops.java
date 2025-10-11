package com.recursive_pineapple.matter_manipulator.mixin;

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface BlockCaptureDrops {

    void captureDrops();

    List<ItemStack> stopCapturingDrops();

    static void captureDrops(World world) {
        if (world instanceof BlockCaptureDrops drops) drops.captureDrops();
    }

    static List<ItemStack> stopCapturingDrops(World world) {
        if (world instanceof BlockCaptureDrops drops) {
            return drops.stopCapturingDrops();
        } else {
            return Collections.emptyList();
        }
    }
}
