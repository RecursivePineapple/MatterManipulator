package matter_manipulator.common.items;

import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import matter_manipulator.GlobalMMConfig;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public enum ManipulatorTier {

    // spotless:off
    Tier0(
        OptionalInt.of(32),
        16,
        20,
        3,
        10_000_000L,
        ItemMatterManipulator.ALLOW_GEOMETRY,
        ImmutableList.of(MMUpgrades.Mining, MMUpgrades.Speed, MMUpgrades.PowerEff),
        MMItemList.MK0),
    Tier1(
        OptionalInt.of(64),
        32,
        10,
        5,
        100_000_000L,
        ItemMatterManipulator.ALLOW_GEOMETRY
            | ItemMatterManipulator.CONNECTS_TO_AE
            | ItemMatterManipulator.ALLOW_REMOVING
            | ItemMatterManipulator.ALLOW_EXCHANGING
            | ItemMatterManipulator.ALLOW_CONFIGURING
            | ItemMatterManipulator.ALLOW_CABLES,
        ImmutableList.of(MMUpgrades.Speed, MMUpgrades.PowerEff),
        MMItemList.MK1),
    Tier2(
        OptionalInt.of(128),
        64,
        5,
        6,
        1_000_000_000L,
        ItemMatterManipulator.ALLOW_GEOMETRY
            | ItemMatterManipulator.CONNECTS_TO_AE
            | ItemMatterManipulator.ALLOW_REMOVING
            | ItemMatterManipulator.ALLOW_EXCHANGING
            | ItemMatterManipulator.ALLOW_CONFIGURING
            | ItemMatterManipulator.ALLOW_CABLES
            | ItemMatterManipulator.ALLOW_COPYING
            | ItemMatterManipulator.ALLOW_MOVING,
        ImmutableList.of(MMUpgrades.Speed, MMUpgrades.PowerEff),
        MMItemList.MK2),
    Tier3(
        OptionalInt.empty(),
        GlobalMMConfig.BuildingConfig.mk3BlocksPerPlace,
        5,
        7,
        10_000_000_000L,
        ItemMatterManipulator.ALLOW_GEOMETRY
            | ItemMatterManipulator.CONNECTS_TO_AE
            | ItemMatterManipulator.ALLOW_REMOVING
            | ItemMatterManipulator.ALLOW_EXCHANGING
            | ItemMatterManipulator.ALLOW_CONFIGURING
            | ItemMatterManipulator.ALLOW_CABLES
            | ItemMatterManipulator.ALLOW_COPYING
            | ItemMatterManipulator.ALLOW_MOVING
            | ItemMatterManipulator.CONNECTS_TO_UPLINK,
        ImmutableList.of(MMUpgrades.PowerEff, MMUpgrades.PowerP2P),
        MMItemList.MK3);
    // spotless:on

    public final int tier = ordinal();
    public final OptionalInt maxRange;
    public final int placeSpeed, placeTicks;
    public final int voltageTier;
    public final long maxCharge;
    public final int capabilities;
    public final Set<MMUpgrades> allowedUpgrades;
    public final MMItemList container;

    ManipulatorTier(OptionalInt maxRange, int placeSpeed, int placeTicks, int voltageTier, long maxCharge, int capabilities,
        List<MMUpgrades> allowedUpgrades, MMItemList container) {
        this.maxRange = maxRange;
        this.placeSpeed = placeSpeed;
        this.placeTicks = placeTicks;
        this.voltageTier = voltageTier;
        this.maxCharge = maxCharge;
        this.capabilities = capabilities;
        this.allowedUpgrades = Collections.unmodifiableSet(new ObjectOpenHashSet<>(allowedUpgrades));
        this.container = container;
    }
}
