package com.fiberg.wsio.processor;

import com.fiberg.wsio.util.WsIOUtil;
import com.squareup.javapoet.*;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.*;
import io.vavr.control.Option;
import javassist.CtClass;
import javassist.CtMethod;
import org.apache.commons.lang3.ObjectUtils;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.*;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * Class of utilities with herlper methods over classes derivated from
 * {@link Element} and {@link com.squareup.javapoet.TypeName}
 */
final class WsIOUtils {

	/**
	 * Private empty contructor.
	 */
	private WsIOUtils() {}

	/** List of priorities levels */
	private static final List<WsIOLevel> priotities = List.of(
			WsIOLevel.NONE,
			WsIOLevel.LOCAL,
			WsIOLevel.CLASS_INTERNAL,
			WsIOLevel.INTERFACE_INTERNAL,
			WsIOLevel.CLASS_EXTERNAL,
			WsIOLevel.INTERFACE_EXTERNAL
	);

	/**
	 * Method that extract recursively all enclosing type elements of a type element.
	 *
	 * @param element type element
	 * @return empty list when a type element is a root element or a list with all recursive
	 * enclosing type elements when is not. The first element is the root element and the last is the parent pf the element.
	 */
	static List<TypeElement> extractTypes(TypeElement element) {

		/* Check enclosing element is instance of type element (is not root element) */
		if (element.getEnclosingElement() instanceof TypeElement) {

			/* Extract the enclosing element and return the recursive call with current type appended */
			TypeElement type = (TypeElement) element.getEnclosingElement();
			return extractTypes(type).append(type);

		} else {

			/* Return an empty list when type element is root element */
			return List.empty();

		}
	}

	/**
	 * Method that extracts the package element of a type element.
	 *
	 * @param element type element
	 * @return package element of a type element
	 * @throws IllegalStateException when enclosing element type is unknown
	 */
	static PackageElement extractPackage(TypeElement element) throws IllegalStateException {
		Objects.requireNonNull(element, "element is null");

		/* Check the instance type of the enclosing element */
		if (element.getEnclosingElement() instanceof PackageElement) {

			/* Return the enclosing package element */
			return (PackageElement) element.getEnclosingElement();

		} else if (element.getEnclosingElement() instanceof TypeElement){

			/* Return the recursive call the function */
			return extractPackage((TypeElement) element.getEnclosingElement());

		} else {

			/* Throw exception when enclosing element type is unknown */
			throw new IllegalStateException(String.format("Unknow type of the element %s", element));
		}

	}

	/**
	 * Method that extracts the method and additionals info.
	 *
	 * @param executable excecutable element
	 * @param additional info of the additional annotations
	 * @return object containing all the info required for the generation of a wrapper element.
	 */
	static WsIOInfo extractInfo(ExecutableElement executable, WsIOAdditional additional) {

		/* Extract the executable annotations */
		WebMethod webMethod = executable.getAnnotation(WebMethod.class);
		XmlElementWrapper elementWrapper = executable.getAnnotation(XmlElementWrapper.class);
		WebResult webResult = executable.getAnnotation(WebResult.class);

		/* Get the parameters of the executable */
		List<TypeMirror> parameterTypes = Stream.ofAll(executable.getParameters())
				.map(VariableElement::asType)
				.toList();

		/* Get the parameter names of the executable */
		List<String> parameterNames = Stream.ofAll(executable.getParameters())
				.map(variable -> variable.getAnnotation(WebParam.class))
				.map(webParam -> webParam != null ? webParam.name() : null)
				.toList();

		/* Get the parameter name spaces of the executable */
		List<String> parameterNameSpaces = Stream.ofAll(executable.getParameters())
				.map(variable -> variable.getAnnotation(WebParam.class))
				.map(webParam -> webParam != null ? webParam.targetNamespace() : null)
				.toList();

		/* Extract the use annotations that are not ignored */
		Set<WsIOWrapper> wrappers = HashSet.of(WsIOWrapper.values())
				.flatMap(wrapper -> additional.getAnnotated().get(wrapper).filter(Objects::nonNull)
						.filter(annotation -> additional.getIgnored().get(wrapper).filter(Objects::nonNull).isEmpty())
						.map(annotation -> wrapper));

		/* Get return name, namespace and type */
		String returnName = webResult != null ? webResult.name() : "";
		String returnNameSpace = webResult != null ? webResult.targetNamespace() : "";
		TypeMirror returnType = executable.getReturnType();

		/* Get wrapper name nad namespace type */
		String wrapperName = elementWrapper != null ? elementWrapper.name() : "";
		String wrapperNameSpace = elementWrapper != null ? elementWrapper.namespace() : "";

		/* Get operation and method names */
		String operationName = webMethod != null ? webMethod.operationName() : "";
		String methodName = executable.getSimpleName().toString();

		/* Return the info */
		return WsIOInfo.of(methodName, operationName, executable,
				parameterNames, parameterNameSpaces, parameterTypes, returnName, returnNameSpace, returnType,
				wrapperName, wrapperNameSpace, wrappers);

	}

