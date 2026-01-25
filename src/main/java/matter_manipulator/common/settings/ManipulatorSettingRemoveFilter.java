package matter_manipulator.common.settings;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.cleanroommc.modularui.widget.Widget;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.settings.ManipulatorSetting;

public class ManipulatorSettingRemoveFilter implements ManipulatorSetting {

    @Override
    public String getLocalizedName() {
        return "Remove Filter";
    }

    @Override
    public Object load(IDataStorage storage) {
        return null;
    }

    @Override
    public void save(IDataStorage storage, Object value) {

    }

    @Override
    public Widget createEditor(Supplier getter, Consumer setter) {
        return null;
    }
}
