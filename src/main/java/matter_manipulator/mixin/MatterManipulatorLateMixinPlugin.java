package matter_manipulator.mixin;

import java.util.ArrayList;
import java.util.List;

import zone.rong.mixinbooter.ILateMixinLoader;

public class MatterManipulatorLateMixinPlugin implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        List<String> configs = new ArrayList<>();

//        configs.add("mixins.gregtech.theoneprobe.json");
//        configs.add("mixins.gregtech.jei.json");
//        configs.add("mixins.gregtech.ctm.json");
//        configs.add("mixins.gregtech.ccl.json");
//        configs.add("mixins.gregtech.littletiles.json");
//        configs.add("mixins.gregtech.vintagium.json");
//        configs.add("mixins.gregtech.mui2.json");
//        configs.add("mixins.gregtech.nothirium.json");

        return configs;
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        return switch (mixinConfig) {
            default -> true;
        };
    }
}
