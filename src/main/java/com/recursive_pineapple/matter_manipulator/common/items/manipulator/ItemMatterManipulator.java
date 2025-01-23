package com.recursive_pineapple.matter_manipulator.common.items.manipulator;

import static com.recursive_pineapple.matter_manipulator.common.utils.MMUtils.BLUE;
import static com.recursive_pineapple.matter_manipulator.common.utils.MMUtils.GREEN;
import static com.recursive_pineapple.matter_manipulator.common.utils.MMUtils.RED;
import static com.recursive_pineapple.matter_manipulator.common.utils.MMUtils.formatNumbers;
import static com.recursive_pineapple.matter_manipulator.common.utils.MMValues.V;
import static com.recursive_pineapple.matter_manipulator.common.utils.Mods.AppliedEnergistics2;
import static com.recursive_pineapple.matter_manipulator.common.utils.Mods.GregTech;
import static net.minecraftforge.common.util.ForgeDirection.EAST;
import static net.minecraftforge.common.util.ForgeDirection.SOUTH;
import static net.minecraftforge.common.util.ForgeDirection.UP;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.features.INetworkEncodable;

import com.google.common.collect.MapMaker;
import com.gtnewhorizon.gtnhlib.util.AboveHotbarHUD;
import com.gtnewhorizon.structurelib.StructureLibAPI;
import com.gtnewhorizon.structurelib.entity.fx.WeightlessParticleFX;
import com.gtnewhorizons.modularui.api.UIInfos;
import com.gtnewhorizons.modularui.api.drawable.AdaptableUITexture;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.OffsetDrawable;
import com.gtnewhorizons.modularui.api.drawable.shapes.Rectangle;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.CrossAxisAlignment;
import com.gtnewhorizons.modularui.api.math.MainAxisAlignment;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.screen.ModularUIContext;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer;
import com.gtnewhorizons.modularui.common.widget.Column;
import com.gtnewhorizons.modularui.common.widget.DynamicTextWidget;
import com.gtnewhorizons.modularui.common.widget.MultiChildWidget;
import com.gtnewhorizons.modularui.common.widget.Row;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.gtnewhorizons.modularui.common.widget.VanillaButtonWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.NumericWidget;
import com.recursive_pineapple.matter_manipulator.GlobalMMConfig.InteractionConfig;
import com.recursive_pineapple.matter_manipulator.GlobalMMConfig.RenderingConfig;
import com.recursive_pineapple.matter_manipulator.MMMod;
import com.recursive_pineapple.matter_manipulator.client.gui.DirectionDrawable;
import com.recursive_pineapple.matter_manipulator.client.gui.RadialMenuBuilder;
import com.recursive_pineapple.matter_manipulator.client.rendering.BoxRenderer;
import com.recursive_pineapple.matter_manipulator.common.building.BlockSpec;
import com.recursive_pineapple.matter_manipulator.common.building.IBuildable;
import com.recursive_pineapple.matter_manipulator.common.building.PendingBlock;
import com.recursive_pineapple.matter_manipulator.common.building.PendingBuild;
import com.recursive_pineapple.matter_manipulator.common.building.PendingMove;
import com.recursive_pineapple.matter_manipulator.common.data.WeightedSpecList;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMConfig.VoxelAABB;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState.BlockRemoveMode;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState.BlockSelectMode;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState.PendingAction;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState.PlaceMode;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState.Shape;
import com.recursive_pineapple.matter_manipulator.common.networking.Messages;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods.Names;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;

@InterfaceList({
    @Interface(modid = Names.APPLIED_ENERGISTICS2, iface = "appeng.api.features.INetworkEncodable", striprefs = true),
    @Interface(modid = Names.INDUSTRIAL_CRAFT2, iface = "ic2.api.item.ISpecialElectricItem"),
    @Interface(modid = Names.INDUSTRIAL_CRAFT2, iface = "ic2.api.item.IElectricItemManager", striprefs = true),
})
public class ItemMatterManipulator extends Item implements ISpecialElectricItem, IElectricItemManager, INetworkEncodable {

    public final ManipulatorTier tier;

    @SideOnly(Side.CLIENT)
    private MatterManipulatorRenderer renderer;

    public ItemMatterManipulator(ManipulatorTier tier) {
        String name = "itemMatterManipulator" + tier.tier;

        this.setCreativeTab(CreativeTabs.tabTools);
        this.setUnlocalizedName(name);
        this.setMaxStackSize(1);
        this.setTextureName("matter-manipulator" + ":" + name);

        this.tier = tier;

        GameRegistry.registerItem(this, name);
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance()
            .bus()
            .register(this);

        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            renderer = new MatterManipulatorRenderer();
        }
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

    public static enum ManipulatorTier {

        // spotless:off
        Tier0(32, 16, 20, 3,      1_000_000d, ALLOW_GEOMETRY),
        Tier1(64, 32, 10, 5,    100_000_000d, ALLOW_GEOMETRY | CONNECTS_TO_AE | ALLOW_REMOVING | ALLOW_EXCHANGING | ALLOW_CONFIGURING | ALLOW_CABLES),
        Tier2(128, 64, 5, 6,  1_000_000_000d, ALLOW_GEOMETRY | CONNECTS_TO_AE | ALLOW_REMOVING | ALLOW_EXCHANGING | ALLOW_CONFIGURING | ALLOW_CABLES | ALLOW_COPYING | ALLOW_MOVING),
        Tier3(-1, 256, 5, 7, 10_000_000_000d, ALLOW_GEOMETRY | CONNECTS_TO_AE | ALLOW_REMOVING | ALLOW_EXCHANGING | ALLOW_CONFIGURING | ALLOW_CABLES | ALLOW_COPYING | ALLOW_MOVING | CONNECTS_TO_UPLINK);
        // spotless:on

        public final int tier = ordinal();
        public final int maxRange;
        public final int placeSpeed, placeTicks;
        public final int voltageTier;
        public final double maxCharge;
        public final int capabilities;

        private ManipulatorTier(
            int maxRange,
            int placeSpeed,
            int placeTicks,
            int voltageTier,
            double maxCharge,
            int capabilities
        ) {
            this.maxRange = maxRange;
            this.placeSpeed = placeSpeed;
            this.placeTicks = placeTicks;
            this.voltageTier = voltageTier;
            this.maxCharge = maxCharge;
            this.capabilities = capabilities;
        }

        public boolean hasCap(int cap) {
            return (capabilities & cap) == cap;
        }
    }

    // #region Energy

    @Override
    public Item getEmptyItem(ItemStack itemStack) {
        return this;
    }

    @Override
    public boolean canProvideEnergy(ItemStack itemStack) {
        return false;
    }

    @Override
    public double getMaxCharge(ItemStack itemStack) {
        return tier.maxCharge;
    }

    @Override
    public int getTier(ItemStack itemStack) {
        return tier.voltageTier;
    }

    @Override
    public double getTransferLimit(ItemStack itemStack) {
        return V[tier.voltageTier] * 16;
    }

    @Override
    public Item getChargedItem(ItemStack itemStack) {
        return this;
    }

    @Override
    public IElectricItemManager getManager(ItemStack arg0) {
        return this;
    }

    @Override
    public final double charge(
        ItemStack stack,
        double toCharge,
        int voltageTier,
        boolean ignoreTransferLimit,
        boolean simulate
    ) {
        NBTTagCompound tag = getOrCreateNbtData(stack);

        double maxTransfer = ignoreTransferLimit ? toCharge : Math.min(toCharge, getTransferLimit(stack));
        double currentCharge = tag.getDouble("charge");
        double remainingSpace = tier.maxCharge - currentCharge;

        double toConsume = Math.min(maxTransfer, remainingSpace);

        if (!simulate) tag.setDouble("charge", currentCharge + toConsume);

        return toConsume;
    }

    @Override
    public final double discharge(
        ItemStack stack,
        double toDischarge,
        int voltageTier,
        boolean ignoreTransferLimit,
        boolean batteryLike,
        boolean simulate
    ) {
        if (voltageTier != Integer.MAX_VALUE && voltageTier > tier.voltageTier) { return 0; }

        NBTTagCompound tag = getOrCreateNbtData(stack);

        double maxTransfer = ignoreTransferLimit ? toDischarge : Math.min(toDischarge, getTransferLimit(stack));
        double currentCharge = tag.getDouble("charge");

        double toConsume = Math.min(maxTransfer, currentCharge);

        if (!simulate) tag.setDouble("charge", currentCharge - toConsume);

        return toConsume;
    }

    @Override
    public final double getCharge(ItemStack stack) {
        NBTTagCompound tag = getOrCreateNbtData(stack);

        return tag.getDouble("charge");
    }

    @Override
    public final boolean canUse(ItemStack stack, double amount) {
        return getCharge(stack) >= amount;
    }

    @Override
    public final boolean use(ItemStack stack, double toDischarge, EntityLivingBase holder) {
        if (holder instanceof EntityPlayer player && player.capabilities.isCreativeMode) return true;
        double toTransfer = discharge(stack, toDischarge, Integer.MAX_VALUE, true, false, true);
        if (Math.abs(toTransfer - toDischarge) < .0000001) {
            discharge(stack, toDischarge, Integer.MAX_VALUE, true, false, false);
            return true;
        }
        discharge(stack, toDischarge, Integer.MAX_VALUE, true, false, false);
        return false;
    }

    @Override
    public final void chargeFromArmor(ItemStack heldStack, EntityLivingBase holder) {
        // do nothing, there's no point in charging from armour because manipulator buffers are huge
    }

    @Override
    public final String getToolTip(ItemStack aStack) {
        return null;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return !GregTech.isModLoaded();
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1d - getCharge(stack) / tier.maxCharge;
    }

