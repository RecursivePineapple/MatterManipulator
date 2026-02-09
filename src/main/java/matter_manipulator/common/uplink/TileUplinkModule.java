package matter_manipulator.common.uplink;

import java.util.HashSet;

import net.minecraft.tileentity.TileEntity;

public class TileUplinkModule extends TileEntity {

    public final HashSet<TileUplinkController> controllers = new HashSet<>();

    public void connect(TileUplinkController controller) {
        controllers.add(controller);
    }

    public void disconnect(TileUplinkController controller) {
        controllers.remove(controller);
    }

    public boolean isConnected() {
        return !controllers.isEmpty();
    }
}
