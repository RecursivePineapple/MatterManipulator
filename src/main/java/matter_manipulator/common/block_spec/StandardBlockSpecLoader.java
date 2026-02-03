package matter_manipulator.common.block_spec;

import java.util.Optional;

import net.minecraft.block.state.IBlockState;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.block_spec.IBlockSpecLoader;
import matter_manipulator.core.block_spec.ICopyInteropModule;
import matter_manipulator.core.persist.DataStorage;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.persist.NBTPersist;

public class StandardBlockSpecLoader implements IBlockSpecLoader {

    public static final StandardBlockSpecLoader INSTANCE = new StandardBlockSpecLoader();

    private StandardBlockSpecLoader() { }

    @Override
    public String getKey() {
        return "block";
    }

    @Override
    public StandardBlockSpec load(JsonElement element) {
        if (!(element instanceof JsonObject obj)) return null;

        IBlockState state = NBTPersist.GSON.fromJson(obj.get("state"), IBlockState.class);

        StandardBlockSpec spec = new StandardBlockSpec(state);

        if (obj.has("interop")) {
            DataStorage storage = NBTPersist.GSON.fromJson(obj.get("interop"), DataStorage.class);

            //noinspection rawtypes
            for (ICopyInteropModule interop : MMRegistriesInternal.INTEROP_MODULES.sorted()) {
                @SuppressWarnings("rawtypes")
                Optional result = interop.load(storage);

                if (result.isPresent()) {
                    spec.interop.put(interop, result.get());
                }
            }
        }

        return spec;
    }

    @Override
    public JsonElement save(IBlockSpec spec2) {
        StandardBlockSpec spec = (StandardBlockSpec) spec2;

        JsonObject obj = new JsonObject();

        obj.add("state", NBTPersist.GSON.toJsonTree(spec.state, IBlockState.class));

        DataStorage storage = new DataStorage();

        for (var e : spec.interop.object2ObjectEntrySet()) {
            //noinspection unchecked
            e.getKey().save(storage, e.getValue());
        }

        if (storage.state.size() > 0) {
            obj.add("interop", NBTPersist.GSON.toJsonTree(storage, IDataStorage.class));
        }

        return obj;
    }
}
