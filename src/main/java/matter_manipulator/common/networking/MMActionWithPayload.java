package matter_manipulator.common.networking;

import java.util.function.BiConsumer;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.github.bsideup.jabel.Desugar;
import matter_manipulator.MMMod;
import matter_manipulator.Tags;
import matter_manipulator.common.networking.MMActionWithPayload.ActionPacketWithPayload;
import matter_manipulator.common.networking.MMPacketBuffer.Decoder;
import matter_manipulator.common.networking.MMPacketBuffer.Encoder;

public class MMActionWithPayload<T> extends MMPacketEncoder<ActionPacketWithPayload<T>> {

    @Desugar
    public record ActionPacketWithPayload<T>(ResourceLocation id, T payload) implements MMPacket {
        @Override
        public ResourceLocation getPacketID() {
            return id;
        }
    }

    public final ResourceLocation id;
    private final BiConsumer<EntityPlayer, T> callback;
    private final Side receiver;
    private final Encoder<T> encoder;
    private final Decoder<T> decoder;

    private MMActionWithPayload(ResourceLocation id, BiConsumer<EntityPlayer, T> callback, Side receiver, Encoder<T> encoder, Decoder<T> decoder) {
        this.id = id;
        this.callback = callback;
        this.receiver = receiver;
        this.encoder = encoder;
        this.decoder = decoder;

        MMNetwork.CHANNEL.registerPacketEncoder(id, this);
    }

    public static <T> MMActionWithPayload<T> client(ResourceLocation id, BiConsumer<EntityPlayer, T> callback, Encoder<T> encoder, Decoder<T> decoder) {
        return new MMActionWithPayload<>(id, callback, Side.CLIENT, encoder, decoder);
    }

    public static <T> MMActionWithPayload<T> server(ResourceLocation id, BiConsumer<EntityPlayerMP, T> callback, Encoder<T> encoder, Decoder<T> decoder) {
        return new MMActionWithPayload<>(id, (player, value) -> callback.accept((EntityPlayerMP) player, value), Side.SERVER, encoder, decoder);
    }

    @Internal
    public static <T> MMActionWithPayload<T> client(String id, BiConsumer<EntityPlayer, T> callback, Encoder<T> encoder, Decoder<T> decoder) {
        return new MMActionWithPayload<>(new ResourceLocation(Tags.MODID, id), callback, Side.CLIENT, encoder, decoder);
    }

    @Internal
    public static <T> MMActionWithPayload<T> server(String id, BiConsumer<EntityPlayerMP, T> callback, Encoder<T> encoder, Decoder<T> decoder) {
        return new MMActionWithPayload<>(new ResourceLocation(Tags.MODID, id), (player, value) -> callback.accept((EntityPlayerMP) player, value), Side.SERVER, encoder, decoder);
    }

    public ActionPacketWithPayload<T> createPacket(T payload) {
        return new ActionPacketWithPayload<>(id, payload);
    }

    public void sendToServer(T payload) {
        MMNetwork.CHANNEL.sendToServer(createPacket(payload));
    }

    public void sendToPlayer(EntityPlayerMP player, T payload) {
        MMNetwork.CHANNEL.sendToPlayer(createPacket(payload), player);
    }

    public void sendToPlayersWatching(WorldServer world, T payload, int chunkX, int chunkZ) {
        MMNetwork.CHANNEL.sendToPlayersWatching(world, createPacket(payload), chunkX, chunkZ);
    }

    @Override
    public ResourceLocation getPacketID() {
        return id;
    }

    @Override
    public void writePacket(MMPacketBuffer buffer, ActionPacketWithPayload<T> packet) {
        encoder.encode(buffer, packet.payload);
    }

    @Override
    public ActionPacketWithPayload<T> readPacket(MMPacketBuffer buffer) {
        return new ActionPacketWithPayload<>(id, decoder.decode(buffer));
    }

    private EntityPlayer player;

    @Override
    public void setINetHandler(INetHandler handler, ActionPacketWithPayload<T> packet) {
        player = null;

        if (handler instanceof NetHandlerPlayServer server) {
            if (receiver != Side.SERVER) {
                MMMod.LOG.error("Handling server->client action on server: this packet should only be sent to the client from the server ({})", id);
                return;
            }

            player = server.player;
        } else if (handler instanceof NetHandlerPlayClient) {
            if (receiver != Side.CLIENT) {
                MMMod.LOG.error("Handling client->server action on client: this packet should only be sent to the server from the client ({})", id);
                return;
            }

            player = MMMod.proxy.getThePlayer();
        }
    }

    @Override
    public void process(ActionPacketWithPayload<T> packet) {
        boolean shouldBeClient = this.receiver == Side.CLIENT;
        boolean isClient = player != null && player.world.isRemote;

        if (shouldBeClient == isClient) {
            callback.accept(player, packet.payload);
        } else {
            MMMod.LOG.warn("Not running callback for packet {} (should be client: {}, is client: {}, player: {})", id, shouldBeClient, isClient, player);
        }
    }
}
