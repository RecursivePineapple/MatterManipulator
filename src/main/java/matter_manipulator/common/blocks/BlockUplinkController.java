package matter_manipulator.common.blocks;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

import matter_manipulator.CommonProxy;
import matter_manipulator.Tags;
import matter_manipulator.client.rendering.models.MachineModelRegistry;
import matter_manipulator.common.uplink.TileUplinkController;
import matter_manipulator.common.uplink.UplinkState;
import matter_manipulator.common.utils.enums.ExtendedFacing;
import mcp.MethodsReturnNonnullByDefault;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockUplinkController extends BlockContainer {

    public static final PropertyEnum<UplinkState> STATE = PropertyEnum.create("state", UplinkState.class);

    public BlockUplinkController() {
        super(Material.IRON);

        setTranslationKey("uplink-controller");
        setRegistryName(Tags.MODID, "uplink-controller");

        this.setDefaultState(this.blockState.getBaseState().withProperty(STATE, UplinkState.off));
    }

    @Override
    public @Nullable TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileUplinkController();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
        EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        TileUplinkController tile = (TileUplinkController) worldIn.getTileEntity(pos);
        assert tile != null;

        ItemStack held = playerIn.getHeldItem(hand);

        if (held.getItem() == CommonProxy.HOLOGRAM_PROJECTOR) {
            if (playerIn.isSneaking()) {
                tile.onBuild(playerIn, held);
            } else {
                tile.emitHints(playerIn, held);
            }
        } else {
            tile.openUI(playerIn);
        }

        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        ((TileUplinkController) worldIn.getTileEntity(pos)).setOrientation(ExtendedFacing.of(placer.getHorizontalFacing().getOpposite()));
    }

    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[] { STATE }, new IUnlistedProperty[] { MachineModelRegistry.EXTENDED_FACING });
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
            .withProperty(STATE, UplinkState.values()[meta % 3]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(STATE).ordinal();
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileUplinkController controller = (TileUplinkController) world.getTileEntity(pos);

        return ((IExtendedBlockState) state)
            .withProperty(MachineModelRegistry.EXTENDED_FACING, controller.getOrientation());
    }
}
