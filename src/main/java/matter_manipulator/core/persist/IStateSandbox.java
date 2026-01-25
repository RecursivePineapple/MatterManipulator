package matter_manipulator.core.persist;

import java.lang.reflect.Type;

import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public interface IStateSandbox {

    boolean isMutable();

    @Nullable
    JsonElement getValue();
    void setValue(@Nullable JsonElement obj);

    @Nullable
    <T> T load(Type type);
    void save(@Nullable Object state);

    @Nullable
    <T> T load(Gson loader, Type type);
    void save(Gson loader, @Nullable Object state);
}
