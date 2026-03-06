package matter_manipulator.client.rendering.overlays.transform;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class TransformationList extends Transformation {

    private ArrayList<Transformation> transformations = new ArrayList<Transformation>();
    private Matrix4f mat;

    public TransformationList(Transformation... transforms) {
        for (Transformation t : transforms)
            if (t instanceof TransformationList) transformations.addAll(((TransformationList) t).transformations);
            else transformations.add(t);

        compact();
    }

    public MatrixTransformation compile() {
        if (mat == null) {
            mat = new Matrix4f();
            for (int i = transformations.size() - 1; i >= 0; i--) transformations.get(i).apply(mat);
        }
        return new MatrixTransformation(new Matrix4f(mat));
    }

    /**
     * Returns a global space matrix as opposed to an object space matrix (reverse application order)
     *
     * @return
     */
    public Matrix4f reverseCompile() {
        Matrix4f mat = new Matrix4f();
        for (Transformation t : transformations) t.apply(mat);
        return mat;
    }

    @Override
    public void apply(Vector3f vec) {
        if (mat != null) mat.transformPosition(vec);
        else for (int i = 0; i < transformations.size(); i++) transformations.get(i).apply(vec);
    }

    @Override
    public void applyNormal(Vector3f normal) {
        if (mat != null) mat.transformDirection(normal);
        else for (int i = 0; i < transformations.size(); i++) transformations.get(i).applyNormal(normal);
    }

    @Override
    public void apply(Matrix4f mat) {
        mat.mul(compile().mat);
    }

    @Override
    public TransformationList with(Transformation t) {
        if (t.isRedundant()) return this;

        mat = null; // matrix invalid
        if (t instanceof TransformationList) transformations.addAll(((TransformationList) t).transformations);
        else transformations.add(t);

        compact();
        return this;
    }

    public TransformationList prepend(Transformation t) {
        if (t.isRedundant()) return this;

        mat = null; // matrix invalid
        if (t instanceof TransformationList) transformations.addAll(0, ((TransformationList) t).transformations);
        else transformations.add(0, t);

        compact();
        return this;
    }

    private void compact() {
        ArrayList<Transformation> newList = new ArrayList<Transformation>(transformations.size());
        Iterator<Transformation> iterator = transformations.iterator();
        Transformation prev = null;
        while (iterator.hasNext()) {
            Transformation t = iterator.next();
            if (t.isRedundant()) continue;

            if (prev != null) {
                Transformation m = prev.merge(t);
                if (m == null) newList.add(prev);
                else if (m.isRedundant()) t = null;
                else t = m;
            }
            prev = t;
        }
        if (prev != null) newList.add(prev);

        if (newList.size() < transformations.size()) {
            transformations = newList;
            mat = null;
        }

        if (transformations.size() > 3 && mat == null) compile();
    }

    @Override
    public boolean isRedundant() {
        return transformations.isEmpty();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void glApply() {
        for (int i = transformations.size() - 1; i >= 0; i--) transformations.get(i).glApply();
    }

    @Override
    public Transformation inverse() {
        TransformationList rev = new TransformationList();
        for (int i = transformations.size() - 1; i >= 0; i--) rev.with(transformations.get(i).inverse());
        return rev;
    }

    @Override
    public String toString() {
        String s = "";
        for (Transformation t : transformations) s += "\n" + t.toString();
        return s.trim();
    }
}
