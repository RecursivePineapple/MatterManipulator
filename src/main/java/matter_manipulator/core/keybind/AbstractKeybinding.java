package matter_manipulator.core.keybind;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import matter_manipulator.common.context.ManipulatorContextImpl;
import matter_manipulator.common.items.ItemMatterManipulator;
import matter_manipulator.common.networking.MMAction;
import matter_manipulator.common.state.MMState;
import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.interop.MMRegistries;

public abstract class AbstractKeybinding implements ManipulatorKeybind {

    protected final ResourceLocation id;

    protected final MMAction action;

    @SideOnly(Side.CLIENT)
    protected ClientDelegate delegate;

    public AbstractKeybinding(ResourceLocation id, String name, String category, KeyModifier modifier, int keycode) {
        this.id = id;

        this.action = MMAction.server(id, player -> {
            if (!tryInvoke(player, EnumHand.MAIN_HAND)) tryInvoke(player, EnumHand.OFF_HAND);
        });

        if (FMLCommonHandler.instance().getSide().isClient()) {
            delegate = ClientDelegate.create(this, name, category, modifier, keycode);
        }

        MMRegistries.registerKeybind(this);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public KeyBinding getKeybinding() {
        return delegate.keyBinding;
    }

    @Override
    public ResourceLocation getKeybindId() {
        return id;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onPressed() {
        EntityPlayer player = Minecraft.getMinecraft().player;

        if (tryInvoke(player, EnumHand.MAIN_HAND) || tryInvoke(player, EnumHand.OFF_HAND)) {
            action.sendToServer();
        }
    }

    protected boolean tryInvoke(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);

        if (held.isEmpty() || !(held.getItem() instanceof ItemMatterManipulator)) return false;

        MMState state = ItemMatterManipulator.getState(held);

        ManipulatorContextImpl context = new ManipulatorContextImpl(player.world, player, held, state);

        return process(context);
    }

    @Override
    public abstract boolean process(ManipulatorContext context);

    @SideOnly(Side.CLIENT)
    protected static class ClientDelegate {

        private final AbstractKeybinding owner;
        public KeyBinding keyBinding;

        private ClientDelegate(AbstractKeybinding owner, String name, String category, KeyModifier modifier, int keycode) {
            this.owner = owner;

            keyBinding = new KeyBinding(name, KeyConflictContext.IN_GAME, modifier, keycode, category);

            ClientRegistry.registerKeyBinding(keyBinding);

            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void handleKey(KeyInputEvent event) {
            if (keyBinding.isPressed()) {
                owner.onPressed();
            }
        }

        public static ClientDelegate create(AbstractKeybinding owner, String name, String category, KeyModifier modifier, int keycode) {
            return new ClientDelegate(owner, name, category, modifier, keycode);
        }
    }
}
