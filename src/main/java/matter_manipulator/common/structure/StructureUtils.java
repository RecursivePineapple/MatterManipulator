package matter_manipulator.common.structure;

import java.util.function.Supplier;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

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

}
