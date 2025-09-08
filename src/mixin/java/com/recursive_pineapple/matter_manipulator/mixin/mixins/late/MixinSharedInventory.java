package com.recursive_pineapple.matter_manipulator.mixin.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.recursive_pineapple.matter_manipulator.mixin.interfaces.MTELinkedInputBusExt;

@Mixin(targets = "ggfab.mte.MTELinkedInputBus$SharedInventory")
public class MixinSharedInventory implements MTELinkedInputBusExt.SharedInventoryExt {

    @Shadow(remap = false)
    private int ref;

    @Override
    public int mm$getRefCount() {
        return ref;
    }
}
