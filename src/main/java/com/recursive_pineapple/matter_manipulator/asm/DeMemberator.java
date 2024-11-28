package com.recursive_pineapple.matter_manipulator.asm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.gtnewhorizon.gtnhlib.asm.ClassConstantPoolParser;

import net.minecraft.launchwrapper.IClassTransformer;

/**
 * Removes members
 */
public class DeMemberator implements IClassTransformer {
    
    private final ClassConstantPoolParser parser = new ClassConstantPoolParser("Lcom/recursive_pineapple/matter_manipulator/asm/Optional;");

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {

        if (parser.find(basicClass)) {
            ClassReader reader = new ClassReader(basicClass);
            ClassNode node = new ClassNode(Opcodes.ASM5);
            reader.accept(node, 0);

            Iterator<FieldNode> fields = node.fields.iterator();

            while (fields.hasNext()) {
                FieldNode field = fields.next();
                if (field.invisibleAnnotations != null) {
                    for (AnnotationNode an : field.invisibleAnnotations) {
                        if (shouldBeRemoved(an)) {
                            fields.remove();
                            break;
                        }
                    }
                }
            }

            Iterator<MethodNode> methods = node.methods.iterator();

            outer: while (methods.hasNext()) {
                MethodNode method = methods.next();

                if (method.invisibleAnnotations != null) {
                    for (AnnotationNode an : method.invisibleAnnotations) {
                        if (shouldBeRemoved(an)) {
                            methods.remove();
                            continue outer;
                        }
                    }
                }
            }

            final ClassWriter writer = new ClassWriter(0);
            node.accept(writer);
            return writer.toByteArray();
        } else {
            return basicClass;
        }
    }

    private boolean shouldBeRemoved(AnnotationNode an) {
        if ("Lcom/recursive_pineapple/matter_manipulator/asm/Optional;".equals(an.desc)) {
            if (an.values != null && an.values.size() == 2) {
                @SuppressWarnings("unchecked")
                List<String> mods = (List<String>) an.values.get(1);

                for (String mod : mods) {
                    try {
                        Method method = Class.forName("cpw.mods.fml.common.Loader").getMethod("isModLoaded", String.class);

                        if (!(boolean) method.invoke(null, mod)) {
                            return true;
                        }
                    } catch (NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return false;
    }
}
