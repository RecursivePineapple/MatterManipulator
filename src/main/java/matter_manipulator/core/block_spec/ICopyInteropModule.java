package matter_manipulator.core.block_spec;

import java.util.List;
import java.util.Optional;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Contract;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import matter_manipulator.common.utils.math.Transform;
import matter_manipulator.core.context.BlockAnalysisContext;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.interop.MMRegistries;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.persist.IStateSandbox;

/// A module that allows a matter manipulator to interact with a block or tile entity. This may do anything so long as
/// it only affects the target coordinates. This object should not contain any state - the analysis result
/// (see [AnalysisResult]) should contain all required information for this module's operation.
/// <p />
/// This module is used as follows:
/// <br />
/// 1. A manipulator starts analyzing a block in the world.
/// <br />
/// 2. It loops over all registered [ICopyInteropModule] objects and calls [#analyze(BlockAnalysisContext)].
/// <br />
/// 3. The analysis results are stored in a `Map<ICopyInteropModule<State>, State>` within a [IBlockSpec].
/// <p />
/// From here, the [IBlockSpec] can either be saved, applied to a block, or discarded entirely.
/// <p />
/// When saving, this is the data flow:
/// <br />
/// 1. An [IBlockSpec] loops over its stored analysis results and calls [#save(IDataStorage, Object)].
/// <br />
/// 2. Each interop module retrieves a sandboxed state storage via one of the [IDataStorage#getSandbox(String, String)]
/// overloads and writes its data to a [JsonObject], which is persisted by calling [IStateSandbox#setValue(JsonElement)].
/// [IStateSandbox#save(Object)] compacts the two operations into one call if the exact format doesn't matter.
/// <br />
/// 3. The [IBlockSpec] saves its own state into a [JsonObject], which is converted to a [NBTTagCompound].
/// <p />
/// When loading, this is the data flow:
/// <br />
/// 1. An [IBlockSpec] is loaded from a [NBTTagCompound]. In the process, the state sandbox is loaded and the
/// [IBlockSpec] loops over all analysis results (which are identified by their name, see [MMRegistries#interop()]) and
/// loads their results via [#load(IDataStorage)].
/// <br />
/// 2. Each [ICopyInteropModule] loads its sandbox and deserializes its state into its object, which is stored in a
/// `Map<ICopyInteropModule<State>, State>` within the [IBlockSpec].
/// <br />
/// 3. As before, the [IBlockSpec] can either be saved again, applied to a block, or discarded entirely.
///
/// @param <AnalysisResult> The analysis result.
///
/// @see IBlockSpec
/// @see IStateSandbox
/// @see IDataStorage
/// @see MMRegistries#interop()
public interface ICopyInteropModule<AnalysisResult> {

    /// Analyzes a block in the world and returns this interop module's analysis result. Returns [Optional#empty()] if
    /// this module cannot analyze or affect the requested block.
    /// @see BlockAnalysisContext#getPos()
    @Contract(pure = true)
    Optional<AnalysisResult> analyze(BlockAnalysisContext context);

    /// Applies an analysis result that was previously retrieved from [#analyze(BlockAnalysisContext)] or
    /// [#load(IDataStorage)].
    ApplyResult apply(BlockPlacingContext context, AnalysisResult analysis);

    /// Gets the items required to update an existing block. Items should be extracted from the context, but not placed
    /// in the world.
    ApplyResult getRequiredItemsForExistingBlock(BlockPlacingContext context, AnalysisResult analysis);
    /// Gets the items required to create a block from scratch. This should ignore any existing blocks at the location.
    /// Items should be extracted from the context, but not placed in the world.
    ApplyResult getRequiredItemsForNewBlock(BlockPlacingContext context, AnalysisResult analysis);

    @Contract(mutates = "param1")
    void save(IDataStorage storage, AnalysisResult analysis);
    Optional<AnalysisResult> load(IDataStorage storage);

    @Contract(mutates = "param1")
    default void modifyResource(Object resource, AnalysisResult analysis) {

    }

    @Contract(mutates = "param1")
    default void getDetails(List<String> text, AnalysisResult analysis) {

    }

    @Contract(mutates = "param1")
    default void transform(AnalysisResult analysis, Transform transform) {

    }

}
