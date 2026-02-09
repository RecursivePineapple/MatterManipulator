package matter_manipulator.common.structure.impl;

import it.unimi.dsi.fastutil.chars.Char2CharOpenHashMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import matter_manipulator.common.structure.IStructureDefinition;
import matter_manipulator.common.structure.IStructureElement;
import matter_manipulator.common.structure.coords.Offset;
import matter_manipulator.common.structure.coords.StructureDefinitionCoords;

public class StructureDefinitionBuilder<T> {

    private final Object2ObjectOpenHashMap<String, String[][]> parts = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<String, Offset<StructureDefinitionCoords>> offsets = new Object2ObjectOpenHashMap<>();

    private final Char2ObjectOpenHashMap<IStructureElement<? super T>> elements = new Char2ObjectOpenHashMap<>();

    private final Char2CharOpenHashMap sockets = new Char2CharOpenHashMap();

    public StructureDefinitionBuilder<T> addPart(String name, String[][] shape) {
        parts.put(name, shape);
        return this;
    }

    public StructureDefinitionBuilder<T> setPartOffset(String name, Offset<StructureDefinitionCoords> offset) {
        offsets.put(name, offset);
        return this;
    }

    public StructureDefinitionBuilder<T> addElement(char name, IStructureElement<? super T> element) {
        elements.put(name, element);
        return this;
    }

    /// Denotes `name` as a socket. Sockets are virtual structure elements that record their position in the
    /// structure for future retrieval. They are transformed into real structure elements and do not need their own
    /// element (see `replacement`).
    ///
    /// @param name        The socket name character
    /// @param replacement The replacement character
    /// @see IStructureDefinition#getSocket(String, char)
    /// @see IStructureDefinition#getAllSockets(String, char)
    public StructureDefinitionBuilder<T> addSocket(char name, char replacement) {
        sockets.put(name, replacement);
        return this;
    }

    /// Like [#addSocket(char, char)] but without the replacement. The structure must have a structure element
    /// with the given character.
    ///
    /// @param name The socket name character
    public StructureDefinitionBuilder<T> addSocket(char name) {
        // Adding a name -> name entry like this isn't ideal but this is the cleanest solution
        sockets.put(name, name);
        return this;
    }

    public IStructureDefinition<T> build() {
        StructureDefinition<T> def = new StructureDefinition<>();

        parts.forEach((name, shape) -> {
            def.parts.put(name, new StructurePart<>(name, shape, elements, sockets, offsets.get(name)));
        });

        return def;
    }
}
