package matter_manipulator.client.rendering.vertex;

import java.util.Comparator;

import org.joml.Vector3f;

import matter_manipulator.client.rendering.MMRenderUtils.Quad;

public class QuadProjectionComparator implements Comparator<Quad> {

    private final Vector3f pov = new Vector3f();

    private final Plane plane = new Plane();

    public void setOrigin(float x, float y, float z) {
        pov.set(x, y, z);
    }

    @Override
    public int compare(Quad q1, Quad q2) {
        plane.set(q1.a(), q1.b(), q1.c());

        float t1 = plane.intersect(pov, q2.a());
        float t2 = plane.intersect(pov, q2.b());
        float t3 = plane.intersect(pov, q2.c());
        float t4 = plane.intersect(pov, q2.d());

        return Float.compare(0, t1) + Float.compare(0, t2) + Float.compare(0, t3) + Float.compare(0, t4);
    }

    private static class Plane {
        private final Vector3f origin = new Vector3f();
        private final Vector3f normal = new Vector3f();

        private final Vector3f temp1 = new Vector3f();
        private final Vector3f temp2 = new Vector3f();

        public void set(Vector3f a, Vector3f b, Vector3f c) {
            origin.set(a);
            temp1.set(b).sub(a);
            temp2.set(c).sub(a);
            normal.set(temp1).cross(temp2);
        }

        /// The result of this method is only useful for comparisons since the normal isn't normalized.
        public float intersect(Vector3f a, Vector3f b) {
            temp1.set(b).sub(a);

            float denom = normal.dot(temp1);

            if (denom <= 0.0001) return Float.POSITIVE_INFINITY;

            return temp1.set(origin).sub(a).dot(normal) / denom;
        }
    }
}
