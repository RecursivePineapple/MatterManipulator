package com.recursive_pineapple.matter_manipulator.mixin;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;

import org.jetbrains.annotations.NotNull;

public enum Mixin implements IMixins {

    BlockDropCapturing(
        new MixinBuilder("Expose mechanism to capture non-standard block drops")
            .addCommonMixins("MixinBlockDropCapturing", "MixinWorldDropCapturing")
            .setPhase(Phase.EARLY)
    ),
    DireAutoCraftDrops(
        new MixinBuilder("Change dire autocrafting table to use getDrops instead of breakBlock")
            .addCommonMixins("MixinBlockExtremeAutoCrafter")
            .addRequiredMod(Mods.AvaritiaAddons)
            .setPhase(Phase.LATE)
    ),
    KeyCancelling(
        new MixinBuilder("Cancel non-mm key presses")
            .addClientMixins("MixinKeyBinding")
            .setPhase(Phase.EARLY)
    ),
    FrameBoxTECreation(
        new MixinBuilder("Expose BlockFrameBox.spawnFrameEntity")
            .addCommonMixins("MixinBlockFrameBox")
            .addRequiredMod(Mods.GregTech)
            .setPhase(Phase.LATE)
    ),
    LinkedInputBusAccessors(
        new MixinBuilder("Expose various internals of MTELinkedInputBus")
            .addCommonMixins("MixinMTELinkedInputBus", "MixinSharedInventory")
            .addRequiredMod(Mods.GregTech)
            .setPhase(Phase.LATE)
    ),
    //
    ;

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
