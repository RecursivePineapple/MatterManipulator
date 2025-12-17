package com.recursive_pineapple.matter_manipulator.asm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ASMUtils {

    @FunctionalInterface
    public static interface InsnPredicate {

        public boolean test(MethodNode method, AbstractInsnNode node);
    }

    public static interface InsnConsumer {

        public void consume(AbstractInsnNode first, AbstractInsnNode last, List<AbstractInsnNode> contents);
    }

    public static boolean findInsns(MethodNode method, InsnPredicate[] matchers, InsnConsumer consumer) {
        if (matchers.length == 0) { return false; }

        AbstractInsnNode current = method.instructions.getFirst();

        ArrayList<AbstractInsnNode> contents = new ArrayList<>();

        boolean foundAnything = false;

        while (current != null) {
            int i = 0;

            AbstractInsnNode cursor = current;

            contents.clear();

            while (cursor != null && i < matchers.length) {
                if (cursor.getOpcode() != -1) {
                    if (matchers[i].test(method, cursor)) {
                        i++;
                    } else {
                        break;
                    }
                }

                contents.add(cursor);
                cursor = cursor.getNext();
            }

            if (i == matchers.length) {
                AbstractInsnNode first = current;
                current = cursor.getNext();

                consumer.consume(first, cursor, contents);

                foundAnything = true;
            } else {
                current = current.getNext();
            }
        }

        return foundAnything;
    }

    public static boolean injectInsns(MethodNode method, InsnPredicate[] matchers, Supplier<AbstractInsnNode[]> toInject) {
        return findInsns(method, matchers, (first, last, nodes) -> {
            AbstractInsnNode[] inject = toInject.get();

            for (var node : inject) {
                method.instructions.insert(last, node);
                last = node;
            }
        });
    }

    public static boolean removeInsns(MethodNode method, InsnPredicate... matchers) {
        return findInsns(method, matchers, (first, last, nodes) -> {
            removeInsns(method, nodes);
        });
    }

    public static void removeInsns(MethodNode method, List<AbstractInsnNode> nodes) {
        for (AbstractInsnNode node : nodes) {
            removeInsn(method, node);
        }
    }

    public static void removeInsns(MethodNode method, AbstractInsnNode start, AbstractInsnNode end) {
        while (start != end) {
            AbstractInsnNode next = start.getNext();

            removeInsn(method, start);

            start = next;
        }
    }

    public static void removeInsn(MethodNode method, AbstractInsnNode node) {
        method.instructions.remove(node);

        if (node instanceof LabelNode label) {
            var iter = method.tryCatchBlocks.iterator();

            while (iter.hasNext()) {
                if (iter.next().start == label) {
                    iter.remove();
                }
            }
        }
    }

    public static MethodNode findMethod(List<MethodNode> methods, int access, String name, String desc) {
        for (MethodNode method : methods) {
            if ((method.access & access) != access) continue;
            if (name != null && !Objects.equals(method.name, name)) continue;
            if (desc != null && !Objects.equals(method.desc, desc)) continue;

            return method;
        }

        return null;
    }

    public static <T extends AbstractInsnNode> T findInsn(MethodNode method, AbstractInsnNode start, InsnPredicate matcher) {
        for (; start != null; start = start.getNext()) {
            if (matcher.test(method, start)) {
                // noinspection unchecked
                return (T) start;
            }
        }

        return null;
    }

    public static InsnPredicate isAny() {
        return (method, node) -> true;
    }

    public static InsnPredicate isGetStatic(@Nullable String owner, @Nullable String desc, @Nullable String name) {
        return (method, insn) -> insn.getOpcode() == Opcodes.GETSTATIC &&
            insn instanceof FieldInsnNode node &&
            (owner == null || owner.equals(node.owner)) &&
            (desc == null || desc.equals(node.desc)) &&
            (name == null || name.equals(node.name));
    }

    public static InsnPredicate isPutStatic(@Nullable String owner, @Nullable String desc, @Nullable String name) {
        return (method, insn) -> insn.getOpcode() == Opcodes.PUTSTATIC &&
            insn instanceof FieldInsnNode node &&
            (owner == null || owner.equals(node.owner)) &&
            (desc == null || desc.equals(node.desc)) &&
            (name == null || name.equals(node.name));
    }

    public static InsnPredicate isGetVar(@Nullable String desc, @Nullable String name) {
        return (method, insn) -> {
            if (insn.getOpcode() == Opcodes.ALOAD && insn instanceof VarInsnNode node) {
                LocalVariableNode var = getVariableNode(method.localVariables, node.var);

                return var != null && (desc == null || desc.equals(var.desc)) && (name == null || name.equals(var.name));
            } else {
                return false;
            }
        };
    }

    public static InsnPredicate isStoreVar(@Nullable String desc, @Nullable String name) {
        return (method, insn) -> {
            if (insn.getOpcode() == Opcodes.ASTORE && insn instanceof VarInsnNode node) {
                LocalVariableNode var = getVariableNode(method.localVariables, node.var);

                return var != null && (desc == null || desc.equals(var.desc)) && (name == null || name.equals(var.name));
            } else {
                return false;
            }
        };
    }

    public static InsnPredicate isInvokeStatic(@Nullable String owner, @Nullable String desc, @Nullable String name) {
        return (method, insn) -> insn.getOpcode() == Opcodes.INVOKESTATIC &&
            insn instanceof MethodInsnNode node &&
            (owner == null || owner.equals(node.owner)) &&
            (desc == null || desc.equals(node.desc)) &&
            (name == null || name.equals(node.name)) &&
            node.itf == false;
    }

    public static InsnPredicate isInvokeInterface(@Nullable String owner, @Nullable String desc, @Nullable String name) {
        return (method, insn) -> insn.getOpcode() == Opcodes.INVOKEINTERFACE &&
            insn instanceof MethodInsnNode node &&
            (owner == null || owner.equals(node.owner)) &&
            (desc == null || desc.equals(node.desc)) &&
            (name == null || name.equals(node.name)) &&
            node.itf == true;
    }

    public static InsnPredicate isInvokeSpecial(@Nullable String owner, @Nullable String desc, @Nullable String name) {
        return (method, insn) -> insn.getOpcode() == Opcodes.INVOKESPECIAL &&
            insn instanceof MethodInsnNode node &&
            (owner == null || owner.equals(node.owner)) &&
            (desc == null || desc.equals(node.desc)) &&
            (name == null || name.equals(node.name));
    }

    public static InsnPredicate isInvoke(@Nullable String owner, @Nullable String desc, @Nullable String name) {
        return (method, insn) -> insn instanceof MethodInsnNode node &&
            (owner == null || owner.equals(node.owner)) &&
            (desc == null || desc.equals(node.desc)) &&
            (name == null || name.equals(node.name));
    }

    public static InsnPredicate isNew(@Nullable String desc) {
        return (method, insn) -> insn.getOpcode() == Opcodes.NEW &&
            insn instanceof TypeInsnNode node &&
            (desc == null || desc.equals(node.desc));
    }

    public static InsnPredicate isInit(@Nullable String owner, @Nullable String desc) {
        return (method, insn) -> insn.getOpcode() == Opcodes.INVOKESPECIAL &&
            insn instanceof MethodInsnNode node &&
            (owner == null || owner.equals(node.owner)) &&
            node.name.equals("<init>") &&
            (desc == null || desc.equals(node.desc)) &&
            !node.itf;
    }

    public static InsnPredicate isIf(LabelNode label) {
        return (method, insn) -> insn.getOpcode() == Opcodes.IFEQ &&
            insn instanceof JumpInsnNode node &&
            (label == null || label.equals(node.label));
    }

    public static InsnPredicate isBasic(int opcode) {
        return (method, insn) -> insn.getOpcode() == opcode;
    }

    public static InsnPredicate isLDC(@org.jetbrains.annotations.Nullable Object cst) {
        return (method, insn) -> insn.getOpcode() == Opcodes.LDC &&
            insn instanceof LdcInsnNode node &&
            (cst == null || cst.equals(node.cst));
    }

    public static InsnPredicate isLoadInt(Integer value) {
        return (method, node) -> getLoadedInt(node) != null;
    }

    public static InsnPredicate isLineNumber(@org.jetbrains.annotations.Nullable Integer line) {
        return (method, node) -> node instanceof LineNumberNode lnn &&
            (line == null || line == lnn.line);
    }

    public static Integer getLoadedInt(AbstractInsnNode insn) {
        if (insn instanceof InsnNode basic) {
            return switch (basic.getOpcode()) {
                case Opcodes.ICONST_0 -> 0;
                case Opcodes.ICONST_1 -> 1;
                case Opcodes.ICONST_2 -> 2;
                case Opcodes.ICONST_3 -> 3;
                case Opcodes.ICONST_4 -> 4;
                case Opcodes.ICONST_5 -> 5;
                default -> null;
            };
        }

        if (insn instanceof IntInsnNode intNode && intNode.getOpcode() == Opcodes.BIPUSH) { return intNode.operand; }

        return null;
    }

    public static AbstractInsnNode getIntConstInsn(int value) {
        return switch (value) {
            case 0 -> new InsnNode(Opcodes.ICONST_0);
            case 1 -> new InsnNode(Opcodes.ICONST_1);
            case 2 -> new InsnNode(Opcodes.ICONST_2);
            case 3 -> new InsnNode(Opcodes.ICONST_3);
            case 4 -> new InsnNode(Opcodes.ICONST_4);
            case 5 -> new InsnNode(Opcodes.ICONST_5);
            default -> new LdcInsnNode(value);
        };
    }

    public static @Nullable LocalVariableNode getVariableNode(@Nullable List<LocalVariableNode> vars, int index) {
        if (vars == null) { return null; }

        for (var var : vars) {
            if (var.index == index) { return var; }
        }

        return null;
    }

    public static void debug(LocalVariableNode var) {
        System.out.format(
            "LocalVariableNode{name=%s, desc=%s, signature=%s, index=%s}\n",
            var.name,
            var.desc,
            var.signature,
            var.index
        );
    }

    public static void debug(AbstractInsnNode insn, @Nullable List<LocalVariableNode> vars) {
        if (insn instanceof MethodInsnNode method) {
            System.out.format(
                "MethodInsnNode{opcode=%s, owner=%s, name=%s, desc=%s, itf=%b}\n",
                getOpcodeName(method.getOpcode()),
                method.owner,
                method.name,
                method.desc,
                method.itf
            );
        } else if (insn instanceof FieldInsnNode field) {
            System.out.format(
                "FieldInsnNode{opcode=%s, owner=%s, name=%s, desc=%s}\n",
                getOpcodeName(field.getOpcode()),
                field.owner,
                field.name,
                field.desc
            );
        } else if (insn instanceof TypeInsnNode type) {
            System.out.format(
                "TypeInsnNode{opcode=%s, desc=%s}\n",
                getOpcodeName(type.getOpcode()),
                type.desc
            );
        } else if (insn instanceof LdcInsnNode ldc) {
            System.out.format(
                "LdcInsnNode{opcode=%s, cst=%s}\n",
                getOpcodeName(ldc.getOpcode()),
                ldc.cst
            );
        } else if (insn instanceof VarInsnNode var) {
            if (vars == null) {
                System.out.format(
                    "VarInsnNode{opcode=%s, var index=%s, var name=%s}\n",
                    getOpcodeName(var.getOpcode()),
                    var.var
                );
            } else {
                LocalVariableNode vnode = getVariableNode(vars, var.var);
                if (vnode == null) {
                    System.out.format(
                        "VarInsnNode{opcode=%s, var index=%s, name=unknown}\n",
                        getOpcodeName(var.getOpcode()),
                        var.var
                    );
                } else {
                    System.out.format(
                        "VarInsnNode{opcode=%s, var index=%s, name=%s, desc=%s, signature=%s}\n",
                        getOpcodeName(var.getOpcode()),
                        var.var,
                        vnode.name,
                        vnode.desc,
                        vnode.signature
                    );
                }
            }
        } else if (insn instanceof InsnNode insnNode) {
            System.out.format(
                "InsnNode{opcode=%s}\n",
                getOpcodeName(insnNode.getOpcode())
            );
        } else if (insn instanceof LabelNode label) {
            System.out.format(
                "LabelNode{label=%s}\n",
                label.getLabel()
            );
        } else if (insn instanceof LineNumberNode ln) {
            System.out.format(
                "LineNumberNode{start=%s, line=%d}\n",
                ln.start.getLabel(),
                ln.line
            );
        } else if (insn instanceof FrameNode frame) {
            List<String> locals = new ArrayList<>();

            for (int i = 0; i < (frame.local == null ? 0 : frame.local.size()); i++) {
                String varName = vars == null ? "[unknown]" : i >= vars.size() ? "[illegal var index]" : vars.get(i).name;

                Object local = frame.local.get(i);

                if (local instanceof Integer primitive) {
                    locals.add(varName + ": " + switch (primitive.intValue()) {
                        case 0 -> "ITEM_TOP";
                        case 1 -> "ITEM_INTEGER";
                        case 2 -> "ITEM_FLOAT";
                        case 3 -> "ITEM_DOUBLE";
                        case 4 -> "ITEM_LONG";
                        case 5 -> "ITEM_NULL";
                        case 6 -> "ITEM_UNINITIALIZED_THIS";
                        case 7 -> "ITEM_OBJECT";
                        case 8 -> "ITEM_UNINITIALIZED";
                        case 9 -> "ITEM_ASM_BOOLEAN";
                        case 10 -> "ITEM_ASM_BYTE";
                        case 11 -> "ITEM_ASM_CHAR";
                        case 12 -> "ITEM_ASM_SHORT";
                        default -> "[ILLEGAL TYPE]";
                    });
                } else if (local instanceof String ref) {
                    locals.add(varName + ": " + ref);
                } else if (local instanceof LabelNode uninit) {
                    locals.add(varName + ": uninit until label " + uninit.getLabel());
                } else {
                    locals.add(varName + ": " + local);
                }
            }

            List<String> stack = new ArrayList<>();

            for (int i = 0; i < (frame.stack == null ? 0 : frame.stack.size()); i++) {
                Object stackValue = frame.stack.get(i);

                if (stackValue instanceof Integer primitive) {
                    locals.add(i + ": " + switch (primitive.intValue()) {
                        case 0 -> "ITEM_TOP";
                        case 1 -> "ITEM_INTEGER";
                        case 2 -> "ITEM_FLOAT";
                        case 3 -> "ITEM_DOUBLE";
                        case 4 -> "ITEM_LONG";
                        case 5 -> "ITEM_NULL";
                        case 6 -> "ITEM_UNINITIALIZED_THIS";
                        case 7 -> "ITEM_OBJECT";
                        case 8 -> "ITEM_UNINITIALIZED";
                        case 9 -> "ITEM_ASM_BOOLEAN";
                        case 10 -> "ITEM_ASM_BYTE";
                        case 11 -> "ITEM_ASM_CHAR";
                        case 12 -> "ITEM_ASM_SHORT";
                        default -> "[ILLEGAL TYPE]";
                    });
                } else if (stackValue instanceof String ref) {
                    locals.add(i + ": " + ref);
                } else if (stackValue instanceof LabelNode uninit) {
                    locals.add(i + ": uninit until label w/ offset " + uninit.getLabel().getOffset());
                } else {
                    locals.add(i + ": " + stackValue);
                }
            }

            System.out.format(
                "FrameNode{type=%s, locals={%s}, stack={%s}}\n",
                switch (frame.type) {
                    case -1 -> "F_NEW";
                    case 0 -> "F_FULL";
                    case 1 -> "F_APPEND";
                    case 2 -> "F_CHOP";
                    case 3 -> "F_SAME";
                    case 4 -> "F_SAME1";
                    default -> "[INVALID]";
                },
                String.join(", ", locals),
                String.join(", ", stack)
            );
        } else if (insn instanceof JumpInsnNode jump) {
            System.out.format(
                "JumpInsnNode{opcode=%s, label=%s}\n",
                getOpcodeName(insn.getOpcode()),
                jump.label
            );
        } else {
            var insnName = switch (insn.getType()) {
                case 0 -> "INSN";
                case 1 -> "INT_INSN";
                case 2 -> "VAR_INSN";
                case 3 -> "TYPE_INSN";
                case 4 -> "FIELD_INSN";
                case 5 -> "METHOD_INSN";
                case 6 -> "INVOKE_DYNAMIC_INSN";
                case 7 -> "JUMP_INSN";
                case 9 -> "LDC_INSN";
                case 10 -> "IINC_INSN";
                case 11 -> "TABLESWITCH_INSN";
                case 12 -> "LOOKUPSWITCH_INSN";
                case 13 -> "MULTIANEWARRAY_INSN";
                case 14 -> "FRAME";
                case 15 -> "LINE";
                default -> "AbstractInsnNode";
            };
            System.out.format("%s{opcode=%s, ...}\n", insnName, getOpcodeName(insn.getOpcode()));
        }
    }

    public static void debug(String prefix, InsnList insns, @Nullable List<LocalVariableNode> vars) {
        for (AbstractInsnNode insn : insns.toArray()) {
            System.out.print(prefix);
            debug(insn, vars);
        }
    }

    public static String getOpcodeName(int opcode) {
        return switch (opcode) {
            case 0 -> "NOP";
            case 1 -> "ACONST_NULL";
            case 2 -> "ICONST_M1";
            case 3 -> "ICONST_0";
            case 4 -> "ICONST_1";
            case 5 -> "ICONST_2";
            case 6 -> "ICONST_3";
            case 7 -> "ICONST_4";
            case 8 -> "ICONST_5";
            case 9 -> "LCONST_0";
            case 10 -> "LCONST_1";
            case 11 -> "FCONST_0";
            case 12 -> "FCONST_1";
            case 13 -> "FCONST_2";
            case 14 -> "DCONST_0";
            case 15 -> "DCONST_1";
            case 16 -> "BIPUSH";
            case 17 -> "SIPUSH";
            case 18 -> "LDC";
            case 21 -> "ILOAD";
            case 22 -> "LLOAD";
            case 23 -> "FLOAD";
            case 24 -> "DLOAD";
            case 25 -> "ALOAD";
            case 46 -> "IALOAD";
            case 47 -> "LALOAD";
            case 48 -> "FALOAD";
            case 49 -> "DALOAD";
            case 50 -> "AALOAD";
            case 51 -> "BALOAD";
            case 52 -> "CALOAD";
            case 53 -> "SALOAD";
            case 54 -> "ISTORE";
            case 55 -> "LSTORE";
            case 56 -> "FSTORE";
            case 57 -> "DSTORE";
            case 58 -> "ASTORE";
            case 79 -> "IASTORE";
            case 80 -> "LASTORE";
            case 81 -> "FASTORE";
            case 82 -> "DASTORE";
            case 83 -> "AASTORE";
            case 84 -> "BASTORE";
            case 85 -> "CASTORE";
            case 86 -> "SASTORE";
            case 87 -> "POP";
            case 88 -> "POP2";
            case 89 -> "DUP";
            case 90 -> "DUP_X1";
            case 91 -> "DUP_X2";
            case 92 -> "DUP2";
            case 93 -> "DUP2_X1";
            case 94 -> "DUP2_X2";
            case 95 -> "SWAP";
            case 96 -> "IADD";
            case 97 -> "LADD";
            case 98 -> "FADD";
            case 99 -> "DADD";
            case 100 -> "ISUB";
            case 101 -> "LSUB";
            case 102 -> "FSUB";
            case 103 -> "DSUB";
            case 104 -> "IMUL";
            case 105 -> "LMUL";
            case 106 -> "FMUL";
            case 107 -> "DMUL";
            case 108 -> "IDIV";
            case 109 -> "LDIV";
            case 110 -> "FDIV";
            case 111 -> "DDIV";
            case 112 -> "IREM";
            case 113 -> "LREM";
            case 114 -> "FREM";
            case 115 -> "DREM";
            case 116 -> "INEG";
            case 117 -> "LNEG";
            case 118 -> "FNEG";
            case 119 -> "DNEG";
            case 120 -> "ISHL";
            case 121 -> "LSHL";
            case 122 -> "ISHR";
            case 123 -> "LSHR";
            case 124 -> "IUSHR";
            case 125 -> "LUSHR";
            case 126 -> "IAND";
            case 127 -> "LAND";
            case 128 -> "IOR";
            case 129 -> "LOR";
            case 130 -> "IXOR";
            case 131 -> "LXOR";
            case 132 -> "IINC";
            case 133 -> "I2L";
            case 134 -> "I2F";
            case 135 -> "I2D";
            case 136 -> "L2I";
            case 137 -> "L2F";
            case 138 -> "L2D";
            case 139 -> "F2I";
            case 140 -> "F2L";
            case 141 -> "F2D";
            case 142 -> "D2I";
            case 143 -> "D2L";
            case 144 -> "D2F";
            case 145 -> "I2B";
            case 146 -> "I2C";
            case 147 -> "I2S";
            case 148 -> "LCMP";
            case 149 -> "FCMPL";
            case 150 -> "FCMPG";
            case 151 -> "DCMPL";
            case 152 -> "DCMPG";
            case 153 -> "IFEQ";
            case 154 -> "IFNE";
            case 155 -> "IFLT";
            case 156 -> "IFGE";
            case 157 -> "IFGT";
            case 158 -> "IFLE";
            case 159 -> "IF_ICMPEQ";
            case 160 -> "IF_ICMPNE";
            case 161 -> "IF_ICMPLT";
            case 162 -> "IF_ICMPGE";
            case 163 -> "IF_ICMPGT";
            case 164 -> "IF_ICMPLE";
            case 165 -> "IF_ACMPEQ";
            case 166 -> "IF_ACMPNE";
            case 167 -> "GOTO";
            case 168 -> "JSR";
            case 169 -> "RET";
            case 170 -> "TABLESWITCH";
            case 171 -> "LOOKUPSWITCH";
            case 172 -> "IRETURN";
            case 173 -> "LRETURN";
            case 174 -> "FRETURN";
            case 175 -> "DRETURN";
            case 176 -> "ARETURN";
            case 177 -> "RETURN";
            case 178 -> "GETSTATIC";
            case 179 -> "PUTSTATIC";
            case 180 -> "GETFIELD";
            case 181 -> "PUTFIELD";
            case 182 -> "INVOKEVIRTUAL";
            case 183 -> "INVOKESPECIAL";
            case 184 -> "INVOKESTATIC";
            case 185 -> "INVOKEINTERFACE";
            case 186 -> "INVOKEDYNAMIC";
            case 187 -> "NEW";
            case 188 -> "NEWARRAY";
            case 189 -> "ANEWARRAY";
            case 190 -> "ARRAYLENGTH";
            case 191 -> "ATHROW";
            case 192 -> "CHECKCAST";
            case 193 -> "INSTANCEOF";
            case 194 -> "MONITORENTER";
            case 195 -> "MONITOREXIT";
            case 197 -> "MULTIANEWARRAY";
            case 198 -> "IFNULL";
            case 199 -> "IFNONNULL";
            default -> Integer.toString(opcode);
        };
    }
}
