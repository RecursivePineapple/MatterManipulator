package matter_manipulator.mixin.mixins.minecraft;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import matter_manipulator.core.meta.MetaKey;
import matter_manipulator.core.meta.MetaMap;
import matter_manipulator.core.meta.MetadataContainer;

@Mixin({ EntityPlayer.class, World.class })
public class MixinMiscMetadata implements MetadataContainer {

    @Unique
    private final MetaMap mm$meta = new MetaMap();

    @Nullable
    @Override
    public <T> T getMetaValue(MetaKey<T> key) {
        return mm$meta.getMetaValue(key);
    }

    @Nullable
    @Override
    public <T> T getRequiredMetaValue(MetaKey<T> key) {
        return mm$meta.getRequiredMetaValue(key);
    }

    @Override
    public boolean containsMetaValue(MetaKey<?> key) {
        return mm$meta.containsMetaValue(key);
    }

    @Override
    public <T> T removeMetaValue(MetaKey<T> key) {
        return mm$meta.removeMetaValue(key);
    }

    @Override
    public <T> void putMetaValue(MetaKey<T> key, T value) {
        mm$meta.putMetaValue(key, value);
    }
}
