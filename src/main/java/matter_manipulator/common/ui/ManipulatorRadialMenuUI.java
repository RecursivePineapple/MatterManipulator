package matter_manipulator.common.ui;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import matter_manipulator.client.gui.RadialMenu;
import matter_manipulator.client.gui.RadialMenuBuilder;
import matter_manipulator.common.context.ManipulatorContextImpl;
import matter_manipulator.common.items.ItemMatterManipulator;
import matter_manipulator.common.state.MMState;
import matter_manipulator.core.modes.ManipulatorMode;

public class ManipulatorRadialMenuUI implements IGuiHolder<ManipulatorGuiData> {

    @Override
    public ModularPanel buildUI(ManipulatorGuiData data, PanelSyncManager syncManager, UISettings settings) {
        ModularPanel panel = new ModularPanel("ManipulatorRadialMenu");

        panel.fullScreenInvisible();

        panel.child(createMenu(data, syncManager, settings));

        return panel;
    }

    // spotless:off
    private RadialMenu createMenu(ManipulatorGuiData data, PanelSyncManager syncManager, UISettings settings) {
        ItemStack heldStack = data.getManipulatorStack();
        MMState initialState = ItemMatterManipulator.getState(heldStack);

        ManipulatorContextImpl context = new ManipulatorContextImpl(data.getWorld(), data.getPlayer(), heldStack, initialState);

        return new RadialMenuBuilder("menu", syncManager)
            .innerIcon(heldStack)
            .pipe(builder -> {
                addCommonOptions(builder, context);

                ManipulatorMode<?, ?> mode = initialState.getActiveMode();

                if (mode != null) {
                    mode.addMenuItems(context, builder);
                }
            })
            .build()
            .relativeToScreen()
            .top(0)
            .bottom(0)
            .left(0)
            .right(0);
    }

    @SuppressWarnings("rawtypes")
    private void addCommonOptions(RadialMenuBuilder builder, ManipulatorContextImpl context) {
        builder
            .branch()
                .label(IKey.str("Settings"))
                .branch()
                    .label(IKey.str("Edit Remove Mode"))
                    .option()
                        .label(IKey.str("Remove All Blocks"))
                    .done()
                    .option()
                        .label(IKey.str("Remove Replaceable Blocks"))
                    .done()
                    .option()
                        .label(IKey.str("Remove No Blocks"))
                    .done()
                .done()
            .done()
            .branch()
                .label(IKey.str("Change Mode"))
                .pipe(modes -> {
                    for (ManipulatorMode mode : context.getState().getPossibleModes()) {
                        mode.addModeSelect(context, modes);;
                    }
                })
            .done();
    }
}
