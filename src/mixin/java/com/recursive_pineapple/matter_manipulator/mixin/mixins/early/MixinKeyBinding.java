package com.recursive_pineapple.matter_manipulator.mixin.mixins.early;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.recursive_pineapple.matter_manipulator.MMMod;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMKeyInputs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Cancel mm key presses for conflicting keybinds when the player is holding a manipulator.
 */
@Mixin(KeyBinding.class)
public class MixinKeyBinding {

    @ModifyReturnValue(method = "getIsKeyPressed", at = @At("RETURN"))
    public boolean mm$cancelGetIsKeyPressed(boolean pressed) {
        KeyBinding self = (KeyBinding) (Object) this;

        if (self == MMKeyInputs.CONTROL) return pressed;
        if (self == MMKeyInputs.CUT) return pressed;
        if (self == MMKeyInputs.COPY) return pressed;
        if (self == MMKeyInputs.PASTE) return pressed;
        if (self == MMKeyInputs.RESET) return pressed;

        if (MMKeyInputs.CONTROL.getKeyCode() == 0 || MMKeyInputs.CONTROL.getIsKeyPressed()) {
            EntityPlayer player = MMMod.proxy.getThePlayer();

            if (player != null) {
                ItemStack held = player.getHeldItem();
    
                if (held != null && held.getItem() instanceof ItemMatterManipulator) {
                    if (self.getKeyCode() == MMKeyInputs.CUT.getKeyCode()) return false;
                    if (self.getKeyCode() == MMKeyInputs.COPY.getKeyCode()) return false;
                    if (self.getKeyCode() == MMKeyInputs.PASTE.getKeyCode()) return false;
                    if (self.getKeyCode() == MMKeyInputs.RESET.getKeyCode()) return false;
                }
            }
        }

        return pressed;
    }

    @ModifyReturnValue(method = "isPressed", at = @At("RETURN"))
    public boolean mm$cancelIsPressed(boolean pressed) {
        KeyBinding self = (KeyBinding) (Object) this;

        if (self == MMKeyInputs.CONTROL) return pressed;
        if (self == MMKeyInputs.CUT) return pressed;
        if (self == MMKeyInputs.COPY) return pressed;
        if (self == MMKeyInputs.PASTE) return pressed;
        if (self == MMKeyInputs.RESET) return pressed;

        if (MMKeyInputs.CONTROL.getKeyCode() == 0 || MMKeyInputs.CONTROL.getIsKeyPressed()) {
            EntityPlayer player = MMMod.proxy.getThePlayer();
            
            if (player != null) {
                ItemStack held = player.getHeldItem();
    
                if (held != null && held.getItem() instanceof ItemMatterManipulator) {
                    if (self.getKeyCode() == MMKeyInputs.CUT.getKeyCode()) return false;
                    if (self.getKeyCode() == MMKeyInputs.COPY.getKeyCode()) return false;
                    if (self.getKeyCode() == MMKeyInputs.PASTE.getKeyCode()) return false;
                    if (self.getKeyCode() == MMKeyInputs.RESET.getKeyCode()) return false;
                }
            }
        }

        return pressed;
    }
}
