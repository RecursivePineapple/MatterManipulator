package matter_manipulator.common.utils;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.oredict.OreDictionary;

import matter_manipulator.common.items.ItemMatterManipulator;
import matter_manipulator.common.state.MMState;

public class MMUtils {

    /**
     * The ME network is online and the player is within range of an access point.
     */
    public static final int TOOLTIP_AE_WORKS = 0b1;
    /**
     * The uplink is online and active.
     */
    public static final int TOOLTIP_UPLINK_WORKS = 0b10;
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

    /**
     * Plans will have jobs automatically started (see
     * {@link matter_manipulator.common.utils.items.ItemUtils#createPlanImpl(EntityPlayer, MMState, ItemMatterManipulator, int)}).
     */
    public static final int PLAN_AUTO_SUBMIT = 0b1;
    /**
     * Plans will ignore existing blocks (see {@link matter_manipulator.common.utils.items.ItemUtils#createPlanImpl(EntityPlayer, MMState, ItemMatterManipulator, int)}).
     */
    public static final int PLAN_ALL = 0b10;

}
