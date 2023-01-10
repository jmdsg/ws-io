package com.fiberg.wsio.processor;

import com.fiberg.wsio.annotation.*;
import com.fiberg.wsio.util.WsIOUtil;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.*;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.*;
import io.vavr.control.Option;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.*;
import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * Class of utilities with helper methods over classes derived from
 * {@link Element} and {@link com.squareup.javapoet.TypeName}
 */
final class WsIOUtils {

	/**
	 * Private empty constructor.
	 */
	private WsIOUtils() {}

	/** List of priorities levels */
	private static final List<WsIOLevel> priorities = List.of(
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
			throw new IllegalStateException(String.format("Unknown type of the element %s", element));
		}

	}

	/**
	 * Method that extracts the method and additional info.
	 *
	 * @param executable executable element
	 * @param descriptor object with all element annotations
	 * @return object containing all the info required for the generation of a wrapper element.
	 */
	static WsIOInfo extractInfo(ExecutableElement executable, WsIODescriptor descriptor) {

		/* Extract the executable annotations */
		WebMethod webMethod = executable.getAnnotation(WebMethod.class);
		XmlElementWrapper elementWrapper = executable.getAnnotation(XmlElementWrapper.class);
		WebResult webResult = executable.getAnnotation(WebResult.class);

		/* Get the parameters of the executable */
		List<TypeMirror> parameterTypes = Stream.ofAll(executable.getParameters())
				.map(VariableElement::asType)
				.toList();

		/* Get the external parameter names of the executable */
		List<String> parameterExternalNames = Stream.ofAll(executable.getParameters())
				.map(variable -> variable.getAnnotation(WebParam.class))
				.map(webParam -> webParam != null ? webParam.name() : null)
				.toList();

		/* Get the external parameter name spaces of the executable */
		List<String> parameterExternalNameSpaces = Stream.ofAll(executable.getParameters())
				.map(variable -> variable.getAnnotation(WebParam.class))
				.map(webParam -> webParam != null ? webParam.targetNamespace() : null)
				.toList();

		/* Get the internal parameter required of the executable */
		List<Boolean> parameterRequired = Stream.ofAll(executable.getParameters())
				.map(variable -> variable.getAnnotation(WsIOParam.class))
				.map(param -> param != null ? param.required() : null)
				.toList();

		/* Get the internal parameter nillable of the executable */
		List<Boolean> parameterNillable = Stream.ofAll(executable.getParameters())
				.map(variable -> variable.getAnnotation(WsIOParam.class))
				.map(param -> param != null ? param.nillable() : null)
				.toList();

		/* Get the internal parameter names of the executable */
		List<String> parameterInternalNames = Stream.ofAll(executable.getParameters())
				.map(variable -> variable.getAnnotation(WsIOParam.class))
				.map(param -> param != null ? param.name() : null)
				.toList();

		/* Get the internal parameter name spaces of the executable */
		List<String> parameterInternalNameSpaces = Stream.ofAll(executable.getParameters())
				.map(variable -> variable.getAnnotation(WsIOParam.class))
				.map(param -> param != null ? param.targetNamespace() : null)
				.toList();

		/* Get the parameter names of the executable, internal has priority over external */
		List<String> parameterNames = Stream.range(0, executable.getParameters().size())
				.map(index -> StringUtils.isNotBlank(parameterInternalNames.get(index)) ? parameterInternalNames.get(index) : parameterExternalNames.get(index))
				.toList();

		/* Get the parameter names of the executable, internal has priority over external */
		List<String> parameterNameSpaces = Stream.range(0, executable.getParameters().size())
				.map(index -> StringUtils.isNotBlank(parameterInternalNameSpaces.get(index)) ? parameterInternalNameSpaces.get(index) : parameterExternalNameSpaces.get(index))
				.toList();

		/* Get ws qualifier names */
		List<Tuple2<String, String>> parameterQualifiers = List.ofAll(executable.getParameters())
				.map(variable -> variable.getAnnotation(WsIOQualifier.class))
				.map(qualifier -> qualifier != null ? Tuple.of(qualifier.prefix(), qualifier.suffix()) : null);

		/* Get the parameter attributes of the executable */
		List<Boolean> parameterAttributes = Stream.ofAll(executable.getParameters())
				.map(variable -> variable.getAnnotation(WsIOAttribute.class))
				.map(attribute -> attribute != null ? true : null)
				.toList();

		/* Get the parameter wrappers of the executable */
		List<String> parameterWrappers = Stream.ofAll(executable.getParameters())
				.map(variable -> variable.getAnnotation(WsIOWrapper.class))
				.map(wrapper -> wrapper != null ? wrapper.inner() : null)
				.toList();

		/* Get the parameter wrapper required of the executable */
		List<Boolean> parameterWrapperRequired = Stream.ofAll(executable.getParameters())
				.map(variable -> variable.getAnnotation(WsIOWrapper.class))
				.map(wrapper -> wrapper != null ? wrapper.required() : null)
				.toList();

		/* Get the parameter wrapper nillable of the executable */
		List<Boolean> parameterWrapperNillable = Stream.ofAll(executable.getParameters())
				.map(variable -> variable.getAnnotation(WsIOWrapper.class))
				.map(wrapper -> wrapper != null ? wrapper.nillable() : null)
				.toList();

		/* Get the parameter adapters of the executable */
		List<String> parameterAdapters = Stream.ofAll(executable.getParameters())
				.map(variable -> getAnnotationStringValue(variable, WsIOAdapter.class, "value"))
				.toList();

		/* Extract the use annotations that are defined */
		Set<WsIOWrapped> wrappers = WsIOWrapped.ANNOTATIONS.filterValues(annotation ->
				descriptor.getSingle(annotation).isDefined())
				.keySet();

		/* Get return name, namespace, type and the prefix and suffix of ws qualifier */
		String returnName = webResult != null ? webResult.name() : "";
		String returnNameSpace = webResult != null ? webResult.targetNamespace() : "";
		TypeMirror returnType = executable.getReturnType();
		Tuple2<String, String> returnQualifier = Option.of(executable.getAnnotation(WsIOQualifier.class))
				.map(qualifier -> Tuple.of(qualifier.prefix(), qualifier.suffix()))
				.getOrNull();

		/* Get wrapper name nad namespace type */
		String wrapperName = elementWrapper != null ? elementWrapper.name() : "";
		String wrapperNameSpace = elementWrapper != null ? elementWrapper.namespace() : "";

		/* Get operation and method names */
		String operationName = webMethod != null ? webMethod.operationName() : "";
		String methodName = executable.getSimpleName().toString();

		/* Return the info */
		return WsIOInfo.of(methodName, operationName, executable,
				parameterNames, parameterNameSpaces, parameterTypes,
				parameterRequired, parameterNillable, parameterQualifiers, parameterAttributes,
				parameterWrappers, parameterWrapperRequired, parameterWrapperNillable, parameterAdapters,
				returnName, returnNameSpace, returnType, returnQualifier,
				wrapperName, wrapperNameSpace, wrappers);

	}

	/**
	 * Method that extracts the delegator type of the delegate or clone class.
	 *
	 * @param element type element ot extract the delegator
	 * @return delegator type of the delegate or clone class
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
	 * Method that extracts the delegate type of the metadata class.
	 *
	 * @param element type element ot extract the delegate
	 * @return delegate type of a metadata class
	 */
	static TypeElement extractMetadataType(TypeElement element) {

		/* Get all fields, check the name and type of the field.
		 * Check the generics size is of size 1 and return the type element or null */
		return Option.of(element)
				.toStream()
				.flatMap(TypeElement::getEnclosedElements)
				.filter(variableElement -> ElementKind.FIELD.equals(variableElement.getKind()))
				.filter(VariableElement.class::isInstance)
				.map(VariableElement.class::cast)
				.filter(variableElement -> WsIOConstant.METADATA_DELEGATE_FIELD
						.equals(variableElement.getSimpleName().toString()))
				.map(Element::asType)
				.filter(DeclaredType.class::isInstance)
				.map(DeclaredType.class::cast)
				.filter(declared -> "Class".equals(declared.asElement()
						.getSimpleName()
						.toString()))
				.map(DeclaredType::getTypeArguments)
				.filter(arguments -> arguments.size() == 1)
				.flatMap(list -> list)
				.filter(DeclaredType.class::isInstance)
				.map(DeclaredType.class::cast)
				.map(DeclaredType::asElement)
				.filter(TypeElement.class::isInstance)
				.map(TypeElement.class::cast)
				.getOrNull();

	}

	/**
	 * Method that checks if an element implements an interface or extends a class
	 *
	 * @param subclass class to check if is sub-class of the super class
	 * @param superclass super class
	 * @return {@code true} if an element implements an interface or extends a class {@code false} otherwise.
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

				/* Check if one of the super classes has an inherited superclass */
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

		/* Return the unwild type mirror only */
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
	 * @param mirror mirror type to extract the generics
	 * @return a list with the generics of the declared types
	 */
	private static List<TypeMirror> getGenericTypes(TypeMirror mirror) {

		/* Return the list of type arguments */
		if (mirror instanceof DeclaredType) {

			/* Return the type arguments of the declared type */
			return List.ofAll(((DeclaredType) mirror).getTypeArguments());

		} else {

			/* Return empty list by default */
			return List.empty();

		}

	}

	/**
	 * Method that returns a list with the cleared generic types.
	 * Cleared types include wildcard and array types.
	 *
	 * @param mirror mirror type to extract the generics
	 * @return a list with the generics of the cleared mirror types
	 */
	static List<TypeMirror> getClearedGenericTypes(TypeMirror mirror) {

		/* Return the type elements of the generic list */
		return getGenericTypes(mirror)
				.flatMap(type -> {

					/* Check the type of the mirror */
					if (type instanceof WildcardType) {

						/* Return the defined super or extend bound */
						return Option.<TypeMirror>none()
								.orElse(Option.of(((WildcardType) type).getExtendsBound()))
								.orElse(Option.of(((WildcardType) type).getSuperBound()));

					}

					/* Return empty option */
					return Option.of(type);

				});

	}

	/**
	 * Method that returns a list with the cleared generic types.
	 * Cleared types include wildcard and array types.
	 *
	 * @param typeName type name to extract the generics
	 * @return a list with the generics of the cleared type names
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
	 * Method that checks if an executable is a getter.
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
	 * Method that checks if an executable is a setter.
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
	 * Method that searches the maximum priority level of a method name.
	 *
	 * @param executables map with all executables to search
	 * @param methodName name of the method to search
	 * @return the maximum priority level of a method name
	 */
	static WsIOLevel getPriorityLevelByName(Map<WsIOLevel, Map<String, ExecutableElement>> executables,
	                                         String methodName) {
		return getPriorityLevelByNameFromExclusive(executables, methodName, WsIOLevel.NONE);
	}

	/**
	 * Method that searches the maximum priority level of a method name staring in the specified level.
	 *
	 * @param executables map with all executables to search
	 * @param methodName name of the method to search
	 * @param from min priority level to search
	 * @return the maximum priority level of a method name
	 */
	static WsIOLevel getPriorityLevelByNameFromExclusive(Map<WsIOLevel, Map<String, ExecutableElement>> executables,
	                                                      String methodName, WsIOLevel from) {

		/* Initialize from level in case of null */
		WsIOLevel fromLevel = ObjectUtils.firstNonNull(from, WsIOLevel.NONE);

		/* Check each in level from the specified one if method is defined */
		for (WsIOLevel level : priorities.dropUntil(fromLevel::equals).drop(1)) {
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
	 * Method that returns the string value of an annotation field.
	 *
	 * @param target element to process
	 * @param type class of the annotation
	 * @param field field of the annotation
	 * @return the string value of the annotation field
	 */
	static String getAnnotationStringValue(Element target, Class<? extends Annotation> type, String field) {
		AnnotationMirror annotationMirror = getAnnotationMirror(target, type);
		if (annotationMirror == null) {
			return null;
		}
		AnnotationValue annotationValue = getAnnotationValue(annotationMirror, field);
		if (annotationValue == null) {
			return null;
		} else {
			return annotationValue.toString();
		}
	}

	/**
	 * Method that returns the annotation mirror of an element.
	 *
	 * @param target element to process
	 * @param type class of the annotation
	 * @return the annotation mirror of an element
	 */
	private static AnnotationMirror getAnnotationMirror(Element target, Class<? extends Annotation> type) {
		String typeName = type.getName();
		for (AnnotationMirror annotationMirror : target.getAnnotationMirrors()) {
			if (annotationMirror.getAnnotationType().toString().equals(typeName)) {
				return annotationMirror;
			}
		}
		return null;
	}

	/**
	 * Method that returns the annotation value of an annotation field.
	 *
	 * @param annotationMirror annotation mirror to process
	 * @param field field of the annotation
	 * @return the annotation value of the annotation field
	 */
	private static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String field) {
		for (java.util.Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet() ) {
			if (entry.getKey().getSimpleName().toString().equals(field)) {
				return entry.getValue();
			}
		}
		return null;
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
	 * Method that extracts the info of a mirror type.
	 *
	 * @param mirror mirror type
	 * @return tuple containing the component type of the array and the count with dimension level
	 */
	static Tuple2<TypeMirror, Integer> getArrayInfo(TypeMirror mirror) {

		/* Variable to indicate if is an array or not */
		int arrayCount = 0;

		/* Check the type of the mirror */
		if (mirror instanceof ArrayType) {

			/* Get nested components and increase array count */
			TypeMirror component = mirror;
			while (component instanceof ArrayType) {
				component = ((ArrayType) component).getComponentType();
				arrayCount++;
			}

			/* Return mirror type and array count */
			return Tuple.of(component, arrayCount);

		}

		/* Return mirror type and zero in array count */
		return Tuple.of(mirror, 0);

	}

	/**
	 * Method that creates an array type name of the specified dimension with mirror.
	 *
	 * @param typeName type name
	 * @param dimension dimension of the array
	 * @return array type name of the specified dimension with mirror
	 */
	static TypeName getArrayType(TypeName typeName,
	                             int dimension) {

		/* Return the array of specified dimension */
		return Stream.iterate(typeName, ArrayTypeName::of)
				.get(dimension);

	}

	/**
	 * Method that checks if the string is upper case or not.
	 *
	 * @param name name to check
	 * @return {@code true} when the name is not null and all its characters
	 * are upper case or underscore.
	 */
	static boolean isUpperCase(String name) {
		return Objects.nonNull(name)
				&& name.matches("^([A-Z]+(_[A-Z]+)?)+$");
	}

	/**
	 * Method that checks if the string is snake case or not.
	 *
	 * @param name name to check
	 * @return {@code true} when the name is not null and is snake case.
	 */
	static boolean isSnakeCase(String name) {
		return Objects.nonNull(name)
				&& name.matches("^([a-z]+(_[a-z]+)?)+$");
	}

	/**
	 * Method that transforms a lower camel case name to a snake case.
	 *
	 * @param name name to transform
	 * @return the name transformed to snake case
	 */
	static String toSnakeCase(String name) {
		return Objects.nonNull(name) ?
				CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name) : null;
	}

	/**
	 * Method that transforms a lower camel case name to an upper case separated by underscores.
	 *
	 * @param name name to transform
	 * @return the name transformed to upper underscore case
	 */
	static String toUpperCase(String name) {
		return Objects.nonNull(name) ?
				CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, name) : null;
	}

	/**
	 * Method that transforms a field name to the destination depending on the field is static or not.
	 *
	 * @param field       field name
	 * @param isStatic    flag indicating if the field is static or not
	 * @param destination destination case
	 * @return the transformed name or {@code null} when the destination or field is {@code null}
	 * or the destination is unknown.
	 */
	static String transformField(String field, boolean isStatic, Case destination) {

		/* Check if the field is null or not */
		if (Objects.nonNull(field) && Objects.nonNull(destination)) {

			/* Switch the destination and transform the string depending on the destination */
			switch (destination) {

				case ORIGINAL:

					/* Return the original field */
					return field;

				case UPPER:

					/* Check if the field is static or not */
					if (isStatic && isUpperCase(field)) {

						/* Return the field when is upper case and destination is upper case */
						return field;

					} else {

						/* Transform to upper case and return */
						return toUpperCase(field);

					}

				case SNAKE:

					/* Check if the field is static or not, and if is snake or upper case */
					if (isStatic && isSnakeCase(field)) {

						/* Return the field when is upper case and destination is upper case */
						return field;

					} else if (isStatic && isUpperCase(field)) {

						/* Return the lower case field when is upper case */
						return field.toLowerCase();

					} else {

						/* Transform to upper case and return */
						return toUpperCase(field);

					}

				default:
					break;

			}

		}

		/* Return null when field or destination are null */
		return null;

	}

}
