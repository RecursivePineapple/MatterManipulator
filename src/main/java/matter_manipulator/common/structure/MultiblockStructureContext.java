package matter_manipulator.common.structure;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import matter_manipulator.client.rendering.MMHintRenderer;
import matter_manipulator.common.structure.coords.ControllerRelativeCoords;
import matter_manipulator.common.structure.coords.Offset;
import matter_manipulator.common.structure.coords.StructureRelativeCoords;
import matter_manipulator.common.utils.enums.ExtendedFacing;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.color.ImmutableColor;

public class MultiblockStructureContext<T extends TileEntity & MultiblockController<T>> implements StructureContext<T> {

    public T controller;
    public String part = "main";
    public Offset<StructureRelativeCoords> partOffset;

    public EntityPlayer player;
    public ItemStack trigger;
    public int placeQuota = 0;

    public MultiblockStructureContext(T controller) {
        this.controller = controller;
    }

    public MultiblockStructureContext(T controller, EntityPlayer player, ItemStack trigger, int placeQuota) {
        this.controller = controller;
        this.player = player;
        this.trigger = trigger;
        this.placeQuota = placeQuota;
    }

    @Override
    public T getData() {
        return controller;
    }

    @Override
    public IStructureDefinition<? super T> getStructureDefinition() {
        return controller.getDefinition();
    }

    @Override
    public World getWorld() {
        return controller.getWorld();
    }

    @Override
    public ExtendedFacing getOrientation() {
        return controller.getOrientation();
    }

    @Override
    public ControllerRelativeCoords getControllerPos() {
        return controller.getControllerPos();
    }

    @Override
    public String getPartName() {
        return part;
    }

    @Override
    public Offset<StructureRelativeCoords> getPartOffset() {
        return partOffset;
    }

    @Override
    public @Nullable EntityPlayer getPlayer() {
        return player;
    }

    @Override
    public @Nullable ItemStack getTrigger() {
        return trigger;
    }

    @Override
    public void emitHint(BlockPos pos, IBlockSpec spec, ImmutableColor tint) {
        MMHintRenderer.INSTANCE.addHint(pos.getX(), pos.getY(), pos.getZ(), spec, tint);
    }

    @Override
    public boolean consumePlaceQuota() {
        if (placeQuota <= 0) return false;

        placeQuota--;

        return true;
    }
}
