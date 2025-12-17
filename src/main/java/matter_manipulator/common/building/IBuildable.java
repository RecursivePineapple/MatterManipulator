package matter_manipulator.common.building;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Something that can be built.
 */
public interface IBuildable {

    public void tryPlaceBlocks(ItemStack stack, EntityPlayer player);

    public void onStopped();
}
