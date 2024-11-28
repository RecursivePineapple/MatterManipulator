package com.recursive_pineapple.matter_manipulator.common.networking;

import static com.recursive_pineapple.matter_manipulator.common.utils.Mods.AppliedEnergistics2;
import static com.recursive_pineapple.matter_manipulator.common.utils.Mods.GregTech;

import java.util.ArrayList;
import java.util.function.BiConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import com.google.common.io.ByteArrayDataInput;
import com.gtnewhorizon.gtnhlib.util.CoordinatePacker;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import com.recursive_pineapple.matter_manipulator.Config;
import com.recursive_pineapple.matter_manipulator.MMMod;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.Location;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.Transform;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState.BlockRemoveMode;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState.BlockSelectMode;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState.PendingAction;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState.PlaceMode;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState.Shape;
import com.recursive_pineapple.matter_manipulator.common.uplink.IUplinkMulti;
import com.recursive_pineapple.matter_manipulator.common.uplink.UplinkState;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.Pair;

/**
 * Contains all networking messages that the manipulator can send.
 * Entries can be reordered.
 */
public enum Messages {

    MMBPressed(
        server(simple((player, stack, manipulator, state) -> { manipulator.onMMBPressed(player, stack, state); }))),
    SetRemoveMode(server(enumPacket(BlockRemoveMode.values(), (state, value) -> state.config.removeMode = value))),
    SetPlaceMode(server(enumPacket(PlaceMode.values(), (player, stack, manipulator, state, value) -> {

        int requiredBit = switch (value) {
            case COPYING -> ItemMatterManipulator.ALLOW_COPYING;
            case EXCHANGING -> ItemMatterManipulator.ALLOW_EXCHANGING;
            case GEOMETRY -> 0;
            case MOVING -> ItemMatterManipulator.ALLOW_MOVING;
            case CABLES -> ItemMatterManipulator.ALLOW_CABLES;
        };

        if (manipulator.tier.hasCap(requiredBit)) {
            state.config.placeMode = value;
        }
    }))),
    SetBlockSelectMode(
        server(enumPacket(BlockSelectMode.values(), (state, value) -> state.config.blockSelectMode = value))),
    SetPendingAction(server(enumPacket(PendingAction.values(), (state, value) -> state.config.action = value))),
    ClearBlocks(server(simple((player, stack, manipulator, state) -> {
        state.config.setCorners(null);
        state.config.setEdges(null);
        state.config.setFaces(null);
        state.config.setVolumes(null);
        state.config.action = null;
    }))),
    SetShape(server(enumPacket(Shape.values(), (state, value) -> state.config.shape = value))),
    MoveA(server(simple((player, stack, manipulator, state) -> {
        state.config.action = PendingAction.MOVING_COORDS;
        state.config.coordAOffset = new Vector3i();
        state.config.coordBOffset = null;
        state.config.coordCOffset = null;
    }))),
    MoveB(server(simple((player, stack, manipulator, state) -> {
        state.config.action = PendingAction.MOVING_COORDS;
        state.config.coordAOffset = null;
        state.config.coordBOffset = new Vector3i();
        state.config.coordCOffset = null;
    }))),
    MoveC(server(simple((player, stack, manipulator, state) -> {
        state.config.action = PendingAction.MOVING_COORDS;
        state.config.coordAOffset = null;
        state.config.coordBOffset = null;
        state.config.coordCOffset = new Vector3i();
    }))),
    MoveAll(server(simple((player, stack, manipulator, state) -> {
        state.config.action = PendingAction.MOVING_COORDS;

        Vector3i lookingAt = MMUtils.getLookingAtLocation(player);

        if (state.config.coordA == null) {
            state.config.coordAOffset = null;
        } else {
            state.config.coordAOffset = state.config.coordA.toVec()
                .sub(lookingAt);
        }

        if (state.config.coordB == null) {
            state.config.coordBOffset = null;
        } else {
            state.config.coordBOffset = state.config.coordB.toVec()
                .sub(lookingAt);
        }

        if (state.config.coordC == null) {
            state.config.coordCOffset = null;
        } else {
            state.config.coordCOffset = state.config.coordC.toVec()
                .sub(lookingAt);
        }

    }))),
    MoveHere(server(simple((player, stack, manipulator, state) -> {
        if (state.config.shape.requiresC()) {
            if (Location.areCompatible(state.config.coordA, state.config.coordB, state.config.coordC)) {
                Vector3i offsetB = state.config.coordB.toVec()
                    .sub(state.config.coordA.toVec());
                Vector3i offsetC = state.config.coordC.toVec()
                    .sub(state.config.coordA.toVec());

                Vector3i newA = MMUtils.getLookingAtLocation(player);
                Vector3i newB = new Vector3i(newA).add(offsetB);
                Vector3i newC = new Vector3i(newA).add(offsetC);

                state.config.coordA = new Location(player.getEntityWorld(), newA);
                state.config.coordB = new Location(player.getEntityWorld(), newB);
                state.config.coordC = new Location(player.getEntityWorld(), newC);
            }
        } else {
            if (Location.areCompatible(state.config.coordA, state.config.coordB)) {
                Vector3i offsetB = state.config.coordB.toVec()
                    .sub(state.config.coordA.toVec());

                Vector3i newA = MMUtils.getLookingAtLocation(player);
                Vector3i newB = new Vector3i(newA).add(offsetB);

                state.config.coordA = new Location(player.getEntityWorld(), newA);
                state.config.coordB = new Location(player.getEntityWorld(), newB);
            }
        }
    }))),
    MarkCopy(server(simple((player, stack, manipulator, state) -> {
        state.config.action = PendingAction.MARK_COPY_A;
        state.config.coordA = null;
        state.config.coordB = null;
    }))),
    MarkCut(server(simple((player, stack, manipulator, state) -> {
        state.config.action = PendingAction.MARK_CUT_A;
        state.config.coordA = null;
        state.config.coordB = null;
    }))),
    MarkPaste(server(simple((player, stack, manipulator, state) -> {
        state.config.action = PendingAction.MARK_PASTE;
        state.config.coordC = null;
    }))),
    GetRequiredItems(server(intPacket((player, stack, manipulator, state, value) -> {
        if (state.config.placeMode != PlaceMode.COPYING) {
            return;
        }

        if (state.config.coordA == null || state.config.coordB == null || state.config.coordC == null) {
            return;
        }

        MMUtils.createPlanImpl(player, state, manipulator, value);
    }))),
    ClearManualPlans(server(simple((player, stack, manipulator, state) -> {
        if (state.connectToUplink()) {
            state.uplink.clearManualPlans(player);
        }
    }))),
    CancelAutoPlans(server(simple((player, stack, manipulator, state) -> {
        if (state.connectToUplink()) {
            state.uplink.cancelAutoPlans(player);
        }
    }))),
    ClearWhitelist(server(simple((player, stack, manipulator, state) -> { state.config.replaceWhitelist = null; }))),
    UpdateUplinkState(client(new ISimplePacketHandler<UplinkPacket>() {

        @Override
        @SideOnly(Side.CLIENT)
        public void handle(EntityPlayer player, UplinkPacket packet) {
            World theWorld = Minecraft.getMinecraft().theWorld;

            if (theWorld.provider.dimensionId == packet.worldId) {
                Location l = packet.getLocation();

                // TODO: fix this
                // if (theWorld.getTileEntity(l.x, l.y, l.z) instanceof IGregTechTileEntity igte
                //     && igte.getMetaTileEntity() instanceof IUplinkMulti uplink) {
                //     uplink.setState(packet.getState());
                // }
            }
        }

        @Override
        public UplinkPacket getNewPacket(Messages message, @Nullable Object value) {
            UplinkPacket packet = new UplinkPacket(message);

            if (value != null) {
                IUplinkMulti uplink = (IUplinkMulti) value;

                packet.setState(uplink.getLocation(), uplink.getState());
            }

            return packet;
        }
    })),
    TooltipResponse(client(new ISimplePacketHandler<Messages.IntPacket>() {

        @Override
        public void handle(EntityPlayer player, IntPacket packet) {
            ItemMatterManipulator.onTooltipResponse(packet.value);
        }

        @Override
        public IntPacket getNewPacket(Messages message, @Nullable Object value) {
            IntPacket packet = new IntPacket(message);
            packet.value = value == null ? 0 : (int) (Integer) value;
            return packet;
        }
    })),
    TooltipQuery(server(new ISimplePacketHandler<Messages.IntPacket>() {

        @Override
        public void handle(EntityPlayer player, IntPacket packet) {
            if (packet.value < 0 || packet.value > player.inventory.getSizeInventory()) return;

            ItemStack held = player.inventory.getStackInSlot(packet.value);

            if (held != null && held.getItem() instanceof ItemMatterManipulator manipulator) {
                MMState state = ItemMatterManipulator.getState(held);

                int result = 0;

                if (manipulator.tier.hasCap(ItemMatterManipulator.CONNECTS_TO_AE) && AppliedEnergistics2.isModLoaded()) {
                    if (state.connectToMESystem()) {
                        result |= MMUtils.TOOLTIP_HAS_AE;
                        if (state.canInteractWithAE(player)) {
                            result |= MMUtils.TOOLTIP_AE_WORKS;
                        }
                    }
                }

                if (manipulator.tier.hasCap(ItemMatterManipulator.CONNECTS_TO_UPLINK) && GregTech.isModLoaded()) {
                    if (state.uplinkAddress != null) {
                        result |= MMUtils.TOOLTIP_HAS_UPLINK;
                        if (state.connectToUplink()) {
                            result |= MMUtils.TOOLTIP_UPLINK_WORKS;
                        }
                    }
                }

                Messages.TooltipResponse.sendToPlayer((EntityPlayerMP) player, (Integer) result);
            }
        }

        @Override
        public IntPacket getNewPacket(Messages message, @Nullable Object value) {
            IntPacket packet = new IntPacket(message);
            packet.value = value == null ? 0 : (int) (Integer) value;
            return packet;
        }
    })),
    ResetArray(server(simple((player, stack, manipulator, state) -> {
        state.config.arraySpan = null;
        player.inventoryContainer.detectAndSendChanges();
    }))),
    ResetTransform(server(intPacket((player, stack, manipulator, state, value) -> {
        state.config.transform = new Transform();
        player.inventoryContainer.detectAndSendChanges();
    }))),
    ToggleTransformFlip(server(intPacket((player, stack, manipulator, state, value) -> {
        if (state.config.transform == null) state.config.transform = new Transform();

        if ((value & Transform.FLIP_X) != 0) state.config.transform.flipX ^= true;
        if ((value & Transform.FLIP_Y) != 0) state.config.transform.flipY ^= true;
        if ((value & Transform.FLIP_Z) != 0) state.config.transform.flipZ ^= true;
        player.inventoryContainer.detectAndSendChanges();
    }))),
    RotateTransform(server(intPacket((player, stack, manipulator, state, value) -> {
        if (state.config.transform == null) state.config.transform = new Transform();

        int dir = value & 0xFF;

        if (dir < 0 || dir >= ForgeDirection.VALID_DIRECTIONS.length) return;

        int amount = ((value >> 8) & 0xFF) != 0 ? 1 : -1;

        state.config.transform.rotate(ForgeDirection.VALID_DIRECTIONS[dir], amount);
        player.inventoryContainer.detectAndSendChanges();
    }))),
    PlaySound(client(new ISimplePacketHandler<Messages.SoundPacket>() {

        @Override
        @SideOnly(Side.CLIENT)
        public void handle(EntityPlayer player, SoundPacket packet) {
            if (packet.worldId == Minecraft.getMinecraft().theWorld.provider.dimensionId) {
                int x = CoordinatePacker.unpackX(packet.location);
                int y = CoordinatePacker.unpackY(packet.location);
                int z = CoordinatePacker.unpackZ(packet.location);

                SoundResource[] sounds = SoundResource.values();

                if (packet.sound < 0 || packet.sound >= sounds.length) return;

                Minecraft.getMinecraft().theWorld.playSound(x, y, z, sounds[packet.sound].toString(), y, z, false);
            }
        }

        @Override
        public SoundPacket getNewPacket(Messages message, @Nullable Object value) {
            @SuppressWarnings("unchecked")
            Pair<Location, SoundResource> sound = (Pair<Location, SoundResource>) value;

            SoundPacket packet = new SoundPacket(message);

            if (sound != null) {
                packet.worldId = sound.left().worldId;
                packet.location = CoordinatePacker.pack(sound.left().x, sound.left().y, sound.left().z);
                packet.sound = sound.right().ordinal();
            }

            return packet;
        }
    })),

