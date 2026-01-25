package matter_manipulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import zone.rong.mixinbooter.IEarlyMixinLoader;

@IFMLLoadingPlugin.MCVersion("1.12.2")
public class MMModCore implements IFMLLoadingPlugin, IEarlyMixinLoader {

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
            "matter_manipulator.asm.DeMemberator"
        };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public List<String> getMixinConfigs() {
        List<String> configs = new ArrayList<>();

        configs.add("mixins.matter-manipulator.minecraft.json");
        configs.add("mixins.matter-manipulator.forge.json");

        return configs;
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        return IEarlyMixinLoader.super.shouldMixinConfigQueue(mixinConfig);
    }
}
