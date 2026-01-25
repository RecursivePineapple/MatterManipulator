package matter_manipulator.core.persist;

import java.lang.reflect.Type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;

public class DataStorage implements IDataStorage {

    public JsonObject state = new JsonObject();
    public boolean mutable = true;
    public Runnable save;

    public DataStorage() {
    }

    public DataStorage(JsonObject state, boolean mutable) {
        this.state = state;
        this.mutable = mutable;
    }

    @Override
    public @NotNull IStateSandbox getSandbox(String domain, String name) {
        String key = domain + ":" + name;

        return new StateSandbox(key, state.get(key));
    }

    private class StateSandbox implements IStateSandbox {

        public final String key;
        public JsonElement existing;

        public StateSandbox(String key, JsonElement existing) {
            this.key = key;
            this.existing = existing;
        }

        @Override
        public boolean isMutable() {
            return mutable;
        }

        @Override
        public @Nullable JsonElement getValue() {
            return existing;
        }

        @Override
        public void setValue(JsonElement obj) {
            if (!mutable) throw new UnsupportedOperationException("Cannot update non-mutable IStateSandbox: " + key + " was set to " + obj);

            this.existing = obj;

            if (obj != null) {
                state.add(key, obj);
            } else {
                state.remove(key);
            }

            if (save != null) save.run();
        }

        @SneakyThrows
        @Override
        public <T> T load(Type type) {
            if (existing != null) {
                return NBTPersist.GSON.fromJson(existing, type);
            }

            if (type instanceof Class<?> clazz) {
                return (T) clazz.newInstance();
            }

            return null;
        }

        @Override
        public void save(Object state) {
            setValue(NBTPersist.GSON.toJsonTree(state));
        }

        @Override
        public <T> T load(Gson loader, Type type) {
            return existing == null ? null : loader.fromJson(existing, type);
        }

        @Override
        public void save(Gson loader, Object state) {
            setValue(loader.toJsonTree(state));
        }
    }
}
