package com.recursive_pineapple.matter_manipulator.mixin.mixins.late;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import wanion.avaritiaddons.block.extremeautocrafter.BlockExtremeAutoCrafter;
import wanion.avaritiaddons.block.extremeautocrafter.TileEntityExtremeAutoCrafter;

@Mixin(BlockExtremeAutoCrafter.class)
public abstract class MixinBlockExtremeAutoCrafter extends BlockContainer {
    
    @Unique
    protected ThreadLocal<TileEntityExtremeAutoCrafter> tile = new ThreadLocal<>();

    protected MixinBlockExtremeAutoCrafter(Material p_i45386_1_) {
        super(p_i45386_1_);
    }

    /**
     * @author Recursive Pineapple
     * @reason Because this is shrimple
     */
    @Overwrite
    public final void breakBlock(final World world, final int x, final int y, final int z, final Block block, final int metadata) {
        final TileEntityExtremeAutoCrafter tileEntityExtremeAutoCrafter = (TileEntityExtremeAutoCrafter) world.getTileEntity(x, y, z);

        // never unset because we can't guarantee when getDrops will be called (sorry GC :tootroll:)
        tile.set(tileEntityExtremeAutoCrafter);

        super.breakBlock(world, x, y, z, block, metadata);
        world.func_147453_f(x, y, z, block);
    }

    /**
     * @author Recursive Pineapple
     * @reason Because it doesn't make sense to inject into this
     */
    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        final ItemStack droppedStack = new ItemStack(this, 1, 0);

        final TileEntityExtremeAutoCrafter tile = world.getTileEntity(x, y, z) instanceof TileEntityExtremeAutoCrafter t ? t : this.tile.get();

        if (tile != null && tile.xCoord == x && tile.yCoord == y && tile.zCoord == z) {
            droppedStack.setTagCompound(tile.writeCustomNBT(new NBTTagCompound()));
            this.tile.set(null);
        }

        return new ArrayList<>(Arrays.asList(droppedStack));
    }
}
