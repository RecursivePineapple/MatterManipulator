package matter_manipulator;

import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

import com.recursive_pineapple.matter_manipulator.common.entities.EntityItemLarge;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMKeyInputs;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMRenderer;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        EntityItemLarge.registerClient();
        MMRenderer.init();
        MMKeyInputs.init();
    }

    @Override
    public EntityPlayer getThePlayer() {
        return FMLClientHandler.instance().getClientPlayerEntity();
    }
}
