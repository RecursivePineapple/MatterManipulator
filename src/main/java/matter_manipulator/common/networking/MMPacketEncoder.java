package matter_manipulator.common.networking;

import net.minecraft.network.INetHandler;
import net.minecraft.util.ResourceLocation;

public abstract class MMPacketEncoder<Packet extends MMPacket> {

    protected MMPacketEncoder() {}

    /**
     * Unique ID of this packet.
     */
    public abstract ResourceLocation getPacketID();

    /**
     * Encode the data into given byte buffer.
     */
    public void writePacket(MMPacketBuffer buffer, Packet packet) {
        throw new UnsupportedOperationException("Wrong side");
    }

    /**
     * Decode byte buffer into packet object.
     */
    public Packet readPacket(MMPacketBuffer buffer) {
        throw new UnsupportedOperationException("Wrong side");
    }

    /**
     * Process the received packet.
     *
     */
    public void process(Packet packet) {
        throw new UnsupportedOperationException("Wrong side");
    }

    /**
     * This will be called just before {@link #process(MMPacket)}} to inform the handler about the source and
     * type of
     * connection.
     */
    public void setINetHandler(INetHandler handler, Packet packet) {}
}
