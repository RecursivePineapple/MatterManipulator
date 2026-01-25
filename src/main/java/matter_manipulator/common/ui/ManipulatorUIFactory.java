package matter_manipulator.common.ui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import matter_manipulator.Tags;
import matter_manipulator.common.items.ItemMatterManipulator;

public class ManipulatorUIFactory extends AbstractUIFactory<ManipulatorGuiData> {

    public static final ManipulatorUIFactory INSTANCE = new ManipulatorUIFactory();

    protected ManipulatorUIFactory() {
        super(Tags.MODID + ":manipulator");
    }

    @Override
    public @NotNull IGuiHolder<ManipulatorGuiData> getGuiHolder(ManipulatorGuiData data) {
        return (ItemMatterManipulator) data.getManipulatorStack().getItem();
    }

    @Override
    public void writeGuiData(ManipulatorGuiData guiData, PacketBuffer buffer) {
        buffer.writeInt(guiData.hand.ordinal());
    }

    @Override
    public @NotNull ManipulatorGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new ManipulatorGuiData(player, EnumHand.values()[buffer.readInt()]);
    }
}
