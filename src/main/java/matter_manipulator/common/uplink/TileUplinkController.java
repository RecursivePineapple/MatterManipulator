package matter_manipulator.common.uplink;

import java.time.Duration;
import java.util.HashSet;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import lombok.Setter;
import matter_manipulator.client.rendering.MMHintRenderer;
import matter_manipulator.common.structure.IAlignment;
import matter_manipulator.common.structure.IAlignmentLimits;
import matter_manipulator.common.structure.IStructureDefinition;
import matter_manipulator.common.structure.MultiblockController;
import matter_manipulator.common.structure.MultiblockStructureContext;
import matter_manipulator.common.structure.StructureOverlord;
import matter_manipulator.common.structure.StructureUtils;
import matter_manipulator.common.structure.coords.ControllerRelativeCoords;
import matter_manipulator.common.utils.enums.ExtendedFacing;
import matter_manipulator.common.utils.math.VoxelAABB;

public class TileUplinkController extends TileEntity implements MultiblockController<TileUplinkController>, ITickable,
    IAlignment {

    private static final String[][] SHAPE = {
        {
            "         ",
            "         ",
            "         ",
            "         ",
            "  AA~AA  ",
            "         ",
            "         ",
            "         ",
            "         "
        }, {
            "         ",
            "         ",
            "  A   A  ",
            " AA   AA ",
            " AA   AA ",
            " AA   AA ",
            "  A   A  ",
            "         ",
            "         "
        }, {
            "         ",
            "  A   A  ",
            " ACCCCCA ",
            " AD   DA ",
            "A D   D A",
            " AD   DA ",
            " ACCCCCA ",
            "  A   A  ",
            "         "
        }, {
            "         ",
            " AA   AA ",
            " AD   DA ",
            "A       A",
            "A       A",
            "A       A",
            " AD   DA ",
            " AA   AA ",
            "         "
        }, {
            "  A   A  ",
            " AA   AA ",
            "A D   D A",
            "A       A",
            "ABBE EBBA",
            "A       A",
            "A D   D A",
            " AA   AA ",
            "  A   A  "
        }, {
            "         ",
            " AA   AA ",
            " AD   DA ",
            "A       A",
            "A       A",
            "A       A",
            " AD   DA ",
            " AA   AA ",
            "         "
        }, {
            "         ",
            "  A   A  ",
            " ACCCCCA ",
            " AD   DA ",
            "A D   D A",
            " AD   DA ",
            " ACCCCCA ",
            "  A   A  ",
            "         "
        }, {
            "         ",
            "         ",
            "  A   A  ",
            " AA   AA ",
            " AA   AA ",
            " AA   AA ",
            "  A   A  ",
            "         ",
            "         "
        }, {
            "         ",
            "         ",
            "         ",
            "         ",
            "  A   A  ",
            "         ",
            "         ",
            "         ",
            "         "
        }
    };

    private static final IStructureDefinition<TileUplinkController> STRUCTURE = IStructureDefinition.<TileUplinkController>builder()
        .addPart("main", SHAPE)
        .addElement('A', StructureUtils.block(() -> Blocks.STONE.getDefaultState()))
        .addElement('B', StructureUtils.block(() -> Blocks.STONE.getDefaultState()))
        .addElement('C', StructureUtils.block(() -> Blocks.STONE.getDefaultState()))
        .addElement('D', StructureUtils.block(() -> Blocks.STONE.getDefaultState()))
        .addElement('E', StructureUtils.block(() -> Blocks.STONE.getDefaultState()))
        .build();

    @Setter
    private ExtendedFacing orientation = ExtendedFacing.DEFAULT;
    private ControllerRelativeCoords controllerCoords;
    private VoxelAABB aabb;

    private boolean structureDirty = true, formed = false;
    private final HashSet<TileUplinkModule> modules = new HashSet<>();

    @Override
    public void onMachineUpdate(BlockPos pos) {
        if (aabb.contains(pos.getX(), pos.getY(), pos.getZ())) {
            structureDirty = true;
        }
    }

    @Override
    public IStructureDefinition<? super TileUplinkController> getDefinition() {
        return STRUCTURE;
    }

    @Override
    public ExtendedFacing getOrientation() {
        return orientation;
    }

    @Override
    public ControllerRelativeCoords getControllerPos() {
        return controllerCoords;
    }

    @Override
    public void validate() {
        super.validate();
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if (aabb != null && !this.world.isRemote) {
            StructureOverlord.get((WorldServer) this.world).removeController(this);
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public void updateBoundingBox() {
        if (aabb != null && !this.world.isRemote) {
            StructureOverlord.get((WorldServer) this.world).removeController(this);
        }

        controllerCoords = new ControllerRelativeCoords(this.pos);

        var min = STRUCTURE.getMinCorner("main");
        var min2 = orientation.asCoordinateSystem().translateInverse(min);
        var min3 = controllerCoords.translateInverse(min2);

        var max = STRUCTURE.getMaxCorner("main");
        var max2 = orientation.asCoordinateSystem().translateInverse(max);
        var max3 = controllerCoords.translateInverse(max2);

        this.aabb = new VoxelAABB(min3.toVector3i(), max3.toVector3i());

        this.aabb.origin.set(this.pos.getX(), this.pos.getY(), this.pos.getZ());

        if (!this.world.isRemote) {
            StructureOverlord.get((WorldServer) this.world).addController(this);
        }
    }

    @Override
    public VoxelAABB getBoundingBox() {
        if (this.aabb == null) updateBoundingBox();

        return aabb;
    }

    @Override
    public void setPos(BlockPos posIn) {
        super.setPos(posIn);

        updateBoundingBox();
    }

    @Override
    public int getBlockX() {
        return pos.getX();
    }

    @Override
    public int getBlockY() {
        return pos.getY();
    }

    @Override
    public int getBlockZ() {
        return pos.getZ();
    }

    @Override
    public void update() {
        if (this.aabb == null) updateBoundingBox();

        if (this.structureDirty) {
            this.structureDirty = false;

            for (var module : modules) {
                module.disconnect(this);
            }

            modules.clear();

            MultiblockStructureContext<TileUplinkController> context = new MultiblockStructureContext<>(this);
            context.part = "main";
            formed = STRUCTURE.checkPart(context);

            if (formed) {
                for (var module : modules) {
                    module.connect(this);
                }
            }
        }
    }

    public void onBuild(EntityPlayer player, ItemStack trigger) {
        MultiblockStructureContext<TileUplinkController> context = new MultiblockStructureContext<>(this, player, trigger, 10);

        STRUCTURE.build(context);
    }

    public void emitHints(EntityPlayer player, ItemStack trigger) {
        MultiblockStructureContext<TileUplinkController> context = new MultiblockStructureContext<>(this, player, trigger, 0);

        MMHintRenderer.INSTANCE.start();
        MMHintRenderer.INSTANCE.setExpiry(Duration.ofSeconds(30));
        MMHintRenderer.INSTANCE.setDepthTest(false);

        STRUCTURE.emitHints(context);

        MMHintRenderer.INSTANCE.finish();
    }

    public void openUI(EntityPlayer player) {

    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);

        readFromNBT(pkt.getNbtCompound());

        IBlockState state = this.world.getBlockState(this.pos);

        this.world.notifyBlockUpdate(this.pos, state, state, 3);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setString("facing", this.orientation.name());

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        this.orientation = ExtendedFacing.valueOf(compound.getString("facing"));
    }

    @Override
    public ExtendedFacing getExtendedFacing() {
        return this.orientation;
    }

    @Override
    public void setExtendedFacing(ExtendedFacing alignment) {
        this.orientation = alignment;

        updateBoundingBox();

        IBlockState state = this.world.getBlockState(this.pos);

        this.world.notifyBlockUpdate(this.pos, state, state, 3);
    }

    @Override
    public IAlignmentLimits getAlignmentLimits() {
        return (dir, rot, flip) -> !flip.isVerticallyFlipped();
    }
}
