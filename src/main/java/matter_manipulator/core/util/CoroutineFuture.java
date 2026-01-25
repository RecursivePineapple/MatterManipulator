package matter_manipulator.core.util;

import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface CoroutineFuture<V> extends Future<V> {

    void setCallback(Consumer<V> callback);

}
