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
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.*;
import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.stream.Collectors;

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
	 * Method that checks if executable info matches an executable method.
	 *
	 * @param info executable info to compare
	 * @param method executable method to compare
	 * @return the comparison between the executable info and the executable method.
	 */
	static boolean methodMatch(WsIOExecutableInfo info, ExecutableElement method) {
		if (method != null) {
			return method.getSimpleName().toString().equals(info.getMethodName())
					&& method.getParameters().stream()
					.map(VariableElement::asType)
					.collect(Collectors.toList())
					.equals(info.getParameterInfos()
							.map(WsIOParameterInfo::getTypeMirror))
					&& method.getReturnType().equals(info.getReturnType());
		}
		return false;
	}

	/**
	 * Method that extracts the method and additional info.
	 *
	 * @param executable executable element
	 * @param descriptor object with all element annotations
	 * @return object containing all the info required for the generation of a wrapper element.
	 */
	static WsIOExecutableInfo extractExecutableInfo(ExecutableElement executable, WsIODescriptor descriptor) {

		/* Get operation name */
		String operationName = WsIOUtils.getAnnotationTypeValue(executable, WebMethod.class, "operationName", String.class, "");

		/* Get wrapper name nad namespace type */
		String wrapperName = WsIOUtils.getAnnotationTypeValue(executable, XmlElementWrapper.class, "name", String.class, "");
		String wrapperNamespace = WsIOUtils.getAnnotationTypeValue(executable, XmlElementWrapper.class, "namespace", String.class, "");

		/* Get return name, namespace, type and the prefix and suffix of ws qualifier */
		String returnName = WsIOUtils.getAnnotationTypeValue(executable, WebResult.class, "name", String.class, "");
		String returnNamespace = WsIOUtils.getAnnotationTypeValue(executable, WebResult.class, "targetNamespace", String.class, "");
		TypeMirror returnType = executable.getReturnType();
		WsIOQualifierInfo returnQualifierInfo = Option.of(WsIOUtils.getAnnotationMirror(executable, WsIOQualifier.class))
				.map(mirror -> WsIOQualifierInfo.of(
						WsIOUtils.getAnnotationTypeValue(mirror, "prefix", String.class, ""),
						WsIOUtils.getAnnotationTypeValue(mirror, "suffix", String.class, "")))
				.getOrNull();

		/* Get operation and method names */
		String methodName = executable.getSimpleName().toString();

		List<WsIOParameterInfo> parameterInfos = List.ofAll(executable.getParameters())
				.map(argument -> {

					// Get the type mirror of the variable
					TypeMirror type = argument.asType();

					AnnotationMirror internalParamMirror = WsIOUtils.getAnnotationMirror(argument, WebParam.class);
					AnnotationMirror externalParamMirror = WsIOUtils.getAnnotationMirror(argument, WsIOParam.class);
					AnnotationMirror paramMirror = ObjectUtils.firstNonNull(
							internalParamMirror, externalParamMirror
					);

					/* Get the internal argument name of the param */
					String internalParamName = WsIOUtils.getAnnotationTypeValue(
							internalParamMirror, "name", String.class, "");

					/* Get the internal argument part name of the param */
					String internalParamPartName = WsIOUtils.getAnnotationTypeValue(
							internalParamMirror, "partName", String.class, "");

					/* Get the internal argument target name space of the param */
					String internalParamTargetNamespace = WsIOUtils.getAnnotationTypeValue(
							internalParamMirror, "targetNamespace", String.class, "");

					/* Get the internal argument mode of the param */
					String internalParamMode = WsIOUtils.getAnnotationLiteralValue(internalParamMirror, "mode");

					/* Get the internal argument header of the param */
					Boolean internalParamHeader = WsIOUtils.getAnnotationTypeValue(
							internalParamMirror, "header", Boolean.class);

					/* Get the external argument names of the param */
					String externalParamName = WsIOUtils.getAnnotationTypeValue(
							externalParamMirror, "name", String.class, "");

					/* Get the external argument part name of the param */
					String externalParamPartName = WsIOUtils.getAnnotationTypeValue(
							externalParamMirror, "partName", String.class, "");

					/* Get the external argument target name space of the param */
					String externalParamTargetNamespace = WsIOUtils.getAnnotationTypeValue(
							externalParamMirror, "targetNamespace", String.class, "");

					/* Get the external argument mode of the param */
					String externalParamMode = WsIOUtils.getAnnotationLiteralValue(externalParamMirror, "mode");

					/* Get the external argument header of the param */
					Boolean externalParamHeader = WsIOUtils.getAnnotationTypeValue(
							externalParamMirror, "header", Boolean.class);

					AnnotationMirror internalElementMirror = WsIOUtils.getAnnotationMirror(argument, XmlElement.class);
					AnnotationMirror externalElementMirror = WsIOUtils.getAnnotationMirror(argument, WsIOElement.class);
					AnnotationMirror elementMirror = ObjectUtils.firstNonNull(
							internalElementMirror, externalElementMirror
					);

					/* Get the internal argument name of the element */
					String internalElementName = WsIOUtils.getAnnotationTypeValue(
							internalElementMirror, "name", String.class, "");

					/* Get the internal argument namespace of the element */
					String internalElementNamespace = WsIOUtils.getAnnotationTypeValue(
							internalElementMirror, "namespace", String.class, "");

					/* Get the internal argument required of the element */
					Boolean internalElementRequired = WsIOUtils.getAnnotationTypeValue(
							internalElementMirror, "required", Boolean.class);

					/* Get the internal argument nillable of the element */
					Boolean internalElementNillable = WsIOUtils.getAnnotationTypeValue(
							internalElementMirror, "nillable", Boolean.class);

					/* Get the internal argument default value of the element */
					String internalElementDefaultValue = WsIOUtils.getAnnotationTypeValue(
							internalElementMirror, "defaultValue", String.class);

					/* Get the internal argument type of the element */
					String internalElementType = WsIOUtils.getAnnotationLiteralValue(internalElementMirror, "type");

					/* Get the external argument name of the element */
					String externalElementName = WsIOUtils.getAnnotationTypeValue(
							externalElementMirror, "name", String.class, "");

					/* Get the external argument namespace of the element */
					String externalElementNamespace = WsIOUtils.getAnnotationTypeValue(
							externalElementMirror, "namespace", String.class, "");

					/* Get the external argument required of the element */
					Boolean externalElementRequired = WsIOUtils.getAnnotationTypeValue(
							externalElementMirror, "required", Boolean.class);

					/* Get the external argument nillable of the element */
					Boolean externalElementNillable = WsIOUtils.getAnnotationTypeValue(
							externalElementMirror, "nillable", Boolean.class);

					/* Get the external argument default value of the element */
					String externalElementDefaultValue = WsIOUtils.getAnnotationTypeValue(
							externalElementMirror, "defaultValue", String.class);

					/* Get the external argument type of the element */
					String externalElementType = WsIOUtils.getAnnotationLiteralValue(externalElementMirror, "type");

					AnnotationMirror internalElementWrapperMirror = WsIOUtils.getAnnotationMirror(argument, XmlElementWrapper.class);
					AnnotationMirror externalElementWrapperMirror = WsIOUtils.getAnnotationMirror(argument, WsIOElementWrapper.class);
					AnnotationMirror elementWrapperMirror = ObjectUtils.firstNonNull(
							internalElementWrapperMirror, externalElementWrapperMirror
					);

					/* Get the internal argument name of the element wrapper */
					String internalElementWrapperName = WsIOUtils.getAnnotationTypeValue(
							internalElementWrapperMirror, "name", String.class, "");

					/* Get the internal argument namespace of the element wrapper */
					String internalElementWrapperNamespace = WsIOUtils.getAnnotationTypeValue(
							internalElementWrapperMirror, "namespace", String.class, "");

					/* Get the internal argument required of the element wrapper */
					Boolean internalElementWrapperRequired = WsIOUtils.getAnnotationTypeValue(
							internalElementWrapperMirror, "required", Boolean.class);

					/* Get the internal argument nillable of the element wrapper */
					Boolean internalElementWrapperNillable = WsIOUtils.getAnnotationTypeValue(
							internalElementWrapperMirror, "nillable", Boolean.class);

					/* Get the external argument name of the element wrapper */
					String externalElementWrapperName = WsIOUtils.getAnnotationTypeValue(
							externalElementWrapperMirror, "name", String.class, "");

					/* Get the external argument namespace of the element wrapper */
					String externalElementWrapperNamespace = WsIOUtils.getAnnotationTypeValue(
							externalElementWrapperMirror, "namespace", String.class, "");

					/* Get the external argument required of the element wrapper */
					Boolean externalElementWrapperRequired = WsIOUtils.getAnnotationTypeValue(
							externalElementWrapperMirror, "required", Boolean.class);

					/* Get the external argument nillable of the element wrapper */
					Boolean externalElementWrapperNillable = WsIOUtils.getAnnotationTypeValue(
							externalElementWrapperMirror, "nillable", Boolean.class);

					AnnotationMirror internalAttributeMirror = WsIOUtils.getAnnotationMirror(argument, XmlAttribute.class);
					AnnotationMirror externalAttributeMirror = WsIOUtils.getAnnotationMirror(argument, WsIOAttribute.class);
					AnnotationMirror attributeMirror = ObjectUtils.firstNonNull(
							internalAttributeMirror, externalAttributeMirror
					);

					/* Get the internal argument name of the attribute */
					String internalAttributeName = WsIOUtils.getAnnotationTypeValue(
							internalAttributeMirror, "name", String.class, "");

					/* Get the internal argument namespace of the attribute */
					String internalAttributeNamespace = WsIOUtils.getAnnotationTypeValue(
							internalAttributeMirror, "namespace", String.class, "");

					/* Get the internal argument required of the attribute */
					Boolean internalAttributeRequired = WsIOUtils.getAnnotationTypeValue(
							internalAttributeMirror, "required", Boolean.class);

					/* Get the external argument name of the attribute */
					String externalAttributeName = WsIOUtils.getAnnotationTypeValue(
							externalAttributeMirror, "name", String.class, "");

					/* Get the external argument namespace of the attribute */
					String externalAttributeNamespace = WsIOUtils.getAnnotationTypeValue(
							externalAttributeMirror, "namespace", String.class, "");

					/* Get the external argument required of the attribute */
					Boolean externalAttributeRequired = WsIOUtils.getAnnotationTypeValue(
							externalAttributeMirror, "required", Boolean.class);

					AnnotationMirror internalAdapterMirror = WsIOUtils.getAnnotationMirror(argument, XmlJavaTypeAdapter.class);
					AnnotationMirror externalAdapterMirror = WsIOUtils.getAnnotationMirror(argument, WsIOJavaTypeAdapter.class);
					AnnotationMirror adapterMirror = ObjectUtils.firstNonNull(
							internalAdapterMirror, externalAdapterMirror
					);

					/* Get the internal adapter value of the adapter */
					String internalAdapterValue = WsIOUtils.getAnnotationLiteralValue(internalAdapterMirror, "value");

					/* Get the internal adapter type of the adapter */
					String internalAdapterType = WsIOUtils.getAnnotationLiteralValue(internalAdapterMirror, "type");

					/* Get the external adapter value of the adapter */
					String externalAdapterValue = WsIOUtils.getAnnotationLiteralValue(externalAdapterMirror, "value");

					/* Get the external adapter type of the adapter */
					String externalAdapterType = WsIOUtils.getAnnotationLiteralValue(externalAdapterMirror, "type");

					/* Get the priority param values */
					String paramName = StringUtils.firstNonBlank(internalParamName, externalParamName);
					String paramPartName = StringUtils.firstNonBlank(internalParamPartName, externalParamPartName);
					String paramTargetNamespace = StringUtils.firstNonBlank(internalParamTargetNamespace, externalParamTargetNamespace);
					String paramMode = StringUtils.firstNonBlank(internalParamMode, externalParamMode);
					Boolean paramHeader = ObjectUtils.firstNonNull(internalParamHeader, externalParamHeader);

					/* Get the priority element values */
					String elementName = StringUtils.firstNonBlank(internalElementName, externalElementName);
					String elementNamespace = StringUtils.firstNonBlank(internalElementNamespace, externalElementNamespace);
					Boolean elementRequired = ObjectUtils.firstNonNull(internalElementRequired, externalElementRequired);
					Boolean elementNillable = ObjectUtils.firstNonNull(internalElementNillable, externalElementNillable);
					String elementDefaultValue = ObjectUtils.firstNonNull(internalElementDefaultValue, externalElementDefaultValue);
					String elementType = ObjectUtils.firstNonNull(internalElementType, externalElementType);

					/* Get the priority element wrapper values */
					String elementWrapperName = StringUtils.firstNonBlank(internalElementWrapperName, externalElementWrapperName);
					String elementWrapperNamespace = StringUtils.firstNonBlank(internalElementWrapperNamespace, externalElementWrapperNamespace);
					Boolean elementWrapperRequired = ObjectUtils.firstNonNull(internalElementWrapperRequired, externalElementWrapperRequired);
					Boolean elementWrapperNillable = ObjectUtils.firstNonNull(internalElementWrapperNillable, externalElementWrapperNillable);

					/* Get the priority attribute values */
					String attributeName = StringUtils.firstNonBlank(internalAttributeName, externalAttributeName);
					String attributeNamespace = StringUtils.firstNonBlank(internalAttributeNamespace, externalAttributeNamespace);
					Boolean attributeRequired = ObjectUtils.firstNonNull(internalAttributeRequired, externalAttributeRequired);

					/* Get the priority adapter values */
					String adapterValue = StringUtils.firstNonBlank(internalAdapterValue, externalAdapterValue);
					String adapterType = StringUtils.firstNonBlank(internalAdapterType, externalAdapterType);

					boolean paramPresent = paramMirror != null;
					boolean elementPresent = elementMirror != null;
					boolean elementWrapperPresent = elementWrapperMirror != null;
					boolean attributePresent = attributeMirror != null;
					boolean adapterPresent = adapterMirror != null;

					/* Get the ws io qualifier prefix */
					String qualifierPrefix = WsIOUtils.getAnnotationTypeValue(
							argument, WsIOQualifier.class, "prefix", String.class, "");

					/* Get the ws io qualifier suffix */
					String qualifierSuffix = WsIOUtils.getAnnotationTypeValue(
							argument, WsIOQualifier.class, "suffix", String.class, "");

					/* Get the qualifier info */
					WsIOQualifierInfo qualifierInfo = WsIOQualifierInfo.of(qualifierPrefix, qualifierSuffix);

					return WsIOParameterInfo.of(
							argument,
							type,
							paramPresent,
							paramName,
							paramPartName,
							paramTargetNamespace,
							paramMode,
							paramHeader,
							elementPresent,
							elementName,
							elementNamespace,
							elementRequired,
							elementNillable,
							elementDefaultValue,
							elementType,
							elementWrapperPresent,
							elementWrapperName,
							elementWrapperNamespace,
							elementWrapperRequired,
							elementWrapperNillable,
							attributePresent,
							attributeName,
							attributeNamespace,
							attributeRequired,
							adapterPresent,
							adapterValue,
							adapterType,
							qualifierInfo
					);

				});

		/* Extract the use annotations that are defined */
		Set<WsIOWrapped> executableWrappers = WsIOWrapped.ANNOTATIONS.filterValues(annotation ->
				descriptor.getSingle(annotation).isDefined())
				.keySet();

		/* Return the info */
		return WsIOExecutableInfo.of(executable, methodName, operationName, parameterInfos,
				returnName, returnNamespace, returnType, returnQualifierInfo, wrapperName, wrapperNamespace, executableWrappers);

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
	 * @param annotation class of the annotation
	 * @param field field of the annotation
	 * @return the string value of the annotation field
	 */
	static String getAnnotationLiteralValue(Element target, Class<? extends Annotation> annotation, String field) {
		AnnotationMirror annotationMirror = getAnnotationMirror(target, annotation);
		if (annotationMirror == null) {
			return null;
		}
		return getAnnotationLiteralValue(annotationMirror, field);
	}

	/**
	 * Method that returns the string value of an annotation field.
	 *
	 * @param mirror annotated mirror
	 * @param field field of the annotation
	 * @return the string value of the annotation field
	 */
	static String getAnnotationLiteralValue(AnnotationMirror mirror, String field) {
		AnnotationValue annotationValue = getAnnotationValue(mirror, field);
		if (annotationValue == null) {
			return null;
		} else {
			return annotationValue.toString();
		}
	}

	/**
	 * Method that returns the type value of an annotation field.
	 *
	 * @param target element to process
	 * @param annotation class of the annotation
	 * @param field field of the annotation
	 * @return the type value of the annotation field
	 */
	static <T> T getAnnotationTypeValue(Element target, Class<? extends Annotation> annotation, String field, Class<T> type) {
		return getAnnotationTypeValue(target, annotation, field, type, null);
	}

	/**
	 * Method that returns the type value of an annotation field.
	 *
	 * @param mirror annotated mirror
	 * @param field field of the annotation
	 * @return the type value of the annotation field
	 */
	static <T> T getAnnotationTypeValue(AnnotationMirror mirror, String field, Class<T> type) {
		return getAnnotationTypeValue(mirror, field, type, null);
	}

	/**
	 * Method that returns the type value of an annotation field.
	 *
	 * @param target element to process
	 * @param annotation class of the annotation
	 * @param field field of the annotation
	 * @param or default element to return
	 * @return the type value of the annotation field
	 */
	static <T> T getAnnotationTypeValue(Element target, Class<? extends Annotation> annotation, String field, Class<T> type, T or) {
		AnnotationMirror annotationMirror = getAnnotationMirror(target, annotation);
		if (annotationMirror == null) {
			return or;
		}
		return getAnnotationTypeValue(annotationMirror, field, type, or);
	}

	/**
	 * Method that returns the type value of an annotation field.
	 *
	 * @param mirror annotated mirror
	 * @param field field of the annotation
	 * @param or default element to return
	 * @return the type value of the annotation field
	 */
	static <T> T getAnnotationTypeValue(AnnotationMirror mirror, String field, Class<T> type, T or) {
		AnnotationValue annotationValue = getAnnotationValue(mirror, field);
		if (annotationValue == null) {
			return null;
		} else {
			Object value = annotationValue.getValue();
			if (type.isInstance(value)) {
				return type.cast(value);
			}
			return or;
		}
	}

	/**
	 * Method that returns the type value of an annotation field.
	 *
	 * @param target element to process
	 * @param annotation class of the annotation
	 * @param field field of the annotation
	 * @return the type value of the annotation field
	 */
	static <T> java.util.List<T> getAnnotationTypeListValue(Element target, Class<? extends Annotation> annotation, String field, Class<T> type) {
		AnnotationMirror annotationMirror = getAnnotationMirror(target, annotation);
		if (annotationMirror == null) {
			return null;
		}
		return getAnnotationTypeListValue(annotationMirror, field, type);
	}

	/**
	 * Method that returns the type value of an annotation field.
	 *
	 * @param mirror annotated mirror
	 * @param field field of the annotation
	 * @return the type value of the annotation field
	 */
	static <T> java.util.List<T> getAnnotationTypeListValue(AnnotationMirror mirror, String field, Class<T> type) {
		AnnotationValue annotationValue = getAnnotationValue(mirror, field);
		if (annotationValue == null) {
			return null;
		} else {
			Object value = annotationValue.getValue();
			if (value instanceof java.util.List list) {

				@SuppressWarnings("unchecked")
				java.util.List<T> casted = (java.util.List<T>) list;

				return casted.stream()
						.map(val -> {
							if (type.isInstance(val)) {
								return type.cast(val);
							}
							return null;
						}).collect(java.util.stream.Collectors.toList());

			}
			return null;
		}
	}

	/**
	 * Method that returns the annotation mirror of an element.
	 *
	 * @param target element to process
	 * @param annotation class of the annotation
	 * @return the annotation mirror of an element
	 */
	static AnnotationMirror getAnnotationMirror(Element target, Class<? extends Annotation> annotation) {
		String annotationName = annotation.getName();
		for (AnnotationMirror annotationMirror : target.getAnnotationMirrors()) {
			if (annotationMirror.getAnnotationType().toString().equals(annotationName)) {
				return annotationMirror;
			}
		}
		return null;
	}

	/**
	 * Method that returns the annotation mirror of an element.
	 *
	 * @param target element to process
	 * @param annotation class of the annotation
	 * @param wrapper class of the annotation
	 * @return the annotations mirror of an element
	 */
	static List<AnnotationMirror> getAnnotationMirrors(Element target, Class<? extends Annotation> annotation, Class<? extends Annotation> wrapper) {
		return getAnnotationMirrors(target, annotation, wrapper, "value");
	}

	/**
	 * Method that returns the annotation mirror of an element.
	 *
	 * @param target element to process
	 * @param annotation class of the annotation
	 * @param wrapper class of the annotation
	 * @param field name of the annotation wrapper
	 * @return the annotations mirror of an element
	 */
	static List<AnnotationMirror> getAnnotationMirrors(Element target, Class<? extends Annotation> annotation, Class<? extends Annotation> wrapper, String field) {

		String wrapperName = wrapper.getName();
		String annotationName = annotation.getName();

		for (AnnotationMirror wrapperMirror : target.getAnnotationMirrors()) {
			if (wrapperMirror.getAnnotationType().toString().equals(wrapperName)) {

				AnnotationValue wrapperValue = getAnnotationValue(wrapperMirror, field);
				if (wrapperValue != null) {

					Object wrapperObject = wrapperValue.getValue();
					if (wrapperObject instanceof java.util.List wrapperObjects) {

						@SuppressWarnings("unchecked")
						java.util.List<Object> innerRaws = (java.util.List<Object>) wrapperObjects;

						return Stream.ofAll(innerRaws)
								.map(innerRaw -> {

									if (innerRaw instanceof AnnotationValue innerValue) {
										Object innerObject = innerValue.getValue();
										if (innerObject instanceof AnnotationMirror annotationMirror) {
											if (annotationMirror.getAnnotationType().toString().equals(annotationName)) {
												return annotationMirror;
											}
										}
									}
									return null;

								}).filter(Objects::nonNull).toList();

					}

				}

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
	static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String field) {
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