	/**
	 * Method that extracts the delegator type of a delegate or clone class.
	 *
	 * @param element type element ot extract the delegator
	 * @return delegator type of a delegate or clone class
	 */
	static TypeElement extractDelegatorType(TypeElement element) {

		/* Check if the element has a method named as getter delegator
		 * and return its type element or null when not found*/
		return List.ofAll(element.getEnclosedElements())
				.filter(elem -> ElementKind.METHOD.equals(elem.getKind()))
				.filter(method -> WsIOConstant.GET_DELEGATOR.equals(method.getSimpleName().toString()))
				.filter(ExecutableElement.class::isInstance)
				.map(ExecutableElement.class::cast)
				.filter(executable -> executable.getParameters().size() == 0)
				.map(ExecutableElement::getReturnType)
				.filter(DeclaredType.class::isInstance)
				.map(DeclaredType.class::cast)
				.map(DeclaredType::asElement)
				.filter(TypeElement.class::isInstance)
				.map(TypeElement.class::cast)
				.headOption()
				.getOrNull();

	}

	/**
	 * Method that checks if an element implements a interface or extends a class
	 *
	 * @param subclass class to check if is sub class of the super class
	 * @param superclass super class
	 * @return {@code true} if an element implements a interface or extends a class {@code false} otherwise.
	 */
	static boolean implementsSuperType(TypeElement subclass, TypeElement superclass) {

		/* List of all super classes and interfaces of the current class */
		List<TypeElement> supers = List.of(subclass.getSuperclass()).appendAll(subclass.getInterfaces())
				.filter(Objects::nonNull)
				.filter(DeclaredType.class::isInstance)
				.map(DeclaredType.class::cast)
				.map(DeclaredType::asElement)
				.filter(TypeElement.class::isInstance)
				.map(TypeElement.class::cast);

		/* Check if super classes contain the searched super class */
		if (supers.contains(superclass)) {

			/* Return true when the super class is present */
			return true;

		} else {

			/* Iterate for each super class */
			for (TypeElement type : supers) {

				/* Check if one of the super classes has an inheritate superclass */
				if (implementsSuperType(type, superclass)) {
					return true;
				}

			}

		}

		/* Return false by default */
		return false;

	}

	/**
	 * Method that returns the unwild type.
	 *
	 * @param typeMirror type mirror
	 * @return the unwild type.
	 */
	static TypeMirror getUnwildType(TypeMirror typeMirror) {

		/* Retur the unwild type mirror only */
		return getUnwildTypeWithBounds(typeMirror)._1();

	}

