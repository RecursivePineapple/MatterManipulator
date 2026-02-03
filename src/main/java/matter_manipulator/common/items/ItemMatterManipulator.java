package matter_manipulator.common.items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import matter_manipulator.MMMod;
import matter_manipulator.Tags;
import matter_manipulator.common.building.BuildContainer;
import matter_manipulator.common.building.BuildContainer.BuildContainerMetaKey;
import matter_manipulator.common.context.ManipulatorContextImpl;
import matter_manipulator.common.context.StackManipulatorContextImpl;
import matter_manipulator.common.networking.MMAction;
import matter_manipulator.common.state.MMState;
import matter_manipulator.common.ui.ManipulatorGuiData;
import matter_manipulator.common.ui.ManipulatorRadialMenuUI;
import matter_manipulator.common.ui.ManipulatorUIFactory;
import matter_manipulator.common.utils.MCUtils;
import matter_manipulator.core.meta.MetadataContainer;
import matter_manipulator.core.modes.ManipulatorMode;
import matter_manipulator.core.persist.IDataStorage;
import mcp.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemMatterManipulator extends Item implements IGuiHolder<ManipulatorGuiData> {

    public final ManipulatorTier tier;

    public ItemMatterManipulator(ManipulatorTier tier) {
        String name = "matter-manipulator-" + tier.tier;

        setCreativeTab(CreativeTabs.TOOLS);
        setRegistryName(Tags.MODID, name);
        setTranslationKey(name);
        setMaxStackSize(1);

        this.tier = tier;
    }

    private static int counter = 0;
    public static final int CONNECTS_TO_AE = 0b1 << counter++;
    public static final int CONNECTS_TO_UPLINK = 0b1 << counter++;
    public static final int ALLOW_REMOVING = 0b1 << counter++;
    public static final int ALLOW_GEOMETRY = 0b1 << counter++;
    public static final int ALLOW_CONFIGURING = 0b1 << counter++;
    public static final int ALLOW_COPYING = 0b1 << counter++;
    public static final int ALLOW_EXCHANGING = 0b1 << counter++;
    public static final int ALLOW_MOVING = 0b1 << counter++;
    public static final int ALLOW_CABLES = 0b1 << counter++;

    public static final int ALL_MODES = ALLOW_GEOMETRY | ALLOW_COPYING | ALLOW_EXCHANGING | ALLOW_MOVING | ALLOW_CABLES;

    // #region Energy

//    @Override
//    public boolean showDurabilityBar(ItemStack stack) {
//        return true;
//    }
//
//    @Override
//    public double getDurabilityForDisplay(ItemStack stack) {
//        return 1d - getCharge(stack) / tier.maxCharge;
//    }
//
//    public void refillPower(ItemStack stack, MMState state) {
//        if (!state.hasUpgrade(MMUpgrades.PowerP2P)) return;
//        if (!state.connectToUplink()) return;
//
//        ItemMatterManipulator manipulator = (ItemMatterManipulator) stack.getItem();
//
//        assert manipulator != null;
//
//        double toFill = manipulator.getMaxCharge(stack) - manipulator.getCharge(stack);
//
//        double drained = state.uplink.drainPower(toFill);
//
//        manipulator.charge(stack, drained, 0, true, false);
//    }
//
//    @Override
//    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int p_77663_4_, boolean p_77663_5_) {
//        if (worldIn.getTotalWorldTime() % 100 == 0) {
//            MMState state = getState(stack);
//
//            refillPower(stack, state);
//        }
//    }

    // #endregion

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        final ItemStack stack = new ItemStack(this, 1);
        stack.setTagCompound(new MMState().save());
        items.add(stack.copy());
        //        this.charge(stack, tier.maxCharge, tier.voltageTier, true, false);
        //        subItems.add(stack);
    }

    @Override
    public IRarity getForgeRarity(ItemStack stack) {
        return switch (tier) {
            case Tier0 -> EnumRarity.COMMON;
            case Tier1 -> EnumRarity.UNCOMMON;
            case Tier2 -> EnumRarity.RARE;
            case Tier3 -> EnumRarity.EPIC;
        };
    }

    public static MMState getState(ItemStack itemStack) {
        MMState state = MMState.load(getOrCreateNbtData(itemStack));

        state.manipulator = (ItemMatterManipulator) itemStack.getItem();

        state.setSaveDelegate(() -> {
            setState(itemStack, state);
        });

        return state;
    }

    public static void setState(ItemStack itemStack, MMState state) {
        itemStack.setTagCompound(state.save());
    }

    public static NBTTagCompound getOrCreateNbtData(ItemStack itemStack) {
        NBTTagCompound ret = itemStack.getTagCompound();
        if (ret == null) {
            ret = new NBTTagCompound();
            itemStack.setTagCompound(ret);
        }
        return ret;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(
        ItemStack itemStack,
        @Nullable World worldIn,
        List<String> desc,
        ITooltipFlag flagIn
    ) {
        MMState state = getState(itemStack);

        ManipulatorContextImpl context = new ManipulatorContextImpl(worldIn, Minecraft.getMinecraft().player, itemStack, state);

        if (!GuiScreen.isShiftKeyDown()) {
            desc.add("Hold shift for more information.");
        } else {
            @SuppressWarnings("rawtypes")
            ManipulatorMode mode = state.getActiveMode();

            desc.add(MCUtils.translate("mm.tooltip.mode", mode == null ? MCUtils.translate("mm.info.none") : mode.getLocalizedName()));

            if (mode != null) {
                //noinspection unchecked
                mode.addTooltipInfo(context, desc);
            }

            List<MMUpgrades> upgrades = new ArrayList<>(state.getInstalledUpgrades());
            upgrades.sort(Comparator.comparingInt(Enum::ordinal));

            if (!upgrades.isEmpty()) {
                desc.add(MCUtils.translate("mm.tooltip.installed_upgrades"));

                for (MMUpgrades upgrade : upgrades) {
                    desc.add("- " + upgrade.getStack().getDisplayName());
                }
            }
        }

        var resources = new ArrayList<>(state.getResources(context).entrySet());

        resources.sort(Comparator.comparing(e -> e.getKey().toString()));

        for (var e : resources) {
            e.getValue().addManipulatorTooltipInfo(context, desc);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);

        if (player.getItemInUseMaxCount() > 0) {
            return ActionResult.newResult(EnumActionResult.PASS, held);
        }

        MMState state = getState(held);

        ManipulatorContextImpl context = new ManipulatorContextImpl(world, player, held, state);

        ManipulatorMode mode = state.getActiveMode();

        if (mode == null || !mode.handleRickClick(context)) {
            if (player.isSneaking()) {
                player.setActiveHand(hand);
            } else {
                if (player instanceof EntityPlayerMP playerMP) {
                    GuiManager.open(ManipulatorUIFactory.INSTANCE, new ManipulatorGuiData(playerMP, hand), playerMP);
                }
            }
        }

        return new ActionResult<>(EnumActionResult.PASS, held);
    }

    private static final MMAction PICK_BLOCK = MMAction.server("pick-block", ItemMatterManipulator::onPickBlock);

    public static boolean onPickBlock(EntityPlayer player) {
        if (player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemMatterManipulator) {
            onPickBlock(player, EnumHand.MAIN_HAND);
            if (player.world.isRemote) PICK_BLOCK.sendToServer();
            return true;
        } else if (player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof ItemMatterManipulator) {
            onPickBlock(player, EnumHand.OFF_HAND);
            if (player.world.isRemote) PICK_BLOCK.sendToServer();
            return true;
        }

        return false;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void onPickBlock(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);

        MMState state = getState(held);

        ManipulatorContextImpl context = new ManipulatorContextImpl(player.world, player, held, state);

        ManipulatorMode mode = state.getActiveMode();

        if (mode != null) {
            IDataStorage storage = state.getActiveModeConfigStorage();

            Object config = mode.loadConfig(storage);

            Optional newConfig = mode.onPickBlock(config, context);

            if (newConfig.isPresent()) {
                mode.saveConfig(storage, newConfig.get());
            }
        }

        setState(held, state);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack p_77626_1_) {
        return Integer.MAX_VALUE;
    }

    private static void stopBuildable(EntityPlayer player) {
        if (!player.world.isRemote) {
            BuildContainer container = ((MetadataContainer) player).removeMetaValue(BuildContainerMetaKey.INSTANCE);

            if (container != null && container.buildable != null) {
                container.load();

                container.buildable.onStop(container.context);

                container.save();
            }
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        if (entityLiving instanceof EntityPlayerMP player) {
            stopBuildable(player);
        }
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase entity, int ticksUsed) {
        if (!(entity instanceof EntityPlayerMP player)) return;
        if (!player.world.isRemote) return;

        BuildContainer container = ((MetadataContainer) player).getMetaValue(BuildContainerMetaKey.INSTANCE);

        if (container == null) {
            return;
        }

        container.load();

        if (container.task != null) {
            if (container.task.isDone()) {
                try {
                    container.buildable = container.task.get(1, TimeUnit.MILLISECONDS);
                    container.task = null;
                } catch (InterruptedException e) {
                    MMMod.LOG.error("Could not wait for build to compile", e);
                    // Not fatal, but definitely weird
                } catch (ExecutionException e) {
                    MMMod.LOG.error("Could not wait for build to compile", e);
                    // Fatal
                    player.stopActiveHand();
                    MCUtils.sendErrorToPlayer(player, MCUtils.translate("mm.info.error.build_compile_crash"));
                } catch (TimeoutException e) {
                    // Not finished, do nothing
                }
            }

            if (ticksUsed > 40 && ticksUsed % 20 == 0) {
                MCUtils.sendErrorToPlayer(player, MCUtils.translate("mm.info.error.build_compile_waiting"));
            }
        }

        if (container.buildable != null) {
            int placeTicks = tier.placeTicks;

            if (container.context.state.hasUpgrade(MMUpgrades.Speed)) {
                placeTicks = placeTicks / 2;
            }

            if (ticksUsed >= 10 && ticksUsed % placeTicks == 0) {
                try {
                    container.buildable.onBuildTick(container.context);

                    container.context.onBuildTickFinished();

                    if (container.buildable.isDone()) {
                        player.stopActiveHand();
                    }
                } catch (Throwable t) {
                    MMMod.LOG.error("Could not place blocks", t);
                    MCUtils.sendErrorToPlayer(player, MCUtils.translate("mm.info.error.build_place_crash"));
                    player.stopActiveHand();
                }
            }
        }

        container.save();
    }

    @Override
    public ModularPanel buildUI(ManipulatorGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return new ManipulatorRadialMenuUI().buildUI(data, syncManager, settings);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new ICapabilityProvider() {

            @Override
            public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
                MMState state = getState(stack);

                StackManipulatorContextImpl context = new StackManipulatorContextImpl(stack, state);

                for (var e : state.getResources(context).values()) {
                    if (e.hasCapability(capability, facing)) return true;
                }

                return false;
            }

            @Override
            public @Nullable <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
                MMState state = getState(stack);

                StackManipulatorContextImpl context = new StackManipulatorContextImpl(stack, state);

                for (var e : state.getResources(context).values()) {
                    if (e.hasCapability(capability, facing)) {
                        return e.getCapability(capability, facing);
                    }
                }

                return null;
            }
        };
    }

    @EventBusSubscriber
    public static class EventHandler {

        /// Used for detecting middle mouse button clicks.
        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public void onMouseEvent(MouseEvent event) {
            final EntityPlayer player = Minecraft.getMinecraft().player;

            if (player == null || player.isDead) { return; }

            if (event.getButton() == 2 /* MMB */ && event.isButtonstate()) {
                if (onPickBlock(player)) {
                    event.setCanceled(true);

                    PICK_BLOCK.sendToServer();
                }
            }
        }

        @SubscribeEvent
        public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            stopBuildable(event.player);
        }

        @SubscribeEvent
        public void onPlayerKilled(LivingDeathEvent event) {
            if (event.getEntity() instanceof EntityPlayer player) {
                stopBuildable(player);
            }
        }
    }
}
