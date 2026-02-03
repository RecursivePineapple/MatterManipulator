package matter_manipulator.mixin.mixins.minecraft;

import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(World.class)
public interface AccessorWorld {

    @Invoker("isChunkLoaded")
    boolean mm$isChunkLoaded(int x, int z, boolean allowEmpty);

}