	/**
	 * Method that returns a tuple containing the unwild type and the type mirror.
	 *
	 * @param typeMirror type mirror
	 * @return a tuple containing the unwild type and the type mirror.
	 */
	static Tuple2<TypeMirror, WsIOWild> getUnwildTypeWithBounds(TypeMirror typeMirror) {

		/* Check if type mirror is not null and is instance of wildcard type */
		if (typeMirror != null && typeMirror instanceof WildcardType) {

			/* Check the type of wildcard */
			if (((WildcardType) typeMirror).getExtendsBound() != null) {

				/* Return the extends wildcard */
				return Tuple.of(((WildcardType) typeMirror).getExtendsBound(), WsIOWild.EXTENDS);

			} else if (((WildcardType) typeMirror).getSuperBound() != null) {

				/* Return the super wildcard */
				return Tuple.of(((WildcardType) typeMirror).getSuperBound(), WsIOWild.SUPER);

			}

		}

		/* Return none wildcard */
		return Tuple.of(typeMirror, WsIOWild.NONE);

	}

	/**
	 * Method that returns a list with the generic types.
	 *
	 * @param referenceType reference type to extract the generics
	 * @return a list with the generics of the declared types
	 */
	private static List<TypeMirror> getGenericTypes(ReferenceType referenceType) {

		/* Return the list of type arguments */
		if (referenceType instanceof DeclaredType) {

			/* Return the type arguments of the declared type */
			return List.ofAll(((DeclaredType) referenceType).getTypeArguments());

		} else {

			/* Return empty list by default */
			return List.empty();

		}

	}

	/**
	 * Method that returns a list with the cleared generic types.
	 * Cleared types include wildcard and array types.
	 *
	 * @param referenceType reference type to extract the generics
	 * @return a list with the generics of the cleared declared types
	 */
	static List<ReferenceType> getClearedGenericTypes(ReferenceType referenceType) {

		/* Return the type elements of the generic list */
		return getGenericTypes(referenceType)
				.flatMap(type -> {

					/* Check the type of the mirror */
					if (type instanceof WildcardType) {

						/* Return the defined super or extend bound */
						return Option.none()
								.orElse(Option.of(((WildcardType) type).getExtendsBound()))
								.orElse(Option.of(((WildcardType) type).getSuperBound()));

					} else if (type instanceof ReferenceType) {

						/* Return the element when the type is a reference type */
						return Option.of(type);

					}

					/* Return empty option */
					return Option.none();

				})
				.filter(ReferenceType.class::isInstance)
				.map(ReferenceType.class::cast);

	}

	/**
	 * Method that returns a list with the cleared generic types.
	 * Cleared types include wildcard and array types.
	 *
	 * @param typeName type name to extract the generics
	 * @return a list with the generics of the cleared declared types
	 */
	static List<TypeName> getClearedGenericTypes(TypeName typeName) {

		/* Check type is parameterized */
		if (typeName instanceof ParameterizedTypeName) {

			/* Get generic arguments */
			List<TypeName> generics = List.ofAll(((ParameterizedTypeName) typeName).typeArguments);

			/* Return the type elements of the generic list */
			return generics.flatMap(type -> {

				/* Check the type of the mirror */
				if (type instanceof WildcardTypeName) {

						/* Return the defined super or extend bound */
					return Option.<TypeName>none()
							.orElse(Option.of(((WildcardTypeName) type).upperBounds)
									.flatMap(list -> list.size() > 0 ? Option.of(list.get(0)) : Option.none()))
							.orElse(Option.of(((WildcardTypeName) type).lowerBounds)
									.flatMap(list -> list.size() > 0 ? Option.of(list.get(0)) : Option.none()));

				} else {

					/* Return type by default */
					return Option.of(type);

				}

			});

		} else {

			/* Return empty list */
			return List.empty();

		}

	}

	/**
	 * Method that generates a class name from the fully qualified class name
	 *
	 * @param fullClassName full class name
	 * @return class name object
	 */
	static ClassName getClassName(String fullClassName) {

		/* Extract package and class name */
		String packageName = fullClassName.lastIndexOf(".") >= 0 ?
				fullClassName.substring(0, fullClassName.lastIndexOf(".")) : "";
		String className = fullClassName.substring(Math.max(fullClassName.lastIndexOf(".") + 1, 0));

		/* Return the class name object */
		return ClassName.get(packageName, className);

	}

