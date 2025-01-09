package com.recursive_pineapple.matter_manipulator.common.structure;

import java.util.function.Supplier;

import net.minecraft.block.Block;

import gregtech.api.GregTechAPI;

import gtPlusPlus.core.block.ModBlocks;
import tectech.thing.casing.BlockGTCasingsTT;
import tectech.thing.casing.TTCasingsContainer;

public enum Casings implements ICasing {

    ChemicallyInertMachineCasing(() -> GregTechAPI.sBlockCasings8, 0, 176),
    PTFEPipeCasing(() -> GregTechAPI.sBlockCasings8, 1, 176),
    MiningNeutroniumCasing(() -> GregTechAPI.sBlockCasings8, 2, 176),
    MiningBlackPlutoniumCasing(() -> GregTechAPI.sBlockCasings8, 3, 176),
    ExtremeEngineIntakeCasing(() -> GregTechAPI.sBlockCasings8, 4, 176),
    EuropiumReinforcedRadiationProofMachineCasing(() -> GregTechAPI.sBlockCasings8, 5, 176),
    AdvancedRhodiumPlatedPalladiumMachineCasing(() -> GregTechAPI.sBlockCasings8, 6, 176),
    AdvancedIridiumPlatedMachineCasing(() -> GregTechAPI.sBlockCasings8, 7, 176),
    MagicalMachineCasing(() -> GregTechAPI.sBlockCasings8, 8, 176),
    HSSSTurbineCasing(() -> GregTechAPI.sBlockCasings8, 9, 176),
    RadiantNaquadahAlloyCasing(() -> GregTechAPI.sBlockCasings8, 10, 176),
    BasicPhotolithographicFrameworkCasing(() -> GregTechAPI.sBlockCasings8, 11, 176),
    ReinforcedPhotolithographicFrameworkCasing(() -> GregTechAPI.sBlockCasings8, 12, 176),
    RadiationProofPhotolithographicFrameworkCasing(() -> GregTechAPI.sBlockCasings8, 13, 176),
    InfinityCooledCasing(() -> GregTechAPI.sBlockCasings8, 14, 176),

    CentrifugeCasing(() -> ModBlocks.blockCasingsMisc, 0, 64),
    StructuralCokeOvenCasing(() -> ModBlocks.blockCasingsMisc, 1, 64),
    HeatResistantCokeOvenCasing(() -> ModBlocks.blockCasingsMisc, 2, 64),
    HeatProofCokeOvenCasing(() -> ModBlocks.blockCasingsMisc, 3, 64),
    MaterialPressMachineCasing(() -> ModBlocks.blockCasingsMisc, 4, 64),
    ElectrolyzerCasing(() -> ModBlocks.blockCasingsMisc, 5, 64),
    WireFactoryCasing(() -> ModBlocks.blockCasingsMisc, 6, 64),
    MacerationStackCasing(() -> ModBlocks.blockCasingsMisc, 7, 64),
    MatterGenerationCoil(() -> ModBlocks.blockCasingsMisc, 8, 64),
    MatterFabricatorCasing(() -> ModBlocks.blockCasingsMisc, 9, 64),
    IronPlatedBricks(() -> ModBlocks.blockCasingsMisc, 10, 64),
    MultitankExteriorCasing(() -> ModBlocks.blockCasingsMisc, 11, 64),
    HastelloyNReactorCasing(() -> ModBlocks.blockCasingsMisc, 12, 64),
    Zeron100ReactorShielding(() -> ModBlocks.blockCasingsMisc, 13, 64),
    BlastSmelterHeatContainmentCoil(() -> ModBlocks.blockCasingsMisc, 14, 64),
    BlastSmelterCasingBlock(() -> ModBlocks.blockCasingsMisc, 15, 64),

    FusionMachineCasingMKIV(() -> ModBlocks.blockCasings6Misc, 0, 116),
    AdvancedFusionCoilII(() -> ModBlocks.blockCasings6Misc, 1, 116),

    HighPowerCasing(() -> TTCasingsContainer.sBlockCasingsTT, 0, BlockGTCasingsTT.texturePage),
    ComputerCasing(() -> TTCasingsContainer.sBlockCasingsTT, 1, BlockGTCasingsTT.texturePage),
    ComputerHeatVent(() -> TTCasingsContainer.sBlockCasingsTT, 2, BlockGTCasingsTT.texturePage),
    AdvancedComputerCasing(() -> TTCasingsContainer.sBlockCasingsTT, 3, BlockGTCasingsTT.texturePage),
    MolecularCasing(() -> TTCasingsContainer.sBlockCasingsTT, 4, BlockGTCasingsTT.texturePage),
    AdvancedMolecularCasing(() -> TTCasingsContainer.sBlockCasingsTT, 5, BlockGTCasingsTT.texturePage),
    ContainmentFieldGenerator(() -> TTCasingsContainer.sBlockCasingsTT, 6, BlockGTCasingsTT.texturePage),
    MolecularCoil(() -> TTCasingsContainer.sBlockCasingsTT, 7, BlockGTCasingsTT.texturePage),
    HollowCasing(() -> TTCasingsContainer.sBlockCasingsTT, 8, BlockGTCasingsTT.texturePage),
    SpacetimeAlteringCasing(() -> TTCasingsContainer.sBlockCasingsTT, 9, BlockGTCasingsTT.texturePage),
    TeleportationCasing(() -> TTCasingsContainer.sBlockCasingsTT, 10, BlockGTCasingsTT.texturePage),
    DimensionalBridgeGenerator(() -> TTCasingsContainer.sBlockCasingsTT, 11, BlockGTCasingsTT.texturePage),
    UltimateMolecularCasing(() -> TTCasingsContainer.sBlockCasingsTT, 12, BlockGTCasingsTT.texturePage),
    UltimateAdvancedMolecularCasing(() -> TTCasingsContainer.sBlockCasingsTT, 13, BlockGTCasingsTT.texturePage),
    UltimateContainmentFieldGenerator(() -> TTCasingsContainer.sBlockCasingsTT, 14, BlockGTCasingsTT.texturePage),
    ;

    public final Supplier<Block> blockGetter;
    public final int meta;
    public final int textureOffset;

    private Casings(Supplier<Block> blockGetter, int meta, int textureOffset) {
        this.blockGetter = blockGetter;
        this.meta = meta;
        this.textureOffset = textureOffset;
    }

    @Override
    public Block getBlock() {
        return blockGetter.get();
    }

    @Override
    public int getMeta() {
        return meta;
    }

    @Override
    public int getTextureId() {
        if (textureOffset == -1) {
            throw new UnsupportedOperationException("Casing " + name() + " does not have a casing texture; The result of getTextureId() is undefined.");
        }

        return textureOffset + meta;
    }
}
