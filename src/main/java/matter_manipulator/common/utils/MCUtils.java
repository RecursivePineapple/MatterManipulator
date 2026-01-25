package matter_manipulator.common.utils;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class MCUtils {

    public static final String BLACK = TextFormatting.BLACK.toString();
    public static final String DARK_BLUE = TextFormatting.DARK_BLUE.toString();
    public static final String DARK_GREEN = TextFormatting.DARK_GREEN.toString();
    public static final String DARK_AQUA = TextFormatting.DARK_AQUA.toString();
    public static final String DARK_RED = TextFormatting.DARK_RED.toString();
    public static final String DARK_PURPLE = TextFormatting.DARK_PURPLE.toString();
    public static final String GOLD = TextFormatting.GOLD.toString();
    public static final String GRAY = TextFormatting.GRAY.toString();
    public static final String DARK_GRAY = TextFormatting.DARK_GRAY.toString();
    public static final String BLUE = TextFormatting.BLUE.toString();
    public static final String GREEN = TextFormatting.GREEN.toString();
    public static final String AQUA = TextFormatting.AQUA.toString();
    public static final String RED = TextFormatting.RED.toString();
    public static final String LIGHT_PURPLE = TextFormatting.LIGHT_PURPLE.toString();
    public static final String YELLOW = TextFormatting.YELLOW.toString();
    public static final String WHITE = TextFormatting.WHITE.toString();
    public static final String OBFUSCATED = TextFormatting.OBFUSCATED.toString();
    public static final String BOLD = TextFormatting.BOLD.toString();
    public static final String STRIKETHROUGH = TextFormatting.STRIKETHROUGH.toString();
    public static final String UNDERLINE = TextFormatting.UNDERLINE.toString();
    public static final String ITALIC = TextFormatting.ITALIC.toString();
    public static final String RESET = TextFormatting.RESET.toString();
    public static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");
    /**
     * Formats a number with group separator and at most 2 fraction digits.
     */
    private static final Map<Locale, DecimalFormat> decimalFormatters = new HashMap<>();
    static final Pattern INTEGER = Pattern.compile("\\d+");
    static final Pattern FLOAT = Pattern.compile("\\d+\\.\\d+");

    public static void sendErrorToPlayer(EntityPlayer player, String msg) {
        player.sendStatusMessage(new TextComponentString(RED + msg), true);
    }

    public static void sendWarningToPlayer(EntityPlayer player, String msg) {
        player.sendStatusMessage(new TextComponentString(GOLD + msg), true);
    }

    public static void sendInfoToPlayer(EntityPlayer player, String msg) {
        player.sendStatusMessage(new TextComponentString(GRAY + msg), true);
    }

    public static void sendChatToPlayer(EntityPlayer player, String msg) {
        player.sendStatusMessage(new TextComponentString(msg), true);
    }

    public static String translate(String key, Object... args) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            return net.minecraft.client.resources.I18n.format(key, args);
        } else {
            //noinspection deprecation
            return net.minecraft.util.text.translation.I18n.translateToLocalFormatted(key, args);
        }
    }

    public static String stripFormat(String text) {
        return FORMATTING_CODE_PATTERN.matcher(text).replaceAll("");
    }

    private static DecimalFormat getDecimalFormat() {
        return decimalFormatters.computeIfAbsent(Locale.getDefault(Locale.Category.FORMAT), locale -> {
            DecimalFormat numberFormat = new DecimalFormat(); // uses the necessary locale inside anyway
            numberFormat.setGroupingUsed(true);
            numberFormat.setMaximumFractionDigits(2);
            numberFormat.setRoundingMode(RoundingMode.HALF_UP);
            DecimalFormatSymbols decimalFormatSymbols = numberFormat.getDecimalFormatSymbols();
            decimalFormatSymbols.setGroupingSeparator(','); // Use sensible separator for best clarity.
            numberFormat.setDecimalFormatSymbols(decimalFormatSymbols);
            return numberFormat;
        });
    }

    public static String formatNumbers(BigInteger aNumber) {
        return getDecimalFormat().format(aNumber);
    }

    public static String formatNumbers(long aNumber) {
        return getDecimalFormat().format(aNumber);
    }

    public static String formatNumbers(double aNumber) {
        return getDecimalFormat().format(aNumber);
    }

    public static String getDirectionDisplayName(EnumFacing dir) {
        return getDirectionDisplayName(dir, false);
    }

    public static String getDirectionDisplayName(EnumFacing dir, boolean unknownIsCentre) {
        if (dir == null) return unknownIsCentre ? "Center" : "Unknown";

        return switch (dir) {
            case DOWN -> "Down";
            case EAST -> "East";
            case NORTH -> "North";
            case SOUTH -> "South";
            case UP -> "Up";
            case WEST -> "West";
        };
    }

    public static <T extends NBTBase> T copy(T tag) {
        //noinspection unchecked
        return tag == null ? null : (T) tag.copy();
    }

    private static final char FORMAT_ESCAPE = '§';
    public static final String FORMAT_PUSH_STACK = FORMAT_ESCAPE + "s";
    public static final String FORMAT_POP_STACK = FORMAT_ESCAPE + "t";

    /**
     * Pre-processes a localized chat message with custom format push/pop instructions. This allows nested localizations
     * to set their own format without clobbering whatever came before, without needing any extra context info.
     * <p>
     * </p>
     * Example: {@code §a§lHello §s§eWorld§t!} is transformed into {@code §a§lHello §eWorld§a§l!}
     */
    public static String processFormatStacks(String message) {
        // Short circuit if there aren't any pops, because pops mutate while pushes don't.
        // Invalid codes are ignored by the font renderer so it won't cause problems if we ignore erroneous pushes.
        if (!message.contains(FORMAT_POP_STACK)) return message;

        StringBuilder out = new StringBuilder();
        out.ensureCapacity(message.length() * 6 / 5);

        int len = message.length();

        ArrayDeque<String> stack = new ArrayDeque<>();
        String currentFormat = "";

        int start = 0;

        while (start < len) {
            // Find the next format escape char
            int end = message.indexOf(FORMAT_ESCAPE, start);

            // If we hit the end of the string, push the rest of the input and stop
            if (end == -1) {
                out.append(message, start, len);
                break;
            }

            // If there was any text from the end of the previous code to the start of this one, add it to the output
            // buffer
            if (end > start) {
                out.append(message, start, end);
            }

            // If the current format escape char has a code after it, check it
            if (end < len - 1) {
                char code = message.charAt(end + 1);

                if (code >= '0' && code <= '9' || code >= 'a' && code <= 'f') {
                    // Colours, use as-is and erase the previous format
                    currentFormat = "" + FORMAT_ESCAPE + code;
                    out.append(FORMAT_ESCAPE)
                        .append(code);
                } else if (code >= 'k' && code <= 'o') {
                    // Styles, use as-is and append to the colour style (note: this may act up with repeated style
                    // codes, but it shouldn't break anything).
                    currentFormat = currentFormat + FORMAT_ESCAPE + code;
                    out.append(FORMAT_ESCAPE)
                        .append(code);
                } else if (code == 'r') {
                    // Reset, use as-is and clear the format
                    currentFormat = "";
                    out.append(FORMAT_ESCAPE)
                        .append(code);
                } else if (code == 's') {
                    // Push, save the current format and don't emit to the output buffer
                    stack.push(currentFormat);
                } else if (code == 't') {
                    if (stack.isEmpty()) {
                        // No format in the stack, reset it
                        out.append(FORMAT_ESCAPE)
                            .append('r');
                    } else {
                        // Pop, restore the top format and don't emit to the output buffer
                        currentFormat = stack.pop();
                        out.append(currentFormat);
                    }
                }

                // Skip the format escape along with its code
                start = end + 2;
            }
        }

        return out.toString();
    }
}