	/**
	 * Method that returns the full inner name.
	 *
	 * @param element type element
	 * @return full inner name of the class
	 */
	static String getFullSimpleInnerName(TypeElement element) {

		/* Extract current simple name and inner name */
		String simpleName = element.getSimpleName().toString();
		String innerName = getInnerName(element);

		/* Return the inner name and the simple name of the class */
		return WsIOUtil.addPrefix(simpleName, innerName);

	}

	/**
	 * Method that returns the full inner name.
	 *
	 * @param element type element
	 * @return full inner name of the class
	 */
	private static String getInnerName(TypeElement element) {

		/* Extract full name from inner classes, it does not return the current class name */
		return WsIOUtils.extractTypes(element)
				.map(TypeElement::getSimpleName)
				.map(Name::toString)
				.mkString();

	}

	/**
	 * Method that checks if a executable is a getter.
	 *
	 * @param method executable method
	 * @return {@code true} if the method is a getter {@code false} otherwise
	 */
	static boolean isGetter(ExecutableElement method) {
		return method.getSimpleName().toString().matches("^get.+")
				&& method.getModifiers().contains(Modifier.PUBLIC)
				&& method.getParameters().size() == 0
				&& !(method.getReturnType() instanceof NoType);
	}

	/**
	 * Method that checks if a executable is a setter.
	 *
	 * @param method executable method
	 * @return {@code true} if the method is a setter {@code false} otherwise
	 */
	static boolean isSetter(ExecutableElement method) {
		return method.getSimpleName().toString().matches("^set.+")
				&& method.getModifiers().contains(Modifier.PUBLIC)
				&& method.getParameters().size() == 1
				&& method.getReturnType() instanceof NoType;
	}

	/**
	 * Method that searches the maximun priority level of a method name.
	 *
	 * @param executables map with all executables to search
	 * @param methodName name of the method to search
	 * @return the maximun priority level of a method name
	 */
	static WsIOLevel getPriorityLevelByName(Map<WsIOLevel, Map<String, ExecutableElement>> executables,
	                                         String methodName) {
		return getPriorityLevelByNameFromExclusive(executables, methodName, WsIOLevel.NONE);
	}

	/**
	 * Method that searches the maximun priority level of a method name staring in the specified level.
	 *
	 * @param executables map with all executables to search
	 * @param methodName name of the method to search
	 * @param from min priority level to search
	 * @return the maximun priority level of a method name
	 */
	static WsIOLevel getPriorityLevelByNameFromExclusive(Map<WsIOLevel, Map<String, ExecutableElement>> executables,
	                                                      String methodName, WsIOLevel from) {

		/* Initialize from level in case of null */
		WsIOLevel fromLevel = ObjectUtils.firstNonNull(from, WsIOLevel.NONE);

		/* Check each in level from the specified one if method is defined */
		for (WsIOLevel level : priotities.dropUntil(fromLevel::equals).drop(1)) {
			if (executables.getOrElse(level, HashMap.empty()).get(methodName).isDefined()) {
				return level;
			}
		}

		/* Return none if not match found */
		return WsIOLevel.NONE;

	}

	/**
	 * Method that returns recursive executable elements by level.
	 *
	 * @param element element to process
	 * @return recursive executable elements by level
	 */
	static Map<Integer, Set<ExecutableElement>> getRecursiveExecutableElementsWithLevel(TypeElement element) {

		/* Return recursive executable elements */
		return getRecursiveTypeElementsWithLevel(element)
				.mapValues(set -> set.flatMap(TypeElement::getEnclosedElements)
						.filter(ExecutableElement.class::isInstance)
						.map(ExecutableElement.class::cast));

	}

	/**
	 * Method that returns recursive type elements by level.
	 *
	 * @param element element to process
	 * @return recursive type elements by level
	 */
	private static Map<Integer, Set<TypeElement>> getRecursiveTypeElementsWithLevel(TypeElement element) {

		/* Return recursive elements from level 1 */
		return getRecursiveTypeElementsWithLevel(element, HashSet.empty(), 1)
				.put(0, HashSet.of(element));

	}

