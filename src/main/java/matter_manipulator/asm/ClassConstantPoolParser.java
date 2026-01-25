package matter_manipulator.asm;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Using this class to search for a (single) String reference is > 40 times faster than parsing a class with a
 * ClassReader + ClassNode while using way less RAM
 */
// This class might be loaded by different class loaders,
// it should not reference any code from the main mod.
// See {@link com.gtnewhorizon.gtnhlib.core.shared.package-info}
public class ClassConstantPoolParser {

    enum ConstantTypes {

        INVALID, // 0 unused, if that ever changes shift this to the next unused index
        UTF8,
        // 2 unused
        INT,
        FLOAT,
        LONG,
        DOUBLE,
        CLASS_REF,
        STR_REF,
        FIELD,
        METH_REF,
        IMETH_REF,
        NAME_TYPE,
        // 13,14 unused
        METH_HANDLE,
        METH_TYPE,
        DYNAMIC,
        INVOKE_DYNAMIC,
        MODULE,
        PACKAGE;

        // spotless:off
        // Indices in this table directly map to the values of these constants - disable spotless to make it easier to
        // see this
        static final ConstantTypes[] MAP = {      INVALID,
            UTF8,    INVALID,        INT,         FLOAT,
            LONG,    DOUBLE,         CLASS_REF,   STR_REF,
            FIELD,   METH_REF,       IMETH_REF,   NAME_TYPE,
            INVALID, INVALID,        METH_HANDLE, METH_TYPE,
            DYNAMIC, INVOKE_DYNAMIC, MODULE,      PACKAGE };
        //spotless:on

        static ConstantTypes toType(byte code) {
            var ret = MAP[Byte.toUnsignedInt(code)];
            if (ret == INVALID) throw new RuntimeException("Invalid constant type: " + code);
            return ret;
        }
    }

    private byte[][] BYTES_TO_SEARCH;

    public ClassConstantPoolParser(String... strings) {
        BYTES_TO_SEARCH = new byte[strings.length][];
        for (int i = 0; i < BYTES_TO_SEARCH.length; i++) {
            BYTES_TO_SEARCH[i] = strings[i].getBytes(StandardCharsets.UTF_8);
        }
    }

    public void addString(String string) {
        BYTES_TO_SEARCH = Arrays.copyOf(BYTES_TO_SEARCH, BYTES_TO_SEARCH.length + 1);
        BYTES_TO_SEARCH[BYTES_TO_SEARCH.length - 1] = string.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Returns true if the constant pool of the class represented by this byte array contains one of the Strings we are
     * looking for
     */
    public boolean find(byte[] basicClass) {
        return find(basicClass, false);
    }

    /**
     * Returns true if the constant pool of the class represented by this byte array contains one of the Strings we are
     * looking for.
     *
     * @param prefixes If true, it is enough for a constant pool entry to <i>start</i> with one of our Strings to count
     *                 as a match - otherwise, the entire String has to match.
     */
    public boolean find(byte[] basicClass, boolean prefixes) {
        if (basicClass == null || basicClass.length == 0) {
            return false;
        }

        // checks the class version
        final var maxSupported = 69; // Java 25
        var major = readShort(6, basicClass);
        if (major > maxSupported || (major == maxSupported && readShort(4, basicClass) > 0)) {
            return false;
        }

        // Loop through each entry in the constant pool, getting the size and content before jumping to the next.
        // Strings get scanned for the searched constants, and if found result in an early exit.
        final int numConstants = readUnsignedShort(8, basicClass);
        int index = 10;
        for (int i = 1; i < numConstants; ++i) {
            int size = -1;

            switch (ConstantTypes.toType(basicClass[index])) {
                case UTF8:
                    final int strLen = readUnsignedShort(index + 1, basicClass);
                    size = 3 + strLen;

                    for (byte[] bytes : BYTES_TO_SEARCH) {
                        if (prefixes ? strLen < bytes.length : strLen != bytes.length) continue;

                        boolean found = true;
                        for (int j = index + 3; j < index + 3 + bytes.length; j++) {
                            if (basicClass[j] != bytes[j - (index + 3)]) {
                                found = false;
                                break;
                            }
                        }

                        if (found) return true;
                    }
                    break;
                case INT:
                case FLOAT:
                case FIELD:
                case METH_REF:
                case IMETH_REF:
                case NAME_TYPE:
                case DYNAMIC:
                case INVOKE_DYNAMIC:
                    size = 5;
                    break;
                case LONG:
                case DOUBLE:
                    size = 9;
                    ++i;
                    break;
                case METH_HANDLE:
                    size = 4;
                    break;
                case CLASS_REF:
                case STR_REF:
                case METH_TYPE:
                case MODULE:
                case PACKAGE:
                    size = 3;
                    break;
            }

            if (size < 0) throw new RuntimeException("Error parsing constant pool!");
            index += size;
        }

        return false;
    }

    private static short readShort(final int index, byte[] basicClass) {
        return (short) readUnsignedShort(index, basicClass);
    }

    private static int readUnsignedShort(final int index, byte[] basicClass) {
        return ((basicClass[index] & 0xFF) << 8) | (basicClass[index + 1] & 0xFF);
    }

}
