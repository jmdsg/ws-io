package com.fiberg.wsio.auto;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.vavr.API;
import io.vavr.Function2;
import io.vavr.Predicates;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.apache.commons.lang3.text.WordUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Objects;
import java.util.function.Predicate;

import static java.util.Objects.isNull;

/**
 * Class that uses reflection to provide utility methods.
 */
public class ReflectUtils {

    /**
     * Enum containing the possible method properties.
     */
    public enum MethodProperty {

        /** Getters */
        GETTER,

        /** Setters */
        SETTER,

        /** Getters and Setters */
        BOTH,

        /** All methods */
        ALL

    }

    /**
     * Method that prints all the annotations of a class and its methods.
     *
     * @param clazz class to print the annotations
     * @throws NullPointerException when clazz is null
     */
    public static void printClassAnnotations(final Class<?> clazz) throws NullPointerException {

        /* Print each of the annotations of the class */
        for (final Annotation annotation : clazz.getDeclaredAnnotations()) {
            System.out.println(annotation);
        }

        /* Print class name */
        final Method[] methods = clazz.getMethods();
        System.out.print(clazz.getName());

        if (methods.length > 0) System.out.println(" {");
        else System.out.println(" { }");

        /* Iterate for each method */
        String indentation = "  ";
        for (final Method method : methods) {

            /* Print each annotation */
            for (final Annotation annotation : method.getDeclaredAnnotations()) {
                System.out.print(indentation);
                System.out.println(annotation);
            }

            final Parameter[] parameters = method.getParameters();

            /* Print the method name */
            System.out.print(indentation);
            System.out.print(method.getName());

            if (parameters.length > 0) System.out.println(" (");
            else System.out.println(" ( )");

            /* Iterate for each parameter */
            for (final Parameter parameter : parameters) {

                /* Print each annotation of the current parameter */
                for (final Annotation annotation : parameter.getDeclaredAnnotations()) {
                    System.out.print(indentation);
                    System.out.print(indentation);
                    System.out.println(annotation);
                }
                System.out.print(indentation);
                System.out.print(indentation);
                System.out.println(parameter.getName());

            }

            if (parameters.length > 0) {
                System.out.print(indentation);
                System.out.println(")");
            }

        }

        if (methods.length > 0) System.out.println("}");
        System.out.println();

    }

    /**
     * Method that get the class names from the package of the base class.
     *
     * @param baseClass base class to extract the base package
     * @return class names from the package of the base class
     */
    public static Set<String> getClassNamesFrom(final Class<?> baseClass) {

        /* Extract the base package and get class names */
        return Option.of(baseClass)
                .map(Class::getPackage)
                .map(Package::getName)
                .map(ReflectUtils::getClassNamesFrom)
                .getOrElse(HashSet::empty);

    }

    /**
     * Method that get the class names from the base package.
     *
     * @param basePackage base package to get the classes
     * @return class names from the base package
     */
    public static Set<String> getClassNamesFrom(final String basePackage) {

        /* Obtain and return all class names of the base package */
        return Option.of(basePackage)
                .map(base -> HashSet.ofAll(new FastClasspathScanner(basePackage).scan().getNamesOfAllClasses())
                        .filter(name -> name.startsWith(basePackage))

                        .filter(name -> !name.endsWith("package-info")))
                .getOrElse(HashSet::empty);

    }

    /**
     * Method that get the classes from the package of the base class.
     *
     * @param baseClass base class to extract the base package
     * @return classes from the package of the base class
     */
    public static Set<Class<?>> getClassesFrom(final Class<?> baseClass) {

        /* Return the classes from the base class package */
        return Option.of(baseClass)
                .map(Class::getPackage)
                .map(Package::getName)
                .map(ReflectUtils::getClassesFrom)
                .getOrElse(HashSet::empty);

    }

    /**
     * Method that get the classes from the base package.
     *
     * @param basePackage base package to get the classes
     * @return classes from the base package
     */
    public static Set<Class<?>> getClassesFrom(final String basePackage) {

        /* Return all classes of the base package */
        return getClassNamesFrom(basePackage)
                .flatMap(name ->
                        Try.of(() -> Class.forName(name)));

    }

