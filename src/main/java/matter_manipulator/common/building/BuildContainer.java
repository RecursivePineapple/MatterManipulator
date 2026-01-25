package matter_manipulator.common.building;

import java.util.concurrent.Future;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import matter_manipulator.common.context.BuildingContextImpl;
import matter_manipulator.common.items.ItemMatterManipulator;
import matter_manipulator.core.building.IBuildable;
import matter_manipulator.core.meta.MetaKey;

public class BuildContainer {

    public EntityPlayerMP player;
    private final EnumHand hand;

    public BuildingContextImpl context;

    public Future<IBuildable> task;
    public int ticksWaited = 0;

    public IBuildable buildable;

    public BuildContainer(World world, EntityPlayerMP player, EnumHand hand, Future<IBuildable> task) {
        this.player = player;
        this.hand = hand;
        this.task = task;

        this.context = new BuildingContextImpl(world, player, null, null);
    }

    public void load() {
        context.manipulator = player.getHeldItem(hand);
        context.state = ItemMatterManipulator.getState(context.manipulator);
    }

    public void save() {
        ItemMatterManipulator.setState(player.getHeldItem(hand), context.state);

        context.manipulator = null;
        context.state = null;
    }

    public static class BuildContainerMetaKey implements MetaKey<BuildContainer> {
        public static final BuildContainerMetaKey INSTANCE = new BuildContainerMetaKey();
    }
}
