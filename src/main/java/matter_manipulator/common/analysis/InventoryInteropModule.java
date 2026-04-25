package matter_manipulator.common.analysis;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import matter_manipulator.MMMod;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.core.analysis.InventoryAnalysis;
import matter_manipulator.core.analysis.ApplyMode;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.block_spec.IInteropModule;
import matter_manipulator.core.context.BlockAnalysisContext;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.persist.IDataStorage;

public class InventoryInteropModule implements IInteropModule<InventoryAnalysis> {

    @Override
    public Optional<InventoryAnalysis> analyze(BlockAnalysisContext context) {
        TileEntity te = context.getTileEntity();

        if (te == null) return Optional.empty();

        var adapter = MMRegistriesInternal.getInventoryAdapter(te, null);

        if (adapter == null) return Optional.empty();

        return Optional.of(InventoryAnalysis.fromInventory(adapter));
    }

    @Override
    public Set<ApplyResult> apply(BlockPlacingContext context, InventoryAnalysis analysis) {
        TileEntity te = context.getTileEntity();

        if (te == null) {
            return EnumSet.of(ApplyResult.NotApplicable);
        }

        var adapter = MMRegistriesInternal.getInventoryAdapter(te, null);

        if (adapter == null) {
            return EnumSet.of(ApplyResult.NotApplicable);
        }

        return analysis.apply(context, adapter, ApplyMode.NORMAL);
    }

    @Override
    public Set<ApplyResult> getRequiredItemsForExistingBlock(BlockPlacingContext context, InventoryAnalysis analysis) {
        TileEntity te = context.getTileEntity();

        if (te == null) {
            return EnumSet.of(ApplyResult.NotApplicable);
        }

        var adapter = MMRegistriesInternal.getInventoryAdapter(te, null);

        if (adapter == null) {
            return EnumSet.of(ApplyResult.NotApplicable);
        }

        return analysis.apply(context, adapter, ApplyMode.SIMULATE_EXISTING);
    }

    @Override
    public Set<ApplyResult> getRequiredItemsForNewBlock(BlockPlacingContext context, InventoryAnalysis analysis) {
        TileEntity te = context.getTileEntity();

        if (te == null) {
            return EnumSet.of(ApplyResult.NotApplicable);
        }

        var adapter = MMRegistriesInternal.getInventoryAdapter(te, null);

        if (adapter == null) {
            return EnumSet.of(ApplyResult.NotApplicable);
        }

        return analysis.apply(context, adapter, ApplyMode.SIMULATE_EMPTY);
    }

    private static final ResourceLocation LOC = MMMod.loc("inv");

    @Override
    public void save(IDataStorage storage, InventoryAnalysis analysis) {
        storage.getSandbox(LOC).save(analysis);
    }

    @Override
    public Optional<InventoryAnalysis> load(IDataStorage storage) {
        return Optional.ofNullable(storage.getSandbox(LOC).load(InventoryAnalysis.class));
    }
}
