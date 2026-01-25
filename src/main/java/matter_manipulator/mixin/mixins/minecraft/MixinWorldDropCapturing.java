package matter_manipulator.mixin.mixins.minecraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import matter_manipulator.mixin.BlockCaptureDrops;

@Mixin(World.class)
public class MixinWorldDropCapturing implements BlockCaptureDrops {

    @Unique
    private List<ItemStack> mm$capturedDrops;

    @Override
    public void mm$captureDrops() {
        if (mm$capturedDrops == null) {
            mm$capturedDrops = new ArrayList<>();
        }
    }

    @Override
    public List<ItemStack> mm$stopCapturingDrops() {
        List<ItemStack> drops = mm$capturedDrops;
        mm$capturedDrops = null;
        return drops;
    }

    @Inject(method = "spawnEntity", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isChunkLoaded(IIZ)Z"))
    public void mm$captureEntityItems(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof EntityItem entityItem && mm$capturedDrops != null) {
            mm$capturedDrops.add(entityItem.getItem());
            cir.setReturnValue(true);
        }
    }
}
