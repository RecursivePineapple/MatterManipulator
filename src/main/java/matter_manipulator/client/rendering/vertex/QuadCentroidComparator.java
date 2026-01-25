package matter_manipulator.client.rendering.vertex;

import java.util.Comparator;

import org.joml.Vector3f;

import matter_manipulator.client.rendering.MMRenderUtils.Quad;

/**
 * Sorts QuadViews according to their distance from the player.
 * It's mostly copied from the vanilla comparator.
 * This can certainly be improved, but it works well enough so I haven't.
 */
public class QuadCentroidComparator implements Comparator<Quad> {

    private final Vector3f origin = new Vector3f();
    private final Vector3f avg1 = new Vector3f();
    private final Vector3f avg2 = new Vector3f();

    public void setOrigin(float x, float y, float z) {
        origin.set(x, y, z);
    }

    @Override
    public int compare(Quad q1, Quad q2) {
        avg1.set(q1.a()).add(q1.b()).add(q1.c()).add(q1.d()).mul(0.25f);
        avg2.set(q1.a()).add(q1.b()).add(q1.c()).add(q1.d()).mul(0.25f);

        return Float.compare(avg2.distanceSquared(origin), avg1.distanceSquared(origin));
    }
}
