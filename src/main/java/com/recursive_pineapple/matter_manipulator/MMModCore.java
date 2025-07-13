package com.recursive_pineapple.matter_manipulator;

import java.util.List;
import java.util.Map;
import java.util.Set;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.recursive_pineapple.matter_manipulator.mixin.Mixin;

@IFMLLoadingPlugin.MCVersion("1.7.10")
public class MMModCore implements IFMLLoadingPlugin, IEarlyMixinLoader {

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
            "com.recursive_pineapple.matter_manipulator.asm.DeMemberator"
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
    public String getMixinConfig() {
        return "mixins.matter-manipulator.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        return IMixins.getEarlyMixins(Mixin.class, loadedCoreMods);
    }
}
