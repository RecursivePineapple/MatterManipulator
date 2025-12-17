package matter_manipulator.common.items.manipulator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.client.event.MouseEvent;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;

import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber;
import com.recursive_pineapple.matter_manipulator.GlobalMMConfig.InteractionConfig;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState.PlaceMode;
import com.recursive_pineapple.matter_manipulator.common.networking.Messages;

import org.lwjgl.input.Keyboard;

@EventBusSubscriber
public class MMKeyInputs {

    public static final KeyBinding CONTROL = new KeyBinding("key.mm-ctrl", Keyboard.KEY_LCONTROL, "key.mm");
    public static final KeyBinding CUT = new KeyBinding("key.mm-cut", Keyboard.KEY_X, "key.mm");
    public static final KeyBinding COPY = new KeyBinding("key.mm-copy", Keyboard.KEY_C, "key.mm");
    public static final KeyBinding PASTE = new KeyBinding("key.mm-paste", Keyboard.KEY_V, "key.mm");
    public static final KeyBinding RESET = new KeyBinding("key.mm-reset", Keyboard.KEY_Z, "key.mm");

    public static void init() {
        ClientRegistry.registerKeyBinding(CONTROL);
        ClientRegistry.registerKeyBinding(CUT);
        ClientRegistry.registerKeyBinding(COPY);
        ClientRegistry.registerKeyBinding(PASTE);
        ClientRegistry.registerKeyBinding(RESET);
    }

    @SubscribeEvent
    public static void onKeyPressed(KeyInputEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        ItemStack held = player.getHeldItem();

        if (held == null || !(held.getItem() instanceof ItemMatterManipulator)) return;

        MMState state = ItemMatterManipulator.getState(held);

        if (CONTROL.getKeyCode() != 0 && !CONTROL.getIsKeyPressed()) return;

        if (CUT.isPressed()) {
            if (state.config.placeMode != PlaceMode.MOVING) {
                Messages.SetPlaceMode.sendToServer(PlaceMode.MOVING);
            }

            if (InteractionConfig.pasteAutoClear) {
                Messages.ClearCoords.sendToServer();

                if (InteractionConfig.resetTransform) {
                    Messages.ClearTransform.sendToServer();
                    Messages.ResetArray.sendToServer();
                }
            }

            Messages.MarkCut.sendToServer();

            return;
        }

        if (COPY.isPressed()) {
            if (state.config.placeMode != PlaceMode.COPYING) {
                Messages.SetPlaceMode.sendToServer(PlaceMode.COPYING);
            }

            if (InteractionConfig.pasteAutoClear) {
                Messages.ClearCoords.sendToServer();

                if (InteractionConfig.resetTransform) {
                    Messages.ClearTransform.sendToServer();
                    Messages.ResetArray.sendToServer();
                }
            }

            Messages.MarkCopy.sendToServer();

            return;
        }

        if (PASTE.isPressed()) {
            // set the mode to copying if we aren't in a mode supports pasting (moving/copying)
            if (state.config.placeMode != PlaceMode.COPYING && state.config.placeMode != PlaceMode.MOVING) {
                Messages.SetPlaceMode.sendToServer(PlaceMode.COPYING);
            }

            Messages.MarkPaste.sendToServer();

            return;
        }

        if (RESET.isPressed()) {
            Messages.ClearCoords.sendToServer();

            if (InteractionConfig.resetTransform) {
                Messages.ClearTransform.sendToServer();
                Messages.ResetArray.sendToServer();
            }

            return;
        }
    }

    /**
     * For some reason, the key bindings will be phantom pressed if you change your hotbar while they're pressed.
     * If you do the following, it will act up.
     * <ol>
     * <li>Bind hotbar 1 to C</li>
     * <li>Press C</li>
     * <li>Scroll back to the manipulator</li>
     * <li>Press control</li>
     * <li>Magically presses C somehow</li>
     * </ol>
     */
    @SubscribeEvent
    public static void onMouseScroll(MouseEvent event) {
        if (event.dwheel == 0) return;

        CUT.unpressKey();
        COPY.unpressKey();
        PASTE.unpressKey();
        RESET.unpressKey();
    }
}
