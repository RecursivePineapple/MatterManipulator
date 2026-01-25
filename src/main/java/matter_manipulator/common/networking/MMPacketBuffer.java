package matter_manipulator.common.networking;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.Pair;
import matter_manipulator.MMMod;
import matter_manipulator.common.utils.DataUtils;

@SuppressWarnings({ "unused", "UnusedReturnValue" })
public class MMPacketBuffer extends PacketBuffer {

    public MMPacketBuffer(ByteBuf wrapped) {
        super(wrapped);
    }

    @Override
    public NBTTagCompound readCompoundTag() {
        try {
            return super.readCompoundTag();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull ItemStack readItemStack() {
        try {
            return super.readItemStack();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public interface Encoder<T> {

        void encode(MMPacketBuffer buffer, T value);
    }

    public interface Decoder<T> {

        T decode(MMPacketBuffer buffer);
    }

    public <T> void writeArray(T[] array, Encoder<T> encoder) {
        writeVarInt(array.length);

        for (T value : array) {
            encoder.encode(this, value);
        }
    }

    public <T> T[] readArray(T[] zeroLength, Decoder<T> decoder) {
        T[] out = Arrays.copyOf(zeroLength, readVarInt());

        for (int i = 0; i < out.length; i++) {
            out[i] = decoder.decode(this);
        }

        return out;
    }

    public MMPacketBuffer writeByteArray(byte[] array, int offset, int length) {
        writeVarInt(length);

        writeBytes(array, offset, length);

        return this;
    }

    public byte[] readByteArray(byte[] cached) {
        int len = readVarInt();
        byte[] out = len < cached.length ? cached : new byte[len];

        readBytes(out, 0, len);

        return out;
    }

    public MMPacketBuffer writeByteBuf(ByteBuf buffer) {
        byte[] data = new byte[buffer.readableBytes()];
        buffer.readBytes(data);

        writeByteArray(data);
        return this;
    }

    public ByteBuf readByteBuf() {
        return Unpooled.wrappedBuffer(readByteArray());
    }

    public MMPacketBuffer writeByteBuffer(ByteBuffer buffer) {
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        writeByteArray(data);
        return this;
    }

    public ByteBuffer readByteBuffer() {
        return ByteBuffer.wrap(readByteArray());
    }

    public <T> void writeList(List<T> list, Encoder<T> encoder) {
        writeVarInt(list.size());

        for (T value : list) {
            encoder.encode(this, value);
        }
    }

    public <T> ArrayList<T> readList(Decoder<T> decoder) {
        int len = readVarInt();

        ArrayList<T> out = new ArrayList<>(len);

        for (int i = 0; i < len; i++) {
            out.add(decoder.decode(this));
        }

        return out;
    }

    public MMPacketBuffer writeBlockID(int id) {
        writeVarInt(id);

        return this;
    }

    public int readBlockID() {
        return readVarInt();
    }

    private int lastCachedBlock = -1;
    private Block cache;

    public MMPacketBuffer writeBlock(Block block) {
        if (block == cache) {
            writeBlockID(lastCachedBlock);
        } else {
            cache = block;
            lastCachedBlock = Block.getIdFromBlock(block);

            writeBlockID(lastCachedBlock);
        }

        return this;
    }

    public Block readBlock() {
        int id = readBlockID();

        if (id == lastCachedBlock) return cache;

        lastCachedBlock = id;
        cache = Block.getBlockById(id);

        return cache;
    }

    public MMPacketBuffer writeBlockMeta(int meta) {
        writeVarInt(meta);

        return this;
    }

    public int readBlockMeta() {
        return readVarInt();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public IBlockState readBlockState() {
        Block block = readBlock();

        IBlockState state = block.getDefaultState();

        List<Pair<String, String>> props = readList(buffer -> Pair.of(buffer.readString(256), buffer.readString(Short.MAX_VALUE)));

        for (var p : props) {
            IProperty prop = DataUtils.find(state.getPropertyKeys(), p2 -> p2.getName().equals(p.left()));

            if (prop == null) {
                MMMod.LOG.warn("Tried to set invalid property {} ({}) on block {}", p.left(), p.right(), block);
                continue;
            }

            state = mutate(state, prop, p.right());
        }

        return state;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void writeBlockState(IBlockState state) {
        Block block = state.getBlock();

        writeBlock(block);

        IBlockState base = block.getDefaultState();

        List<Pair<String, String>> values = new ArrayList<>();

        for (IProperty prop : state.getPropertyKeys()) {
            Comparable obj = state.getValue(prop);

            if (Objects.equals(obj, base.getValue(prop))) continue;

            values.add(Pair.of(prop.getName(), prop.getName(obj)));
        }

        writeList(values, (buffer, value) -> {
            buffer.writeString(value.left());
            buffer.writeString(value.right());
        });
    }

    private <T extends Comparable<T>, V extends T> IBlockState mutate(IBlockState state, IProperty<T> prop, String value) {
        Optional<T> parsed = prop.parseValue(value);

        if (!parsed.isPresent()) {
            MMMod.LOG.warn("Tried to set invalid property {} ({}) on block {}", prop.getName(), value, state.getBlock());
            return state;
        }

        return state.withProperty(prop, parsed.get());
    }
}
