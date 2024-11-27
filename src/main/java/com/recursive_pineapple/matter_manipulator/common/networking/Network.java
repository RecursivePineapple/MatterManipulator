package com.recursive_pineapple.matter_manipulator.common.networking;

import java.util.EnumMap;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.recursive_pineapple.matter_manipulator.MMMod;

import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import gregtech.api.enums.GTValues;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.MessageToMessageCodec;

@ChannelHandler.Sharable
public class Network extends MessageToMessageCodec<FMLProxyPacket, MMPacket> {

    private final EnumMap<Side, FMLEmbeddedChannel> mChannel;
    private final MMPacket[] mSubChannels;

    public Network(String channelName, MMPacket... packetTypes) {
        this.mChannel = NetworkRegistry.INSTANCE.newChannel(channelName, this, new HandlerShared());
        final int lastPId = packetTypes[packetTypes.length - 1].getPacketID();
        this.mSubChannels = new MMPacket[lastPId + 1];
        for (MMPacket packetType : packetTypes) {
            final int pId = packetType.getPacketID();
            if (this.mSubChannels[pId] == null) this.mSubChannels[pId] = packetType;
            else throw new IllegalArgumentException("Duplicate Packet ID! " + pId);
        }
    }

    @Override
    protected void encode(ChannelHandlerContext aContext, MMPacket aPacket, List<Object> aOutput) {
        final ByteBuf tBuf = Unpooled.buffer()
            .writeByte(aPacket.getPacketID());
        aPacket.encode(tBuf);
        aOutput.add(
            new FMLProxyPacket(
                tBuf,
                aContext.channel()
                    .attr(NetworkRegistry.FML_CHANNEL)
                    .get()));
    }

    @Override
    protected void decode(ChannelHandlerContext aContext, FMLProxyPacket aPacket, List<Object> aOutput) {
        final ByteArrayDataInput aData = ByteStreams.newDataInput(
            aPacket.payload()
                .array());
        final MMPacket tPacket = this.mSubChannels[aData.readByte()].decode(aData);
        tPacket.setINetHandler(aPacket.handler());
        aOutput.add(tPacket);
    }

    public void sendToPlayer(MMPacket aPacket, EntityPlayerMP aPlayer) {
        if (aPacket == null) {
            MMMod.LOG.info("packet null");
            return;
        }
        if (aPlayer == null) {
            MMMod.LOG.info("player null");
            return;
        }
        this.mChannel.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.PLAYER);
        this.mChannel.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
            .set(aPlayer);
        this.mChannel.get(Side.SERVER)
            .writeAndFlush(aPacket);
    }

    public void sendToAllAround(MMPacket aPacket, NetworkRegistry.TargetPoint aPosition) {
        this.mChannel.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        this.mChannel.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
            .set(aPosition);
        this.mChannel.get(Side.SERVER)
            .writeAndFlush(aPacket);
    }

    public void sendToAll(MMPacket aPacket) {
        this.mChannel.get(Side.SERVER)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.ALL);
        this.mChannel.get(Side.SERVER)
            .writeAndFlush(aPacket);
    }

    public void sendToServer(MMPacket aPacket) {
        this.mChannel.get(Side.CLIENT)
            .attr(FMLOutboundHandler.FML_MESSAGETARGET)
            .set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        this.mChannel.get(Side.CLIENT)
            .writeAndFlush(aPacket);
    }

    public void sendPacketToAllPlayersInRange(World aWorld, MMPacket aPacket, int aX, int aZ) {
        if (!aWorld.isRemote) {
            for (Object tObject : aWorld.playerEntities) {
                if (!(tObject instanceof EntityPlayerMP tPlayer)) {
                    break;
                }
                Chunk tChunk = aWorld.getChunkFromBlockCoords(aX, aZ);
                if (tPlayer.getServerForPlayer()
                    .getPlayerManager()
                    .isPlayerWatchingChunk(tPlayer, tChunk.xPosition, tChunk.zPosition)) {
                    sendToPlayer(aPacket, tPlayer);
                }
            }
        }
    }

    @ChannelHandler.Sharable
    static final class HandlerShared extends SimpleChannelInboundHandler<MMPacket> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, MMPacket aPacket) {
            final EntityPlayer aPlayer = GTValues.GT.getThePlayer();
            aPacket.process(aPlayer == null ? null : aPlayer.worldObj);
        }
    }
}