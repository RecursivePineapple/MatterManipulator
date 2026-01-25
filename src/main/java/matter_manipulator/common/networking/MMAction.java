package matter_manipulator.common.networking;

import java.util.function.Consumer;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.github.bsideup.jabel.Desugar;
import matter_manipulator.MMMod;
import matter_manipulator.Tags;
import matter_manipulator.common.networking.MMAction.ActionPacket;

public class MMAction extends MMPacketEncoder<ActionPacket> {

    @Desugar
    public record ActionPacket(ResourceLocation id) implements MMPacket {
        @Override
        public ResourceLocation getPacketID() {
            return id;
        }
    }

    public final ResourceLocation id;
    private final Consumer<EntityPlayer> callback;
    private final Side receiver;

    private final ActionPacket packet;

    private MMAction(ResourceLocation id, Consumer<EntityPlayer> callback, Side receiver) {
        this.id = id;
        this.callback = callback;
        this.receiver = receiver;

        this.packet = new ActionPacket(id);

        MMNetwork.CHANNEL.registerPacketEncoder(id, this);
    }

    public static MMAction client(ResourceLocation id, Consumer<EntityPlayer> callback) {
        return new MMAction(id, callback, Side.CLIENT);
    }

    public static MMAction server(ResourceLocation id, Consumer<EntityPlayerMP> callback) {
        return new MMAction(id, player -> callback.accept((EntityPlayerMP) player), Side.SERVER);
    }

    @Internal
    public static MMAction client(String id, Consumer<EntityPlayer> callback) {
        return new MMAction(new ResourceLocation(Tags.MODID, id), callback, Side.CLIENT);
    }

    @Internal
    public static MMAction server(String id, Consumer<EntityPlayerMP> callback) {
        return new MMAction(new ResourceLocation(Tags.MODID, id), player -> callback.accept((EntityPlayerMP) player), Side.SERVER);
    }

    public ActionPacket createPacket() {
        return packet;
    }

    public void sendToServer() {
        MMNetwork.CHANNEL.sendToServer(createPacket());
    }

    public void sendToPlayer(EntityPlayerMP player) {
        MMNetwork.CHANNEL.sendToPlayer(createPacket(), player);
    }

    @Override
    public ResourceLocation getPacketID() {
        return id;
    }

    @Override
    public void writePacket(MMPacketBuffer buffer, ActionPacket packet) {

    }

    @Override
    public ActionPacket readPacket(MMPacketBuffer buffer) {
        return new ActionPacket(id);
    }

    private EntityPlayer player;

    @Override
    public void setINetHandler(INetHandler handler, ActionPacket packet) {
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
    public void process(ActionPacket packet) {
        boolean shouldBeClient = this.receiver == Side.CLIENT;
        boolean isClient = player != null && player.world.isRemote;

        if (shouldBeClient == isClient) {
            callback.accept(player);
        } else {
            MMMod.LOG.warn("Not running callback for packet {} (should be client: {}, is client: {}, player: {})", id, shouldBeClient, isClient, player);
        }
    }
}
