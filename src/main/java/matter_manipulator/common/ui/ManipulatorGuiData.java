package matter_manipulator.common.ui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import com.cleanroommc.modularui.factory.GuiData;

public class ManipulatorGuiData extends GuiData {

    public final EnumHand hand;

    public ManipulatorGuiData(EntityPlayer player, EnumHand hand) {
        super(player);
        this.hand = hand;
    }

    public ItemStack getManipulatorStack() {
        return getPlayer().getHeldItem(hand);
    }

    public void setManipulatorStack(ItemStack stack) {
        getPlayer().setHeldItem(hand, stack);
    }
}
