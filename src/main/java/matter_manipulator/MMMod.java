package matter_manipulator;


import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = MMMod.MODID, version = Tags.VERSION, name = "Matter Manipulator", acceptedMinecraftVersions = "[1.7.10]")
public class MMMod {

    public static final String MODID = "matter-manipulator";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(
        clientSide = "matter_manipulator.ClientProxy",
        serverSide = "matter_manipulator.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

    @EventHandler
    public void missingMapping(FMLMissingMappingsEvent event) {
        for (MissingMapping mapping : event.getAll()) {
            if (mapping.type != Type.ITEM) continue;
            if (mapping.name.equals("gregtech:gt.metaitem.04")) mapping.ignore();
            if (mapping.name.equals("gregtech:itemMatterManipulator0")) mapping.ignore();
            if (mapping.name.equals("gregtech:itemMatterManipulator1")) mapping.ignore();
            if (mapping.name.equals("gregtech:itemMatterManipulator2")) mapping.ignore();
            if (mapping.name.equals("gregtech:itemMatterManipulator3")) mapping.ignore();
        }
    }
}
