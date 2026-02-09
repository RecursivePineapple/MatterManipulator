package matter_manipulator.common.block_spec;

import java.util.EnumSet;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import matter_manipulator.MMMod;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.common.utils.math.Transform;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.block_spec.IBlockSpecLoader;
import matter_manipulator.core.block_spec.ICopyInteropModule;
import matter_manipulator.core.context.BlockAnalysisContext;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.interop.BlockAdapter;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.ItemStackWrapper;

/// An [IBlockSpec] that places a standard block, along with some interop state.
public class StandardBlockSpec implements IBlockSpec {

    public IBlockState state;
    @SuppressWarnings("rawtypes")
    public final Object2ObjectOpenHashMap<ICopyInteropModule, Object> interop = new Object2ObjectOpenHashMap<>(0);

    private boolean hasResource = false;
    private ResourceStack resource;

    public StandardBlockSpec(IBlockState state) {
        this.state = state;
    }

    @Override
    public IBlockSpecLoader getLoader() {
        return StandardBlockSpecLoader.INSTANCE;
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
        if (hasResource) return resource;

        BlockAdapter adapter = MMRegistriesInternal.getBlockAdapter(state);

        if (adapter == null) {
            MMMod.LOG.warn("Could not determine stack form of the following IBlockState because an adapter for this state does not exist: {}", state);
            return new ItemStackWrapper(ItemStack.EMPTY);
        }

        resource = adapter.getResourceForm(state);
        hasResource = true;

        return resource;
    }

    @Override
    public Localized getDisplayName() {
        return getResource().getName();
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

        ResourceStack resource = adapter.getResourceForm(context.getSpec().getBlockState());

        if (!context.resource(resource.getResource()).extract(resource)) return ApplyResult.Retry;

        ApplyResult result = adapter.place(context.getWorld(), context.getPos(), resource);

        switch (result) {
            case DidNothing, NotApplicable, Retry, Error -> {
                // For whatever reason the adapter couldn't place the block, so we have to reinsert it.
                context.resource(resource.getResource()).insert(resource);
            }
            default -> {

            }
        }

        return result;
    }

    @Override
    public EnumSet<ApplyResult> update(BlockPlacingContext context) {
        EnumSet<ApplyResult> result = EnumSet.noneOf(ApplyResult.class);

        for (var e : interop.object2ObjectEntrySet()) {
            //noinspection unchecked
            result.add(e.getKey().apply(context, e.getValue()));
        }

        return result;
    }

    @Override
    public IBlockSpec clone() {
        StandardBlockSpec spec = new StandardBlockSpec(this.state);

        this.interop.forEach((module, analysis) -> {
            //noinspection unchecked
            spec.interop.put(module, module.cloneAnalysis(analysis));
        });

        return spec;
    }

    @Override
    public IBlockSpec clone(IBlockState newState) {
        StandardBlockSpec spec = new StandardBlockSpec(newState);

        this.interop.forEach((module, analysis) -> {
            //noinspection unchecked
            spec.interop.put(module, module.cloneAnalysis(analysis));
        });

        return spec;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof StandardBlockSpec that)) return false;

        return state.equals(that.state) && interop.equals(that.interop);
    }

    @Override
    public int hashCode() {
        int result = state.hashCode();
        result = 31 * result + interop.hashCode();
        return result;
    }

    @NotNull
    public static StandardBlockSpec fromWorld(BlockAnalysisContext context) {
        IBlockState state = context.getBlockState();

        StandardBlockSpec spec = new StandardBlockSpec(state);

        for (ICopyInteropModule<?> interop : MMRegistriesInternal.INTEROP_MODULES.sorted()) {
            var result = interop.analyze(context);

            if (!result.isPresent()) continue;

            spec.interop.put(interop, result.get());
        }

        return spec;
    }
}
