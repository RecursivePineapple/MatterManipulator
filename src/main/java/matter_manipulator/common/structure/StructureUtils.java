package matter_manipulator.common.structure;

import java.util.function.Supplier;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import matter_manipulator.common.utils.data.Lazy;
import matter_manipulator.core.meta.MetaKey;

public class StructureUtils {

    @SuppressWarnings("Convert2MethodRef")
    private static final IStructureElement<?> AIR = block(() -> Blocks.AIR.getDefaultState());

    public interface BlockStateSupplier extends Supplier<IBlockState> {

    }

    public static <T> IStructureElement<T> block(BlockStateSupplier supplier) {
        return lazy(() -> new BlockStateStructureElement<>(supplier.get()));
    }

    public static <T> IStructureElement<T> lazy(Supplier<IStructureElement<T>> next) {
        Lazy<IStructureElement<T>> element = new Lazy<>(next);

        return new IStructureElement<>() {

            @Override
            public <K> K getMetadata(MetaKey<K> key) {
                return element.get().getMetadata(key);
            }

            @Override
            public boolean check(StructureContext<? extends T> context, BlockPos pos) {
                return element.get().check(context, pos);
            }

            @Override
            public boolean build(StructureContext<? extends T> context, BlockPos pos) {
                return element.get().build(context, pos);
            }

            @Override
            public void emitHint(StructureContext<? extends T> context, BlockPos pos) {
                element.get().emitHint(context, pos);
            }
        };
    }

    public static <T> IStructureElement<T> air() {
        //noinspection unchecked
        return (IStructureElement<T>) AIR;
    }

    public static boolean wrench(EntityPlayer player, World world, BlockPos pos) {
        TileEntity tTileEntity = world.getTileEntity(pos);

        if (tTileEntity == null || player instanceof FakePlayer) {
            return player instanceof EntityPlayerMP;
        }

        if (player instanceof EntityPlayerMP && tTileEntity instanceof IAlignmentProvider) {
            IAlignment alignment = ((IAlignmentProvider) tTileEntity).getAlignment();

            if (alignment != null) {
                if (player.isSneaking()) {
                    alignment.toolSetFlip(null);
                } else {
                    alignment.toolSetRotation(null);
                }

                return true;
            }
        }
        return false;
    }

    /**
     * This Function determines the direction a Block gets when being Wrenched. returns -1 if invalid. Even though that
     * could never happen. Normalizes values into the range [0.0f, 1.0f].
     */
    public static EnumFacing determineWrenchingSide(EnumFacing side, float aX, float aY, float aZ) {
        float modX = (aX % 1.0f + 1.0f) % 1.0f;
        float modY = (aY % 1.0f + 1.0f) % 1.0f;
        float modZ = (aZ % 1.0f + 1.0f) % 1.0f;
        EnumFacing tBack = side.getOpposite();
        // The = here is necessary; Since the hitVec only has a precision of 1/16th on MP and gets rounded down,
        // a value of 0.8 would be 0.75 on MP, which is not > 0.75, returning false.
        switch (side) {
            case DOWN, UP -> {
                if (modX < 0.25) {
                    if (modZ < 0.25) return tBack;
                    if (modZ >= 0.75) return tBack;
                    return EnumFacing.WEST;
                }
                if (modX >= 0.75) {
                    if (modZ < 0.25) return tBack;
                    if (modZ >= 0.75) return tBack;
                    return EnumFacing.EAST;
                }
                if (modZ < 0.25) return EnumFacing.NORTH;
                if (modZ >= 0.75) return EnumFacing.SOUTH;
            }
            case NORTH, SOUTH -> {
                if (modX < 0.25) {
                    if (modY < 0.25) return tBack;
                    if (modY >= 0.75) return tBack;
                    return EnumFacing.WEST;
                }
                if (modX >= 0.75) {
                    if (modY < 0.25) return tBack;
                    if (modY >= 0.75) return tBack;
                    return EnumFacing.EAST;
                }
                if (modY < 0.25) return EnumFacing.DOWN;
                if (modY >= 0.75) return EnumFacing.UP;
            }
            case WEST, EAST -> {
                if (modZ < 0.25) {
                    if (modY < 0.25) return tBack;
                    if (modY >= 0.75) return tBack;
                    return EnumFacing.NORTH;
                }
                if (modZ >= 0.75) {
                    if (modY < 0.25) return tBack;
                    if (modY >= 0.75) return tBack;
                    return EnumFacing.SOUTH;
                }
                if (modY < 0.25) return EnumFacing.DOWN;
                if (modY >= 0.75) return EnumFacing.UP;
            }
        }

        return side;
    }
}