    // #endregion

    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List<ItemStack> subItems) {
        final ItemStack stack = new ItemStack(this, 1);
        stack.setTagCompound(new MMState().save());
        subItems.add(stack.copy());
        this.charge(stack, tier.maxCharge, tier.voltageTier, true, false);
        subItems.add(stack);
    }

    @Override
    public EnumRarity getRarity(ItemStack p_77613_1_) {
        return switch (tier) {
            case Tier0 -> EnumRarity.common;
            case Tier1 -> EnumRarity.uncommon;
            case Tier2 -> EnumRarity.rare;
            case Tier3 -> EnumRarity.epic;
        };
    }

    public static MMState getState(ItemStack itemStack) {
        return MMState.load(getOrCreateNbtData(itemStack));
    }

    public static void setState(ItemStack itemStack, MMState state) {
        itemStack.setTagCompound(state.save());
    }

    /**
     * This is used to prevent the client's held manipulator from wiggling around each time power is drawn.
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void stopClientClearUsing(PlayerTickEvent event) {
        // spotless:off
        boolean isHandValid = event.player.getItemInUse() != null && event.player.getItemInUse().getItem() == this;
        boolean isCurrentItemValid = event.player.inventory.getCurrentItem() != null && event.player.inventory.getCurrentItem().getItem() == this;
        // spotless:on

        if (isHandValid && isCurrentItemValid) {
            NBTTagCompound inInventory = event.player.inventory.getCurrentItem()
                .getTagCompound();
            NBTTagCompound using = (NBTTagCompound) event.player.getItemInUse()
                .getTagCompound()
                .copy();

            // we don't want to stop using the item if only the charge changes
            using.setDouble("charge", inInventory.getDouble("charge"));

            if (inInventory.equals(using)) {
                event.player.setItemInUse(event.player.inventory.getCurrentItem(), event.player.getItemInUseCount());
            }
        }
    }

    public static NBTTagCompound getOrCreateNbtData(ItemStack itemStack) {
        NBTTagCompound ret = itemStack.getTagCompound();
        if (ret == null) {
            ret = new NBTTagCompound();
            itemStack.setTagCompound(ret);
        }
        return ret;
    }

    // this is super cursed but doing it properly would take a ton of effort for no real gain
    private static boolean ttAEWorks, ttUplinkWorks;

    public static void onTooltipResponse(int state) {
        ttAEWorks = (state & MMUtils.TOOLTIP_AE_WORKS) != 0;
        ttUplinkWorks = (state & MMUtils.TOOLTIP_UPLINK_WORKS) != 0;
    }

    private long lastTooltipQueryMS;

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(
        ItemStack itemStack,
        EntityPlayer player,
        List<String> desc,
        boolean advancedItemTooltips
    ) {
        MMState state = getState(itemStack);

        // spotless:off
        if (!GuiScreen.isShiftKeyDown()) {
            desc.add("Hold shift for more information.");
        } else {
            if (tier.hasCap(CONNECTS_TO_AE) || tier.hasCap(CONNECTS_TO_UPLINK)) {
                long time = System.currentTimeMillis();

                if ((time - lastTooltipQueryMS) > 1000) {
                    lastTooltipQueryMS = time;

                    int slot = -1;
                    Container c = player.openContainer;

                    for (int i = 0; i < c.inventorySlots.size(); i++) {
                        if (c.getSlot(i).getStack() == itemStack) {
                            slot = i;
                            break;
                        }
                    }

                    if (slot != -1) {
                        Messages.TooltipQuery.sendToServer(slot);
                    }
                }
            }

            if (tier.hasCap(CONNECTS_TO_AE)) {
                if (state.encKey != null) {
                    if (ttAEWorks) {
                        desc.add("Has an ME connection. (Can interact currently)");
                    } else {
                        desc.add("Has an ME connection. (Cannot interact currently)");
                    }
                } else {
                    desc.add("Does not have an ME connection.");
                }
            }

            if (tier.hasCap(CONNECTS_TO_UPLINK)) {
                if (state.uplinkAddress != null) {
                    if (ttUplinkWorks) {
                        desc.add("Has an Uplink connection. (Can interact currently)");
                    } else {
                        desc.add("Has an Uplink connection. (Cannot interact currently)");
                    }
                    addInfoLine(desc, "Uplink address: %s", state.uplinkAddress, Long::toHexString);
                } else {
                    desc.add("Does not have an Uplink connection.");
                }
            }

            if (state.config.action != null) {
                addInfoLine(desc, "Pending Action: %s", switch (state.config.action) {
                    case MOVING_COORDS -> "Moving coordinates";
                    case GEOM_SELECTING_BLOCK -> "Selecting blocks to place";
                    case MARK_COPY_A -> "Marking first copy corner";
                    case MARK_COPY_B -> "Marking second copy corner";
                    case MARK_CUT_A -> "Marking first cut corner";
                    case MARK_CUT_B -> "Marking second cut corner";
                    case MARK_PASTE -> "Marking paste location";
                    case EXCH_ADD_REPLACE -> "Adding block to replace whitelist";
                    case EXCH_SET_REPLACE -> "Setting block in replace whitelist";
                    case EXCH_SET_TARGET -> "Setting block to replace with";
                    case PICK_CABLE -> "Picking cable";
                    case MARK_ARRAY -> "Marking array bounds";
                });
            }

            if (Integer.bitCount(tier.capabilities & ALL_MODES) > 1) {
                addInfoLine(desc, "Mode: %s", switch (state.config.placeMode) {
                    case GEOMETRY -> "Geometry";
                    case MOVING -> "Moving";
                    case COPYING -> "Copying";
                    case EXCHANGING -> "Exchanging";
                    case CABLES -> "Cables";
                });
            }

            if (tier.hasCap(ALLOW_REMOVING)) {
                addInfoLine(desc, "Removing: %s", switch (state.config.removeMode) {
                    case ALL -> "All blocks";
                    case REPLACEABLE -> "Replaceable blocks";
                    case NONE -> "No blocks";
                });
            }

            if (state.config.placeMode == PlaceMode.GEOMETRY) {
                addInfoLine(desc, "Shape: %s", switch (state.config.shape) {
                    case LINE -> "Line";
                    case CUBE -> "Cube";
                    case SPHERE -> "Sphere";
                    case CYLINDER -> "Cylinder";
                });

                addInfoLine(desc, "Coordinate A: %s", state.config.coordA);
                addInfoLine(desc, "Coordinate B: %s", state.config.coordB);

                addInfoLine(desc, "Corner block: %s", state.config.corners);
                addInfoLine(desc, "Edge block: %s", state.config.edges);
                addInfoLine(desc, "Face block: %s", state.config.faces);
                addInfoLine(desc, "Volume block: %s", state.config.volumes);
            }

            if (state.config.placeMode == PlaceMode.COPYING) {
                addInfoLine(desc, "Copy Coordinate A: %s", state.config.coordA);
                addInfoLine(desc, "Copy Coordinate B: %s", state.config.coordB);

                addInfoLine(desc, "Paste Coordinate: %s", state.config.coordC);

                addInfoLine(desc,
                    "Stack: %s",
                    state.config.arraySpan,
                    span -> String.format(
                        "X: %dx, Y: %dx, Z: %dx",
                        span.x + (span.x < 0 ? -1 : 1),
                        span.y + (span.y < 0 ? -1 : 1),
                        span.z + (span.z < 0 ? -1 : 1)));
            }

            if (state.config.placeMode == PlaceMode.MOVING) {
                addInfoLine(desc, "Cut Coordinate A: %s", state.config.coordA);
                addInfoLine(desc, "Cut Coordinate B: %s", state.config.coordB);

                addInfoLine(desc, "Paste Coordinate: %s", state.config.coordC);
            }

            if (state.config.placeMode == PlaceMode.EXCHANGING) {
                addInfoLine(desc, "Removable blocks: %s", state.config.replaceWhitelist);
                addInfoLine(desc, "Replacing blocks with: %s", state.config.replaceWith);
            }

            if (state.config.placeMode == PlaceMode.CABLES) {
                addInfoLine(desc, "Coordinate A: %s", state.config.coordA);
                addInfoLine(desc, "Coordinate B: %s", state.config.coordB);

                addInfoLine(desc, "Cable: %s", state.config.cables);
            }
        }

        desc.add(
            EnumChatFormatting.AQUA
                + I18n.format(
                    "mm.tooltip.voltage",
                    formatNumbers(MathHelper.floor_double_long(state.charge)),
                    formatNumbers(tier.maxCharge),
                    formatNumbers(V[tier.voltageTier]))
                + EnumChatFormatting.GRAY);

        // spotless:on
    }

    private <T> void addInfoLine(List<String> desc, String format, T value) {
        addInfoLine(desc, format, value, T::toString);
    }

    private <T> void addInfoLine(List<String> desc, String format, T value, Function<T, String> toString) {
        if (value != null) {
            desc.add(
                String.format(
                    format,
                    EnumChatFormatting.BLUE.toString() + toString.apply(value) + EnumChatFormatting.RESET.toString()
                )
            );
        } else {
            desc.add(
                String
                    .format(format, EnumChatFormatting.GRAY.toString() + "None" + EnumChatFormatting.RESET.toString())
            );
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (player.isUsingItem()) return stack;

        MMState state = getState(stack);

        if (state.config.action != null) {
            MovingObjectPosition hit = MMUtils.getHitResult(player, true);

            if (handleAction(stack, world, player, state, hit)) {
                setState(stack, state);

                return stack;
            }
        }

        MovingObjectPosition hit = MMUtils.getHitResult(player, false);

        if (hit != null) {
            Location location = new Location(world, hit.blockX, hit.blockY, hit.blockZ);

            if (!player.isSneaking()) {
                location.offset(ForgeDirection.getOrientation(hit.sideHit));
            }

            if (state.config.placeMode == PlaceMode.GEOMETRY || state.config.placeMode == PlaceMode.EXCHANGING || state.config.placeMode == PlaceMode.CABLES) {
                state.config.coordA = location;
                state.config.coordB = null;
                state.config.coordC = null;
                state.config.coordBOffset = new Vector3i();
                state.config.action = PendingAction.MOVING_COORDS;
            }

            setState(stack, state);

            return stack;
        } else {
            if (player.isSneaking()) {
                player.setItemInUse(stack, Integer.MAX_VALUE);
            } else if (world.isRemote) {
                UIInfos.openClientUI(player, this::createWindow);
            }

            return stack;
        }
    }

    /**
     * Handles the pending action. Responsible for clearing the action afterwards.
     *
     * @return True when the action was successfully handled. Treated as a no-op when false.
     */
    public boolean handleAction(
        ItemStack itemStack,
        World world,
        EntityPlayer player,
        MMState state,
        MovingObjectPosition hit
    ) {
        switch (state.config.action) {
            case MOVING_COORDS: {
                Vector3i lookingAt = MMUtils.getLookingAtLocation(player);

                if (
                    state.config.placeMode == PlaceMode.GEOMETRY && state.config.coordAOffset == null &&
                        state.config.coordBOffset != null &&
                        state.config.coordCOffset == null &&
                        state.config.shape.requiresC()
                ) {
                    state.config.coordA = state.config.getCoordA(world, lookingAt);
                    state.config.coordB = state.config.getCoordB(world, lookingAt);
                    state.config.coordC = null;
                    state.config.coordAOffset = null;
                    state.config.coordBOffset = null;
                    state.config.coordCOffset = new Vector3i();
                } else {
                    state.config.coordA = state.config.getCoordA(world, lookingAt);
                    state.config.coordB = state.config.getCoordB(world, lookingAt);
                    state.config.coordC = state.config.getCoordC(world, lookingAt);
                    state.config.coordAOffset = null;
                    state.config.coordBOffset = null;
                    state.config.coordCOffset = null;
                    state.config.action = null;
                }

                return true;
            }
            case GEOM_SELECTING_BLOCK: {
                state.config.action = null;

                onPickBlock(world, player, itemStack, state, hit);

                return true;
            }
            case MARK_COPY_A: {
                state.config.coordA = new Location(world, MMUtils.getLookingAtLocation(player));
                state.config.action = PendingAction.MARK_COPY_B;
                return true;
            }
            case MARK_COPY_B: {
                state.config.coordB = new Location(world, MMUtils.getLookingAtLocation(player));
                state.config.action = null;
                return true;
            }
            case MARK_CUT_A: {
                state.config.coordA = new Location(world, MMUtils.getLookingAtLocation(player));
                state.config.action = PendingAction.MARK_CUT_B;
                return true;
            }
            case MARK_CUT_B: {
                state.config.coordB = new Location(world, MMUtils.getLookingAtLocation(player));
                state.config.action = null;
                return true;
            }
            case MARK_PASTE: {
                state.config.coordC = new Location(world, MMUtils.getLookingAtLocation(player));
                state.config.action = null;
                return true;
            }
            case EXCH_SET_TARGET: {
                onExchangeSetTarget(world, player, itemStack, state, hit);
                state.config.action = null;
                return true;
            }
            case EXCH_ADD_REPLACE: {
                onExchangeAddWhitelist(world, player, itemStack, state, hit);
                state.config.action = null;
                return true;
            }
            case EXCH_SET_REPLACE: {
                onExchangeSetWhitelist(world, player, itemStack, state, hit);
                state.config.action = null;
                return true;
            }
            case PICK_CABLE: {
                onPickCable(world, player, itemStack, state, hit);
                state.config.action = null;
                return true;
            }
            case MARK_ARRAY: {
                onMarkArray(world, player, itemStack, state);
                state.config.action = null;
                return true;
            }
        }

        return false;
    }

    /**
     * Used for detecting middle mouse button clicks.
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onMouseEvent(MouseEvent event) {
        final EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        if (player == null || player.isDead) { return; }

        final ItemStack heldItem = player.getHeldItem();
        if (heldItem == null) { return; }

        if (event.button == 2 /* MMB */ && event.buttonstate && heldItem.getItem() == this) {
            event.setCanceled(true);

            // call onMMBPressed on the client and the server
            MMState state = getState(heldItem);
            onMMBPressed(player, heldItem, state);
            setState(heldItem, state);

            Messages.MMBPressed.sendToServer();
        }
    }

    public void onMMBPressed(EntityPlayer player, ItemStack stack, MMState state) {
        if (state.config.placeMode == PlaceMode.GEOMETRY) {
            onPickBlock(player.getEntityWorld(), player, stack, state, MMUtils.getHitResult(player, true));
        }
        if (state.config.placeMode == PlaceMode.EXCHANGING) {
            if (player.isSneaking()) {
                onExchangeSetWhitelist(player.worldObj, player, stack, state, MMUtils.getHitResult(player, true));
            } else {
                onExchangeSetTarget(player.worldObj, player, stack, state, MMUtils.getHitResult(player, true));
            }
        }
        if (state.config.placeMode == PlaceMode.CABLES) {
            onPickCable(player.getEntityWorld(), player, stack, state, MMUtils.getHitResult(player, true));
        }
    }

    private void onPickBlock(
        World world,
        EntityPlayer player,
        ItemStack stack,
        MMState state,
        MovingObjectPosition hit
    ) {

        BlockSpec block = BlockSpec.fromPickBlock(world, player, hit);

        String what = null;

        boolean add = player.isSneaking();

        switch (state.config.blockSelectMode) {
            case CORNERS: {
                if (state.config.corners == null || !add) state.config.corners = new WeightedSpecList();
                state.config.corners.add(block);
                what = "corners";
                break;
            }
            case EDGES: {
                if (state.config.edges == null || !add) state.config.edges = new WeightedSpecList();
                state.config.edges.add(block);
                what = "edges";
                break;
            }
            case FACES: {
                if (state.config.faces == null || !add) state.config.faces = new WeightedSpecList();
                state.config.faces.add(block);
                what = "faces";
                break;
            }
            case VOLUMES: {
                if (state.config.volumes == null || !add) state.config.volumes = new WeightedSpecList();
                state.config.volumes.add(block);
                what = "volumes";
                break;
            }
            case ALL: {
                if (state.config.corners == null || !add) state.config.corners = new WeightedSpecList();
                if (state.config.edges == null || !add) state.config.edges = new WeightedSpecList();
                if (state.config.faces == null || !add) state.config.faces = new WeightedSpecList();
                if (state.config.volumes == null || !add) state.config.volumes = new WeightedSpecList();
                state.config.corners.add(block);
                state.config.edges.add(block);
                state.config.faces.add(block);
                state.config.volumes.add(block);
                what = "all blocks";
                break;
            }
        }

        if (add) {
            MMUtils.sendInfoToPlayer(
                player,
                String.format("Added %s to %s", block.getDisplayName(), what)
            );
        } else {
            MMUtils.sendInfoToPlayer(
                player,
                String.format("Set %s to: %s", what, block.getDisplayName())
            );
        }
    }

    private void onExchangeSetTarget(
        World world,
        EntityPlayer player,
        ItemStack stack,
        MMState state,
        MovingObjectPosition hit
    ) {

        BlockSpec block = BlockSpec.fromPickBlock(player.worldObj, player, hit);

        if (hit != null) checkForAECables(block, world, hit.blockX, hit.blockY, hit.blockZ);

        state.config.replaceWith = new WeightedSpecList();
        state.config.replaceWith.add(block);

        MMUtils.sendInfoToPlayer(
            player,
            String.format(
                "Set block to replace with to: %s",
                block.getDisplayName()
            )
        );
    }

    private void onExchangeAddWhitelist(
        World world,
        EntityPlayer player,
        ItemStack stack,
        MMState state,
        MovingObjectPosition hit
    ) {

        BlockSpec block = BlockSpec.fromPickBlock(player.worldObj, player, hit);

        if (hit != null) checkForAECables(block, world, hit.blockX, hit.blockY, hit.blockZ);

        if (state.config.replaceWhitelist == null) {
            state.config.replaceWhitelist = new WeightedSpecList();
        }

        state.config.replaceWhitelist.add(block);

        MMUtils.sendInfoToPlayer(
            player,
            String.format(
                "Added block to exchange whitelist: %s",
                block.getDisplayName()
            )
        );
    }

    private void onExchangeSetWhitelist(World world, EntityPlayer player, ItemStack stack, MMState state, MovingObjectPosition hit) {
        BlockSpec block = BlockSpec.fromPickBlock(player.worldObj, player, hit);

        if (hit != null) checkForAECables(block, world, hit.blockX, hit.blockY, hit.blockZ);

        state.config.replaceWhitelist = new WeightedSpecList();
        state.config.replaceWhitelist.add(block);

        MMUtils.sendInfoToPlayer(
            player,
            String.format(
                "Set exchange whitelist to only contain: %s",
                block.getDisplayName()
            )
        );
    }

    private void onPickCable(World world, EntityPlayer player, ItemStack stack, MMState state, MovingObjectPosition hit) {
        BlockSpec cable = new BlockSpec();

        if (hit != null) {
            if (Mods.GregTech.isModLoaded()) {
                MMUtils.getGTCable(cable, world, hit.blockX, hit.blockY, hit.blockZ);
            }

            if (cable.isAir() && Mods.AppliedEnergistics2.isModLoaded()) {
                MMUtils.getAECable(cable, world, hit.blockX, hit.blockY, hit.blockZ);
            }
        }

        state.config.cables = cable.isAir() ? null : cable;

        MMUtils.sendInfoToPlayer(
            player,
            String.format("Set cables to: %s", cable.getDisplayName())
        );
    }

    private void checkForAECables(BlockSpec spec, World world, int x, int y, int z) {
        if (tier.hasCap(ALLOW_CABLES) && AppliedEnergistics2.isModLoaded()) {
            if (MMUtils.AE_BLOCK_CABLE.matches(spec)) {
                MMUtils.getAECable(spec, world, x, y, z);
            }
        }
    }

    private void onMarkArray(World world, EntityPlayer player, ItemStack stack, MMState state) {
        Vector3i lookingAt = MMUtils.getLookingAtLocation(player);

        if (!Location.areCompatible(state.config.coordA, state.config.coordB)) {
            MMUtils.sendErrorToPlayer(player, "Cannot mark array: copy region is invalid");
            state.config.arraySpan = null;
            return;
        }

        if (state.config.coordC == null || !state.config.coordC.isInWorld(world)) {
            MMUtils.sendErrorToPlayer(player, "Cannot mark array: paste coordinate is invalid");
            state.config.arraySpan = null;
            return;
        }

        state.config.arraySpan = state.config
            .getArrayMult(world, state.config.coordA, state.config.coordB, state.config.coordC, lookingAt);
    }

    /**
     * A weak-keyed map containing every pending build.
     * Entries are added when the player starts holding shift+right click.
     * Entries are removed once the player stops holding shift+right click.
     */
    static final Map<EntityPlayer, IBuildable> PENDING_BUILDS = new MapMaker().weakKeys()
        .makeMap();

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.bow;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack p_77626_1_) {
        return Integer.MAX_VALUE;
    }

    private void stopBuildable(EntityPlayer player) {
        if (!player.worldObj.isRemote) {
            IBuildable buildable = PENDING_BUILDS.remove(player);

            if (buildable != null) buildable.onStopped();
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int itemUseCount) {
        stopBuildable(player);
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        stopBuildable(event.player);
    }

    @SubscribeEvent
    public void onPlayerKilled(LivingDeathEvent event) {
        if (event.entity instanceof EntityPlayer player) {
            stopBuildable(player);
        }
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
        if (!player.worldObj.isRemote) {
            int ticksUsed = Integer.MAX_VALUE - count;

            MMState state = getState(stack);

            if (ticksUsed == 1) {
                switch (state.config.placeMode) {
                    case GEOMETRY:
                    case COPYING:
                    case EXCHANGING:
                    case CABLES: {
                        PENDING_BUILDS.put(player, getPendingBuild(player, stack, state));
                        break;
                    }
                    case MOVING: {
                        PENDING_BUILDS.put(player, getPendingMove(player, stack, state));
                        break;
                    }
                }
            }

            if (ticksUsed >= 10 && (ticksUsed % tier.placeTicks) == 0) {
                try {
                    IBuildable buildable = PENDING_BUILDS.get(player);

                    if (buildable != null) buildable.tryPlaceBlocks(stack, player);
                } catch (Throwable t) {
                    MMMod.LOG.error("Could not place blocks", t);
                    MMUtils.sendErrorToPlayer(
                        player,
                        "Could not place blocks due to a crash. Check the logs for more info."
                    );
                }
            }
        }
    }

    private IBuildable getPendingBuild(EntityPlayer player, ItemStack stack, MMState state) {
        List<PendingBlock> blocks = state.getPendingBlocks(tier, player.getEntityWorld());

        if (tier.maxRange != -1) {
            int maxRange2 = tier.maxRange * tier.maxRange;

            Location playerLocation = new Location(
                player.getEntityWorld(),
                MathHelper.floor_double(player.posX),
                MathHelper.floor_double(player.posY),
                MathHelper.floor_double(player.posZ)
            );

            blocks.removeIf(block -> block.distanceTo2(playerLocation) > maxRange2);
        }

        blocks.sort(PendingBlock.getComparator());

        return new PendingBuild(player, state, tier, new LinkedList<>(blocks));
    }

    private IBuildable getPendingMove(EntityPlayer player, ItemStack stack, MMState state) {
        return new PendingMove(player, state, tier);
    }

    @Override
    public String getEncryptionKey(ItemStack item) {
        if (tier.hasCap(CONNECTS_TO_AE)) {
            Long key = getState(item).encKey;
            return key != null ? Long.toHexString(key) : null;
        } else {
            return null;
        }
    }

    @Override
    public void setEncryptionKey(ItemStack item, String encKey, String name) {
        MMState state = getState(item);

        if (tier.hasCap(CONNECTS_TO_AE)) {
            try {
                state.encKey = Long.parseLong(encKey);
            } catch (NumberFormatException e) {
                state.encKey = null;
            }
        } else {
            state.encKey = null;
        }

        setState(item, state);
    }

    public void setUplinkAddress(ItemStack stack, Long address) {
        if (tier.hasCap(CONNECTS_TO_UPLINK)) {
            MMState state = getState(stack);

            state.uplinkAddress = address;

            setState(stack, state);
        }
    }

    // #region UI

    public ModularWindow createWindow(UIBuildContext buildContext) {
        buildContext.setShowNEI(false);

        ModularWindow.Builder builder = ModularWindow.builder(new Size(176, 272));

        builder.widget(getMenuOptions(buildContext).build());

        return builder.build();
    }

    // spotless:off
    /**
     * Builds the radial menu. Pretty please don't enable spotless, it'll mangle these builders.
     */
    private RadialMenuBuilder getMenuOptions(UIBuildContext buildContext) {
        ItemStack heldStack = buildContext.getPlayer().getHeldItem();
        MMState initialState = getState(heldStack);

        return new RadialMenuBuilder(buildContext)
            .innerIcon(new ItemStack(this))
            .pipe(builder -> {
                addCommonOptions(builder, buildContext, heldStack);
            })
            .pipe(builder -> {
                switch (initialState.config.placeMode) {
                    case GEOMETRY -> addGeometryOptions(builder, buildContext, heldStack, initialState);
                    case COPYING -> addCopyingOptions(builder, buildContext, heldStack, initialState);
                    case MOVING -> addMovingOptions(builder, buildContext, heldStack);
                    case EXCHANGING -> addExchangingOptions(builder, buildContext, heldStack);
                    case CABLES -> addCableOptions(builder, buildContext, heldStack);
                }
            });
    }

    private void addCommonOptions(RadialMenuBuilder builder, UIBuildContext buildContext, ItemStack heldStack) {
        builder.branch()
                .label("Set Mode")
                .hidden(tier == ManipulatorTier.Tier0)
                .branch()
                    .label("Set Remove Mode")
                    .hidden(!tier.hasCap(ALLOW_REMOVING))
                    .option()
                        .label("Remove None")
                        .onClicked(() -> {
                            Messages.SetRemoveMode.sendToServer(BlockRemoveMode.NONE);
                        })
                    .done()
                    .option()
                        .label("Remove Replaceable")
                        .onClicked(() -> {
                            Messages.SetRemoveMode.sendToServer(BlockRemoveMode.REPLACEABLE);
                        })
                    .done()
                    .option()
                        .label("Remove All")
                        .onClicked(() -> {
                            Messages.SetRemoveMode.sendToServer(BlockRemoveMode.ALL);
                        })
                    .done()
                .done()
                .option()
                    .label("Geometry")
                    .onClicked(() -> {
                        Messages.SetPlaceMode.sendToServer(PlaceMode.GEOMETRY);
                    })
                .done()
                .option()
                    .label("Moving")
                    .hidden(!tier.hasCap(ALLOW_MOVING))
                    .onClicked(() -> {
                        Messages.SetPlaceMode.sendToServer(PlaceMode.MOVING);
                    })
                .done()
                .option()
                    .label("Copying")
                    .hidden(!tier.hasCap(ALLOW_COPYING))
                    .onClicked(() -> {
                        Messages.SetPlaceMode.sendToServer(PlaceMode.COPYING);
                    })
                .done()
                .option()
                    .label("Exchanging")
                    .hidden(!tier.hasCap(ALLOW_EXCHANGING))
                    .onClicked(() -> {
                        Messages.SetPlaceMode.sendToServer(PlaceMode.EXCHANGING);
                    })
                .done()
                .option()
                    .label("Cables")
                    .hidden(!tier.hasCap(ALLOW_CABLES))
                    .onClicked(() -> {
                        Messages.SetPlaceMode.sendToServer(PlaceMode.CABLES);
                    })
                .done()
            .done();
    }

    private void addGeometryOptions(RadialMenuBuilder builder, UIBuildContext buildContext, ItemStack heldStack, MMState state) {
        builder
            .branch()
                .label("Select Blocks")
                .option()
                    .label("Set Corners")
                    .onClicked(() -> {
                        Messages.SetBlockSelectMode.sendToServer(BlockSelectMode.CORNERS);
                        Messages.SetPendingAction.sendToServer(PendingAction.GEOM_SELECTING_BLOCK);
                    })
                .done()
                .option()
                    .label("Set Edges")
                    .onClicked(() -> {
                        Messages.SetBlockSelectMode.sendToServer(BlockSelectMode.EDGES);
                        Messages.SetPendingAction.sendToServer(PendingAction.GEOM_SELECTING_BLOCK);
                    })
                .done()
                .option()
                    .label("Set Faces")
                    .onClicked(() -> {
                        Messages.SetBlockSelectMode.sendToServer(BlockSelectMode.FACES);
                        Messages.SetPendingAction.sendToServer(PendingAction.GEOM_SELECTING_BLOCK);
                    })
                .done()
                .option()
                    .label("Set Volumes")
                    .onClicked(() -> {
                        Messages.SetBlockSelectMode.sendToServer(BlockSelectMode.VOLUMES);
                        Messages.SetPendingAction.sendToServer(PendingAction.GEOM_SELECTING_BLOCK);
                    })
                .done()
                .option()
                    .label("Set All")
                    .onClicked(() -> {
                        Messages.SetBlockSelectMode.sendToServer(BlockSelectMode.ALL);
                        Messages.SetPendingAction.sendToServer(PendingAction.GEOM_SELECTING_BLOCK);
                    })
                .done()
                .option()
                    .label("Clear All")
                    .onClicked(() -> {
                        Messages.ClearBlocks.sendToServer();
                    })
                .done()
            .done()
            .branch()
                .label("Set Shape")
                .option()
                    .label("Line")
                    .onClicked(() -> {
                        Messages.SetShape.sendToServer(Shape.LINE);
                    })
                .done()
                .option()
                    .label("Cube")
                    .onClicked(() -> {
                        Messages.SetShape.sendToServer(Shape.CUBE);
                    })
                .done()
                .option()
                    .label("Sphere")
                    .onClicked(() -> {
                        Messages.SetShape.sendToServer(Shape.SPHERE);
                    })
                .done()
                .option()
                    .label("Cylinder")
                    .onClicked(() -> {
                        Messages.SetShape.sendToServer(Shape.CYLINDER);
                    })
                .done()
            .done()
            .branch()
                .label("Move Coords")
                .option()
                    .label("Move Coord A")
                    .onClicked(() -> {
                        Messages.MoveA.sendToServer();
                    })
                .done()
                .option()
                    .label("Move All")
                    .onClicked(() -> {
                        Messages.MoveAll.sendToServer();
                    })
                .done()
                .option()
                    .label("Move Coord B")
                    .onClicked(() -> {
                        Messages.MoveB.sendToServer();
                    })
                .done()
                .option()
                    .label("Move Here")
                    .onClicked(() -> {
                        Messages.MoveHere.sendToServer();
                    })
                .done()
            .done();
    }

    private void addCopyingOptions(RadialMenuBuilder builder, UIBuildContext buildContext, ItemStack heldStack, MMState initialState) {
        builder
            .option()
                .label("Mark Copy")
                .onClicked(() -> {
                    Messages.MarkCopy.sendToServer();
                })
            .done()
            .branch()
                .label("Edit Stack")
                .option()
                    .label("Reset")
                    .onClicked(() -> {
                        Messages.ResetArray.sendToServer();
                    })
                .done()
                .option()
                    .label("Mark")
                    .onClicked(() -> {
                        Messages.SetPendingAction.sendToServer(PendingAction.MARK_ARRAY);
                    })
                .done()
            .done()
            .branch()
                .label("Planning")
                .option()
                    .label("Cancel Auto Plans")
                    .onClicked(() -> {
                        Messages.CancelAutoPlans.sendToServer();
                    })
                .done()
                .option()
                    .label("Plan (All, Auto)")
                    .onClicked(() -> {
                        Messages.GetRequiredItems.sendToServer(MMUtils.PLAN_ALL | MMUtils.PLAN_AUTO_SUBMIT);
                    })
                .done()
                .option()
                    .label("Plan (All, Manual)")
                    .onClicked(() -> {
                        Messages.GetRequiredItems.sendToServer(MMUtils.PLAN_ALL);
                    })
                .done()
                .option()
                    .label("Clear Manual Plans")
                    .onClicked(() -> {
                        Messages.ClearManualPlans.sendToServer();
                    })
                .done()
                .option()
                    .label("Plan (Missing, Manual)")
                    .onClicked(() -> {
                        Messages.GetRequiredItems.sendToServer(0);
                    })
                .done()
                .option()
                    .label("Plan (Missing, Auto)")
                    .onClicked(() -> {
                        Messages.GetRequiredItems.sendToServer(MMUtils.PLAN_AUTO_SUBMIT);
                    })
                .done()
            .done()
            .option()
                .label("Edit Transform")
                .onClicked((menu, option, mouseButton, doubleClicked) -> {
                    UIBuildContext buildContext2 = new UIBuildContext(buildContext.getPlayer());
                    ModularWindow window = createTransformWindow(buildContext2, heldStack, initialState);
                    GuiScreen screen = new TransparentModularGui(
                            new ModularUIContainer(new ModularUIContext(buildContext2, null, true), window));
                    FMLCommonHandler.instance().showGuiScreen(screen);
                })
            .done()
            .option()
                .label("Mark Paste")
                .onClicked(() -> {
                    Messages.MarkPaste.sendToServer();
                })
            .done();
    }

    @SideOnly(Side.CLIENT)
    private static class TransparentModularGui extends ModularGui {
        public TransparentModularGui(ModularUIContainer container) {
            super(container);
        }

        public void drawDefaultBackground() {}
    }

    private void addMovingOptions(RadialMenuBuilder builder, UIBuildContext buildContext, ItemStack heldStack) {
        builder
            .option()
                .label("Mark Cut")
                .onClicked(() -> {
                    Messages.MarkCut.sendToServer();
                })
            .done()
            .option()
                .label("Mark Paste")
                .onClicked(() -> {
                    Messages.MarkPaste.sendToServer();
                })
            .done();
    }

    private void addExchangingOptions(RadialMenuBuilder builder, UIBuildContext buildContext, ItemStack heldStack) {
        builder
            .branch()
                .label("Edit Replace Whitelist")
                .option()
                    .label("Clear")
                    .onClicked(() -> {
                        Messages.ClearWhitelist.sendToServer();
                    })
                .done()
                .option()
                    .label("Add Block")
                    .onClicked(() -> {
                        Messages.SetPendingAction.sendToServer(PendingAction.EXCH_ADD_REPLACE);
                    })
                .done()
                .option()
                    .label("Set Block")
                    .onClicked(() -> {
                        Messages.SetPendingAction.sendToServer(PendingAction.EXCH_SET_REPLACE);
                    })
                .done()
            .done()
            .option()
                .label("Set Block To Replace With")
                .onClicked(() -> {
                    Messages.SetPendingAction.sendToServer(PendingAction.EXCH_SET_TARGET);
                })
            .done()
            .branch()
                .label("Move Coords")
                .option()
                    .label("Move Coord A")
                    .onClicked(() -> {
                        Messages.MoveA.sendToServer();
                    })
                .done()
                .option()
                    .label("Move All")
                    .onClicked(() -> {
                        Messages.MoveAll.sendToServer();
                    })
                .done()
                .option()
                    .label("Move Coord B")
                    .onClicked(() -> {
                        Messages.MoveB.sendToServer();
                    })
                .done()
                .option()
                    .label("Move Here")
                    .onClicked(() -> {
                        Messages.MoveHere.sendToServer();
                    })
                .done()
            .done();
    }

    private void addCableOptions(RadialMenuBuilder builder, UIBuildContext buildContext, ItemStack heldStack) {
        builder
            .option()
                .label("Set Cable")
                .onClicked(() -> {
                    Messages.SetPendingAction.sendToServer(PendingAction.PICK_CABLE);
                })
            .done()
            .branch()
                .label("Move Coords")
                .option()
                    .label("Move Coord A")
                    .onClicked(() -> {
                        Messages.MoveA.sendToServer();
                    })
                .done()
                .option()
                    .label("Move All")
                    .onClicked(() -> {
                        Messages.MoveAll.sendToServer();
                    })
                .done()
                .option()
                    .label("Move Coord B")
                    .onClicked(() -> {
                        Messages.MoveB.sendToServer();
                    })
                .done()
                .option()
                    .label("Move Here")
                    .onClicked(() -> {
                        Messages.MoveHere.sendToServer();
                    })
                .done()
            .done();
    }

    private static final IDrawable[] BACKGROUND = {
        new Rectangle().setColor(0xFF888888),
        new OffsetDrawable(new Rectangle().setColor(0xFF111111), 2, 2, -4, -4),
    };

    private static Widget padding(int width, int height) {
        return new Row().setSize(width, height);
    }

    public ModularWindow createTransformWindow(UIBuildContext buildContext, ItemStack heldStack,
        MMState initialState) {
        buildContext.setShowNEI(false);

        ModularWindow.Builder builder = ModularWindow.builderFullScreen();

        builder.bindPlayerInventory(buildContext.getPlayer(), 0, -9001);

        if (NetworkUtils.isClient()) {
            DynamicTextWidget rotationInfo = DynamicTextWidget.dynamicString(() -> {
                MMState currState = getState(
                    buildContext.getPlayer()
                        .getHeldItem());

                Transform t = currState.getTransform();

                ArrayList<String> flips = new ArrayList<>();

                if (t.flipX) flips.add("X");
                if (t.flipY) flips.add("Y");
                if (t.flipZ) flips.add("Z");

                return String.format(
                    "Flip: %s\nUp: %s\nForward: %s",
                    flips.isEmpty() ? "None" : String.join(", ", flips),
                    MMUtils.getDirectionDisplayName(t.up),
                    MMUtils.getDirectionDisplayName(t.forward));
            });

            Widget[] left = {
                new Row().widgets(
                    new VanillaButtonWidget().setDisplayString("Rotate X-")
                        .setOnClick((t, u) -> { Transform.sendRotate(EAST, false); })
                        .setSynced(false, false)
                        .setSize(62, 18),
                    padding(6, 6),
                    new VanillaButtonWidget().setDisplayString("Rotate X+")
                        .setOnClick((t, u) -> { Transform.sendRotate(EAST, true); })
                        .setSynced(false, false)
                        .setSize(62, 18)),
                padding(10, 10),
                new Row().widgets(
                    new VanillaButtonWidget().setDisplayString("Rotate Y-")
                        .setOnClick((t, u) -> { Transform.sendRotate(UP, false); })
                        .setSynced(false, false)
                        .setSize(62, 18),
                    padding(6, 6),
                    new VanillaButtonWidget().setDisplayString("Rotate Y+")
                        .setOnClick((t, u) -> { Transform.sendRotate(UP, true); })
                        .setSynced(false, false)
                        .setSize(62, 18)),
                padding(10, 10),
                new Row().widgets(
                    new VanillaButtonWidget().setDisplayString("Rotate Z-")
                        .setOnClick((t, u) -> { Transform.sendRotate(SOUTH, false); })
                        .setSynced(false, false)
                        .setSize(62, 18),
                    padding(6, 6),
                    new VanillaButtonWidget().setDisplayString("Rotate Z+")
                        .setOnClick((t, u) -> { Transform.sendRotate(SOUTH, true); })
                        .setSynced(false, false)
                        .setSize(62, 18)),
                padding(10, 10),
                new Row().widgets(
                    new VanillaButtonWidget().setDisplayString("Flip X")
                        .setOnClick(
                            (t, u) -> { Messages.ToggleTransformFlip.sendToServer(Transform.FLIP_X); })
                        .setSynced(false, false)
                        .setSize(40, 18),
                    padding(5, 5),
                    new VanillaButtonWidget().setDisplayString("Flip Y")
                        .setOnClick(
                            (t, u) -> { Messages.ToggleTransformFlip.sendToServer(Transform.FLIP_Y); })
                        .setSynced(false, false)
                        .setSize(40, 18),
                    padding(5, 5),
                    new VanillaButtonWidget().setDisplayString("Flip Z")
                        .setOnClick(
                            (t, u) -> { Messages.ToggleTransformFlip.sendToServer(Transform.FLIP_Z); })
                        .setSynced(false, false)
                        .setSize(40, 18)),
                padding(10, 10),
                new Row().widgets(
                    new MultiChildWidget()
                        .addChild(
                            rotationInfo
                                .setSynced(false)
                                .setTextAlignment(Alignment.CenterLeft)
                                .setDefaultColor(Color.WHITE.dark(1))
                                .setSize(80, 36)
                                .setPos(3, 0))
                        .addChild(
                            new DirectionDrawable()
                                .asWidget()
                                .setSize(30, 30)
                                .setPos(3, 36))
                        .addChild(
                            new TextWidget(RED + "X+ " + GREEN + "Y+ " + BLUE + "Z+")
                                .setSize(50, 20)
                                .setPos(34, 39))
                        .setBackground(BACKGROUND)
                        .setSize(88, 36 + 30),
                    padding(2, 2),
                    new Column()
                        .setAlignment(MainAxisAlignment.CENTER, CrossAxisAlignment.END)
                        .widget(new VanillaButtonWidget().setDisplayString("Reset")
                            .setOnClick((t, u) -> { Messages.ResetTransform.sendToServer(); })
                            .setSynced(false, false)
                            .setSize(40, 18))
                        .setSize(40, 36 + 30))
            };

            Widget[] right = {
                makeHeader("Copy"),
                padding(2, 2),
                makeCoordinateEditor(buildContext.getPlayer(), -1, 0),
                padding(2, 2),
                makeCoordinateEditor(buildContext.getPlayer(), -1, 1),
                padding(2, 2),
                makeCoordinateEditor(buildContext.getPlayer(), -1, 2),
                padding(2, 2),
                makeHeader("Copy (A)"),
                padding(2, 2),
                makeCoordinateEditor(buildContext.getPlayer(), 0, 0),
                padding(2, 2),
                makeCoordinateEditor(buildContext.getPlayer(), 0, 1),
                padding(2, 2),
                makeCoordinateEditor(buildContext.getPlayer(), 0, 2),
                padding(10, 2),
                makeHeader("Copy (B)"),
                padding(2, 2),
                makeCoordinateEditor(buildContext.getPlayer(), 1, 0),
                padding(2, 2),
                makeCoordinateEditor(buildContext.getPlayer(), 1, 1),
                padding(2, 2),
                makeCoordinateEditor(buildContext.getPlayer(), 1, 2),
                padding(10, 2),
                makeHeader("Paste"),
                padding(2, 2),
                makeCoordinateEditor(buildContext.getPlayer(), 2, 0),
                padding(2, 2),
                makeCoordinateEditor(buildContext.getPlayer(), 2, 1),
                padding(2, 2),
                makeCoordinateEditor(buildContext.getPlayer(), 2, 2),
                padding(10, 2),
                makeHeader("Stacking"),
                padding(2, 2),
                makeCoordinateEditor(buildContext.getPlayer(), 3, 0),
                padding(2, 2),
                makeCoordinateEditor(buildContext.getPlayer(), 3, 1),
                padding(2, 2),
                makeCoordinateEditor(buildContext.getPlayer(), 3, 2),
                padding(10, 2)
            };

            builder.widget(
                new Row().widgets(
                    padding(10, 10),
                    new Column()
                        .setAlignment(MainAxisAlignment.CENTER, CrossAxisAlignment.START)
                        .widgets(left)).fillParent());

            Column cr;

            builder.widget(
                (cr = new Column())
                    .setAlignment(MainAxisAlignment.CENTER, CrossAxisAlignment.END)
                    .widgets(right)
                    .setPosProvider((screenSize, window, parent) -> {
                        return new Pos2d(screenSize.width - cr.getSize().width - 10, 0);
                    }));
        }

        return builder.build();
    }

    private static Widget makeHeader(String text) {
        return new MultiChildWidget()
            .addChild(
                new MultiChildWidget()
                    .addChild(
                        new TextWidget(text)
                            .setTextAlignment(Alignment.BottomCenter)
                            .setDefaultColor(Color.WHITE.dark(1))
                            .setSize(60, 13))
                    .setBackground(BACKGROUND)
                    .setSize(60, 18)
                    .setPos((130 - 60) / 2, 0))
            .setSize(130, 18);
    }

    private static Vector3i getDefaultLocation(EntityPlayer player) {
        return MMUtils.getLookingAtLocation(player);
    }

    private static final AdaptableUITexture DISPLAY = AdaptableUITexture
        .of("modularui:gui/background/display", 143, 75, 2);

    @SideOnly(Side.CLIENT)
    private Row makeCoordinateEditor(EntityPlayer player, int coord, int component) {
        IntSupplier getter = () -> {
            MMState currState = getState(player.getHeldItem());

            Vector3i l = switch (coord) {
                case -1 -> new Vector3i(0);
                case 0 -> currState.config.coordA == null ? null : currState.config.coordA.toVec();
                case 1 -> currState.config.coordB == null ? null : currState.config.coordB.toVec();
                case 2 -> currState.config.coordC == null ? null : currState.config.coordC.toVec();
                case 3 -> currState.config.arraySpan;
                default -> throw new IllegalArgumentException("coord");
            };

            if (l == null) {
                if (coord == 3) {
                    l = new Vector3i(0);
                } else {
                    l = getDefaultLocation(player);
                }
            }

            return switch (component) {
                case 0 -> l.x;
                case 1 -> l.y;
                case 2 -> l.z;
                default -> throw new IllegalArgumentException("component");
            };
        };

        IntSupplier getterVisual = () -> {
            int k = getter.getAsInt();

            if (coord == 3) {
                if (k >= 0) k++;
            }

            return k;
        };

        IntConsumer setter = i -> {
            MMState currState = getState(player.getHeldItem());

            Vector3i l = switch (coord) {
                case -1 -> new Vector3i(0);
                case 0 -> currState.config.coordA == null ? null : currState.config.coordA.toVec();
                case 1 -> currState.config.coordB == null ? null : currState.config.coordB.toVec();
                case 2 -> currState.config.coordC == null ? null : currState.config.coordC.toVec();
                case 3 -> currState.config.arraySpan;
                default -> throw new IllegalArgumentException("coord");
            };

            if (l == null) {
                if (coord == 3) {
                    l = new Vector3i(0);
                } else {
                    l = getDefaultLocation(player);
                }
            }

            switch (component) {
                case 0 -> l.x = i;
                case 1 -> l.y = i;
                case 2 -> l.z = i;
                default -> throw new IllegalArgumentException("component");
            }

            switch (coord) {
                case -1 -> {
                    if (currState.config.coordA != null) {
                        currState.config.coordA = new Location(player.worldObj, currState.config.coordA.toVec().add(l));
                    }

                    if (currState.config.coordB != null) {
                        currState.config.coordB = new Location(player.worldObj, currState.config.coordB.toVec().add(l));
                    }
                }
                case 0 -> currState.config.coordA = new Location(player.worldObj, l);
                case 1 -> currState.config.coordB = new Location(player.worldObj, l);
                case 2 -> currState.config.coordC = new Location(player.worldObj, l);
                case 3 -> currState.config.arraySpan = l;
                default -> throw new IllegalArgumentException("coord");
            }

            ItemMatterManipulator.setState(player.getHeldItem(), currState);

            switch (coord) {
                case -1 -> {
                    if (currState.config.coordA != null) {
                        Messages.SetA.sendToServer(currState.config.coordA.toVec().add(l));
                    }

                    if (currState.config.coordB != null) {
                        Messages.SetB.sendToServer(currState.config.coordB.toVec().add(l));
                    }
                }
                case 0 -> {
                    Messages.SetA.sendToServer(l);
                }
                case 1 -> {
                    Messages.SetB.sendToServer(l);
                }
                case 2 -> {
                    Messages.SetC.sendToServer(l);
                }
                case 3 -> {
                    Messages.SetArray.sendToServer(l);
                }
                default -> throw new IllegalArgumentException("coord");
            }
        };

        String compName = switch (component) {
            case 0 -> "X";
            case 1 -> "Y";
            case 2 -> "Z";
            default -> throw new IllegalArgumentException("component");
        };

        class SizeStorage {
            public int x, y, z;
            public boolean present = false;

            public Vector3i get() {
                if (!present && GuiScreen.isCtrlKeyDown()) {
                    MMState currState = getState(player.getHeldItem());

                    Vector3i size;

                    if (coord == 2) {
                        size = currState.config.getPasteVisualDeltas(null, false).size();
                    } else {
                        size = currState.config.getCopyVisualDeltas(null).size();
                    }

                    x = size.x;
                    y = size.y;
                    z = size.z;
                    present = true;
                }

                if (!GuiScreen.isCtrlKeyDown()) {
                    present = false;
                }

                return present ? new Vector3i(x, y, z) : new Vector3i(1);
            }

            public int getOffset() {
                int offset = 1;

                if (GuiScreen.isShiftKeyDown()) {
                    offset = 10;
                } else if (coord != 3 && GuiScreen.isCtrlKeyDown()) {
                    Vector3i size = get();

                    offset = switch (component) {
                        case 0 -> size.x;
                        case 1 -> size.y;
                        case 2 -> size.z;
                        default -> throw new IllegalArgumentException("component");
                    };
                } else {
                    present = false;
                }

                return offset;
            }
        }

        SizeStorage storage = new SizeStorage();

        return new Row().widgets(
            new VanillaButtonWidget().setDisplayString(compName + " - 1")
                .setOnClick(
                    (t, u) -> {
                        int i = getter.getAsInt();

                        i -= storage.getOffset();

                        setter.accept(i);
                    })
                .setSynced(false, false)
                .setSize(40, 18)
                .setTicker(w -> {
                    ((VanillaButtonWidget) w).setDisplayString(compName + " - " + storage.getOffset());
                }),
            padding(5, 5),
            new MultiChildWidget()
                .addChild(coord != -1 ? (
                    new NumericWidget()
                        .setSynced(false, false)
                        .setIntegerOnly(true)
                        .setGetter(() -> getterVisual.getAsInt())
                        .setSetter(i -> {
                            setter.accept((int) i);
                        })
                        .setBounds(Integer.MIN_VALUE, Integer.MAX_VALUE)
                        .setScrollBar()
                        .setTextColor(Color.WHITE.dark(1))
                        .setBackground(DISPLAY.withOffset(-2, -2, 4, 4))
                        .setSize(36, 14)
                        .setPos(2, 2)
                        .setTicker(w -> {
                            if (!w.isFocused()) {
                                ((NumericWidget) w).setValue(getterVisual.getAsInt());
                            }
                        })
                ) : (
                    new TextWidget("N/A")
                        .setDefaultColor(Color.WHITE.dark(1))
                        .setBackground(BACKGROUND)
                        .setSize(40, 18)
                ))
                .setSize(40, 18),
            padding(5, 5),
            new VanillaButtonWidget().setDisplayString(compName + " + 1")
                .setOnClick(
                    (t, u) -> {
                        int i = getter.getAsInt();

                        i += storage.getOffset();

                        setter.accept(i);
                    })
                .setSynced(false, false)
                .setSize(40, 18)
                .setTicker(w -> {
                    ((VanillaButtonWidget) w).setDisplayString(compName + " + " + storage.getOffset());
                }));
    }
    // spotless:on

    // #endregion

    // #region Rendering

    private long lastExceptionPrint = 0;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    @SideOnly(Side.CLIENT)
    public void renderSelection(RenderWorldLastEvent event) {
        try {
            renderer.renderSelection(event);
        } catch (Throwable t) {
            MMMod.LOG.error("Could not render matter manipulator preview", t);

            long now = System.currentTimeMillis();
            if ((now - lastExceptionPrint) > 10_000) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(
                    new ChatComponentText(
                        EnumChatFormatting.RED
                            + "Could not render preview due to a crash. Check the logs for more info."
                    )
                );
                lastExceptionPrint = now;
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onKeyPressed(InputEvent.KeyInputEvent event) {
        renderer.onKeyPressed();
    }

    @SideOnly(Side.CLIENT)
    public static void initKeybindings() {
        MatterManipulatorRenderer.initKeybindings();
    }

    @SideOnly(Side.CLIENT)
    private class MatterManipulatorRenderer {

        private long lastAnalysisMS = 0;

        private MMConfig lastAnalyzedConfig = null;

        private Location lastPlayerPosition = null;

        private List<PendingBlock> analysisCache = null;

        private ItemMatterManipulator lastDrawer = null;

        private static final long ANALYSIS_INTERVAL_MS = 10_000;

        public static final KeyBinding CONTROL = new KeyBinding("key.mm-ctrl", Keyboard.KEY_LCONTROL, "key.mm");
        public static final KeyBinding CUT = new KeyBinding("key.mm-cut", Keyboard.KEY_X, "key.mm");
        public static final KeyBinding COPY = new KeyBinding("key.mm-copy", Keyboard.KEY_C, "key.mm");
        public static final KeyBinding PASTE = new KeyBinding("key.mm-paste", Keyboard.KEY_V, "key.mm");
        public static final KeyBinding RESET = new KeyBinding("key.mm-reset", Keyboard.KEY_Z, "key.mm");

        public static void initKeybindings() {
            ClientRegistry.registerKeyBinding(CONTROL);
            ClientRegistry.registerKeyBinding(CUT);
            ClientRegistry.registerKeyBinding(COPY);
            ClientRegistry.registerKeyBinding(PASTE);
            ClientRegistry.registerKeyBinding(RESET);
        }

        public void onKeyPressed() {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            ItemStack held = player.getHeldItem();

            if (held != null && held.getItem() == ItemMatterManipulator.this) {
                MMState state = getState(held);

                if (CONTROL.getKeyCode() == 0 || CONTROL.getIsKeyPressed()) {
                    if (CUT.isPressed()) {
                        if (state.config.placeMode != PlaceMode.MOVING) {
                            Messages.SetPlaceMode.sendToServer(PlaceMode.MOVING);
                        }
                        if (InteractionConfig.pasteAutoClear) {
                            Messages.ClearCoords.sendToServer();
                            if (InteractionConfig.resetTransform) {
                                Messages.ClearTransform.sendToServer();
                            }
                        }
                        Messages.MarkCut.sendToServer();
                    } else if (COPY.isPressed()) {
                        if (state.config.placeMode != PlaceMode.COPYING) {
                            Messages.SetPlaceMode.sendToServer(PlaceMode.COPYING);
                        }
                        if (InteractionConfig.pasteAutoClear) {
                            Messages.ClearCoords.sendToServer();
                            if (InteractionConfig.resetTransform) {
                                Messages.ClearTransform.sendToServer();
                            }
                        }
                        Messages.MarkCopy.sendToServer();
                    } else if (PASTE.isPressed()) {
                        // set the mode to copying if we aren't in a mode supports pasting (moving/copying)
                        if (state.config.placeMode != PlaceMode.COPYING && state.config.placeMode != PlaceMode.MOVING) {
                            Messages.SetPlaceMode.sendToServer(PlaceMode.COPYING);
                        }
                        Messages.MarkPaste.sendToServer();
                    } else if (RESET.isPressed()) {
                        Messages.ClearCoords.sendToServer();
                        if (InteractionConfig.resetTransform) {
                            Messages.ClearTransform.sendToServer();
                        }
                    }
                }
            }
        }

        /**
         * Renders the overlay.
         */
        public void renderSelection(RenderWorldLastEvent event) {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            ItemStack held = player.getHeldItem();

            if (held != null && held.getItem() == ItemMatterManipulator.this) {
                MMState state = getState(held);

                switch (state.config.placeMode) {
                    case GEOMETRY:
                    case EXCHANGING:
                    case CABLES: {
                        renderGeom(event, state, player);
                        break;
                    }
                    case COPYING:
                    case MOVING: {
                        renderRegions(event, state, player);
                        break;
                    }
                }

                try {
                    @SuppressWarnings("unchecked")
                    List<EntityFX> fxLayers = Minecraft.getMinecraft().effectRenderer.fxLayers[0];

                    // remove all of the structurelib hint particles
                    fxLayers.removeIf(particle -> particle instanceof WeightlessParticleFX);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            } else {
                if (lastDrawer == ItemMatterManipulator.this) {
                    lastAnalysisMS = 0;
                    lastAnalyzedConfig = null;
                    lastPlayerPosition = null;
                    analysisCache = null;
                    lastDrawer = null;

                    // reset the hints when this item just drew and the player isn't holding it anymore
                    StructureLibAPI.startHinting(player.worldObj);
                    StructureLibAPI.endHinting(player.worldObj);

                    AboveHotbarHUD.renderTextAboveHotbar("", 0, false, false);
                }
            }
        }

        private void renderGeom(RenderWorldLastEvent event, MMState state, EntityPlayer player) {
            Vector3i lookingAt = MMUtils.getLookingAtLocation(player);

            Location coordA = state.config.getCoordA(player.worldObj, lookingAt);
            Location coordB = state.config.getCoordB(player.worldObj, lookingAt);
            Location coordC = state.config.getCoordC(player.worldObj, lookingAt);

            state.config.coordA = coordA;
            state.config.coordB = coordB;
            state.config.coordC = coordC;

            boolean isAValid = coordA != null && coordA.isInWorld(player.worldObj);
            boolean isBValid = coordB != null && coordB.isInWorld(player.worldObj);
            boolean isCValid = coordC != null && coordC.isInWorld(player.worldObj);

            // For cylinders, coord B must be pinned to one of the axis planes and coord C must be on the normal of that
            // plane
            if (state.config.placeMode == PlaceMode.GEOMETRY && state.config.shape == Shape.CYLINDER) {
                if (isAValid && isBValid) {
                    Objects.requireNonNull(coordA);
                    Objects.requireNonNull(coordB);

                    Vector3i b2 = MMState.pinToPlanes(coordA.toVec(), coordB.toVec());

                    coordB.x = b2.x;
                    coordB.y = b2.y;
                    coordB.z = b2.z;

                    if (isCValid) {
                        Objects.requireNonNull(coordC);
                        Vector3i height = MMState.pinToLine(coordA.toVec(), b2, coordC.toVec());

                        coordC.x = height.x;
                        coordC.y = height.y;
                        coordC.z = height.z;
                    }
                }
            }

            // For cables, coord B must be somewhere on one of the axes
            if (isAValid && isBValid && state.config.placeMode == PlaceMode.CABLES) {
                Objects.requireNonNull(coordA);
                Objects.requireNonNull(coordB);

                Vector3i b = MMState.pinToAxes(coordA.toVec(), coordB.toVec());

                coordB.x = b.x;
                coordB.y = b.y;
                coordB.z = b.z;
            }

            if (isAValid && state.config.coordAOffset != null) {
                GL11.glColor4f(0.15f, 0.6f, 0.75f, 0.75F);
                drawRulers(player, coordA, false, event.partialTicks);
            }

            if (isBValid && state.config.coordBOffset != null) {
                GL11.glColor4f(0.15f, 0.6f, 0.75f, 0.75F);
                drawRulers(player, coordB, false, event.partialTicks);
            }

            if (isCValid && state.config.coordCOffset != null) {
                GL11.glColor4f(0.15f, 0.6f, 0.75f, 0.75F);
                drawRulers(player, coordC, false, event.partialTicks);
            }

            if (isAValid && isBValid) {
                Objects.requireNonNull(coordA);
                Objects.requireNonNull(coordB);

                Location playerLocation = new Location(
                    player.getEntityWorld(),
                    MathHelper.floor_double(player.posX),
                    MathHelper.floor_double(player.posY),
                    MathHelper.floor_double(player.posZ)
                );

                Vector3i vA = coordA.toVec();
                Vector3i vB = coordB.toVec();
                Vector3i vC = null;

                VoxelAABB aabb = new VoxelAABB(vA, vB);

                // expand the AABB if the shape uses coord C
                if ((state.config.placeMode != PlaceMode.GEOMETRY || state.config.shape.requiresC()) && isCValid) {
                    Objects.requireNonNull(coordC);
                    vC = coordC.toVec();

                    aabb.union(vC);
                }

                BoxRenderer.INSTANCE.start(event.partialTicks);

                BoxRenderer.INSTANCE.drawAround(aabb.toBoundingBox(), new Vector3f(0.15f, 0.6f, 0.75f));

                BoxRenderer.INSTANCE.finish();

                boolean needsAnalysis = (System.currentTimeMillis() - lastAnalysisMS) >= ANALYSIS_INTERVAL_MS ||
                    !Objects.equals(lastAnalyzedConfig, state.config);

                boolean needsHintDraw = needsAnalysis || (!Objects.equals(lastPlayerPosition, playerLocation) && tier.maxRange != -1);

                if (needsAnalysis) {
                    lastAnalysisMS = System.currentTimeMillis();
                    lastAnalyzedConfig = state.config;
                    analysisCache = state.getPendingBlocks(tier, player.getEntityWorld());
                    analysisCache.removeIf(b -> b == null || b.getBlock() == Blocks.air);
                    analysisCache.sort(Comparator.comparingInt((PendingBlock b) -> b.renderOrder));

                    AboveHotbarHUD
                        .renderTextAboveHotbar(aabb.describe(), (int) (ANALYSIS_INTERVAL_MS * 20 / 1000), false, false);
                }

                if (needsHintDraw) {
                    lastPlayerPosition = playerLocation;
                    lastDrawer = ItemMatterManipulator.this;
                    drawHints(event, state, player, playerLocation);
                }
            }
        }

        private void renderRegions(RenderWorldLastEvent event, MMState state, EntityPlayer player) {
            Location sourceA = state.config.coordA;
            Location sourceB = state.config.coordB;
            Location paste = state.config.coordC;

            Vector3i lookingAt = MMUtils.getLookingAtLocation(player);

            if (state.config.action != null) {
                switch (state.config.action) {
                    case MARK_COPY_A:
                    case MARK_CUT_A: {
                        sourceA = new Location(player.worldObj, lookingAt);
                        GL11.glColor4f(0.15f, 0.6f, 0.75f, 0.75F);
                        drawRulers(player, sourceA, false, event.partialTicks);
                        break;
                    }
                    case MARK_COPY_B:
                    case MARK_CUT_B: {
                        sourceB = new Location(player.worldObj, lookingAt);
                        GL11.glColor4f(0.15f, 0.6f, 0.75f, 0.75F);
                        drawRulers(player, sourceB, false, event.partialTicks);
                        break;
                    }
                    case MARK_PASTE: {
                        paste = new Location(player.worldObj, lookingAt);
                        GL11.glColor4f(0.75f, 0.5f, 0.15f, 0.75F);
                        drawRulers(player, paste, false, event.partialTicks);
                        break;
                    }
                    case MARK_ARRAY: {
                        GL11.glColor4f(0.4f, 0.75f, 0.15f, 0.75F);
                        drawRulers(player, new Location(player.worldObj, lookingAt), false, event.partialTicks);

                        if (paste != null && paste.isInWorld(player.worldObj)) {
                            state.config.arraySpan = state.config
                                .getArrayMult(player.worldObj, sourceA, sourceB, paste, lookingAt);
                        }

                        break;
                    }
                    default: {
                        return;
                    }
                }
            }

            state.config.coordA = sourceA;
            state.config.coordB = sourceB;
            state.config.coordC = paste;

            boolean isSourceAValid = sourceA != null && sourceA.isInWorld(player.worldObj);
            boolean isSourceBValid = sourceB != null && sourceB.isInWorld(player.worldObj);
            boolean isPasteValid = paste != null && paste.isInWorld(player.worldObj);

            VoxelAABB copyDeltas = null;

            BoxRenderer.INSTANCE.start(event.partialTicks);

            try {
                if (isSourceAValid && isSourceBValid) {
                    Objects.requireNonNull(sourceA);
                    Objects.requireNonNull(sourceB);

                    copyDeltas = new VoxelAABB(sourceA.toVec(), sourceB.toVec());

                    BoxRenderer.INSTANCE
                        .drawAround(copyDeltas.toBoundingBox(), new Vector3f(0.15f, 0.6f, 0.75f));
                }

                VoxelAABB pasteDeltas = null;

                if (isPasteValid) {
                    Objects.requireNonNull(paste);

                    pasteDeltas = state.config.getPasteVisualDeltas(player.worldObj, true);

                    if (pasteDeltas == null) {
                        pasteDeltas = new VoxelAABB(paste.toVec(), paste.toVec());
                    }

                    BoxRenderer.INSTANCE.drawAround(pasteDeltas.toBoundingBox(), new Vector3f(0.75f, 0.5f, 0.15f));

                    Location playerLocation = new Location(
                        player.getEntityWorld(),
                        MathHelper.floor_double(player.posX),
                        MathHelper.floor_double(player.posY),
                        MathHelper.floor_double(player.posZ)
                    );

                    boolean needsAnalysis = (System.currentTimeMillis() - lastAnalysisMS) >= ANALYSIS_INTERVAL_MS ||
                        !Objects.equals(lastAnalyzedConfig, state.config);

                    boolean needsHintDraw = needsAnalysis || !Objects.equals(lastPlayerPosition, playerLocation);

                    if (needsAnalysis) {
                        lastAnalysisMS = System.currentTimeMillis();
                        lastAnalyzedConfig = state.config;
                        analysisCache = state.getPendingBlocks(tier, player.getEntityWorld());
                    }

                    if (needsHintDraw) {
                        lastPlayerPosition = playerLocation;
                        lastDrawer = ItemMatterManipulator.this;
                        drawHints(event, state, player, playerLocation);
                    }
                }

                if (pasteDeltas != null) {
                    String array = "";

                    Vector3i span = state.config.arraySpan;
                    if (span != null) {
                        array = String.format(
                            " stX=%d stY=%d stZ=%d",
                            span.x >= 0 ? span.x + 1 : span.x,
                            span.y >= 0 ? span.y + 1 : span.y,
                            span.z >= 0 ? span.z + 1 : span.z
                        );
                    }

                    AboveHotbarHUD.renderTextAboveHotbar(
                        pasteDeltas.describe() + array,
                        (int) (ANALYSIS_INTERVAL_MS * 20 / 1000),
                        false,
                        false
                    );
                } else if (copyDeltas != null) {
                    AboveHotbarHUD.renderTextAboveHotbar(
                        copyDeltas.describe(),
                        (int) (ANALYSIS_INTERVAL_MS * 20 / 1000),
                        false,
                        false
                    );
                }
            } finally {
                BoxRenderer.INSTANCE.finish();
            }
        }

        private void drawHints(
            RenderWorldLastEvent event,
            MMState state,
            EntityPlayer player,
            Location playerLocation
        ) {
            StructureLibAPI.startHinting(player.worldObj);

            int buildable = tier.maxRange * tier.maxRange;

            int i = 0;

            BlockSpec pooled = new BlockSpec();

            for (PendingBlock pendingBlock : analysisCache) {
                if (tier.maxRange != -1) {
                    int dist2 = pendingBlock.distanceTo2(playerLocation);

                    if (dist2 > buildable) continue;
                }

                if (pendingBlock.spec.isAir() && player.worldObj.isAirBlock(pendingBlock.x, pendingBlock.y, pendingBlock.z)) {
                    continue;
                }

                Block block = pendingBlock.getBlock();
                BlockSpec.fromBlock(pooled, player.worldObj, pendingBlock.x, pendingBlock.y, pendingBlock.z);

                if (pendingBlock.isInWorld(player.worldObj) && block != null && block != Blocks.air && !pooled.isEquivalent(pendingBlock.spec)) {

                    if (++i > RenderingConfig.maxHints) break;

                    StructureLibAPI.hintParticle(
                        player.worldObj,
                        pendingBlock.x,
                        pendingBlock.y,
                        pendingBlock.z,
                        block,
                        pendingBlock.spec.getBlockMeta()
                    );

                    // Exchanging hints should be shown through the block
                    if (state.config.placeMode == PlaceMode.EXCHANGING) {
                        StructureLibAPI.markHintParticleError(
                            player,
                            player.worldObj,
                            pendingBlock.x,
                            pendingBlock.y,
                            pendingBlock.z
                        );
                        // Reset the hint colour so that it doesn't look like an error
                        StructureLibAPI.updateHintParticleTint(
                            player,
                            player.worldObj,
                            pendingBlock.x,
                            pendingBlock.y,
                            pendingBlock.z,
                            new short[] {
                                255, 255, 255, 255
                            }
                        );
                    }
                }
            }

            StructureLibAPI.endHinting(player.worldObj);
        }

        private static Vector3d getVecForDir(ForgeDirection dir) {
            return new Vector3d(dir.offsetX, dir.offsetY, dir.offsetZ);
        }

        private static final int RULER_LENGTH = 128;

        private void drawRulers(EntityPlayer player, Location l, boolean fromSurface, float partialTickTime) {
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glLineWidth(2.0F);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDepthMask(false);

            GL11.glPointSize(4);

            OpenGlHelper.glBlendFunc(770, 771, 1, 0);

            GL11.glPushMatrix();

            double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTickTime;
            double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTickTime;
            double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTickTime;
            GL11.glTranslated(l.x - d0 + 0.5, l.y - d1 + 0.5, l.z - d2 + 0.5);

            Tessellator tessellator = Tessellator.instance;

            try {
                tessellator.startDrawing(GL11.GL_LINES);

                for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                    Vector3d delta = getVecForDir(dir);

                    if (fromSurface) {
                        tessellator.addVertex(delta.x * 0.5, delta.y * 0.5, delta.z * 0.5);
                    } else {
                        tessellator.addVertex(0, 0, 0);
                    }
                    tessellator.addVertex(delta.x * RULER_LENGTH, delta.y * RULER_LENGTH, delta.z * RULER_LENGTH);
                }
            } finally {
                try {
                    tessellator.draw();
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                GL11.glPopMatrix();

                GL11.glDepthMask(true);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_BLEND);
            }
        }
    }

    // #endregion
}
