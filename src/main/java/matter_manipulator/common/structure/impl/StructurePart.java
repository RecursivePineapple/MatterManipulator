package matter_manipulator.common.structure.impl;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.chars.Char2CharOpenHashMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import matter_manipulator.common.structure.IStructureElement;
import matter_manipulator.common.structure.StructureUtils;
import matter_manipulator.common.structure.coords.Offset;
import matter_manipulator.common.structure.coords.Position;
import matter_manipulator.common.structure.coords.StructureDefinitionCoords;
import matter_manipulator.common.utils.MathUtils;

class StructurePart<T> {

    public final StructureDefinitionCoords offset;
    public final Long2ObjectLinkedOpenHashMap<IStructureElement<? super T>> elements = new Long2ObjectLinkedOpenHashMap<>();
    public final Char2ObjectOpenHashMap<List<Position<StructureDefinitionCoords>>> sockets = new Char2ObjectOpenHashMap<>();

    public final int depth, width, height;

    public StructurePart(String name, String[][] shape, Char2ObjectOpenHashMap<IStructureElement<? super T>> elements, Char2CharOpenHashMap sockets, Offset<StructureDefinitionCoords> offset) {
        int x = 0, y = 0, z = 0;

        StructureDefinitionCoords offsetTemp = offset == null ? null : new StructureDefinitionCoords(offset.x, offset.y, offset.z);

        int width = 0, height = 0;

        // A vertical slice. Each slice increases the Z index by one
        for (String[] slice : shape) {
            height = Math.max(height, y);

            y = 0;

            // A horizontal layer within a slice. Each layer increases the Y index by one.
            for (String layer : slice) {
                width = Math.max(width, x);

                x = 0;

                // Each element within the layer. Each element increases the X index by one.
                for (char c : layer.toCharArray()) {
                    if (sockets.containsKey(c)) {
                        this.sockets.computeIfAbsent(c, ignored -> new ArrayList<>()).add(new Position<>(x, y, z));

                        c = sockets.get(c);
                    }

                    if (c == '~') {
                        if (offsetTemp != null) throw new IllegalStateException("Part has two tildes (controller elements): " + name);

                        offsetTemp = new StructureDefinitionCoords(x, y, z);
                    } else {
                        IStructureElement<? super T> element = getStructureElement(name, c, elements);

                        if (element != null) {
                            this.elements.put(MathUtils.pack(x, y, z), element);
                        }
                    }

                    x++;
                }

                y++;
            }

            z++;
        }

        this.offset = offsetTemp == null ? new StructureDefinitionCoords(0, 0, 0) : offsetTemp;

        this.width = width;
        this.height = height;
        this.depth = z;
    }

    private static <T> IStructureElement<? super T> getStructureElement(String partName, char c, Char2ObjectOpenHashMap<IStructureElement<? super T>> elements) {
        return switch (c) {
            case '~', ' ' -> null;
            case '+' -> StructureUtils.air();
            default -> {
                IStructureElement<? super T> el = elements.get(c);

                if (el == null) {
                    throw new IllegalStateException("Missing element for character '" + c + "' in part " + partName);
                }

                yield el;
            }
        };
    }
}
