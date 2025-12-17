package com.recursive_pineapple.matter_manipulator.common.structure;

import static gregtech.api.util.GTStructureUtility.ofFrame;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.Block;

import gregtech.api.GregTechAPI;
import gregtech.api.casing.ICasing;
import gregtech.api.enums.Materials;

import com.gtnewhorizon.structurelib.structure.IStructureElement;

import org.jetbrains.annotations.NotNull;

public class CasingGTFrames implements ICasing {

    public final Materials material;

    private static final Map<Materials, CasingGTFrames> FRAMES = new ConcurrentHashMap<>();

    private CasingGTFrames(Materials material) {
        this.material = material;
    }

    @Override
    public @NotNull Block getBlock() {
        return GregTechAPI.sBlockFrames;
    }

    @Override
    public int getBlockMeta() {
        return material.mMetaItemSubID;
    }

    @Override
    public <T> IStructureElement<T> asElement(CasingElementContext<T> context) {
        return ofFrame(material);
    }

    @Override
    public boolean isTiered() {
        return false;
    }

    @Override
    public int getTextureId() {
        throw new UnsupportedOperationException("CasingGTFrames does not support getTextureId()");
    }

    public static CasingGTFrames forMaterial(Materials material) {
        return FRAMES.computeIfAbsent(material, CasingGTFrames::new);
    }
}
