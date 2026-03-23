package matter_manipulator.mixin.mixins.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import matter_manipulator.common.items.ItemMatterManipulator;

@Mixin(value = Minecraft.class, remap = false)
public class MixinMinecraft {

    @Inject(method = "middleClickMouse", at = @At("HEAD"), cancellable = true)
    private static void interceptPickBlock(CallbackInfo ci) {
        EntityPlayer player = Minecraft.getMinecraft().player;

        if (ItemMatterManipulator.onPickBlock(player)) {
            ci.cancel();
        }
    }

}
