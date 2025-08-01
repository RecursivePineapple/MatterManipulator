package com.recursive_pineapple.matter_manipulator.asm;

import static com.recursive_pineapple.matter_manipulator.asm.ASMUtils.*;
import static com.recursive_pineapple.matter_manipulator.asm.ASMUtils.InsnPredicate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.launchwrapper.IClassTransformer;

import com.gtnewhorizon.gtnhlib.asm.ClassConstantPoolParser;

import org.apache.commons.lang3.mutable.MutableObject;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import lombok.SneakyThrows;

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

            List<FieldNode> removedEnumVariants = new ArrayList<>(0);

            while (fields.hasNext()) {
                FieldNode field = fields.next();

                if (field.invisibleAnnotations == null) continue;

                for (AnnotationNode an : field.invisibleAnnotations) {
                    if (!shouldBeRemoved(an)) continue;

                    fields.remove();

                    if ((field.access & Opcodes.ACC_ENUM) != 0) {
                        removedEnumVariants.add(field);
                    }

                    break;
                }
            }

            Iterator<MethodNode> methods = node.methods.iterator();

            while (methods.hasNext()) {
                MethodNode method = methods.next();

                if (method.invisibleAnnotations == null) continue;

                for (AnnotationNode an : method.invisibleAnnotations) {
                    if (!shouldBeRemoved(an)) continue;

                    methods.remove();
                    break;
                }
            }

            if (!removedEnumVariants.isEmpty()) {
                MethodNode clinit = findMethod(node.methods, 0, "<clinit>", null);
                MethodNode $values = findMethod(node.methods, 0, "$values", null);

                for (FieldNode variant : removedEnumVariants) {

                    MutableObject<AbstractInsnNode> afterLDC = new MutableObject<>();

                    // Remove the variant's implicit subclass, if it has one
                    boolean success = findInsns(
                        clinit,
                        new InsnPredicate[] {
                            isNew(null),
                            isBasic(Opcodes.DUP),
                            isLDC(variant.name),
                        },
                        (first, last, contents) -> {
                            TypeInsnNode typeNode = findInsn(clinit, first, isNew(null));
                            assert typeNode != null;
                            String variantClassName = typeNode.desc;

                            if (!variantClassName.equals(node.name)) {
                                node.innerClasses.removeIf(innerClassNode -> {
                                    return innerClassNode.name.equals(variantClassName);
                                });
                            }

                            // Remove the init insns, up to + including the LDC for the variant's name
                            afterLDC.setValue(last);
                            // The end is exclusive, and it points to whatever comes next after the above insn matchers
                            ASMUtils.removeInsns(clinit, first, last);
                        }
                    );

                    // spotless:off
                    if (!success) {
                        throw new IllegalStateException("Could not find NEW insn for enum variant "
                            + variant.name
                            + " in "
                            + name);
                    }
                    // spotless:on

                    // Find the insn after this variant's PUTSTATIC
                    @SuppressWarnings("DataFlowIssue")
                    AbstractInsnNode end = findInsn(
                        clinit,
                        afterLDC.getValue(),
                        isPutStatic(null, null, null)
                    ).getNext();

                    // Remove the remaining insns in the variant's init (post LDC, up to PUTSTATIC)
                    // This should include whatever extra insns are required to call the ctor
                    ASMUtils.removeInsns(
                        clinit,
                        afterLDC.getValue(),
                        end
                    );

                    InsnPredicate lineNumber = isLineNumber(null);
                    InsnPredicate isNew = isNew(null);
                    InsnPredicate dup = isBasic(Opcodes.DUP);
                    InsnPredicate ldc = isLDC(null);

                    // Decrement every following variant's index down one
                    for (; end != null; end = end.getNext()) {
                        AbstractInsnNode cursor = end;

                        if (!lineNumber.test(clinit, cursor)) continue;
                        cursor = cursor.getNext();

                        if (!isNew.test(clinit, cursor)) continue;
                        cursor = cursor.getNext();

                        if (!dup.test(clinit, cursor)) continue;
                        cursor = cursor.getNext();

                        if (!ldc.test(clinit, cursor)) continue;
                        cursor = cursor.getNext();

                        Integer value = getLoadedInt(cursor);

                        if (value != null) {
                            // noinspection DataFlowIssue
                            clinit.instructions.set(cursor, getIntConstInsn(value - 1));
                        }
                    }

                    // Remove the .values() population insns from $values()
                    success = removeInsns(
                        $values,
                        isBasic(Opcodes.DUP),
                        isAny(),
                        isGetStatic(null, null, variant.name),
                        isBasic(Opcodes.AASTORE)
                    );

                    // spotless:off
                    if (!success) {
                        throw new IllegalStateException("Could not remove insns from $values() for enum variant "
                            + variant.name
                            + " in "
                            + name);
                    }
                    // spotless:on
                }

                // Find the array length load insn
                @SuppressWarnings("DataFlowIssue")
                AbstractInsnNode lenNode = findInsn($values, $values.instructions.getFirst(), isLoadInt(null));

                // Set its new length
                // noinspection DataFlowIssue
                $values.instructions.set(lenNode, getIntConstInsn(getLoadedInt(lenNode) - removedEnumVariants.size()));

                // Fix the variant indices in $values()
                InsnPredicate dup = isBasic(Opcodes.DUP);
                int current = 0;

                for (var curr = $values.instructions.getFirst(); curr != null; curr = curr.getNext()) {
                    // Find a DUP followed by a ICONST_X/etc
                    // This method is pretty simple so such a generic predicate is fine
                    if (dup.test($values, curr)) {
                        AbstractInsnNode indexNode = curr.getNext();

                        Integer index = getLoadedInt(indexNode);

                        if (index != null) {
                            if (index != current) {
                                $values.instructions.set(indexNode, getIntConstInsn(current));
                            }

                            current++;
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

    @SneakyThrows
    private boolean shouldBeRemoved(AnnotationNode an) {
        if ("Lcom/recursive_pineapple/matter_manipulator/asm/Optional;".equals(an.desc)) {
            if (an.values != null && an.values.size() == 2) {
                @SuppressWarnings("unchecked")
                List<String> mods = (List<String>) an.values.get(1);

                for (String mod : mods) {
                    Method method = Class.forName("cpw.mods.fml.common.Loader").getMethod("isModLoaded", String.class);

                    if (!(boolean) method.invoke(null, mod)) return true;
                }
            }
        }

        return false;
    }
}
