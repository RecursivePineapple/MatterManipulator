package com.recursive_pineapple.matter_manipulator.common.utils;

import java.util.Locale;

import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.common.Loader;

import com.gtnewhorizon.gtnhlib.util.data.IMod;

public enum Mods implements IMod {

    AE2Stuff(Names.AE2STUFF),
    Angelica(Names.ANGELICA),
    AppliedEnergistics2(Names.APPLIED_ENERGISTICS2),
    ArchitectureCraft(Names.ARCHITECTURE_CRAFT),
    BloodMagic(Names.BLOOD_MAGIC),
    CarpentersBlocks(Names.CARPENTERS_BLOCKS),
    EnderIO(Names.ENDER_I_O),
    EnderStorage(Names.ENDER_STORAGE),
    /** Creates the actual block parts from blocks. */
    ForgeMicroblocks(Names.FORGE_MICROBLOCKS),
    /** The forge multipart library. */
    ForgeMultipart(Names.FORGE_MULTIPART),
    FloodLights(Names.FLOOD_LIGHTS),
    GalacticraftCore(Names.GALACTICRAFT_CORE),
    GalaxySpace(Names.GALAXY_SPACE),
    GregTech(Names.GREG_TECH),
    GTPlusPlus(Names.G_T_PLUS_PLUS),
    GTNHIntergalactic(Names.GTNH_INTERGALACTIC),
    GraviSuite(Names.GRAVI_SUITE),
    Hodgepodge(Names.HODGEPODGE),
    IndustrialCraft2(Names.INDUSTRIAL_CRAFT2),
    LogisticsPipes(Names.LOGISTICS_PIPES),
    MatterManipulator(Names.MATTER_MANIPULATOR),
    Minecraft(Names.MINECRAFT) {

        @Override
        public boolean isModLoaded() {
            return true;
        }
    },
    NewHorizonsCoreMod(Names.NEW_HORIZONS_CORE_MOD),
    NotEnoughItems(Names.NOT_ENOUGH_ITEMS),
    StorageDrawers(Names.STORAGE_DRAWERS),
    Thaumcraft(Names.THAUMCRAFT),

    ;

    public static class Names {

        public static final String AE2STUFF = "ae2stuff";
        public static final String ANGELICA = "angelica";
        public static final String APPLIED_ENERGISTICS2 = "appliedenergistics2";
        public static final String ARCHITECTURE_CRAFT = "ArchitectureCraft";
        public static final String BLOOD_MAGIC = "AWWayofTime";
        public static final String CARPENTERS_BLOCKS = "CarpentersBlocks";
        public static final String ENDER_I_O = "EnderIO";
        public static final String ENDER_STORAGE = "EnderStorage";
        public static final String FORGE_MICROBLOCKS = "ForgeMicroblock";
        public static final String FORGE_MULTIPART = "ForgeMultipart";
        public static final String FLOOD_LIGHTS = "FloodLights";
        public static final String GALACTICRAFT_CORE = "GalacticraftCore";
        public static final String GALAXY_SPACE = "GalaxySpace";
        public static final String GREG_TECH = "gregtech";
        public static final String GRAVI_SUITE = "GraviSuite";
        public static final String G_T_PLUS_PLUS = "miscutils";
        public static final String GTNH_INTERGALACTIC = "gtnhintergalactic";
        public static final String HODGEPODGE = "hodgepodge";
        public static final String INDUSTRIAL_CRAFT2 = "IC2";
        public static final String LOGISTICS_PIPES = "LogisticsPipes";
        public static final String MATTER_MANIPULATOR = "matter-manipulator";
        public static final String MINECRAFT = "minecraft";
        public static final String NEW_HORIZONS_CORE_MOD = "dreamcraft";
        public static final String NOT_ENOUGH_ITEMS = "NotEnoughItems";
        public static final String STORAGE_DRAWERS = "StorageDrawers";
        public static final String THAUMCRAFT = "Thaumcraft";
    }

    public final String ID;
    public final String resourceDomain;
    private boolean checkedMod, modLoaded;

    Mods(String ID) {
        this.ID = ID;
        this.resourceDomain = ID.toLowerCase(Locale.ENGLISH);
    }

    @Override
    public String getID() {
        return ID;
    }

    public boolean isModLoaded() {
        if (!checkedMod) {
            this.modLoaded = Loader.isModLoaded(ID);
            checkedMod = true;
        }
        return this.modLoaded;
    }

    @Override
    public String getResourceLocation() {
        return resourceDomain;
    }

    public String getResourcePath(String... path) {
        return this.getResourceLocation(path)
            .toString();
    }

    public ResourceLocation getResourceLocation(String... path) {
        return new ResourceLocation(this.resourceDomain, String.join("/", path));
    }
}
