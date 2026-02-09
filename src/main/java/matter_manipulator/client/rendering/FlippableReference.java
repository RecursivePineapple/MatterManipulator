package matter_manipulator.client.rendering;

public class FlippableReference<T> {

    private boolean usingA = true;
    private final T a, b;

    public FlippableReference(T a, T b) {
        this.a = a;
        this.b = b;
    }

    public synchronized T flip() {
        if (usingA) {
            usingA = false;
            return b;
        } else {
            usingA = true;
            return a;
        }
    }

    public synchronized T active() {
        return usingA ? a : b;
    }

    public synchronized T passive() {
        return usingA ? b : a;
    }
}
