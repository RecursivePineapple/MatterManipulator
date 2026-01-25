package matter_manipulator.core.util;

public interface CoroutineExecutionContext<T> {

    boolean shouldYield();

    void stop(T value);
}
