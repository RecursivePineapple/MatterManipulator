package matter_manipulator.common.utils;

import net.minecraftforge.oredict.OreDictionary;

public class MMValues {

    private MMValues() {}

    /**
     * Renamed from "MATERIAL_UNIT" to just "M"
     * <p/>
     * This is worth exactly one normal Item. This Constant can be divided by many commonly used Numbers such as 1, 2,
     * 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 15, 16, 18, 20, 21, 24, ... 64 or 81 without losing precision and is for that
     * reason used as Unit of Amount. But it is also small enough to be multiplied with larger Numbers.
     * <p/>
     * This is used to determine the amount of Material contained inside a prefixed Ore. For example Nugget = M / 9 as
     * it contains out of 1/9 of an Ingot.
     */
    public static final long M = 3628800;

    /**
     * Renamed from "FLUID_MATERIAL_UNIT" to just "L"
     * <p/>
     * Fluid per Material Unit (Prime Factors: 3 * 3 * 2 * 2 * 2 * 2)
     */
    public static final long L = 144;

    /**
     * The Item WildCard Tag. Even shorter than the "-1" of the past
     */
    public static final short W = OreDictionary.WILDCARD_VALUE;

    /**
     * The Voltage Tiers. Use this Array instead of the old named Voltage Variables
     */
    public static final long[] V = new long[] {
        8L,
        32L,
        128L,
        512L,
        2048L,
        8192L,
        32_768L,
        131_072L,
        524_288L,
        2_097_152L,
        8_388_608L,
        33_554_432L,
        134_217_728L,
        536_870_912L,
        Integer.MAX_VALUE - 7,
        // Error tier to prevent out of bounds errors. Not really a real tier (for now).
        8_589_934_592L
    };

}