    ;

    private ISimplePacketHandler<? extends SimplePacket> handler;

    private <T extends SimplePacket> Messages(ISimplePacketHandler<T> handler) {
        this.handler = handler;
    }

    public SimplePacket getNewPacket() {
        return handler.getNewPacket(this, null);
    }

    public SimplePacket getNewPacket(Object data) {
        return handler.getNewPacket(this, data);
    }

    public void sendToServer() {
        sendToServer(null);
    }

    public void sendToServer(Object data) {
        if (Config.D1) {
            MMMod.LOG.info("Sending packet to server: " + this + "; " + data);
        }
        CHANNEL.sendToServer(getNewPacket(data));
    }

    public void sendToPlayer(EntityPlayerMP player) {
        sendToPlayer(player, null);
    }

    public void sendToPlayer(EntityPlayerMP player, Object data) {
        if (Config.D1) {
            MMMod.LOG.info("Sending packet to player: " + this + "; " + data + "; " + player);
        }
        CHANNEL.sendToPlayer(getNewPacket(data), player);
    }

    public void sendToPlayersAround(Location location) {
        sendToPlayersAround(location, null);
    }

    public void sendToPlayersAround(Location location, Object data) {
        if (Config.D1) {
            MMMod.LOG
                .info("Sending packet to players around " + location.toString() + ": " + this + "; " + data);
        }
        CHANNEL.sendToAllAround(
            getNewPacket(data),
            new TargetPoint(location.worldId, location.x, location.y, location.z, 256d));
    }

