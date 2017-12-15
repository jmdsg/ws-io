package com.fiberg.wsio.processor;

import com.fiberg.wsio.annotation.*;
import com.fiberg.wsio.util.WsIOUtil;
import com.squareup.javapoet.*;
import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.*;
import io.vavr.control.Option;
import org.apache.commons.lang3.ObjectUtils;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.Objects;

/**
 * Class of utilities with herlper methods over classes derivated from
 * {@link Element} and {@link com.squareup.javapoet.TypeName}
 */
final class WsIOUtils {

	/**
	 * Private empty contructor
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

	static List<TypeElement> extractTypes(TypeElement element) {
		if (element.getEnclosingElement() instanceof TypeElement) {
			TypeElement type = (TypeElement) element.getEnclosingElement();
			return extractTypes(type).append(type);
		} else {
			return List.empty();
		}
	}

	static PackageElement extractPackage(TypeElement element) {
		if (element != null) {
			if (element.getEnclosingElement() instanceof PackageElement) {
				return (PackageElement) element.getEnclosingElement();
			} else if (element.getEnclosingElement() instanceof TypeElement){
				return extractPackage((TypeElement) element.getEnclosingElement());
			}
		}
		throw new IllegalArgumentException(String.format("The type element %s is not valid", element));
	}
	
	static WsIOInfo extractInfo(TypeElement type, ExecutableElement executable) {

		WebMethod webMethod = executable.getAnnotation(WebMethod.class);
		XmlElementWrapper elementWrapper = executable.getAnnotation(XmlElementWrapper.class);
		WebResult webResult = executable.getAnnotation(WebResult.class);

		List<TypeMirror> parameterTypes = Stream.ofAll(executable.getParameters())
				.map(VariableElement::asType)
				.toList();

		List<String> parameterNames = Stream.ofAll(executable.getParameters())
				.map(variable -> variable.getAnnotation(WebParam.class))
				.map(webParam -> webParam != null ? webParam.name() : null)
				.toList();

		List<String> parameterNameSpaces = Stream.ofAll(executable.getParameters())
				.map(variable -> variable.getAnnotation(WebParam.class))
				.map(webParam -> webParam != null ? webParam.targetNamespace() : null)
				.toList();

		Set<WsIOWrapper> wrappers = HashSet.empty();

		WsIOUseTime typeTimeWrapper = type.getAnnotation(WsIOUseTime.class);
		WsIOUseTime methodTimeWrapper = executable.getAnnotation(WsIOUseTime.class);
		WsIOIgnoreTime ignoreTimeWrapper = executable.getAnnotation(WsIOIgnoreTime.class);
		if ((typeTimeWrapper != null || methodTimeWrapper != null) && ignoreTimeWrapper == null) {
			wrappers = wrappers.add(WsIOWrapper.TIME_WRAPPER);
		}

		WsIOUseState typeStateWrapper = type.getAnnotation(WsIOUseState.class);
		WsIOUseState methodStateWrapper = executable.getAnnotation(WsIOUseState.class);
		WsIOIgnoreState ignoreStateWrapper = executable.getAnnotation(WsIOIgnoreState.class);
		if ((typeStateWrapper != null || methodStateWrapper != null) && ignoreStateWrapper == null) {
			wrappers = wrappers.add(WsIOWrapper.STATE_WRAPPER);
		}

		String returnName = webResult != null ? webResult.name() : "";
		String returnNameSpace = webResult != null ? webResult.targetNamespace() : "";
		TypeMirror returnType = executable.getReturnType();

		String wrapperName = elementWrapper != null ? elementWrapper.name() : "";
		String wrapperNameSpace = elementWrapper != null ? elementWrapper.namespace() : "";

		String operationName = webMethod != null ? webMethod.operationName() : "";

		String methodName = executable.getSimpleName().toString();

		return WsIOInfo.of(methodName, operationName, executable,
				parameterNames, parameterNameSpaces, parameterTypes, returnName, returnNameSpace, returnType,
				wrapperName, wrapperNameSpace, wrappers);

	}

	static TypeElement extractDelegatorType(TypeElement element) {

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

	static boolean implementsSuperType(TypeElement typeClass, TypeElement typeInterface) {

		List<TypeElement> supers = List.of(typeClass.getSuperclass()).appendAll(typeClass.getInterfaces())
				.filter(Objects::nonNull)
				.filter(DeclaredType.class::isInstance)
				.map(DeclaredType.class::cast)
				.map(DeclaredType::asElement)
				.filter(TypeElement.class::isInstance)
				.map(TypeElement.class::cast);

		boolean contains = supers.contains(typeInterface);

		for (TypeElement type : supers) {
			contains |= implementsSuperType(type, typeInterface);
		}

		return contains;

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

}
