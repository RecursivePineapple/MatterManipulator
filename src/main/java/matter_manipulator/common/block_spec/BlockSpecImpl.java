package matter_manipulator.common.block_spec;

import java.util.EnumSet;
import java.util.Optional;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import matter_manipulator.MMMod;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.common.utils.math.Transform;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.block_spec.ICopyInteropModule;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.interop.interfaces.BlockAdapter;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.ItemStackWrapper;

public class BlockSpecImpl implements IBlockSpec {

    private IBlockState state;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<ResourceStack> resource;
    @SuppressWarnings("rawtypes")
    private Object2ObjectOpenHashMap<ICopyInteropModule, Object> interop = new Object2ObjectOpenHashMap<>(0);

    public BlockSpecImpl(IBlockState state) {
        this.state = state;
    }

    @Override
    public boolean isValid() {
        return MMRegistriesInternal.getBlockAdapter(state) != null;
    }

    @Override
    public IBlockState getBlockState() {
        return state;
    }

    @Override
    public ResourceStack getResource() {
        //noinspection OptionalAssignedToNull
        if (resource != null) return resource.orElse(null);

        BlockAdapter adapter = MMRegistriesInternal.getBlockAdapter(state);

        if (adapter == null) {
            MMMod.LOG.warn("Could not determine stack form of the following IBlockState because an adapter for this state does not exist: {}", state);
            return new ItemStackWrapper(ItemStack.EMPTY);
        }

        resource = Optional.ofNullable(adapter.getResourceForm(state));

        return resource.orElse(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void transform(Transform transform) {
        for (IProperty<?> prop : state.getPropertyKeys()) {
            if (prop.getName().equals("facing") && prop.getValueClass() == EnumFacing.class) {
                EnumFacing facing = state.getValue((IProperty<EnumFacing>) prop);

                transform.apply(facing);

                state = state.withProperty((IProperty<EnumFacing>) prop, facing);
            }
        }
    }

    @Override
    public ApplyResult place(BlockPlacingContext context) {
        BlockAdapter adapter = MMRegistriesInternal.getBlockAdapter(state);

        if (adapter == null) return ApplyResult.Error;

        IBlockState prev = context.getWorld().getBlockState(context.getPos());

        adapter.place(context.getWorld(), context.getPos(), state);

        return context.getWorld().getBlockState(context.getPos()) != prev ? ApplyResult.DidSomething : ApplyResult.DidNothing;
    }

    @Override
    public EnumSet<ApplyResult> update(BlockPlacingContext context) {
        EnumSet<ApplyResult> result = EnumSet.noneOf(ApplyResult.class);

        for (var e : interop.object2ObjectEntrySet()) {
            result.add(e.getKey().apply(context, e.getValue()));
        }

        return result;
    }

    @Override
    public IBlockSpec clone() {
        return new BlockSpecImpl(this.state);
    }

    public static BlockSpecImpl fromWorld(World world, BlockPos pos) {
        throw new UnsupportedOperationException("TODO");
    }
}