    public void sendToPlayersWithinRange(Location location, Object data) {
        if (Config.D1) {
            MMMod.LOG
                .info("Sending packet to players around " + location.toString() + ": " + this + "; " + data);
        }

        World world = location.getWorld();

        MMPacket packet = getNewPacket(data);

        for (EntityPlayer p : world.playerEntities) {
            EntityPlayerMP player = (EntityPlayerMP) p;

            Chunk chunk = world.getChunkFromBlockCoords(location.x, location.z);
            if (player.getServerForPlayer()
                .getPlayerManager()
                .isPlayerWatchingChunk(player, chunk.xPosition, chunk.zPosition)) {
                CHANNEL.sendToPlayer(packet, player);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void handle(EntityPlayer player, SimplePacket packet) {
        if (Config.D1) {
            MMMod.LOG
                .info("Handling packet: " + this + "; " + packet + "; " + player + "; " + NetworkUtils.isClient());
        }
        ((ISimplePacketHandler<SimplePacket>) handler).handle(player, packet);
    }

    private static final Network CHANNEL = createNetwork();

    private static Network createNetwork() {
        ArrayList<MMPacket> packets = new ArrayList<>();

        for (Messages message : values()) {
            try {
                packets.add(message.getNewPacket());
            } catch (Throwable t) {
                throw new RuntimeException("Could not construct packet in createNetwork", t);
            }
        }

        return new Network("MatterManipulator", packets.toArray(new MMPacket[0]));
    }

    /**
     * Makes sure this class is loaded (initializes {@link #CHANNEL}).
     */
    public static void init() {
        // does nothing
    }

    /**
     * Something that can handle and create simple packets.
     */
    private static interface ISimplePacketHandler<T extends SimplePacket> {

        public void handle(EntityPlayer player, T packet);

        /**
         * Gets a new packet. Called once on initialization with {@code data = null}.
         */
        public T getNewPacket(Messages message, @Nullable Object data);
    }

    /**
     * A packet that doesn't contain any data.
     */
    private static class SimplePacket extends MMPacket {

        public final Messages message;

        public SimplePacket(Messages message) {
            this.message = message;
        }

        @Override
        public byte getPacketID() {
            return (byte) message.ordinal();
        }

        @Override
        public MMPacket decode(ByteArrayDataInput buffer) {
            return message.getNewPacket();
        }

        @Override
        public void encode(ByteBuf buffer) {

        }

        private EntityPlayerMP playerMP;

        @Override
        public void setINetHandler(INetHandler handler) {
            playerMP = handler instanceof NetHandlerPlayServer server ? server.playerEntity : null;
        }

        @Override
        public void process(IBlockAccess world) {
            message.handle(playerMP, this);
        }
    }

    /**
     * A packet that contains a single int.
     */
    private static class IntPacket extends SimplePacket {

        public int value;

        public IntPacket(Messages message) {
            super(message);
        }

        @Override
        public void encode(ByteBuf buffer) {
            buffer.writeInt(value);
        }

        @Override
        public MMPacket decode(ByteArrayDataInput buffer) {
            IntPacket message = new IntPacket(super.message);
            message.value = buffer.readInt();
            return message;
        }

        @Override
        public String toString() {
            return "IntPacket [message=" + message
                + ", value="
                + value
                + " (0b"
                + Integer.toBinaryString(value)
                + ")"
                + "]";
        }
    }

    /**
     * A packet that contains the data needed for updating an uplink.
     */
    private static class UplinkPacket extends SimplePacket {

        public int worldId;
        public long location;
        public byte state;

        public UplinkPacket(Messages message) {
            super(message);
        }

        public void setState(Location location, UplinkState state) {
            this.worldId = location.worldId;
            this.location = CoordinatePacker.pack(location.x, location.y, location.z);
            this.state = (byte) state.ordinal();
        }

        public Location getLocation() {
            return new Location(
                worldId,
                CoordinatePacker.unpackX(location),
                CoordinatePacker.unpackY(location),
                CoordinatePacker.unpackZ(location));
        }

        public UplinkState getState() {
            return UplinkState.values()[state];
        }

        @Override
        public void encode(ByteBuf buffer) {
            buffer.writeInt(worldId);
            buffer.writeLong(location);
            buffer.writeByte(state);
        }

        @Override
        public MMPacket decode(ByteArrayDataInput buffer) {
            UplinkPacket message = new UplinkPacket(super.message);
            message.worldId = buffer.readInt();
            message.location = buffer.readLong();
            message.state = buffer.readByte();
            return message;
        }
    }

    /**
     * Wraps a handler that must be called on the server.
     */
    private static <T extends SimplePacket> ISimplePacketHandler<T> server(ISimplePacketHandler<T> next) {
        return new ISimplePacketHandler<T>() {

            @Override
            public void handle(EntityPlayer player, T packet) {
                if (player == null) {
                    MMMod.LOG
                        .error("Client received server packet, it will be ignored: " + packet.message.name());
                    return;
                }

                next.handle(player, packet);
            }

            @Override
            public T getNewPacket(Messages message, @Nullable Object data) {
                return next.getNewPacket(message, data);
            }
        };
    }

    /**
     * Wraps a handler that must be called on the client.
     */
    private static <T extends SimplePacket> ISimplePacketHandler<T> client(ISimplePacketHandler<T> next) {
        return new ISimplePacketHandler<T>() {

            @Override
            public void handle(EntityPlayer player, T packet) {
                if (player != null) {
                    MMMod.LOG
                        .error("Server received client packet, it will be ignored: " + packet.message.name());
                    return;
                }

                handleImpl(packet);
            }

            @SideOnly(Side.CLIENT)
            private void handleImpl(T packet) {
                next.handle(Minecraft.getMinecraft().thePlayer, packet);
            }

            @Override
            public T getNewPacket(Messages message, @Nullable Object data) {
                return next.getNewPacket(message, data);
            }
        };
    }

    private static interface ISimpleHandler {

        public void handle(EntityPlayer player, ItemStack stack, ItemMatterManipulator manipulator, MMState state);
    }

    /**
     * Handles all manipulator-related state loading & saving.
     * Useful for packets that only change a manipulator's state.
     */
    private static ISimplePacketHandler<SimplePacket> simple(ISimpleHandler handler) {
        return new ISimplePacketHandler<Messages.SimplePacket>() {

            @Override
            public void handle(EntityPlayer player, SimplePacket packet) {
                ItemStack held = player.inventory.getCurrentItem();

                if (held != null && held.getItem() instanceof ItemMatterManipulator manipulator) {
                    MMState state = ItemMatterManipulator.getState(held);

                    handler.handle(player, held, manipulator, state);

                    ItemMatterManipulator.setState(held, state);
                }
            }

            @Override
            public SimplePacket getNewPacket(Messages message, @Nullable Object unused) {
                return new SimplePacket(message);
            }
        };
    }

    /**
     * Wraps a handler that updates an enum within a manipulator's state.
     */
    private static <E extends Enum<E>> ISimplePacketHandler<IntPacket> enumPacket(E[] values,
        BiConsumer<MMState, E> setter) {
        return new ISimplePacketHandler<IntPacket>() {

            @Override
            public void handle(EntityPlayer player, IntPacket packet) {
                E value = packet.value < 0 || packet.value >= values.length ? null : values[packet.value];

                ItemStack held = player.inventory.getCurrentItem();

                if (held != null && held.getItem() instanceof ItemMatterManipulator) {
                    MMState state = ItemMatterManipulator.getState(held);

                    setter.accept(state, value);

                    ItemMatterManipulator.setState(held, state);
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public IntPacket getNewPacket(Messages message, @Nullable Object value) {
                IntPacket packet = new IntPacket(message);
                packet.value = value == null ? -1 : ((E) value).ordinal();
                return packet;
            }
        };
    }

    private static interface IEnumSetter<E extends Enum<E>> {

        public void set(EntityPlayer player, ItemStack stack, ItemMatterManipulator manipulator, MMState state,
            E value);
    }

    /**
     * Wraps a handler that updates an enum. (provides more params than normal)
     */
    private static <E extends Enum<E>> ISimplePacketHandler<IntPacket> enumPacket(E[] values, IEnumSetter<E> setter) {
        return new ISimplePacketHandler<IntPacket>() {

            @Override
            public void handle(EntityPlayer player, IntPacket packet) {
                E value = packet.value < 0 || packet.value >= values.length ? null : values[packet.value];

                ItemStack held = player.inventory.getCurrentItem();

                if (held != null && held.getItem() instanceof ItemMatterManipulator manipulator) {
                    MMState state = ItemMatterManipulator.getState(held);

                    setter.set(player, held, manipulator, state, value);

                    ItemMatterManipulator.setState(held, state);
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public IntPacket getNewPacket(Messages message, @Nullable Object value) {
                IntPacket packet = new IntPacket(message);
                packet.value = value == null ? -1 : ((E) value).ordinal();
                return packet;
            }
        };
    }

    private static interface IIntSetter {

        public void set(EntityPlayer player, ItemStack stack, ItemMatterManipulator manipulator, MMState state,
            int value);
    }

    private static ISimplePacketHandler<IntPacket> intPacket(IIntSetter setter) {
        return new ISimplePacketHandler<Messages.IntPacket>() {

            @Override
            public void handle(EntityPlayer player, IntPacket packet) {
                ItemStack held = player.inventory.getCurrentItem();

                if (held != null && held.getItem() instanceof ItemMatterManipulator manipulator) {
                    MMState state = ItemMatterManipulator.getState(held);

                    setter.set(player, held, manipulator, state, packet.value);

                    ItemMatterManipulator.setState(held, state);
                }
            }

            @Override
            public IntPacket getNewPacket(Messages message, @Nullable Object value) {
                IntPacket packet = new IntPacket(message);
                packet.value = value == null ? 0 : (int) (Integer) value;
                return packet;
            }
        };
    }

    public static void sendSoundToPlayer(EntityPlayerMP player, World world, int x, int y, int z, SoundResource sound, float strength, float pitch) {
        SoundPacket packet = new SoundPacket(Messages.PlaySound);

        packet.worldId = world.provider.dimensionId;
        packet.location = CoordinatePacker.pack(x, y, z);
        packet.sound = sound.ordinal();
        packet.strength = strength;
        packet.pitch = pitch;

        CHANNEL.sendToPlayer(packet, player);
    }

    public static void sendSoundToAllWithinRange(World world, int x, int y, int z, SoundResource sound, float strength, float pitch) {
        SoundPacket packet = new SoundPacket(Messages.PlaySound);

        packet.worldId = world.provider.dimensionId;
        packet.location = CoordinatePacker.pack(x, y, z);
        packet.sound = sound.ordinal();
        packet.strength = strength;
        packet.pitch = pitch;

        for (EntityPlayer p : world.playerEntities) {
            EntityPlayerMP player = (EntityPlayerMP) p;

            Chunk chunk = world.getChunkFromBlockCoords(x, z);
            if (player.getServerForPlayer()
                .getPlayerManager()
                .isPlayerWatchingChunk(player, chunk.xPosition, chunk.zPosition)) {
                CHANNEL.sendToPlayer(packet, player);
            }
        }
    }

    private static class SoundPacket extends SimplePacket {

        public int worldId;
        public long location;
        public int sound;
        public float strength, pitch;

        public SoundPacket(Messages message) {
            super(message);
        }

        @Override
        public void encode(ByteBuf buffer) {
            buffer.writeInt(worldId);
            buffer.writeLong(location);
            buffer.writeInt(sound);
            buffer.writeFloat(strength);
            buffer.writeFloat(pitch);
        }

        @Override
        public MMPacket decode(ByteArrayDataInput buffer) {
            SoundPacket message = new SoundPacket(super.message);

            message.worldId = buffer.readInt();
            message.location = buffer.readLong();
            message.sound = buffer.readInt();
            message.strength = buffer.readFloat();
            message.pitch = buffer.readFloat();

            return message;
        }
    }
}
