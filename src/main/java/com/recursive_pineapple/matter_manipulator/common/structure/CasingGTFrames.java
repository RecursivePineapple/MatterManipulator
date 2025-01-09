package com.recursive_pineapple.matter_manipulator.common.structure;

import static gregtech.api.util.GTStructureUtility.ofFrame;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.Block;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.Materials;

import com.gtnewhorizon.structurelib.structure.IStructureElement;

public class CasingGTFrames implements ICasing {

    public final Materials material;

    private static final Map<Materials, CasingGTFrames> FRAMES = new ConcurrentHashMap<>();

    private CasingGTFrames(Materials material) {
        this.material = material;
    }

    @Override
    public Block getBlock() {
        return GregTechAPI.sBlockFrames;
    }

    @Override
    public int getMeta() {
        return material.mMetaItemSubID;
    }

    @Override
    public <T> IStructureElement<T> asElement() {
        return ofFrame(material);
    }

    @Override
    public int getTextureId() {
        throw new UnsupportedOperationException("CasingGTFrames does not support getTextureId()");
    }

    public static CasingGTFrames forMaterial(Materials material) {
        return FRAMES.computeIfAbsent(material, CasingGTFrames::new);
    }
}
