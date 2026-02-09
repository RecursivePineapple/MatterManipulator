package matter_manipulator.common.structure;

import java.util.Optional;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import matter_manipulator.common.data.AABBMap;
import matter_manipulator.core.meta.MetaKey;
import matter_manipulator.core.meta.MetadataContainer;

public class StructureOverlord {

    private final AABBMap<MultiblockController<?>> controllers = new AABBMap<>();

    private StructureOverlord() { }

    private static final SOMetaKey META_KEY = new SOMetaKey();

    private static class SOMetaKey implements MetaKey<StructureOverlord> {

        @Override
        public Optional<StructureOverlord> getDefault() {
            return Optional.of(new StructureOverlord());
        }
    }

    public static StructureOverlord get(WorldServer world) {
        return ((MetadataContainer) world).getMetaValue(META_KEY);
    }

    public void causeMachineUpdate(BlockPos pos) {
        controllers.get(pos.getX(), pos.getY(), pos.getZ(), controller -> controller.onMachineUpdate(pos));
    }

    public void addController(MultiblockController<?> controller) {
        controllers.add(controller);
    }

    public void removeController(MultiblockController<?> controller) {
        controllers.remove(controller);
    }
}
