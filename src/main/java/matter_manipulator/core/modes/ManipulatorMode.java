package matter_manipulator.core.modes;

import java.util.List;
import java.util.Optional;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Contract;

import com.cleanroommc.modularui.api.drawable.IKey;
import matter_manipulator.client.gui.BranchableRadialMenu;
import matter_manipulator.client.rendering.ModeRenderer;
import matter_manipulator.common.networking.MMPacketBuffer;
import matter_manipulator.core.building.IBuildable;
import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.util.Coroutine;

public interface ManipulatorMode<Config, Buildable extends IBuildable> {

    @Contract(pure = true)
    ResourceLocation getModeID();

    @Contract(pure = true)
    String getLocalizedName();

    @Contract(pure = true)
    @SideOnly(Side.CLIENT)
    ModeRenderer<Config, Buildable> getRenderer(ManipulatorContext context);

    void addTooltipInfo(ManipulatorContext context, List<String> lines);

    void addMenuItems(ManipulatorContext context, BranchableRadialMenu menu);

    default void addModeSelect(ManipulatorContext context, BranchableRadialMenu menu) {
        menu.option()
            .label(IKey.str(getLocalizedName()))
            .onClicked(() -> {
                context.getState().activeMode = getModeID();
                context.getState().save.run();
            })
            .done();
    }

    @Contract(mutates = "param2")
    Optional<Config> onPickBlock(Config config, ManipulatorContext context);

    @Contract(mutates = "param2")
    Optional<Config> onRightClick(Config config, ManipulatorContext context);

    default boolean handleRickClick(ManipulatorContext context) {
        IDataStorage storage = context.getState().getActiveModeConfigStorage();

        Config config = this.loadConfig(storage);

        Optional<Config> newConfig = this.onRightClick(config, context);

        if (newConfig.isPresent()) {
            this.saveConfig(storage, newConfig.get());

            context.saveState();

            return true;
        } else {
            return false;
        }
    }

    /// Creates a coroutine which will produce a [Buildable] once finished. This method should just create the objects,
    /// no analysis should be done until the coroutine is executed.
    @Contract(mutates = "param2")
    Coroutine<Buildable> startAnalysis(Config config, ManipulatorContext context);

    Config loadConfig(IDataStorage storage);
    void saveConfig(IDataStorage storage, Config config);

    void write(MMPacketBuffer buffer, Buildable buildable);
    Buildable read(MMPacketBuffer buffer);
}
