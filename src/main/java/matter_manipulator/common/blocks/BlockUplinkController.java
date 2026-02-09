package matter_manipulator.common.blocks;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import org.jetbrains.annotations.Nullable;

import matter_manipulator.CommonProxy;
import matter_manipulator.Tags;
import matter_manipulator.common.uplink.TileUplinkController;
import matter_manipulator.common.uplink.TileUplinkController.UplinkState;
import matter_manipulator.common.utils.enums.ExtendedFacing;
import mcp.MethodsReturnNonnullByDefault;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockUplinkController extends BlockContainer {

    public static final IUnlistedProperty<EnumFacing> FACING = new IUnlistedProperty<>() {

        @Override
        public String getName() {
            return "facing";
        }

        @Override
        public boolean isValid(EnumFacing value) {
            return value.getYOffset() == 0;
        }

        @Override
        public Class<EnumFacing> getType() {
            return EnumFacing.class;
        }

        @Override
        public String valueToString(EnumFacing value) {
            return value.getName();
        }
    };

    public static final IUnlistedProperty<UplinkState> STATE = new IUnlistedProperty<>() {

        @Override
        public String getName() {
            return "state";
        }

        @Override
        public boolean isValid(UplinkState value) {
            return true;
        }

        @Override
        public Class<UplinkState> getType() {
            return UplinkState.class;
        }

        @Override
        public String valueToString(UplinkState value) {
            return value.name();
        }
    };

    public BlockUplinkController() {
        super(Material.IRON);

        setTranslationKey("uplink-controller");
        setRegistryName(Tags.MODID, "uplink-controller");
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

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[] { FACING, STATE });
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileUplinkController controller = (TileUplinkController) world.getTileEntity(pos);

        return ((IExtendedBlockState) state)
            .withProperty(FACING, controller.getOrientation().getDirection())
            .withProperty(STATE, controller.getState());
    }
}
