package matter_manipulator.common.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
}
