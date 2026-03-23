package matter_manipulator.common.networking;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
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

    public @NotNull FluidStack readFluidStack() {
        return new FluidStack(FluidRegistry.getFluid(readString(8192)), readVarInt(), readCompoundTag());
    }

    public void writeFluidStack(FluidStack stack) {
        writeString(FluidRegistry.getFluidName(stack.getFluid()));
        writeVarInt(stack.amount);
        writeCompoundTag(stack.tag);
    }

    public interface Encoder<T> {

        void encode(T value, MMPacketBuffer buffer);
    }

    public interface Decoder<T> {

        T decode(MMPacketBuffer buffer);
    }

    public <T> void writeArray(T[] array, Encoder<T> encoder) {
        writeVarInt(array.length);

        for (T value : array) {
            encoder.encode(value, this);
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
            encoder.encode(value, this);
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

        writeList(values, (value, buffer) -> {
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

    private static final int BSON_DOUBLE = 1;
    private static final int BSON_UTF8_STRING = 2;
    private static final int BSON_DOCUMENT = 3;
    private static final int BSON_ARRAY = 4;
    private static final int BSON_BOOLEAN = 8;
    private static final int BSON_NULL = 10;
    private static final int BSON_INT = 16;
    private static final int BSON_LONG = 18;

    public void writeBSON(JsonObject obj) {
        writeIntLE(obj.size());

        for (var e : obj.entrySet()) {
            writeBSONElement(e.getKey(), e.getValue());
        }

        writeByte(0);
    }

    private void writeBSONElement(String ename, JsonElement el) {
        if (el.isJsonNull()) {
            writeByte(BSON_NULL);
            writeBsonCString(ename);
        } else if (el instanceof JsonPrimitive prim) {
            if (prim.isBoolean()) {
                writeByte(BSON_BOOLEAN);
                writeBsonCString(ename);

                writeByte(prim.getAsBoolean() ? 1 : 0);
            } else if (prim.isString()) {
                byte[] utf8 = prim.getAsString().getBytes(StandardCharsets.UTF_8);

                writeByte(BSON_UTF8_STRING);
                writeBsonCString(ename);

                writeIntLE(utf8.length + 1);
                writeBytes(utf8);
                writeByte(0);
            } else {
                Class<? extends Number> type = prim.getAsNumber().getClass();

                if (type == Integer.class || type == Short.class) {
                    writeByte(BSON_INT);
                    writeBsonCString(ename);

                    writeIntLE(prim.getAsInt());
                } else if (type == Long.class || type == BigInteger.class) {
                    writeByte(BSON_LONG);
                    writeBsonCString(ename);

                    writeLongLE(prim.getAsLong());
                } else if (type == Float.class || type == Double.class || type == BigDecimal.class) {
                    writeByte(BSON_DOUBLE);
                    writeBsonCString(ename);

                    writeLongLE(Double.doubleToLongBits(prim.getAsDouble()));
                }
            }
        } else if (el.isJsonArray()) {
            int key = 0;

            writeByte(BSON_ARRAY);
            writeBsonCString(ename);

            writeIntLE(el.getAsJsonArray().size());

            for (JsonElement el2 : el.getAsJsonArray()) {
                writeBSONElement(Integer.toString(key++), el2);
            }

            writeByte(0);
        } else if (el.isJsonObject()) {
            writeByte(BSON_DOCUMENT);
            writeBsonCString(ename);

            writeBSON(el.getAsJsonObject());
        }
    }

    public JsonObject readBSON() {
        int len = readIntLE();

        JsonObject obj = new JsonObject();

        BiConsumer<String, JsonElement> adder = obj::add;

        for (int i = 0; i < len; i++) {
            readBSONElement(adder);
        }

        readByte();

        return obj;
    }

    private void readBSONElement(BiConsumer<String, JsonElement> adder) {
        int type = readByte();
        String ename = readBsonCString();

        switch (type) {
            case BSON_DOUBLE -> {
                adder.accept(ename, new JsonPrimitive(Double.longBitsToDouble(readLongLE())));
            }
            case BSON_UTF8_STRING -> {
                int len = readIntLE();

                byte[] data = new byte[len];

                readBytes(data);

                adder.accept(ename, new JsonPrimitive(new String(data, StandardCharsets.UTF_8)));
            }
            case BSON_DOCUMENT -> {
                adder.accept(ename, readBSON());
            }
            case BSON_ARRAY -> {
                int len = readIntLE();

                JsonArray array = new JsonArray();

                BiConsumer<String, JsonElement> adder2 = (key, value) -> array.add(value);

                for (int i = 0; i < len; i++) {
                    readBSONElement(adder2);
                }

                readByte();

                adder.accept(ename, array);
            }
            case BSON_BOOLEAN -> {
                adder.accept(ename, new JsonPrimitive(readByte() != 0));
            }
            case BSON_NULL -> {
                adder.accept(ename, JsonNull.INSTANCE);
            }
            case BSON_INT -> {
                adder.accept(ename, new JsonPrimitive(readIntLE()));
            }
            case BSON_LONG -> {
                adder.accept(ename, new JsonPrimitive(readLongLE()));
            }
            default -> {
                throw new IllegalStateException("Invalid bson code: " + type);
            }
        }
    }

    private void writeBsonCString(String str) {
        byte[] ascii = str.getBytes(StandardCharsets.UTF_8);

        for (byte b : ascii) {
            if (b == 0) {
                throw new IllegalArgumentException("cstring cannot contain 0: '" + str + "' / " + Arrays.toString(ascii));
            }
        }

        writeBytes(ascii);
        writeByte(0);
    }

    private String readBsonCString() {
        final ByteArrayList bytes = new ByteArrayList();

        byte b;

        while ((b = readByte()) != 0) {
            bytes.add(b);
        }

        return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
    }
}