    /**
     * Method that extract all the involved classes with the main class.
     * It only checks the classes matching the base package if is not null.
     *
     * @param clazz               class to extract the involded classes
     * @param baseClass           base class to check, if null is specified all the classes are going to be checked
     * @param checkMethodProperty indicates if the getter, the setter or both properties should be checked
     * @param checkCollections    indicates if the maps and collections should be checked
     * @param checkPublicOnly     indicates if only public fields and methods
     * @param checkStatic         indicates if static field and methods should be checked
     * @param checkInnerClasses   indicates if inner classes should be checked or not
     * @return the involded classes in the class that matched the base package
     */
    public static Set<Class<?>> getInvolvedClassesIn(final Class<?> clazz,
                                                     final Class<?> baseClass,
                                                     final MethodProperty checkMethodProperty,
                                                     final Boolean checkCollections,
                                                     final Boolean checkPublicOnly,
                                                     final Boolean checkStatic,
                                                     final Boolean checkInnerClasses) {

        return Option.of(baseClass)
                .map(Class::getPackage)
                .map(Package::getName)
                .orElse(Option.some(null))
                .map(basePackage -> ReflectUtils.getInvolvedClassesIn(clazz, basePackage,
                        checkMethodProperty, checkCollections, checkPublicOnly, checkStatic, checkInnerClasses))
                .getOrElse(HashSet::empty);

    }

    /**
     * Method that extract all the involved classes with the main class.
     * It only checks the classes matching the base package if is not null.
     *
     * @param clazz               class to extract the involded classes
     * @param basePackage         base package to check, if null is specified all the classes are going to be checked
     * @param checkMethodProperty indicates if the getter, the setter or both properties should be checked
     * @param checkCollections    indicates if the maps and collections should be checked
     * @param checkPublicOnly     indicates if only public fields and methods
     * @param checkStatic         indicates if static field and methods should be checked
     * @param checkInnerClasses   indicates if inner classes should be checked or not
     * @return the involded classes in the class that matched the base package
     */
    public static Set<Class<?>> getInvolvedClassesIn(final Class<?> clazz,
                                                     final String basePackage,
                                                     final MethodProperty checkMethodProperty,
                                                     final Boolean checkCollections,
                                                     final Boolean checkPublicOnly,
                                                     final Boolean checkStatic,
                                                     final Boolean checkInnerClasses) {

        /* Return the involved classes in with an empty hashset as navigateds */
        return getInvolvedClassesIn(clazz, basePackage, checkMethodProperty,
                checkCollections, checkPublicOnly, checkStatic, checkInnerClasses, new java.util.HashSet<>())
                .filter(current -> isNull(basePackage)
                        || current.getCanonicalName().startsWith(basePackage));

    }

    /**
     * Method that extract all the involved classes with the main class.
     * It only checks the classes matching the base package if is not null.
     *
     * @param clazz               class to extract the involded classes
     * @param basePackage         base package to check, if null is specified all the classes are going to be checked
     * @param checkMethodProperty indicates if the getter, the setter or both properties should be checked
     * @param checkCollections    indicates if the maps and collections should be checked
     * @param checkPublicOnly     indicates if only public fields and methods
     * @param checkStatic         indicates if static field and methods should be checked
     * @param checkInnerClasses   indicates if inner classes should be checked or not
     * @param globalNavigated     global set to avoid processes already processed classes
     * @return the involded classes in the class
     */
    private static Set<Class<?>> getInvolvedClassesIn(final Class<?> clazz,
                                                      final String basePackage,
                                                      final MethodProperty checkMethodProperty,
                                                      final Boolean checkCollections,
                                                      final Boolean checkPublicOnly,
                                                      final Boolean checkStatic,
                                                      final Boolean checkInnerClasses,
                                                      final java.util.Set<Class<?>> globalNavigated) {

        /* Predicate that checks if a class is valid or nor.
         * It used the check collection flag and also checks if the base package is null or not */
        final Predicate<Class<?>> checkIsAllowed = current -> isNull(basePackage)
                || current.getCanonicalName().startsWith(basePackage)
                || (checkCollections && java.util.Map.class.isAssignableFrom(current))
                || (checkCollections && java.util.Collection.class.isAssignableFrom(current));

        /* Extract the component class in case is an array and check if is valid */
        final Option<Class<?>> nextClass = Option.of(clazz)
                .<Class<?>>map(ReflectUtils::getComponentClass)
                .filter(Objects::nonNull);
        final Option<Class<?>> filtered = nextClass.filter(checkIsAllowed);

        /* Add the class to navigated and the component class to the navigated set */
        globalNavigated.add(clazz);
        globalNavigated.addAll(nextClass.toJavaList());

        /* Get the classes in generic types of fields, methods, superclasses and superinterfaces.
         * Also get classes defined in current classes. Check if some of this classes is already navigated
         * and call the recursive function with the ones who have not been navigated and add the filtered to the result */
        return filtered.map(current ->
                Stream.concat(
                        ReflectUtils.getFieldTypesFrom(current, checkPublicOnly, checkStatic)
                                .toStream()
                                .flatMap(next -> ReflectUtils.getClassesIn(next, basePackage, checkCollections)),
                        ReflectUtils.getMethodTypesFrom(current, checkMethodProperty, checkPublicOnly, checkStatic)
                                .toStream()
                                .flatMap(next -> ReflectUtils.getClassesIn(next, basePackage, checkCollections)),
                        Stream.of(current.getGenericInterfaces())
                                .flatMap(next -> ReflectUtils.getClassesIn(next, basePackage, checkCollections)),
                        Stream.of(current.getGenericSuperclass())
                                .flatMap(next -> ReflectUtils.getClassesIn(next, basePackage, checkCollections)),
                        Option.when(checkInnerClasses, current.getDeclaredClasses())
                                .toStream()
                                .flatMap(Stream::of))
                        .toSet())
                .getOrElse(HashSet::empty)
                .filter(Predicates.noneOf(globalNavigated::contains))
                .flatMap(current -> getInvolvedClassesIn(current, basePackage, checkMethodProperty,
                        checkCollections, checkPublicOnly, checkStatic, checkInnerClasses, globalNavigated))
                .addAll(filtered);

    }

