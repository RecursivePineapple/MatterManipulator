package matter_manipulator.common.interop.resetters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.oredict.OreDictionary;

import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.core.interop.interfaces.BlockAdapter;
import matter_manipulator.core.interop.interfaces.BlockResetter;
import matter_manipulator.core.item.ItemStackLike;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.ItemStackWrapper;
import matter_manipulator.mixin.BlockCaptureDrops;

public class BlockRemover implements BlockResetter {

    @Override
    public List<ResourceStack> resetBlock(ManipulatorContext context, BlockPos pos) {
        World world = context.getWorld();

        IBlockState state = world.getBlockState(pos);

        BlockAdapter adapter = MMRegistriesInternal.getBlockAdapter(state);

        // Something strange that the MM doesn't understand
        if (adapter == null) return Collections.emptyList();
        // A fluid or something
        if (!(adapter.getResourceForm(state) instanceof ItemStackLike stack)) return Collections.emptyList();

        boolean isOre = false;

        for (int id : OreDictionary.getOreIDs(stack.toStackFast(1))) {
            if (OreDictionary.getOreName(id).startsWith("ore")) {
                isOre = true;
                break;
            }
        }

        List<ResourceStack> resources = new ArrayList<>();

        try (var guard = BlockCaptureDrops.mm$captureDrops(world, resources::addAll)) {
            NonNullList<ItemStack> drops = NonNullList.create();

            state.getBlock().getDrops(drops, world, pos, state, 0);

            float chance = ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, 0, 1f, false, context.getRealPlayer());

            for (ItemStack drop : drops) {
                if (world.rand.nextFloat() <= chance) {
                    resources.add(new ItemStackWrapper(drop));
                }
            }

            world.setBlockToAir(pos);
        }

        return isOre ? Collections.emptyList() : resources;
    }
}
