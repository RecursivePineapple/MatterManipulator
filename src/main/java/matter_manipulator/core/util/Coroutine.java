package matter_manipulator.core.util;

import java.util.function.Function;
import java.util.function.Supplier;

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

    default <T2> Coroutine<T2> then(Function<T, Coroutine<T2>> next) {
        class ProxyContext implements CoroutineExecutionContext<T> {

            public CoroutineExecutionContext<?> base;
            public boolean stopped = false;
            public T value = null;

            public Coroutine<T> first;
            public Coroutine<T2> second;

            @Override
            public boolean shouldYield() {
                return base.shouldYield();
            }

            @Override
            public void stop(T value) {
                stopped = true;
                this.value = value;
            }
        }

        ProxyContext proxy = new ProxyContext();

        proxy.first = this;

        return ctx -> {
            proxy.base = ctx;

            if (!proxy.stopped) {
                proxy.first.run(proxy);
            }

            if (proxy.stopped) {
                if (proxy.second == null) {
                    proxy.second = next.apply(proxy.value);
                }

                proxy.second.run(ctx);
            }
        };
    }

    static <T> Coroutine<T> finished(T value) {
        return ctx -> ctx.stop(value);
    }

    static <T> Coroutine<T> finishedDeferred(Supplier<T> value) {
        return ctx -> ctx.stop(value.get());
    }
}
