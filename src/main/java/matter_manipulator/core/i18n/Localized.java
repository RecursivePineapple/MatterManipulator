package matter_manipulator.core.i18n;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import matter_manipulator.MMMod;
import matter_manipulator.Tags;
import matter_manipulator.common.networking.MMActionWithPayload;
import matter_manipulator.common.networking.MMPacketBuffer;
import matter_manipulator.common.utils.DataUtils;
import matter_manipulator.common.utils.MCUtils;

/**
 * A data structure that represents an unlocalized message. This can be sent over the network easily.
 *
 * @see ILocalizer
 * @see MCUtils#processFormatStacks(String)
 * @see MMTextBuilder
 */
public class Localized {

    public static final MMActionWithPayload<Localized> CHAT = MMActionWithPayload.client(
        new ResourceLocation(Tags.MODNAME, "chat"),
        (player, packet) -> packet.sendChat(player),
        (buffer, value) -> value.encode(buffer),
        buffer -> new Localized().decode(buffer));

    public Object key;
    public Object[] args;
    public TextFormatting baseColour = null;

    public Localized() {

    }

    Localized(Object key, Object[] args) {
        this.key = key;
        this.args = args;
    }

    /** Localizes a lang key directly */
    public Localized(String key, Object... args) {
        this.key = key;
        this.args = args;
    }

    /** Localizes an {@link ILocalizer}, which may have additional processing on the client */
    public Localized(ILocalizer key, Object... args) {
        this.key = key;
        this.args = args;
    }

    /**
     * Sets the base colour for this entry. Does not clobber the previous style, if the output of
     * {@link #localize(ArgProcessor)} is ran through {@link MCUtils#processFormatStacks(String)}.
     */
    public Localized setBase(TextFormatting base) {
        this.baseColour = base;
        return this;
    }

    private static final byte KEY_LOCALIZER = 0;
    private static final byte KEY_LANG = 1;

    public void encode(MMPacketBuffer buffer) {
        if (key instanceof ILocalizer message) {
            buffer.writeByte(KEY_LOCALIZER);
            buffer.writeResourceLocation(LocalizerRegistry.getLocalizerID(message));
        } else {
            buffer.writeByte(KEY_LANG);
            buffer.writeString((String) key);
        }

        buffer.writeByte(baseColour == null ? -1 : baseColour.ordinal());

        buffer.writeInt(args.length);

        for (Object arg : args) {
            encodeArg(buffer, arg);
        }
    }

    public Localized decode(MMPacketBuffer buffer) {
        this.key = switch (buffer.readByte()) {
            case KEY_LOCALIZER -> LocalizerRegistry.getLocalizer(buffer.readResourceLocation());
            case KEY_LANG -> buffer.readString(Short.MAX_VALUE);
            default -> null;
        };

        baseColour = DataUtils.getIndexSafe(TextFormatting.values(), buffer.readByte());

        this.args = new Object[buffer.readInt()];

        for (int i = 0; i < args.length; i++) {
            args[i] = decodeArg(buffer);
        }

        return this;
    }

    /**
     * Something that converts a list of arguments into a list of strings. 99% of the time you'll just want to use
     * {@link #processArgs(Object[])}. The return type is Object[] because
     * {@link net.minecraft.util.text.translation.I18n#translateToLocalFormatted(String, Object...)} has an Object vararg param.
     */
    public interface ArgProcessor {

        Object[] process(Object[] args);
    }

    /**
     * Localizes this object into a string. Most of the time you'll just want to call {@link #toString()}.
     *
     * @see MCUtils#processFormatStacks(String)
     */
    public String localize(ArgProcessor argProcessor) {
        String colour = baseColour == null ? "" : baseColour.toString();

        // §s and §t are format stack codes, see processFormatStacks for more info
        if (key instanceof ILocalizer message) {
            return "§s" + colour + message.localize(args) + "§t";
        } else {
            return "§s" + colour + MCUtils.translate((String) key, argProcessor.process(args)) + "§t";
        }
    }

    @Override
    public String toString() {
        return localize(Localized::processArgs);
    }

    public void sendChat(EntityPlayer player) {
        if (player instanceof EntityPlayerMP playerMP) {
            CHAT.sendToPlayer(playerMP, this);
        } else {
            player.sendStatusMessage(new TextComponentString(this.toString()), false);
        }
    }

    private static final byte TYPE_INVALID = 0;
    private static final byte TYPE_INT = 1;
    private static final byte TYPE_LONG = 2;
    private static final byte TYPE_FLOAT = 3;
    private static final byte TYPE_DOUBLE = 4;
    private static final byte TYPE_STRING = 5;
    private static final byte TYPE_LOCALIZED = 6;

    private static void encodeArg(MMPacketBuffer buffer, Object arg) {
        if (arg instanceof Integer i) {
            buffer.writeByte(TYPE_INT);
            buffer.writeInt(i);
            return;
        }

        if (arg instanceof Long l) {
            buffer.writeByte(TYPE_LONG);
            buffer.writeLong(l);
            return;
        }

        if (arg instanceof Float f) {
            buffer.writeByte(TYPE_FLOAT);
            buffer.writeFloat(f);
            return;
        }

        if (arg instanceof Double d) {
            buffer.writeByte(TYPE_DOUBLE);
            buffer.writeDouble(d);
            return;
        }

        if (arg instanceof String s) {
            buffer.writeByte(TYPE_STRING);
            buffer.writeString(s);
            return;
        }

        if (arg instanceof Localized l) {
            buffer.writeByte(TYPE_LOCALIZED);
            l.encode(buffer);
            return;
        }

        buffer.writeByte(TYPE_INVALID);

        MMMod.LOG.error("Attempted to send illegal Localized argument over the network: {}", arg, new Exception());
    }

    private static Object decodeArg(MMPacketBuffer buffer) {
        switch (buffer.readByte()) {
            case TYPE_INVALID -> {
                return "<invalid value>";
            }
            case TYPE_INT -> {
                return buffer.readInt();
            }
            case TYPE_LONG -> {
                return buffer.readLong();
            }
            case TYPE_FLOAT -> {
                return buffer.readFloat();
            }
            case TYPE_DOUBLE -> {
                return buffer.readDouble();
            }
            case TYPE_STRING -> {
                return buffer.readString(Short.MAX_VALUE);
            }
            case TYPE_LOCALIZED -> {
                Localized l = new Localized();
                l.decode(buffer);
                return l;
            }
        }

        return "<error>";
    }

    public static Object[] processArgs(Object[] args) {
        String[] out = new String[args.length];

        for (int idx = 0; idx < args.length; idx++) {
            Object arg = args[idx];

            if (arg instanceof Localized l) {
                out[idx] = l.localize(Localized::processArgs);
                continue;
            }

            if (arg instanceof Integer i) {
                out[idx] = MCUtils.formatNumbers(i);
                continue;
            }

            if (arg instanceof Long l) {
                out[idx] = MCUtils.formatNumbers(l);
                continue;
            }

            if (arg instanceof Float f) {
                out[idx] = MCUtils.formatNumbers(f);
                continue;
            }

            if (arg instanceof Double d) {
                out[idx] = MCUtils.formatNumbers(d);
                continue;
            }

            out[idx] = arg.toString();
        }

        return out;
    }
}
