package matter_manipulator.core.keybind;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import matter_manipulator.core.context.ManipulatorContext;

public interface ManipulatorKeybind {

    @SideOnly(Side.CLIENT)
    KeyBinding getKeybinding();

    ResourceLocation getKeybindId();

    @SideOnly(Side.CLIENT)
    void onPressed();

    boolean process(ManipulatorContext context);
}
