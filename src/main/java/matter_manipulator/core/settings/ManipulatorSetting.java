package matter_manipulator.core.settings;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Contract;

import com.cleanroommc.modularui.widget.Widget;
import matter_manipulator.core.persist.IDataStorage;

public interface ManipulatorSetting<T> {

    @Contract(pure = true)
    ResourceLocation getSettingID();

    @Contract(pure = true)
    String getLocalizedName();

    <W extends Widget<W>> W createEditor(Supplier<T> getter, Consumer<T> setter);

    T load(IDataStorage storage);
    void save(IDataStorage storage, T value);
}
