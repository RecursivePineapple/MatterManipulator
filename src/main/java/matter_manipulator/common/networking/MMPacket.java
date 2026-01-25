package matter_manipulator.common.networking;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;

public interface MMPacket {

    ResourceLocation getPacketID();

    default void sendToServer() {
        MMNetwork.CHANNEL.sendToServer(this);
    }

    default void sendToPlayer(EntityPlayerMP player) {
        MMNetwork.CHANNEL.sendToPlayer(this, player);
    }

    default void sendToPlayersWatching(WorldServer world, int chunkX, int chunkZ) {
        MMNetwork.CHANNEL.sendToPlayersWatching(world, this, chunkX, chunkZ);
    }
}
