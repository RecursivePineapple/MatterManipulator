package matter_manipulator.common.resources.item.ios;

import java.util.Optional;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;

import org.jetbrains.annotations.NotNull;

import matter_manipulator.common.items.ItemCluster;
import matter_manipulator.core.item.ImmutableItemStack;
import matter_manipulator.common.utils.items.ItemUtils;
import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.resources.item.ItemStackIO;
import matter_manipulator.core.resources.item.ItemStackIOFactory;
import matter_manipulator.core.resources.item.ItemStackIteratorBuilder;

public class DroppingItemStackIOFactory implements ItemStackIOFactory {

    @Override
    public Optional<ItemStackIO> getIO(ManipulatorContext context, IDataStorage storage) {
        EntityPlayer player = context.getRealPlayer();

        return Optional.of(new ItemStackIO() {

            @Override
            public int store(ImmutableItemStack stack) {
                AxisAlignedBB searchbb = player.getEntityBoundingBox().expand(5, 2, 5);

                int remaining = stack.getCount();

                for (Entity entity : player.world.getEntitiesInAABBexcluding(player, searchbb, EntityItem.class::isInstance)) {
                    EntityItem item = (EntityItem) entity;

                    ItemStack cluster = item.getItem();

                    if (!(cluster.getItem() instanceof ItemCluster)) continue;

                    ItemStack contents = ItemCluster.getContents(cluster);

                    if (!ItemUtils.areStacksBasicallyEqual(contents, stack.toStackFast())) continue;

                    int xfer = Math.min(Integer.MAX_VALUE - contents.getCount(), stack.getCount());

                    if (contents.isEmpty()) {
                        contents = stack.toStack(xfer);
                    } else {
                        contents.grow(xfer);
                    }

                    remaining -= xfer;

                    item.setItem(ItemCluster.makeCluster(contents));
                }

                if (remaining > 0) {
                    ItemStack cluster = ItemCluster.makeCluster(stack.toStack(remaining));

                    EntityItem item = new EntityItem(player.world, player.posX + 0.5, player.posY + 0.5, player.posZ + 0.5, cluster);

                    item.motionX = 0;
                    item.motionY = 0;
                    item.motionZ = 0;

                    player.world.spawnEntity(item);
                }

                return 0;
            }

            @Override
            @NotNull
            public ItemStackIteratorBuilder iterator() {
                return ItemStackIteratorBuilder.EMPTY;
            }
        });
    }
}
