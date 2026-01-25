package matter_manipulator.mixin;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import matter_manipulator.common.utils.DataUtils;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.ItemStackWrapper;

public interface BlockCaptureDrops {

    void mm$captureDrops();

    List<ItemStack> mm$stopCapturingDrops();

    static CaptureGuard mm$captureDrops(World world, Consumer<List<ResourceStack>> fn) {
        if (world instanceof BlockCaptureDrops drops) {
            drops.mm$captureDrops();
        }

        return new CaptureGuard(world, fn);
    }

    static List<ItemStack> mm$stopCapturingDrops(World world) {
        if (world instanceof BlockCaptureDrops drops) {
            return drops.mm$stopCapturingDrops();
        } else {
            return Collections.emptyList();
        }
    }

    class CaptureGuard implements AutoCloseable {
        private final World world;
        private final Consumer<List<ResourceStack>> fn;

        private CaptureGuard(World world, Consumer<List<ResourceStack>> fn) {
            this.world = world;
            this.fn = fn;
        }

        @Override
        public void close() {
            fn.accept(DataUtils.mapToList(mm$stopCapturingDrops(world), ItemStackWrapper::new));
        }
    }
}