    /**
     * Method that extracts the classes in a method.
     *
     * @param method           method to extract the classes
     * @param basePackage      base package to check, if null is specified all the classes are going to be checked
     * @param checkCollections indicates if the maps and collections should be checked
     * @return all the classes in a method
     */
    public static Set<Class<?>> getClassesIn(final Method method,
                                             final String basePackage,
                                             final Boolean checkCollections) {

        /* Extract the types of the method and return the classes in the type */
        return Option.of(method)
                .map(current -> Stream.concat(
                        Stream.of(current.getGenericParameterTypes())
                                .flatMap(next -> ReflectUtils.getClassesIn(next, basePackage, checkCollections)),
                        ReflectUtils.getClassesIn(current.getGenericReturnType(), basePackage, checkCollections))
                        .toSet())
                .getOrElse(HashSet::empty);

    }

    /**
     * Method that extracts the classes in a field.
     *
     * @param field            field to extract the classes
     * @param basePackage      base package to check, if null is specified all the classes are going to be checked
     * @param checkCollections indicates if the maps and collections should be checked
     * @return all the classes in a field
     */
    public static Set<Class<?>> getClassesIn(final Field field,
                                             final String basePackage,
                                             final Boolean checkCollections) {

        /* Extract the type of the field and return the classes in the type */
        return Option.of(field)
                .map(Field::getGenericType)
                .map(next -> ReflectUtils.getClassesIn(next, basePackage, checkCollections))
                .getOrElse(HashSet::empty);

    }

    /**
     * Method that extracts the classes in a type.
     *
     * @param type             type to extract the classes
     * @param basePackage      base package to check, if null is specified all the classes are going to be checked
     * @param checkCollections indicates if the maps and collections should be checked
     * @return all the classes in a type
     */
    public static Set<Class<?>> getClassesIn(final Type type,
                                             final String basePackage,
                                             final Boolean checkCollections) {

        /* Return the classes in type with empty hashset as navigated types.
         * Filter the collections to only return classes in base package if specified */
        return getClassesIn(type, basePackage, checkCollections, new java.util.HashSet<>())
                .filter(clazz -> isNull(basePackage)
                        || clazz.getCanonicalName().startsWith(basePackage));

    }

