package matter_manipulator.common.settings;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class RemoveFilter {

    public FilterType filterType = FilterType.Blacklist;

    public ObjectOpenHashSet<ResourceLocation> filter = new ObjectOpenHashSet<>();

    public boolean matchesFilter(IBlockState state) {
        boolean match = filter.contains(state.getBlock().getRegistryName());

        return false; // TODO: this
    }
}
