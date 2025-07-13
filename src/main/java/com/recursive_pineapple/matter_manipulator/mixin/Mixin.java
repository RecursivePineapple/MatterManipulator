package com.recursive_pineapple.matter_manipulator.mixin;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

import org.jetbrains.annotations.NotNull;

public enum Mixin implements IMixins {

    BlockDropCapturing(
        new MixinBuilder("Expose mechanism to capture non-standard block drops")
            .addCommonMixins("MixinBlockDropCapturing")
            .setPhase(Phase.EARLY)
    ),
    DireAutoCraftDrops(
        new MixinBuilder("Change dire autocrafting table to use getDrops instead of breakBlock")
            .addCommonMixins("MixinBlockExtremeAutoCrafter")
            .addRequiredMod(TargetedMod.AVARITIA_ADDONS)
            .setPhase(Phase.LATE)
    ),
    KeyCancelling(
        new MixinBuilder("Cancel non-mm key presses")
            .addClientMixins("MixinKeyBinding")
            .setPhase(Phase.EARLY)
    );

    private final MixinBuilder builder;

    Mixin(MixinBuilder builder) {
        this.builder = builder;
    }

    @NotNull
    @Override
    public MixinBuilder getBuilder() {
        return this.builder;
    }
}
