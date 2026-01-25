package matter_manipulator.core.i18n;

import java.util.ArrayList;

import net.minecraft.util.text.TextFormatting;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import matter_manipulator.common.utils.MCUtils;

/**
 * A text builder meant for use with complex localization formats. All formatting is handled by this text builder, so
 * localization keys should just include the text. All values are converted to strings when localized. Localization is
 * deferred as long as possible, and may be deferred to the client if {@link #toLocalized()} is used.
 *
 * @see Localized
 * @see MCUtils#processFormatStacks(String)
 */
public class MMTextBuilder {

    public static final TextFormatting NAME = TextFormatting.DARK_AQUA;
    public static final TextFormatting NUMERIC = TextFormatting.GOLD;
    public static final TextFormatting VALUE = TextFormatting.GREEN;

    public Object key;
    public ArrayList<Object> values = new ArrayList<>();
    public TextFormatting base = TextFormatting.WHITE;

    public MMTextBuilder(String langKey) {
        this.key = langKey;
    }

    public MMTextBuilder(ILocalizer message) {
        this.key = message;
    }

    public MMTextBuilder setBase(TextFormatting base) {
        this.base = base;
        return this;
    }

    public MMTextBuilder add(@Nullable Object style, @NotNull Object data) {
        values.add((style == null ? "" : style.toString()) + data + base);

        return this;
    }

    public MMTextBuilder addText(String text) {
        add(base, text);
        return this;
    }

    public MMTextBuilder addLocalized(Localized l) {
        values.add(l);
        return this;
    }

    public MMTextBuilder addName(String name) {
        add(NAME, name);
        return this;
    }

    public MMTextBuilder addCoord(int x, int y, int z) {
        values.add(
            "X=" + NUMERIC
                + MCUtils.formatNumbers(x)
                + base
                + " Y="
                + NUMERIC
                + MCUtils.formatNumbers(y)
                + base
                + " Z="
                + NUMERIC
                + MCUtils.formatNumbers(z)
                + base);
        return this;
    }

    public MMTextBuilder addValue(String value) {
        add(VALUE, value);
        return this;
    }

    public MMTextBuilder addNumber(int i) {
        add(NUMERIC, MCUtils.formatNumbers(i));
        return this;
    }

    public MMTextBuilder addNumber(long l) {
        add(NUMERIC, MCUtils.formatNumbers(l));
        return this;
    }

    public MMTextBuilder addNumber(float f) {
        add(NUMERIC, MCUtils.formatNumbers(f));
        return this;
    }

    public MMTextBuilder addNumber(double d) {
        add(NUMERIC, MCUtils.formatNumbers(d));
        return this;
    }

    public MMTextBuilder addNumber(String s) {
        add(NUMERIC, s);
        return this;
    }

    public Localized toLocalized() {
        return new Localized(key, values.toArray()).setBase(base);
    }

    @Override
    public String toString() {
        return toLocalized().toString();
    }
}
