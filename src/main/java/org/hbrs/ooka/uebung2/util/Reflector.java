package org.hbrs.ooka.uebung2.util;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class offers an interface to work with {@code java.lang.reflect} Reflections.
 *
 * @author Militalex
 * @version 2.5.1
 */
public class Reflector {

    /**
     * @param clazz Class where field might be offered or inherited.
     * @param name Name of field.
     * @return Returns true if field with given {@code name} in given {@code clazz} exists. This includes inherited fields.
     */
    public static boolean containsField(@NotNull Class<?> clazz, @NotNull String name){
        return getAllFields(clazz).stream().map(Field::getName).collect(Collectors.toSet()).contains(name);
    }

    /**
     * @param clazz Class where the constructor might be in.
     * @param parameterTypes Parameter types of constructor. <i>(It supports subtypes too.)</i>
     * @return Returns true if constructor with given {@code parameterTypes} in given {@code clazz} exists.
     */
    public static boolean containsConstructor(@NotNull Class<?> clazz, @NotNull Class<?>... parameterTypes){
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (isCompatible(constructor.getParameterTypes(), parameterTypes)) return true;
        }
        return false;
    }

    /**
     * @param clazz Class where the method might be in.
     * @param name Name of method.
     * @return Returns true if method with given {@code name} exists in given {@code clazz}. It also searches in inherited methods.
     */
    public static boolean containsMethod(@NotNull Class<?> clazz, @NotNull String name){
        return getAllMethods(clazz).stream().map(Method::getName).toList().contains(name);
    }

    /**
     * @param clazz Class where the method might be in.
     * @param name Name of method.
     * @param parameterTypes Parameter types of method. <i>(It supports subtypes too.)</i>
     * @return Returns true if method with given {@code name} and given {@code parameterTypes} exists in given {@code clazz}.
     * It also searches in inherited methods.
     */
    public static boolean containsMethod(@NotNull Class<?> clazz, @NotNull String name, @NotNull Class<?>... parameterTypes){
        for (Method method : getAllMethods(clazz)) {
            if (method.getName().equals(name) && isCompatible(method.getParameterTypes(), parameterTypes)) return true;
        }
        return false;
    }

    /**
     * @param clazz Class where the constructors are located.
     * @return Returns the amount of constructor declared in class.
     */
    public static int constructorAmount(@NotNull Class<?> clazz){
        return clazz.getDeclaredConstructors().length;
    }

    /**
     * @param clazz Class where the methods are offered.
     * @param name Name of method.
     * @return Returns the amount of methods given {@code clazz} offers. The amount contains declared and inherited methods.
     */
    public static int methodAmount(@NotNull Class<?> clazz, @NotNull String name){
        return getAllMethods(clazz).stream().map(Method::getName).filter(name::equals).toList().size();
    }

    /**
     * @param clazz Class where all fields including inherited are extracted
     * @return Returns a list of all fields in given {@code clazz} which all have been made accessible, but not accessible through final.
     */
    public static List<Field> getAllFields(@NotNull Class<?> clazz){
        // All collected fields -> set because no duplicates are allowed
        final LinkedHashSet<Field> set = new LinkedHashSet<>();

        // prepare for latitude search
        final ArrayDeque<Class<?>> queue = new ArrayDeque<>(Collections.singleton(clazz));

        // latitude search in class hierarchies to collect all fields
        while (!queue.isEmpty()){
            final Class<?> curClass = queue.remove();

            // add fields to set
            set.addAll(Arrays.stream(curClass.getDeclaredFields()).peek(field -> {
                // make fields accessible
                field.setAccessible(true);
            }).collect(Collectors.toSet()));

            // add superclasses and implemented interfaces to queue
            if (hasSuperclass(curClass)) queue.add(curClass.getSuperclass());
            if (hasInterfaces(curClass)) queue.addAll(Set.of(curClass.getInterfaces()));
        }

        // return set as list
        return set.stream().toList();
    }

    /**
     * @param clazz Class where field is in.
     * @param name Name of field.
     * @return Returns first field corresponding to given {@code name} and given {@code class}. Inherited fields can be found too.
     */
    public static Field getField(@NotNull Class<?> clazz, @NotNull String name){
        return getField(clazz, name, 0);
    }

    /**
     * @param clazz Class where field is in.
     * @param name Name of field.
     * @param pos If multiple fields exist, field at {@code pos} will be used.
     * @return Returns field corresponding to given {@code name}, given {@code class} and at given {@code pos} in list. Inherited fields can be found too.
     */
    public static Field getField(@NotNull Class<?> clazz, @NotNull String name, int pos){
        final Set<Field> fields = getAllFields(clazz).stream().filter(field -> field.getName().equals(name)).collect(Collectors.toSet());

        if (fields.isEmpty()) throw new IllegalArgumentException("Field: " + name + " is not valid field in " + clazz);

        return fields.stream().toList().get(pos);
    }

    /**
     * @param clazz Class where all constructors are extracted.
     * @return Returns a list of all constructors in {@code clazz} which all have been made accessible from given.
     */
    public static List<? extends Constructor<?>> getAllConstructors(@NotNull Class<?> clazz){
        return Arrays.stream(clazz.getDeclaredConstructors()).peek(constructor -> constructor.setAccessible(true)).toList();
    }

    /**
     * @param clazz Class where constructor is located in.
     * @param parameterTypes Parameter types of constructor. <i>(It supports subclasses too.)</i>
     * @return Returns constructor corresponding to given {@code name} and given {@code class}.
     */
    public static Constructor<?> getConstructor(@NotNull Class<?> clazz, @NotNull Class<?>... parameterTypes){
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (isCompatible(constructor.getParameterTypes(), parameterTypes)) {
                constructor.setAccessible(true);
                return constructor;
            }
        }
        throw new IllegalArgumentException("Constructor with following parameters: " +
                Arrays.toString(parameterTypes) + " does not exists in " + clazz);
    }

    /**
     * @param clazz Class where constructor is located in.
     * @param index Position of constructor in {@code clazz} starting by zero.
     * @return Returns constructor at given position {@code index} starting by zero.
     * @param <T> Type of object the constructor would create.
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getConstructor(@NotNull Class<T> clazz, int index){
        if (index >= constructorAmount(clazz)) throw new IllegalArgumentException(clazz +
                " does not have enough constructors for addressing with index number of " + index);
        final Constructor<T> constructor = (Constructor<T>) clazz.getDeclaredConstructors()[index];
        constructor.setAccessible(true);
        return constructor;
    }

    /**
     * @param clazz Class where methods are located in or inherited.
     * @return Returns a list of all methods in given {@code clazz} which all have been made accessible.
     */
    public static List<Method> getAllMethods(@NotNull Class<?> clazz){
        final Set<Method> methods = new HashSet<>(List.of(clazz.getMethods()));
        methods.addAll(List.of(clazz.getDeclaredMethods()));
        return methods.stream().peek(method -> method.setAccessible(true)).toList();
    }

    /**
     * @param clazz Class where method is located in or inherited.
     * @param name Name of method.
     * @param parameterTypes Parameter types of method. <i>(It supports subclasses too.)</i>
     * @return Returns method corresponding to given {@code name} and given {@code clazz}. Inherited methods can be found too.
     */
    public static Method getMethod(@NotNull Class<?> clazz, @NotNull String name, @NotNull Class<?>... parameterTypes){
        for (Method method : getAllMethods(clazz)) {
            if (method.getName().equals(name) && isCompatible(method.getParameterTypes(), parameterTypes)) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new IllegalArgumentException("Method: " + name +
                Arrays.toString(parameterTypes) + " does not exist in " + clazz);
    }

    /**
     * @param clazz Class where method is located in or inherited.
     * @param name Name of method.
     * @param index Position in declaration order starting at zero.
     *              <i>For example if two methods exists with name foo because of overloading,
     *              {@code index} decides which of them will be returned.</i>
     * @return Returns method corresponding to given {@code name} and given position {@code index} in declaration order.
     */
    public static Method getMethod(@NotNull Class<?> clazz, @NotNull String name, int index){
        if (!containsMethod(clazz, name)) throw new IllegalArgumentException("Method: " + name + " does not exist in " + clazz);
        if (index >= methodAmount(clazz, name)) throw new IllegalArgumentException("Method: " +  name + " is not accessible with index " + index);
        final Method method = getAllMethods(clazz).get(index);
        method.setAccessible(true);
        return method;
    }

    /**
     * @param obj Object where field content will be extracted.
     * @param name Name of field.
     * @return Returns content of field corresponding to given {@code name} of given object {@code obj}.
     */
    public static Object getFieldContent(@NotNull Object obj, @NotNull String name){
        final Field field = getField(obj.getClass(), name);
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Field " + name + " is not accessible for object " + obj);
        }
    }

    /**
     * Sets the content of a field corresponding to given {@code name} for given object {@code obj}.
     * @param obj Object where field content will be overridden.
     * @param name Name of field.
     */
    public static void setFieldContent(@NotNull Object obj, @NotNull String name){
        final Field field = getField(obj.getClass(), name);
        try {
            field.set(obj, name);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Field " + name + " is not accessible for object " + obj);
        }
    }

    /**
     * @param clazz Class where static final field is located in.
     * @param name Name of field.
     * @return Returns content of a static field that may be final or private.
     */
    public static Object getStaticFieldContent(@NotNull Class<?> clazz, @NotNull String name){
        final Field field = getField(clazz, name);

        try {
            return field.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot access field: " + name);
        }
    }

    /**
     * Sets content of a static field, that cannot be final, corresponding to given {@code name} for given object {@code obj}.
     * @param clazz Class where static final field is located in.
     * @param name Name of field.
     * @param newVal New value associated with field. It has to match field type.
     */
    public static void setStaticFieldContent(@NotNull Class<?> clazz, @NotNull String name, Object newVal){
        final Field field = getField(clazz, name);

        try {
            field.set(null, newVal);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot access field: " + name);
        }
    }

    /**
     * @param clazz Class where static final field is located in.
     * @param name Name of field.
     * @return Returns content of static final field corresponding to given {@code name} of given object {@code obj}.
     * @deprecated This method can sometimes not work in newer java versions since jdk 12 in certain circumstances.
     */
    @Deprecated
    @SneakyThrows({NoSuchFieldException.class})
    public static Object getStaticFinalFieldContent(@NotNull Class<?> clazz, @NotNull String name){
        final Field field = getField(clazz, name);

        final Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        try {
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Error while making field (" + name + ") not final anymore.");
        }

        try {
            return field.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot access field: " + name);
        }
    }

    /**
     * Sets the content of a static final field corresponding to given {@code name} for given object {@code obj}.
     * @param clazz Class where static final field is located in.
     * @param name Name of field.
     * @param newVal New value associated with field. It has to match field type.
     * @deprecated This method does not work anymore in newer java versions since jdk 12.
     */
    @Deprecated
    @SneakyThrows({NoSuchFieldException.class})
    public static void setStaticFinalFieldContent(@NotNull Class<?> clazz, @NotNull String name, Object newVal){
        final Field field = getField(clazz, name);

        final Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        try {
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Error while making field not final anymore.");
        }

        try {
            field.set(null, newVal);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot access field: " + name);
        }
    }

    /**
     * @param clazz Class where constructor is located in.
     * @param parameter Parameter for constructor. <i>(It supports subclasses too.)</i>
     * @return Returns a new instance of given object using constructor corresponding to given {@code parameter}.
     */
    public static Object invokeConstructor(@NotNull Class<?> clazz, @NotNull Object... parameter){
        final Class<?>[] parameterTypes = getParameterTypes(parameter);
        try {
            return getConstructor(clazz, parameterTypes).newInstance(parameter);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(clazz + " is an abstract class. Therefore no instances can be created.");
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Constructor (" + clazz + Arrays.toString(parameterTypes) + ") is not accessible.");
        } catch (InvocationTargetException e) {
            throw new RuntimeException("The invoked method has thrown an exception: " + e.getCause().getClass(), e.getCause());
        }
    }

    /**
     * @param obj Object where method is invoked on.
     * @param name Name of method.
     * @param parameter Parameter for method. <i>(It supports subclasses too.)</i>
     * @return Returns return value of method if it exists. If void method it is null.
     */
    public static Object invokeMethod(@NotNull Object obj, @NotNull String name, @NotNull Object... parameter){
        final Class<?>[] parameterTypes = getParameterTypes(parameter);
        try {
            return getMethod(obj.getClass(), name, parameterTypes).invoke(obj, parameter);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Constructor (" + obj.getClass() + Arrays.toString(parameterTypes) + ") is not accessible.");
        } catch (InvocationTargetException e) {
            throw new RuntimeException("The invoked method has thrown an exception: " + e.getCause().getClass(), e.getCause());
        }
    }

    /**
     * @param clazz Class where static method is located in.
     * @param name Name of method.
     * @param parameter Parameter for method. <i>(It supports subclasses too.)</i>
     * @return Returns return value of static method if it exists. If void method it is null.
     */
    public static Object invokeStaticMethod(@NotNull Class<?> clazz, @NotNull String name, @NotNull Object... parameter){
        final Class<?>[] parameterTypes = getParameterTypes(parameter);
        try {
            return getMethod(clazz, name, parameterTypes).invoke(null, parameter);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Constructor (" + clazz + Arrays.toString(parameterTypes) + ") is not accessible.");
        } catch (InvocationTargetException e) {
            throw new RuntimeException("The invoked method has thrown an exception: " + e.getCause().getClass(), e.getCause());
        }
    }

    /**
     * @param clazz Class to test on if it has a declared superclass.
     * @return Returns true if given {@code clazz} has a declared superclass.
     */
    public static boolean hasSuperclass(@NotNull Class<?> clazz){
        return clazz.getSuperclass() != null;
    }

    /**
     * <i>(This method can also be used to determine "superinterfaces" if given {@code clazz} is an interface)</i>
     * @param clazz Class to test on if it has interfaces.
     * @return Returns true if given {@code clazz} has at least one interface in declaration.
     */
    public static boolean hasInterfaces(@NotNull Class<?> clazz){
        return clazz.getInterfaces().length > 0;
    }

    /**
     * @return Returns from given parameter object array {@code parameter} corresponding class array.
     */
    @NotNull
    public static Class<?>[] getParameterTypes(@NotNull Object[] parameter) {
        final Class<?>[] parameterTypes = new Class<?>[parameter.length];
        for (int i = 0; i < parameter.length; i++){
            if (parameter[i] == null) parameterTypes[i] = null;
            else parameterTypes[i] = parameter[i].getClass();
        }
        return parameterTypes;
    }

    /**
     * Checks if given {@code parTypes} matches into given {@code methodParTypes} in consideration of substitution principle
     * @return Returns true if given {@code parTypes} matches into given {@code methodParTypes}.
     */
    public static boolean isCompatible(@NotNull Class<?>[] methodParTypes, @NotNull Class<?>[] parTypes){
        // false if length is unequal
        if (methodParTypes.length != parTypes.length) return false;

        for (int i = 0; i < methodParTypes.length; i++){
            // get current types
            Class<?> methType = methodParTypes[i];
            Class<?> parType = parTypes[i];

            // Support null values
            if (parType == null) continue;

            // Supporting primitve types
            if (methType.isPrimitive()){
                methType = toWrapperClass(methType);
            }
            if (parType.isPrimitive()){
                parType = toWrapperClass(parType);
            }

            // go to next parameter if current are equal
            if (methType == parType) continue;

            // Latitude search prepare
            final ArrayDeque<Class<?>> queue = new ArrayDeque<>();
            queue.add(parType);
            boolean breakFlag = false;

            // Latitude search from bottom to up in parQueueType class hierarchy to find matches with methType
            while (!queue.isEmpty() && !breakFlag){
                // remove current from queue
                final Class<?> parQueueType = queue.remove();

                // parQueueType matches -> go to next parameter
                if (parQueueType  == methType) {
                    breakFlag = true;
                    continue;
                }

                // Add superclasses/interfaces to queue
                if (hasSuperclass(parQueueType)) queue.add(parQueueType.getSuperclass());
                if (hasInterfaces(parQueueType)) queue.addAll(Set.of(parQueueType.getInterfaces()));
            }
            // go to next parameter
            if (breakFlag) continue;

            // current parameter does not match -> not compatible
            return false;
        }
        // all parameter matches -> compatible
        return true;
    }

    /**
     * <i>(Provided by ChatGPT)</i><br>
     * Returns the corresponding wrapper class for the given primitive class.
     *
     * @param primitiveClass the primitive class to convert
     * @return the corresponding wrapper class
     * @throws IllegalArgumentException if the given class is not a primitive type
     */
    public static Class<?> toWrapperClass(Class<?> primitiveClass) {
        return switch (primitiveClass.getName()) {
            case "byte" -> Byte.class;
            case "short" -> Short.class;
            case "int" -> Integer.class;
            case "long" -> Long.class;
            case "float" -> Float.class;
            case "double" -> Double.class;
            case "boolean" -> Boolean.class;
            case "char" -> Character.class;
            default -> throw new IllegalArgumentException("Not a primitive type: " + primitiveClass.getName());
        };
    }
}
