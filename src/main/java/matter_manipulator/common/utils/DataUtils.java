package matter_manipulator.common.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@SuppressWarnings("unused")
public class DataUtils {

    public static <S, T> List<T> mapToList(Collection<S> in, Function<S, T> mapper) {
        if (in == null) return null;

        List<T> out = new ArrayList<>(in.size());

        for (S s : in)
            out.add(mapper.apply(s));

        return out;
    }

    public static <S, T> List<T> mapToList(S[] in, Function<S, T> mapper) {
        if (in == null) return null;

        List<T> out = new ArrayList<>(in.length);

        for (S s : in)
            out.add(mapper.apply(s));

        return out;
    }

    public static <S, T> T[] mapToArray(Collection<S> in, IntFunction<T[]> ctor, Function<S, T> mapper) {
        if (in == null) return null;

        T[] out = ctor.apply(in.size());

        Iterator<S> iter = in.iterator();
        for (int i = 0; i < out.length && iter.hasNext(); i++) {
            out[i] = mapper.apply(iter.next());
        }

        return out;
    }

    public static <S, T> T[] mapToArray(S[] in, IntFunction<T[]> ctor, Function<S, T> mapper) {
        if (in == null) return null;

        T[] out = ctor.apply(in.length);

        for (int i = 0; i < out.length; i++)
            out[i] = mapper.apply(in[i]);

        return out;
    }

    public static <T> T find(T[] in, Predicate<T> fn) {
        for (T t : in) {
            if (fn.test(t)) return t;
        }

        return null;
    }

    public static <T> T find(Collection<T> in, Predicate<T> fn) {
        for (T t : in) {
            if (fn.test(t)) return t;
        }

        return null;
    }

    public static <T> int indexOf(T[] array, T value) {
        int l = array.length;

        for (int i = 0; i < l; i++) {
            if (array[i] == value) { return i; }
        }

        return -1;
    }

    public static <T> T[] slice(T[] array, int start, int end) {
        T[] out = Arrays.copyOf(array, end - start);

        int i = 0;

        for (int i2 = start; i2 < end; i2++) {
            out[i++] = array[i2];
        }

        return out;
    }

    public static String join(String sep, String[] array) {
        StringBuilder sb = new StringBuilder();

        for (String chunk : array) {
            //noinspection SizeReplaceableByIsEmpty
            if (sb.length() > 0) {
                sb.append(sep);
            }

            sb.append(chunk);
        }

        return sb.toString();
    }

    public static String join(String sep, Collection<String> col) {
        StringBuilder sb = new StringBuilder();

        for (String chunk : col) {
            //noinspection SizeReplaceableByIsEmpty
            if (sb.length() > 0) {
                sb.append(sep);
            }

            sb.append(chunk);
        }

        return sb.toString();
    }

    public static int countNonNulls(Object[] array) {
        int l = array.length;
        int count = 0;

        for (Object o : array) {
            if (o != null) count++;
        }

        return count;
    }

    public static <T> T[] withoutNulls(T[] array) {
        if (array.length == 0) return array;

        int nonNullCount = countNonNulls(array);

        if (nonNullCount == array.length) return array;

        T[] out = Arrays.copyOf(array, nonNullCount);

        int j = 0;

        for (T t : array) {
            if (t != null) out[j++] = t;
        }

        return out;
    }

    public static <T> ArrayList<T> filterList(List<T> input, Predicate<T> filter) {
        ArrayList<T> output = new ArrayList<>(input.size());

        for (int i = 0, inputSize = input.size(); i < inputSize; i++) {
            T t = input.get(i);

            if (filter.test(t)) {
                output.add(t);
            }
        }

        return output;
    }

    public static <T, S extends T> void addAllFiltered(List<S> input, List<T> output, Predicate<S> filter) {
        for (int i = 0, inputSize = input.size(); i < inputSize; i++) {
            S s = input.get(i);

            if (filter.test(s)) {
                output.add(s);
            }
        }
    }

    /**
     * Upcasts a list of a concrete type into a list of interfaces since java can't do this implicitly with generics.
     */
    public static <I, T extends I> ArrayList<I> upcast(List<T> input) {
        ArrayList<I> output = new ArrayList<>(input.size());

        for (int i = 0, inputSize = input.size(); i < inputSize; i++) {
            output.add(input.get(i));
        }

        return output;
    }

    public static <T> T getIndexSafe(T[] array, int index) {
        return array == null || index < 0 || index >= array.length ? null : array[index];
    }

    public static <T> T getIndexSafe(List<T> list, int index) {
        return list == null || index < 0 || index >= list.size() ? null : list.get(index);
    }

    public static <T> T choose(List<T> list, Random rng) {
        if (list.isEmpty()) return null;
        if (list.size() == 1) return list.get(0);

        return list.get(rng.nextInt(list.size()));
    }

    /**
     * A helper for checking if an arbitrary JsonElement is truthy according to the standard JS rules, with some
     * modifications. This is useful for situations where you have an arbitrary deserialized JsonElement that's supposed
     * to have a boolean in it.
     */
    public static boolean isTruthy(JsonElement element) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = (JsonPrimitive) element;

            if (primitive.isBoolean()) return primitive.getAsBoolean();

            if (primitive.isNumber()) return primitive.getAsNumber().doubleValue() != 0;

            String value = primitive.getAsString();

            if ("true".equals(value)) return true;
            if ("false".equals(value)) return false;

            if (MCUtils.INTEGER.matcher(value).matches()) return Long.parseLong(value) != 0;

            if (MCUtils.FLOAT.matcher(value).matches()) return Double.parseDouble(value) != 0;

            return !value.isEmpty();
        }

        if (element.isJsonArray()) return ((JsonArray) element).size() > 0;

        if (element.isJsonObject()) return !((JsonObject) element).entrySet().isEmpty();

        return false;
    }

    public static MethodHandle exposeFieldGetter(Class<?> clazz, String srgName) {
        try {
            Field field = ObfuscationReflectionHelper.findField(clazz, srgName);
            field.setAccessible(true);
            return MethodHandles.lookup()
                .unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not make field getter for " + clazz.getName() + ":" + srgName, e);
        }
    }

    public static MethodHandle exposeFieldSetter(Class<?> clazz, String srgName) {
        try {
            Field field = ObfuscationReflectionHelper.findField(clazz, srgName);
            field.setAccessible(true);
            return MethodHandles.lookup()
                .unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not make field setter for " + clazz.getName() + ":" + srgName, e);
        }
    }

    public static MethodHandle exposeMethod(Class<?> clazz, MethodType sig, String srgName) {
        try {
            Method method = ObfuscationReflectionHelper.findMethod(clazz, srgName, sig.returnType(), sig.parameterArray());
            method.setAccessible(true);
            return MethodHandles.lookup()
                .unreflect(method);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not make method handle for " + clazz.getName() + ":" + srgName, e);
        }
    }

    public static <K, V> boolean areMapsEqual(Map<K, V> left, Map<K, V> right) {
        if (left == null || right == null) return left == right;

        HashSet<K> keys = new HashSet<>(left.size() + right.size());

        keys.addAll(left.keySet());
        keys.addAll(right.keySet());

        for (K key : keys) {
            if (!Objects.equals(left.get(key), right.get(key))) return false;
        }

        return true;
    }

    public static <T> T[] concat(T[] array, T value) {
        T[] out = Arrays.copyOf(array, array.length + 1);
        out[out.length - 1] = value;
        return out;
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] out = Arrays.copyOf(first, first.length + second.length);

        System.arraycopy(second, 0, out, first.length, second.length);

        return out;
    }
}
