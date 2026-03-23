package matter_manipulator.common.keybind;

import net.minecraftforge.client.settings.KeyModifier;

import org.lwjgl.input.Keyboard;

import matter_manipulator.MMMod;
import matter_manipulator.common.modes.CopyableMode;
import matter_manipulator.common.modes.CuttableMode;
import matter_manipulator.common.modes.PasteableMode;
import matter_manipulator.common.modes.ResettableMode;
import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.keybind.AbstractKeybinding;

public class MMKeybinds {

    public static final AbstractKeybinding RESET = new AbstractKeybinding(
        MMMod.loc("key-reset"),
        "key.mm-reset",
        "key.mm",
        KeyModifier.CONTROL,
        Keyboard.KEY_Z) {

        @Override
        public boolean process(ManipulatorContext context) {
            if (context.getState()
                .getActiveMode() instanceof ResettableMode resettable) {
                return resettable.onResetPressed(context);
            } else {
                return false;
            }
        }
    };

    public static final AbstractKeybinding CUT = new AbstractKeybinding(
        MMMod.loc("key-cut"),
        "key.mm-cut",
        "key.mm",
        KeyModifier.CONTROL,
        Keyboard.KEY_X) {

        @Override
        public boolean process(ManipulatorContext context) {
            if (context.getState()
                .getActiveMode() instanceof CuttableMode cuttable) {
                return cuttable.onCutPressed(context);
            } else {
                return false;
            }
        }
    };

    public static final AbstractKeybinding COPY = new AbstractKeybinding(
        MMMod.loc("key-copy"),
        "key.mm-copy",
        "key.mm",
        KeyModifier.CONTROL,
        Keyboard.KEY_C) {

        @Override
        public boolean process(ManipulatorContext context) {
            if (context.getState()
                .getActiveMode() instanceof CopyableMode copyable) {
                return copyable.onCopyPressed(context);
            } else {
                return false;
            }
        }
    };

    public static final AbstractKeybinding PASTE = new AbstractKeybinding(
        MMMod.loc("key-paste"),
        "key.mm-paste",
        "key.mm",
        KeyModifier.CONTROL,
        Keyboard.KEY_V) {

        @Override
        public boolean process(ManipulatorContext context) {
            if (context.getState()
                .getActiveMode() instanceof PasteableMode pasteable) {
                return pasteable.onPastePressed(context);
            } else {
                return false;
            }
        }
    };

    public static void init() {
        // loads the class
    }
}
