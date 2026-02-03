package matter_manipulator.common.context;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import lombok.Setter;
import matter_manipulator.common.state.MMState;
import matter_manipulator.core.context.BlockAnalysisContext;
import matter_manipulator.core.context.ManipulatorContext;

public class AnalysisContextImpl extends ManipulatorContextImpl implements BlockAnalysisContext {

    @Setter
    public BlockPos pos;

    public AnalysisContextImpl(World world, EntityPlayer player, ItemStack manipulator, MMState state) {
        super(world, player, manipulator, state);
    }

    public AnalysisContextImpl(ManipulatorContext base) {
        this(base.getWorld(), base.getRealPlayer(), base.getManipulator(), base.getState());
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }
}
