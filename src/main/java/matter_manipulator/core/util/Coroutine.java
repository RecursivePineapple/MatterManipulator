package matter_manipulator.core.util;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;

/// Coroutines are very similar to futures, except they differ in that they always run on either the client or server MC
/// thread instead of within a thread pool. These have full control over the client or server and can perform any
/// operations on them.
@FunctionalInterface
public interface Coroutine<T> {

    /// This is called repeatedly each tick until [CoroutineExecutionContext#stop(Object)] is called, or the method
    /// throws an error. The implementation should periodically call [CoroutineExecutionContext#shouldYield()]. When
    /// this method returns true, the implementation should return from this method to yield execution back to another
    /// task or the client/server thread.
    void run(CoroutineExecutionContext<T> ctx);

    /// Runs this coroutine to completion and returns the value, if any. Does not capture errors. Blocks the current
    /// thread until the coroutine returns.
    default T get() {
        MutableObject<T> outer$value = new MutableObject<>();
        MutableBoolean outer$finished = new MutableBoolean(false);

        CoroutineExecutionContext<T> context = new CoroutineExecutionContext<>() {

            @Override
            public boolean shouldYield() {
                return false;
            }

            @Override
            public void stop(T value) {
                outer$value.setValue(value);
                outer$finished.setValue(true);
            }
        };

        while (!outer$finished.booleanValue()) {
            this.run(context);
        }

        return outer$value.getValue();
    }
}
