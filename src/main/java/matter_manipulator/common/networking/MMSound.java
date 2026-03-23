package matter_manipulator.common.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import matter_manipulator.Tags;

public class MMSound {

    private static class SoundPacket {

        public BlockPos pos;
        public SoundEvent sound;
        public float volume, pitch;

        public SoundPacket(BlockPos pos, SoundEvent sound, float volume, float pitch) {
            this.pos = pos;
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
        }

        private SoundPacket() {
        }

        public void encode(MMPacketBuffer buffer) {
            buffer.writeBlockPos(pos);
            buffer.writeResourceLocation(sound.getSoundName());
            buffer.writeFloat(volume);
            buffer.writeFloat(pitch);
        }

        public static SoundPacket decode(MMPacketBuffer buffer) {
            SoundPacket message = new SoundPacket();

            message.pos = buffer.readBlockPos();
            message.sound = SoundEvent.REGISTRY.getObject(buffer.readResourceLocation());
            message.volume = buffer.readFloat();
            message.pitch = buffer.readFloat();

            return message;
        }

        public static void process(EntityPlayer player, SoundPacket packet) {
            Minecraft.getMinecraft().player.playSound(packet.sound, packet.volume, packet.pitch);
        }
    }

    private static final MMActionWithPayload<SoundPacket> PLAY_SOUND = MMActionWithPayload.client(
        new ResourceLocation(Tags.MODID, "play-sound"),
        SoundPacket::process,
        SoundPacket::encode,
        SoundPacket::decode);

    public static void sendSoundToPlayer(SoundEvent sound, EntityPlayerMP player, BlockPos pos, float volume, float pitch) {
        PLAY_SOUND.sendToPlayer(player, new SoundPacket(pos, sound, volume, pitch));
    }

    public static void sendSoundToAll(SoundEvent sound, WorldServer server, BlockPos pos, float volume, float pitch) {
        PLAY_SOUND.sendToPlayersWatching(server, new SoundPacket(pos, sound, volume, pitch), pos.getX() >> 4, pos.getZ() >> 4);
    }
}
