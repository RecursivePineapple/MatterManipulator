package matter_manipulator.mixin.mixins.minecraft;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import matter_manipulator.core.meta.MetaKey;
import matter_manipulator.core.meta.MetaMap;
import matter_manipulator.core.meta.MetadataContainer;

@Mixin(EntityPlayer.class)
public class MixinEntityPlayer implements MetadataContainer {

    @Unique
    private final MetaMap meta = new MetaMap();

    @Nullable
    @Override
    public <T> T getMetaValue(MetaKey<T> key) {
        return meta.getMetaValue(key);
    }

    @Nullable
    @Override
    public <T> T getRequiredMetaValue(MetaKey<T> key) {
        return meta.getRequiredMetaValue(key);
    }

    @Override
    public boolean containsMetaValue(MetaKey<?> key) {
        return meta.containsMetaValue(key);
    }

    @Override
    public <T> T removeMetaValue(MetaKey<T> key) {
        return meta.removeMetaValue(key);
    }

    @Override
    public <T> void put(MetaKey<T> key, T value) {
        meta.put(key, value);
    }
}
