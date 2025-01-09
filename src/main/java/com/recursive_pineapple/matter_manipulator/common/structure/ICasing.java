package com.recursive_pineapple.matter_manipulator.common.structure;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.lazy;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static gregtech.api.enums.Textures.BlockIcons.getCasingTextureForId;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import gregtech.api.interfaces.ITexture;

import com.gtnewhorizon.structurelib.structure.IStructureElement;

public interface ICasing {

    public Block getBlock();

    public int getMeta();

    public int getTextureId();

    public default ItemStack toStack(int amount) {
        return new ItemStack(getBlock(), amount, getMeta());
    }

    public default String getLocalizedName() {
        return toStack(1).getDisplayName();
    }

    public default <T> IStructureElement<T> asElement() {
        return lazy(() -> ofBlock(getBlock(), getMeta()));
    }

    public default ITexture getCasingTexture() {
        return getCasingTextureForId(getTextureId());
    }
}