	/**
	 * Method that returns recursive type elements by level.
	 *
	 * @param element element to process
	 * @param navigated set of navigated elements to avoid cycles
	 * @param level current level of the type
	 * @return recursive type elements by level
	 */
	private static Map<Integer, Set<TypeElement>> getRecursiveTypeElementsWithLevel(TypeElement element,
	                                                                                Set<String> navigated,
	                                                                                int level) {

		/* Extract current element name and add it to the navigated set */
		String name = element.getQualifiedName().toString();
		Set<String> currents = navigated.add(name);

		/* Get super class and super interfaces */
		TypeMirror superType = element.getSuperclass();
		Set<TypeMirror> interTypes = HashSet.ofAll(element.getInterfaces());

		/* Get all types instance of type element */
		Set<TypeElement> allTypes = interTypes.add(superType)
				.filter(Objects::nonNull)
				.filter(DeclaredType.class::isInstance)
				.map(DeclaredType.class::cast)
				.map(DeclaredType::asElement)
				.filter(TypeElement.class::isInstance)
				.map(TypeElement.class::cast);

		/* Create the map with current elements and level */
		Map<Integer, Set<TypeElement>> recursiveTypeElements = HashMap.of(level, allTypes);

		/* Recursive call for each element and merge the map, when a collision occurs join the two sets */
		return allTypes.map(type -> getRecursiveTypeElementsWithLevel(type, currents, level + 1))
				.fold(recursiveTypeElements, (map1, map2) -> map1.merge(map2, Set::addAll));

	}

	/**
	 * Method that extracts the info of a reference type.
	 *
	 * @param reference reference type
	 * @return tuple containing the component type of the array and the count with dimension level
	 */
	static Tuple2<ReferenceType, Integer> getArrayInfo(ReferenceType reference) {

		/* Variable to indicate if is an array or not */
		int arrayCount = 0;

		/* Check the type of the reference */
		if (reference instanceof ArrayType) {

			/* Get nested components and increase array count */
			TypeMirror component = reference;
			while (component instanceof ArrayType) {
				component = ((ArrayType) component).getComponentType();
				arrayCount++;
			}

			/* Process array type */
			if (component instanceof DeclaredType) {

				/* Return declared type and array count */
				return Tuple.of((DeclaredType) component, arrayCount);

			}

		}

		/* Return declared type and zero in array count */
		return Tuple.of(reference, 0);

	}

	/**
	 * Method that creates a array type name of the specified dimension with reference.
	 *
	 * @param typeName type name
	 * @param dimension dimension of the array
	 * @return array type name of the specified dimension with reference
	 */
	static TypeName getArrayType(TypeName typeName,
	                             int dimension) {

		/* Return the array of specified dimension */
		return Stream.iterate(typeName, ArrayTypeName::of)
				.get(dimension);

	}

	/**
	 * Method used to extract the annotation in a ct class.
	 *
	 * @param ctClass ct class to extract the annotation
	 * @param annotation annotation class
	 * @param <T> type argument of the annotation class
	 * @return annotation extracted from ct class
	 */
	@SuppressWarnings("unchecked")
	static <T extends Annotation> T extractAnnotation(CtClass ctClass , Class<T> annotation) {

		/* Return the annotation or null */
		return Option.of(ctClass).toTry()
				.mapTry(ct -> (T) ct.getAnnotation(annotation))
				.getOrNull();

	}

	/**
	 * Method used to extract the annotation in a ct method.
	 *
	 * @param ctMethod ct method to extract the annotation
	 * @param annotation annotation class
	 * @param <T> type argument of the annotation method
	 * @return annotation extracted from ct method
	 */
	@SuppressWarnings("unchecked")
	static <T extends Annotation> T extractAnnotation(CtMethod ctMethod , Class<T> annotation) {

		/* Return the annotation or null */
		return Option.of(ctMethod).toTry()
				.mapTry(ct -> (T) ct.getAnnotation(annotation))
				.getOrNull();

	}

}
