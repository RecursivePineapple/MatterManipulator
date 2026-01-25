package matter_manipulator.core.context;

import java.util.OptionalInt;

import net.minecraft.item.ItemStack;

import matter_manipulator.common.items.ItemMatterManipulator;
import matter_manipulator.common.items.MMUpgrades;
import matter_manipulator.common.items.ManipulatorTier;
import matter_manipulator.common.state.MMState;
import matter_manipulator.core.settings.ManipulatorSetting;

public interface StackManipulatorContext {

    ItemStack getManipulator();

    MMState getState();

    default ManipulatorTier getTier() {
        return ((ItemMatterManipulator) getManipulator().getItem()).tier;
    }

    default int getPlaceSpeed() {
        int speed = getTier().placeSpeed;

        if (hasUpgrade(MMUpgrades.Speed)) {
            speed *= 2;
        }

        return speed;
    }

    default OptionalInt getMaxRange() {
        return getTier().maxRange;
    }

    default boolean hasUpgrade(MMUpgrades upgrade) {
        return getState().hasUpgrade(upgrade);
    }

    default boolean hasCapability(int flags) {
        return getState().hasCap(flags);
    }

    default <T> T getSettingValue(ManipulatorSetting<T> setting) {
        return setting.load(getState().settings);
    }

    default <T> void setSettingValue(ManipulatorSetting<T> setting, T value) {
        setting.save(getState().settings, value);
    }

    default void saveState() {
        ItemMatterManipulator.setState(getManipulator(), getState());
    }
}
