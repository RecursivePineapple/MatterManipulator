package matter_manipulator.mixin.mixins.minecraft;

import net.minecraft.block.BlockSlab;
import net.minecraft.item.ItemSlab;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemSlab.class)
public interface AccessorItemSlab {

    @Accessor("singleSlab")
    BlockSlab getSingleSlab();

    @Accessor("doubleSlab")
    BlockSlab getDoubleSlab();
}
