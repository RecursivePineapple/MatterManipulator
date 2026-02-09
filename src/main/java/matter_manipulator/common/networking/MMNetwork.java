package matter_manipulator.common.networking;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.MessageToMessageCodec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import matter_manipulator.Tags;

@SuppressWarnings("unused")
@ChannelHandler.Sharable
public class MMNetwork extends MessageToMessageCodec<FMLProxyPacket, MMPacket> {

    private final EnumMap<Side, FMLEmbeddedChannel> channel;
    private final Map<ResourceLocation, MMPacketEncoder<MMPacket>> encoders = new Object2ObjectOpenHashMap<>();

    public static final MMNetwork CHANNEL = new MMNetwork();

    private MMNetwork() {
        this.channel = NetworkRegistry.INSTANCE.newChannel(Tags.MODID, this, new HandlerShared());
    }

    public static void init() {
        // forces this class to be loaded
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void registerPacketEncoder(ResourceLocation id, MMPacketEncoder encoder) {
        encoders.put(id, encoder);
    }

    @Override
    protected void encode(ChannelHandlerContext context, MMPacket packet, List<Object> output) {
        MMPacketBuffer buffer = new MMPacketBuffer(Unpooled.buffer(256));

        MMPacketEncoder<MMPacket> encoder = this.encoders.get(packet.getPacketID());

        buffer.writeResourceLocation(packet.getPacketID());
        encoder.writePacket(buffer, packet);

        output.add(
            new FMLProxyPacket(
                buffer,
                context.channel()
                    .attr(NetworkRegistry.FML_CHANNEL)
                    .get()));
    }

    @Override
    protected void decode(ChannelHandlerContext context, FMLProxyPacket proxyPacket, List<Object> output) {
        MMPacketBuffer buffer = new MMPacketBuffer(proxyPacket.payload());

        ResourceLocation loc = buffer.readResourceLocation();

        MMPacketEncoder<MMPacket> encoder = this.encoders.get(loc);

        MMPacket packet = encoder.readPacket(buffer);

        encoder.setINetHandler(proxyPacket.handler(), packet);
        output.add(packet);
    }

    public void sendToPlayer(MMPacket packet, EntityPlayerMP player) {
        this.channel.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.PLAYER);
        this.channel.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
            .set(player);
        this.channel.get(Side.SERVER)
            .writeAndFlush(packet);
    }

    public void sendToAllAround(MMPacket packet, NetworkRegistry.TargetPoint position) {
        this.channel.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        this.channel.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
            .set(position);
        this.channel.get(Side.SERVER)
            .writeAndFlush(packet);
    }

    public void sendToAll(MMPacket packet) {
        this.channel.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.ALL);
        this.channel.get(Side.SERVER)
            .writeAndFlush(packet);
    }

    public void sendToServer(MMPacket packet) {
        this.channel.get(Side.CLIENT)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        this.channel.get(Side.CLIENT)
            .writeAndFlush(packet);
    }

    public void sendToPlayersWatching(WorldServer world, MMPacket packet, int chunkX, int chunkZ) {
        var entry = world.getPlayerChunkMap().getEntry(chunkX, chunkZ);

        //noinspection DataFlowIssue
        for (EntityPlayerMP player : entry.getWatchingPlayers()) {
            sendToPlayer(packet, player);
        }
    }

    @Sharable
    private class HandlerShared extends SimpleChannelInboundHandler<MMPacket> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, MMPacket packet) {
            encoders.get(packet.getPacketID()).process(packet);
        }

        @SideOnly(Side.CLIENT)
        private World getClientWorld() {
            return Minecraft.getMinecraft().world;
        }
    }
}