    /**
     * Method that extracts the classes in a type.
     *
     * @param type             type to extract the classes
     * @param basePackage      base package to check, if null is specified all the classes are going to be checked
     * @param checkCollections indicates if the maps and collections should be checked
     * @param globalNavigated  global set to avoid processes already processed types
     * @return all the classes in a type
     */
    private static Set<Class<?>> getClassesIn(final Type type,
                                              final String basePackage,
                                              final Boolean checkCollections,
                                              final java.util.Set<Type> globalNavigated) {

        /* Get the cleared types */
        final Set<Type> cleared = ReflectUtils.getClearedTypes(type);

        /* Add current type and cleared types to navigated set */
        globalNavigated.add(type);
        globalNavigated.addAll(cleared.toJavaSet());

        /* Predicate that checks if a class is valid or nor.
         * It used the check collection flag and also checks if the base package is null or not */
        final Predicate<Class<?>> checkIsAllowed = clazz -> isNull(basePackage)
                || clazz.getCanonicalName().startsWith(basePackage)
                || (checkCollections && java.util.Map.class.isAssignableFrom(clazz))
                || (checkCollections && java.util.Collection.class.isAssignableFrom(clazz));

        /* Map containing the type as key and the class as value.
         * To extract the value, the type is checked, if it is a parameterized type the raw type is extracted */
        final Map<Type, Class<?>> filtered = cleared.toMap(t -> t,
                t -> Option.of(t)
                        .filter(ParameterizedType.class::isInstance)
                        .map(ParameterizedType.class::cast)
                        .map(ParameterizedType::getRawType)
                        .getOrElse(t))
                .filterValues(Class.class::isInstance)
                .<Class<?>>mapValues(Class.class::cast)
                .filter((key, value) -> checkIsAllowed.test(value));

        /* Extract the parameterized types, check types are not already navigated,
         * then call the function recursively and add current valid classes */
        return filtered.keySet().filter(ParameterizedType.class::isInstance)
                .map(ParameterizedType.class::cast)
                .map(ParameterizedType::getActualTypeArguments)
                .flatMap(Stream::of)
                .filter(Predicates.noneOf(globalNavigated::contains))
                .flatMap(current -> getClassesIn(current, basePackage, checkCollections, globalNavigated))
                .addAll(filtered.values());

    }

    /**
     * Method that extract a type from an array or from the geric lower and upper bounds.
     *
     * @param type type to extract
     * @return type extracted from an array or from the geric lower and upper bounds.
     */
    public static Set<Type> getClearedTypes(final Type type) {

        /* First extract the component class in case type in an array,
         * then check if type is wild card to get its lower and upper bound.
         * Extract the component type of the bounds in case there are arrays and return
         * them if they exits otherwise return the actual type as stream */
        return Option.of(type)
                .map(ReflectUtils::getComponentType)
                .toSet()
                .flatMap(current -> Option.of(current)
                        .filter(WildcardType.class::isInstance)
                        .map(WildcardType.class::cast)
                        .map(next -> Stream.concat(
                                Stream.of(next.getLowerBounds()),
                                Stream.of(next.getUpperBounds()))
                                .map(ReflectUtils::getComponentType))
                        .getOrElse(Stream.of(current)));

    }

    /**
     * Method that extracts the component class of a class.
     *
     * @param clazz class to extract the component type
     * @return the component class of a class
     */
    public static Class<?> getComponentClass(final Class<?> clazz) {

        /* Obtain the stream conformed by the class and all its components,
         * then take the classes while non is not found and finally return the last class or null */
        return Stream.<Class<?>>iterate(clazz, Class::getComponentType)
                .takeWhile(Objects::nonNull)
                .lastOption()
                .getOrNull();

    }

    /**
     * Method that extracts the component type of an array.
     *
     * @param type type to extract the component type
     * @return the component type of an array
     */
    public static Type getComponentType(final Type type) {

        /* Check if the type is a generic array and extract its component,
         * if is not a generic array try to extract the array from the class.
         * Finally call the function recursively and return the result or the current type */
        return Option.of(type)
                .filter(GenericArrayType.class::isInstance)
                .map(GenericArrayType.class::cast)
                .map(GenericArrayType::getGenericComponentType)
                .filter(Objects::nonNull)
                .orElse(Option.of(type)
                        .filter(Class.class::isInstance)
                        .map(Class.class::cast)
                        .map(Class::getComponentType)
                        .filter(Objects::nonNull))
                .map(ReflectUtils::getComponentType)
                .getOrElse(type);

    }

    /**
     * Method that extracts all super classes of the given class.
     *
     * @param clazz class to extract supers
     * @return the set of super classes
     */
    public static Set<Class<?>> getSuperClasses(final Class<?> clazz) {

        /* Return super classes with empty hashset as navigated set */
        return getSuperClasses(clazz, new java.util.HashSet<>());

    }

    /**
     * Method that extracts all super classes of the given class.
     *
     * @param clazz            class to extract supers
     * @param globalNavigated  global set to avoid processes already processed types
     * @return the set of super classes
     */
    public static Set<Class<?>> getSuperClasses(final Class<?> clazz,
                                                final java.util.Set<Type> globalNavigated) {


        /* Add current class to navigated set */
        globalNavigated.add(clazz);

        /* Get super interfaces and classes */
        final Set<Class<?>> supers = HashSet.of(clazz.getInterfaces())
                .add(clazz.getSuperclass())
                .filter(Objects::nonNull)
                .filter(Predicates.noneOf(Object.class::equals));

        /* Return the recursive */
        return supers.flatMap(current -> getSuperClasses(current, globalNavigated)).add(clazz);

    }

