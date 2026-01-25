package matter_manipulator.common.context;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import matter_manipulator.common.state.MMState;
import matter_manipulator.core.context.ManipulatorContext;

public class ManipulatorContextImpl implements ManipulatorContext {

    public final World world;

    public final EntityPlayer player;
    public ItemStack manipulator;
    public MMState state;

    public ManipulatorContextImpl(World world, EntityPlayer player, ItemStack manipulator, MMState state) {
        this.world = world;
        this.player = player;
        this.manipulator = manipulator;
        this.state = state;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public EntityPlayer getRealPlayer() {
        return player;
    }

    @Override
    public ItemStack getManipulator() {
        return manipulator;
    }

    @Override
    public MMState getState() {
        return state;
    }
}
