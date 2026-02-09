package matter_manipulator.common.structure.impl;

import org.apache.commons.lang3.mutable.MutableBoolean;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import matter_manipulator.common.structure.IStructureDefinition;
import matter_manipulator.common.structure.IStructureElement;
import matter_manipulator.common.structure.StructureContext;
import matter_manipulator.common.structure.StructureWalker;
import matter_manipulator.common.structure.coords.ControllerRelativeCoords;
import matter_manipulator.common.structure.coords.Position;
import matter_manipulator.common.structure.coords.StructureDefinitionCoords;
import matter_manipulator.common.structure.coords.StructureRelativeCoords;
import matter_manipulator.common.structure.coords.WorldCoords;
import matter_manipulator.common.utils.MathUtils;

public class StructureDefinition<T> implements IStructureDefinition<T> {

    final Object2ObjectOpenHashMap<String, StructurePart<T>> parts = new Object2ObjectOpenHashMap<>();

    @Override
    public StructureDefinitionCoords getPartOffset(String part) {
        return parts.get(part).offset;
    }

    @Override
    public IStructureElement<? super T> getStructureElement(String part, Position<StructureDefinitionCoords> pos) {
        return parts.get(part).elements.get(MathUtils.pack(pos.x, pos.y, pos.z));
    }

    @Override
    public Position<StructureRelativeCoords> getMinCorner(String partName) {
        StructurePart<T> part = parts.get(partName);

        return part.offset.translateInverse(new Position<>(0, 0, 0));
    }

    @Override
    public Position<StructureRelativeCoords> getMaxCorner(String partName) {
        StructurePart<T> part = parts.get(partName);

        return part.offset.translateInverse(new Position<>(part.width, part.height, part.depth));
    }

    @Override
    public void iterate(StructureContext<T> context, StructureWalker<T> walker) {
        StructurePart<T> part = parts.get(context.getPartName());

        Position<StructureDefinitionCoords> pos = new Position<>(0, 0, 0);

        var iter = part.elements.long2ObjectEntrySet().fastIterator();

        while (iter.hasNext()) {
            var e = iter.next();

            long key = e.getLongKey();
            IStructureElement<? super T> element = e.getValue();

            pos.x = MathUtils.unpackX(key);
            pos.y = MathUtils.unpackY(key);
            pos.z = MathUtils.unpackZ(key);

            Position<StructureRelativeCoords> pos2 = part.offset.translateInverse(pos);

            if (context.getPartOffset() != null) pos2.offset(context.getPartOffset());

            Position<ControllerRelativeCoords> pos3 = context.getOrientation().asCoordinateSystem().translateInverse(pos2);

            Position<WorldCoords> pos4 = context.getControllerPos().translateInverse(pos3);

            if (!walker.step(context, pos4, element)) break;
        }
    }

    @Override
    public boolean checkElement(StructureContext<T> context, Position<StructureDefinitionCoords> pos) {
        StructurePart<T> part = parts.get(context.getPartName());

        Position<StructureDefinitionCoords> pos2 = pos.copy();

        Position<StructureRelativeCoords> pos3 = part.offset.translateInverse(pos2);

        if (context.getPartOffset() != null) pos3.offset(context.getPartOffset());

        Position<ControllerRelativeCoords> pos4 = context.getOrientation().asCoordinateSystem().translateInverse(pos3);

        Position<WorldCoords> pos5 = context.getControllerPos().translateInverse(pos4);

        return part.elements.get(MathUtils.pack(pos.x, pos.y, pos.z)).check(context, pos5.toBlockPos());
    }

    @Override
    public boolean checkPart(StructureContext<T> context) {
        MutableBoolean success = new MutableBoolean(true);

        iterate(context, ($, pos, element) -> {
            boolean s = element.check(context, pos.toBlockPos());

            if (!s) {
                success.setValue(false);
            }

            return s;
        });

        return success.booleanValue();
    }

    @Override
    public void build(StructureContext<T> context) {
        iterate(context, ($, pos, element) -> {
            if (!context.consumePlaceQuota()) return false;

            element.build(context, pos.toBlockPos());

            return true;
        });
    }

    @Override
    public void emitHints(StructureContext<T> context) {
        iterate(context, ($, pos, element) -> {
            element.emitHint(context, pos.toBlockPos());

            return true;
        });
    }
}