    /**
     * Method that extracts the types of the fields.
     *
     * @param clazz           class to extract the field types
     * @param checkPublicOnly indicates if only public fields and methods
     * @param checkStatic     indicates if static field and methods should be checked
     * @return extracted types of the fields
     */
    public static Set<Type> getFieldTypesFrom(final Class<?> clazz,
                                              final Boolean checkPublicOnly,
                                              final Boolean checkStatic) {

        /* Return all fields types */
        return HashSet.of(clazz.getDeclaredFields())
                .filter(field -> (!checkPublicOnly || (field.getModifiers() & Modifier.PUBLIC) != 0)
                        && (!checkStatic || (field.getModifiers() & Modifier.STATIC) == 0))
                .map(Field::getGenericType);

    }

    /**
     * Method that extracts the types of the methods depending on the method property enum.
     *
     * @param clazz           class to extract the method types
     * @param methodProperty  indicates if all methods, getters, setters or both properties should be checked
     * @param checkPublicOnly indicates if only public fields and methods
     * @param checkStatic     indicates if static field and methods should be checked
     * @return extracted types of the specified methods
     */
    public static Set<Type> getMethodTypesFrom(final Class<?> clazz,
                                               final MethodProperty methodProperty,
                                               final Boolean checkPublicOnly,
                                               final Boolean checkStatic) {

        /* Check if all methods should be returned */
        if (MethodProperty.ALL.equals(methodProperty)) {

            /* Return all method types */
            return HashSet.of(clazz.getDeclaredMethods())
                    .filter(method -> (!checkPublicOnly || (method.getModifiers() & Modifier.PUBLIC) != 0)
                            && (!checkStatic || (method.getModifiers() & Modifier.STATIC) == 0))
                    .flatMap(method -> Stream.of(method.getGenericParameterTypes())
                            .append(method.getGenericReturnType())
                            .filter(Objects::nonNull));

        } else {

            /* Get declared get methods and check if is public when flag is enabled */
            final Set<Method> gets = HashSet.of(clazz.getDeclaredMethods())
                    .filter(method -> method.getParameters().length == 0
                            && method.getName().matches("^get.+$")
                            && method.getReturnType() != void.class
                            && (!checkPublicOnly || (method.getModifiers() & Modifier.PUBLIC) != 0)
                            && (!checkStatic || (method.getModifiers() & Modifier.STATIC) == 0));

            /* Get declared set methods and check if is public when flag is enabled */
            final Set<Method> sets = HashSet.of(clazz.getDeclaredMethods())
                    .filter(method -> method.getParameters().length == 1
                            && method.getName().matches("^set.+$")
                            && method.getReturnType() == void.class
                            && (!checkPublicOnly || (method.getModifiers() & Modifier.PUBLIC) != 0)
                            && (!checkStatic || (method.getModifiers() & Modifier.STATIC) == 0));

            /* Return the methods depending on the method property */
            return API.Match(methodProperty).of(

                    API.Case(API.$(MethodProperty.GETTER), () -> gets.map(Method::getGenericReturnType)),
                    API.Case(API.$(MethodProperty.SETTER), () -> sets.map(method -> method.getGenericParameterTypes()[0])),
                    API.Case(API.$(MethodProperty.BOTH), () -> {

                        /* To property function that trasforms the get or set name to propety name */
                        final Function2<String, Method, String> toProperty = (name, method) ->
                                WordUtils.uncapitalize(method.getName()
                                        .replaceAll(String.format("^%s", name), ""));

                        /* Maps identifier by property and with getter or setter as values */
                        final Map<String, Method> propToGet = gets.toMap(toProperty.apply("get"), m -> m);
                        final Map<String, Method> propToSet = gets.toMap(toProperty.apply("set"), m -> m);

                        /* Return the getter and setter when both matches */
                        return propToGet.keySet().flatMap(key -> propToGet.get(key)
                                .flatMap(get -> propToSet.get(key)
                                        .flatMap(set -> {

                                            /* Check if the parameter and return type matches */
                                            if (Objects.equals(set.getParameterTypes()[0],
                                                    get.getReturnType())) {
                                                return Option.of(set.getGenericParameterTypes()[0]);
                                            } else {
                                                return Option.none();
                                            }

                                        })));

                    }),
                    API.Case(API.$(), HashSet::empty)

            );

        }

    }

}
