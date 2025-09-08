package com.recursive_pineapple.matter_manipulator.mixin.mixins.late;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;
import com.recursive_pineapple.matter_manipulator.mixin.interfaces.MTELinkedInputBusExt;
import ggfab.mte.MTELinkedInputBus;
import lombok.SneakyThrows;

@Mixin(value = MTELinkedInputBus.class, remap = false)
public abstract class MixinMTELinkedInputBus implements MTELinkedInputBusExt {

    @Unique
    private Field mm$RealInventory;

    @Unique
    private SharedInventoryExt mm$getRealInventory() {
        if (mm$RealInventory == null) {
            try {
                mm$RealInventory = this.getClass().getDeclaredField("mRealInventory");
                mm$RealInventory.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            return (SharedInventoryExt) mm$RealInventory.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int mm$getChannelRefCount() {
        SharedInventoryExt realInventory = mm$getRealInventory();

        return realInventory == null ? 0 : realInventory.mm$getRefCount();
    }
}
