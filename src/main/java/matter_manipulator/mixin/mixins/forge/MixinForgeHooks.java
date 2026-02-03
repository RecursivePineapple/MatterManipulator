package matter_manipulator.mixin.mixins.forge;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import matter_manipulator.common.items.ItemMatterManipulator;

@Mixin(value = ForgeHooks.class, remap = false)
public class MixinForgeHooks {

    @Inject(method = "onPickBlock", at = @At("HEAD"), cancellable = true)
    private static void interceptPickBlock(RayTraceResult target, EntityPlayer player, World world, CallbackInfoReturnable<Boolean> cir) {
        if (ItemMatterManipulator.onPickBlock(player)) {
            cir.cancel();
        }
    }

}
