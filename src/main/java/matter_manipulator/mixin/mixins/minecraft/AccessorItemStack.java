package matter_manipulator.mixin.mixins.minecraft;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStack.class)
public interface AccessorItemStack {

    @Accessor(value = "capabilities", remap = false)
    CapabilityDispatcher mm$getCapabilities();
}
