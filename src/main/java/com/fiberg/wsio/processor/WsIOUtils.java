package com.fiberg.wsio.processor;

import com.fiberg.wsio.annotation.*;
import com.fiberg.wsio.enumerate.WsIOType;
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
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.ObjectUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.*;
import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fiberg.wsio.processor.WsIOConstant.*;

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
		if (element.getEnclosingElement() instanceof TypeElement type) {

			/* Extract the enclosing element and return the recursive call with current type appended */
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
		if (element.getEnclosingElement() instanceof PackageElement packageElement) {

			/* Return the enclosing package element */
			return packageElement;

		} else if (element.getEnclosingElement() instanceof TypeElement typeElement){

			/* Return the recursive call the function */
			return extractPackage(typeElement);

		} else {

			/* Throw exception when enclosing element type is unknown */
			throw new IllegalStateException(String.format("Unknown type of the element %s", element));
		}

	}

	/**
	 * Method that checks if executable descriptor matches an executable method.
	 *
	 * @param executable executable descriptor to compare
	 * @param method executable method to compare
	 * @return the comparison between the executable descriptor and the executable method.
	 */
	static boolean methodMatch(WsIOExecutable executable, ExecutableElement method) {
		if (method != null) {
			return method.getSimpleName().toString().equals(executable.getExecutableName())
					&& method.getParameters().stream()
					.map(VariableElement::asType)
					.collect(Collectors.toList())
					.equals(executable.getMemberDescriptors()
							.map(WsIOMember::getTypeMirror))
					&& method.getReturnType().equals(executable.getReturnType());
		}
		return false;
	}

	/**
	 * Method that extracts the method and additional descriptor.
	 *
	 * @param source declaring type of the executable element
	 * @param executable executable element
	 * @param descriptor object with all element annotations
	 * @return object containing all the descriptor required for the generation of a wrapper element.
	 */
	static WsIOExecutable extractExecutableDescriptor(TypeElement source,
													  ExecutableElement executable,
													  WsIODescriptor descriptor) {
		return extractExecutableDescriptor(source, executable, descriptor, null, null);
	}

	/**
	 * Method that extracts the method and additional descriptor.
	 *
	 * @param source declaring type of the executable element
	 * @param executable executable element
	 * @param descriptor object with all element annotations
	 * @param targetType target type of the io
	 * @return object containing all the descriptor required for the generation of a wrapper element.
	 */
	static WsIOExecutable extractExecutableDescriptor(TypeElement source,
													  ExecutableElement executable,
													  WsIODescriptor descriptor,
													  WsIOType targetType) {
		return extractExecutableDescriptor(source, executable, descriptor, null, targetType);
	}

	/**
	 * Method that extracts the method and additional descriptor.
	 *
	 * @param source declaring type of the executable element
	 * @param executable executable element
	 * @param descriptor object with all element annotations
	 * @param identifier pair data to specify the target identifier
	 * @return object containing all the descriptor required for the generation of a wrapper element.
	 */
	static WsIOExecutable extractExecutableDescriptor(TypeElement source,
													  ExecutableElement executable,
													  WsIODescriptor descriptor,
													  WsIOIdentifier identifier) {
		return extractExecutableDescriptor(source, executable, descriptor, identifier, null);
	}

	/**
	 * Method that extracts the method and additional descriptor.
	 *
	 * @param source declaring type of the executable element
	 * @param element executable element
	 * @param descriptor object with all element annotations
	 * @param identifier pair data to specify the target identifier
	 * @param target target type of the io
	 * @return object containing all the descriptor required for the generation of a wrapper element.
	 */
	static WsIOExecutable extractExecutableDescriptor(TypeElement source,
													  ExecutableElement element,
													  WsIODescriptor descriptor,
													  WsIOIdentifier identifier,
													  WsIOType target) {

		// Get the type mirror and method names
		TypeMirror type = element.asType();
		String name = element.getSimpleName()
				.toString();

		/* Get the ws io annotated identifier prefix */
		String identifierAnnotatedPrefix = WsIOUtils.getAnnotationTypeValue(
				element, WsIOQualifier.class, "prefix", String.class
		);

		/* Get the ws io annotated identifier suffix */
		String identifierAnnotatedSuffix = WsIOUtils.getAnnotationTypeValue(
				element, WsIOQualifier.class, "suffix", String.class
		);

		/* Get the ws io contextual identifier prefix */
		String identifierContextualPrefix = identifier != null ? identifier.getIdentifierPrefix() : null;

		/* Get the ws io contextual identifier suffix */
		String identifierContextualSuffix = identifier != null ? identifier.getIdentifierSuffix() : null;

		/* Get the ws io identifier prefix */
		String identifierPrefix = ObjectUtils.firstNonNull(identifierContextualPrefix, identifierAnnotatedPrefix, "");

		/* Get the ws io identifier suffix */
		String identifierSuffix = ObjectUtils.firstNonNull(identifierContextualSuffix, identifierAnnotatedSuffix, "");

		/* Get the ws io return prefix */
		String returnPrefix = ObjectUtils.firstNonNull(identifierAnnotatedPrefix, "");

		/* Get the ws io return suffix */
		String returnSuffix = ObjectUtils.firstNonNull(identifierAnnotatedSuffix, "");

		/* Get the identifier descriptor and target type */
		WsIOType targetType = ObjectUtils.firstNonNull(target, WsIOType.BOTH);
		WsIOIdentifier targetIdentifier = WsIOIdentifier.of(identifierPrefix, identifierSuffix);
		WsIODescriptor operationDescriptor = ObjectUtils.firstNonNull(descriptor);

		AnnotationMirror webMethodMirror = WsIOUtils.getAnnotationMirror(element, WebMethod.class);
		AnnotationMirror webResultMirror = WsIOUtils.getAnnotationMirror(element, WebResult.class);
		AnnotationMirror xmlElementMirror = WsIOUtils.getAnnotationMirror(element, XmlElement.class);
		AnnotationMirror xmlElementWrapperMirror = WsIOUtils.getAnnotationMirror(element, XmlElementWrapper.class);

		/* Get operation name, action and exclude */
		String externalMethodOperationName = WsIOUtils.getAnnotationTypeValue(webMethodMirror, "operationName", String.class);
		String externalMethodAction = WsIOUtils.getAnnotationTypeValue(webMethodMirror, "action", String.class);
		Boolean externalMethodExclude = WsIOUtils.getAnnotationTypeValue(webMethodMirror, "exclude", Boolean.class);

		/* Get result name, part name, target namespace and result header */
		String externalResultName = WsIOUtils.getAnnotationTypeValue(webResultMirror, "name", String.class);
		String externalResultPartName = WsIOUtils.getAnnotationTypeValue(webResultMirror, "partName", String.class);
		String externalResultTargetNamespace = WsIOUtils.getAnnotationTypeValue(webResultMirror, "targetNamespace", String.class);
		Boolean externalResultHeader = WsIOUtils.getAnnotationTypeValue(webResultMirror, "header", Boolean.class);

		/* Get wrapper name, namespace, nillable and required */
		String externalElementName = WsIOUtils.getAnnotationTypeValue(xmlElementMirror, "name", String.class);
		String externalElementNamespace = WsIOUtils.getAnnotationTypeValue(xmlElementMirror, "namespace", String.class);
		Boolean externalElementNillable = WsIOUtils.getAnnotationTypeValue(xmlElementMirror, "nillable", Boolean.class);
		Boolean externalElementRequired = WsIOUtils.getAnnotationTypeValue(xmlElementMirror, "required", Boolean.class);
		String externalElementDefaultValue = WsIOUtils.getAnnotationTypeValue(xmlElementMirror, "defaultValue", String.class);
		String externalElementType = WsIOUtils.getAnnotationLiteralValue(xmlElementMirror, "type");

		/* Get wrapper name, namespace, nillable and required */
		String externalElementWrapperName = WsIOUtils.getAnnotationTypeValue(xmlElementWrapperMirror, "name", String.class);
		String externalElementWrapperNamespace = WsIOUtils.getAnnotationTypeValue(xmlElementWrapperMirror, "namespace", String.class);
		Boolean externalElementWrapperNillable = WsIOUtils.getAnnotationTypeValue(xmlElementWrapperMirror, "nillable", Boolean.class);
		Boolean externalElementWrapperRequired = WsIOUtils.getAnnotationTypeValue(xmlElementWrapperMirror, "required", Boolean.class);

		/* Get operation name, action and exclude */
		String methodOperationName = ObjectUtils.firstNonNull(externalMethodOperationName, XML_EMPTY_VALUE);
		String methodAction = ObjectUtils.firstNonNull(externalMethodAction, XML_EMPTY_VALUE);
		Boolean methodExclude = ObjectUtils.firstNonNull(externalMethodExclude, XML_FLAG_VALUE);

		/* Get result name, part name, target namespace and result header */
		String resultName = ObjectUtils.firstNonNull(externalResultName, XML_EMPTY_VALUE);
		String resultPartName = ObjectUtils.firstNonNull(externalResultPartName, XML_EMPTY_VALUE);
		String resultTargetNamespace = ObjectUtils.firstNonNull(externalResultTargetNamespace, XML_EMPTY_VALUE);
		Boolean resultHeader = ObjectUtils.firstNonNull(externalResultHeader, XML_FLAG_VALUE);

		/* Get wrapper name, namespace, nillable and required */
		String elementName = ObjectUtils.firstNonNull(externalElementName, XML_DEFAULT_VALUE);
		String elementNamespace = ObjectUtils.firstNonNull(externalElementNamespace, XML_DEFAULT_VALUE);
		Boolean elementNillable = ObjectUtils.firstNonNull(externalElementNillable, XML_FLAG_VALUE);
		Boolean elementRequired = ObjectUtils.firstNonNull(externalElementRequired, XML_FLAG_VALUE);
		String elementDefaultValue = ObjectUtils.firstNonNull(externalElementDefaultValue, XML_ZERO_VALUE);
		String elementType = ObjectUtils.firstNonNull(externalElementType);

		/* Get wrapper name, namespace, nillable and required */
		String elementWrapperName = ObjectUtils.firstNonNull(externalElementWrapperName, XML_DEFAULT_VALUE);
		String elementWrapperNamespace = ObjectUtils.firstNonNull(externalElementWrapperNamespace, XML_DEFAULT_VALUE);
		Boolean elementWrapperNillable = ObjectUtils.firstNonNull(externalElementWrapperNillable, XML_FLAG_VALUE);
		Boolean elementWrapperRequired = ObjectUtils.firstNonNull(externalElementWrapperRequired, XML_FLAG_VALUE);

		/* Get the present indicator */
		boolean methodPresent = webMethodMirror != null;
		boolean resultPresent = webResultMirror != null;
		boolean elementPresent = xmlElementMirror != null;
		boolean elementWrapperPresent = xmlElementWrapperMirror != null;

		/* Get return type and the prefix and suffix of ws identifier */
		TypeMirror returnType = element.getReturnType();
		WsIOIdentifier returnIdentifier = WsIOIdentifier.of(returnPrefix, returnSuffix);

		List<WsIOMember> memberDescriptors = List.ofAll(element.getParameters())
				.map(variableElement -> WsIOUtils.extractMemberDescriptor(variableElement, identifier, target));

		/* Extract the use annotations that are defined */
		Set<WsIOWrapped> descriptorWrappers = WsIOWrapped.ANNOTATIONS
				.filterValues(annotation -> Option.of(descriptor)
						.flatMap(desc -> desc.getSingle(annotation))
						.isDefined())
				.keySet();

		/* Return the descriptor */
		return WsIOExecutable.of(
				source, element, type, name,
				methodPresent, methodOperationName, methodAction, methodExclude,
				resultPresent, resultName, resultPartName, resultTargetNamespace, resultHeader,
				elementPresent, elementName, elementNamespace, elementRequired, elementNillable, elementDefaultValue, elementType,
				elementWrapperPresent, elementWrapperName, elementWrapperNamespace, elementWrapperRequired, elementWrapperNillable,
				returnType, returnIdentifier,
				memberDescriptors,
				descriptorWrappers,
				targetIdentifier,
				targetType,
				operationDescriptor

		);

	}

	/**
	 * Method that returns the full member descriptor of an element.
	 *
	 * @param element type element
	 * @return the full member descriptor of an element
	 */
	static WsIOMember extractMemberDescriptor(Element element) {
		return extractMemberDescriptor(element, null, null);
	}

	/**
	 * Method that returns the full member descriptor of an element.
	 *
	 * @param element type element
	 * @param identifier pair data to specify the target identifier
	 * @return the full member descriptor of an element
	 */
	static WsIOMember extractMemberDescriptor(Element element, WsIOIdentifier identifier) {
		return extractMemberDescriptor(element, identifier, null);
	}

	/**
	 * Method that returns the full member descriptor of an element.
	 *
	 * @param element type element
	 * @param target type of the io to handle
	 * @return the full member descriptor of an element
	 */
	static WsIOMember extractMemberDescriptor(Element element, WsIOType target) {
		return extractMemberDescriptor(element, null, target);
	}

	/**
	 * Method that returns the full member descriptor of an element.
	 *
	 * @param element type element
	 * @param identifier pair data to specify the target identifier
	 * @param target type of the io to handle
	 * @return the full member descriptor of an element
	 */
	static WsIOMember extractMemberDescriptor(Element element, WsIOIdentifier identifier, WsIOType target) {

		// Get the type mirror of the variable
		TypeMirror type = element.asType();

		/* Get the ws io annotated identifier prefix */
		String identifierAnnotatedPrefix = WsIOUtils.getAnnotationTypeValue(
				element, WsIOQualifier.class, "prefix", String.class
		);

		/* Get the ws io annotated identifier suffix */
		String identifierAnnotatedSuffix = WsIOUtils.getAnnotationTypeValue(
				element, WsIOQualifier.class, "suffix", String.class
		);

		/* Get the ws io contextual identifier prefix */
		String identifierContextualPrefix = identifier != null ? identifier.getIdentifierPrefix() : null;

		/* Get the ws io contextual identifier suffix */
		String identifierContextualSuffix = identifier != null ? identifier.getIdentifierSuffix() : null;

		/* Get the ws io identifier prefix */
		String identifierPrefix = ObjectUtils.firstNonNull(identifierContextualPrefix, identifierAnnotatedPrefix, "");

		/* Get the ws io identifier suffix */
		String identifierSuffix = ObjectUtils.firstNonNull(identifierContextualSuffix, identifierAnnotatedSuffix, "");

		/* Get the identifier descriptor and target type */
		WsIOType targetType = ObjectUtils.firstNonNull(target, WsIOType.BOTH);
		WsIOIdentifier targetIdentifier = WsIOIdentifier.of(identifierPrefix, identifierSuffix);

		AnnotationMirror internalParamMirror = WsIOUtils.getAnnotationMirror(element, WsIOParam.class);
		AnnotationMirror externalParamMirror = WsIOUtils.getAnnotationMirror(element, WebParam.class);
		AnnotationMirror paramMirror = ObjectUtils.firstNonNull(
				internalParamMirror, externalParamMirror
		);

		/* Get the internal argument name of the param */
		String internalParamName = WsIOUtils.getAnnotationTypeValue(
				internalParamMirror, "name", String.class
		);

		/* Get the internal argument part name of the param */
		String internalParamPartName = WsIOUtils.getAnnotationTypeValue(
				internalParamMirror, "partName", String.class
		);

		/* Get the internal argument target name space of the param */
		String internalParamTargetNamespace = WsIOUtils.getAnnotationTypeValue(
				internalParamMirror, "targetNamespace", String.class
		);

		/* Get the internal argument mode of the param */
		String internalParamMode = WsIOUtils.getAnnotationLiteralValue(internalParamMirror, "mode");

		/* Get the internal argument header of the param */
		Boolean internalParamHeader = WsIOUtils.getAnnotationTypeValue(
				internalParamMirror, "header", Boolean.class
		);

		/* Get the external argument names of the param */
		String externalParamName = WsIOUtils.getAnnotationTypeValue(
				externalParamMirror, "name", String.class
		);

		/* Get the external argument part name of the param */
		String externalParamPartName = WsIOUtils.getAnnotationTypeValue(
				externalParamMirror, "partName", String.class
		);

		/* Get the external argument target name space of the param */
		String externalParamTargetNamespace = WsIOUtils.getAnnotationTypeValue(
				externalParamMirror, "targetNamespace", String.class
		);

		/* Get the external argument mode of the param */
		String externalParamMode = WsIOUtils.getAnnotationLiteralValue(externalParamMirror, "mode");

		/* Get the external argument header of the param */
		Boolean externalParamHeader = WsIOUtils.getAnnotationTypeValue(
				externalParamMirror, "header", Boolean.class
		);

		AnnotationMirror externalElementMirror = WsIOUtils.getAnnotationMirror(element, XmlElement.class);
		Tuple2<List<AnnotationMirror>, List<AnnotationMirror>> internalTupleElementMirrors = WsIOUtils.getQualifiedAnnotationMirrors(
				element, WsIOElement.class, WsIOElements.class, targetIdentifier, targetType
		);

		List<AnnotationMirror> internalQualifiedElementMirrors = Option.of(internalTupleElementMirrors).map(Tuple2::_1).getOrElse(List::of);
		List<AnnotationMirror> internalDefaultElementMirrors = Option.of(internalTupleElementMirrors).map(Tuple2::_2).getOrElse(List::of);

		List<AnnotationMirror> elementMirrors = List
				.of(internalQualifiedElementMirrors, internalDefaultElementMirrors, Option.of(externalElementMirror).toList())
				.filter(Objects::nonNull)
				.flatMap(Function.identity());

		/* Get the internal qualified argument name of the element */
		String internalQualifiedElementName = Option.of(internalQualifiedElementMirrors)
				.getOrElse(List::of)
				.map(internalQualifiedElementMirror -> WsIOUtils.getAnnotationTypeValue(
						internalQualifiedElementMirror, "name", String.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal qualified argument namespace of the element */
		String internalQualifiedElementNamespace = Option.of(internalQualifiedElementMirrors)
				.getOrElse(List::of)
				.map(internalQualifiedElementMirror -> WsIOUtils.getAnnotationTypeValue(
						internalQualifiedElementMirror, "namespace", String.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal qualified argument required of the element */
		Boolean internalQualifiedElementRequired = Option.of(internalQualifiedElementMirrors)
				.getOrElse(List::of)
				.map(internalQualifiedElementMirror -> WsIOUtils.getAnnotationTypeValue(
						internalQualifiedElementMirror, "required", Boolean.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal qualified argument nillable of the element */
		Boolean internalQualifiedElementNillable = Option.of(internalQualifiedElementMirrors)
				.getOrElse(List::of)
				.map(internalQualifiedElementMirror -> WsIOUtils.getAnnotationTypeValue(
						internalQualifiedElementMirror, "nillable", Boolean.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal qualified argument default value of the element */
		String internalQualifiedElementDefaultValue = Option.of(internalQualifiedElementMirrors)
				.getOrElse(List::of)
				.map(internalQualifiedElementMirror -> WsIOUtils.getAnnotationTypeValue(
						internalQualifiedElementMirror, "defaultValue", String.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal qualified argument type of the element */
		String internalQualifiedElementType = Option.of(internalQualifiedElementMirrors)
				.getOrElse(List::of)
				.map(internalQualifiedElementMirror -> WsIOUtils.getAnnotationLiteralValue(internalQualifiedElementMirror, "type"))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal default argument name of the element */
		String internalDefaultElementName = Option.of(internalDefaultElementMirrors)
				.getOrElse(List::of)
				.map(internalDefaultElementMirror -> WsIOUtils.getAnnotationTypeValue(
						internalDefaultElementMirror, "name", String.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal default argument namespace of the element */
		String internalDefaultElementNamespace = Option.of(internalDefaultElementMirrors)
				.getOrElse(List::of)
				.map(internalDefaultElementMirror -> WsIOUtils.getAnnotationTypeValue(
						internalDefaultElementMirror, "namespace", String.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal default argument required of the element */
		Boolean internalDefaultElementRequired = Option.of(internalDefaultElementMirrors)
				.getOrElse(List::of)
				.map(internalDefaultElementMirror -> WsIOUtils.getAnnotationTypeValue(
						internalDefaultElementMirror, "required", Boolean.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal default argument nillable of the element */
		Boolean internalDefaultElementNillable = Option.of(internalDefaultElementMirrors)
				.getOrElse(List::of)
				.map(internalDefaultElementMirror -> WsIOUtils.getAnnotationTypeValue(
						internalDefaultElementMirror, "nillable", Boolean.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal default argument default value of the element */
		String internalDefaultElementDefaultValue = Option.of(internalDefaultElementMirrors)
				.getOrElse(List::of)
				.map(internalDefaultElementMirror -> WsIOUtils.getAnnotationTypeValue(
						internalDefaultElementMirror, "defaultValue", String.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal default argument type of the element */
		String internalDefaultElementType = Option.of(internalQualifiedElementMirrors)
				.getOrElse(List::of)
				.map(internalDefaultElementMirror -> WsIOUtils.getAnnotationLiteralValue(internalDefaultElementMirror, "type"))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the external argument name of the element */
		String externalElementName = WsIOUtils.getAnnotationTypeValue(
				externalElementMirror, "name", String.class
		);

		/* Get the external argument namespace of the element */
		String externalElementNamespace = WsIOUtils.getAnnotationTypeValue(
				externalElementMirror, "namespace", String.class
		);

		/* Get the external argument required of the element */
		Boolean externalElementRequired = WsIOUtils.getAnnotationTypeValue(
				externalElementMirror, "required", Boolean.class
		);

		/* Get the external argument nillable of the element */
		Boolean externalElementNillable = WsIOUtils.getAnnotationTypeValue(
				externalElementMirror, "nillable", Boolean.class
		);

		/* Get the external argument default value of the element */
		String externalElementDefaultValue = WsIOUtils.getAnnotationTypeValue(
				externalElementMirror, "defaultValue", String.class
		);

		/* Get the external argument type of the element */
		String externalElementType = WsIOUtils.getAnnotationLiteralValue(externalElementMirror, "type");

		AnnotationMirror externalElementWrapperMirror = WsIOUtils.getAnnotationMirror(element, XmlElementWrapper.class);
		Tuple2<List<AnnotationMirror>, List<AnnotationMirror>> internalTupleElementWrapperMirrors = WsIOUtils.getQualifiedAnnotationMirrors(
				element, WsIOElementWrapper.class, WsIOElementWrappers.class, targetIdentifier, targetType
		);

		List<AnnotationMirror> internalQualifiedElementWrapperMirrors = Option.of(internalTupleElementWrapperMirrors).map(Tuple2::_1).getOrElse(List::of);
		List<AnnotationMirror> internalDefaultElementWrapperMirrors = Option.of(internalTupleElementWrapperMirrors).map(Tuple2::_2).getOrElse(List::of);

		List<AnnotationMirror> elementWrapperMirrors = List
				.of(internalQualifiedElementWrapperMirrors, internalDefaultElementWrapperMirrors, Option.of(externalElementWrapperMirror).toList())
				.filter(Objects::nonNull)
				.flatMap(Function.identity());

		/* Get the internal qualified argument name of the element wrapper */
		String internalQualifiedElementWrapperName = Option.of(internalQualifiedElementWrapperMirrors)
				.getOrElse(List::of)
				.map(internalQualifiedElementWrapperMirror -> WsIOUtils.getAnnotationTypeValue(
						internalQualifiedElementWrapperMirror, "name", String.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal qualified argument namespace of the element wrapper */
		String internalQualifiedElementWrapperNamespace = Option.of(internalQualifiedElementWrapperMirrors)
				.getOrElse(List::of)
				.map(internalQualifiedElementWrapperMirror -> WsIOUtils.getAnnotationTypeValue(
						internalQualifiedElementWrapperMirror, "namespace", String.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal qualified argument required of the element wrapper */
		Boolean internalQualifiedElementWrapperRequired = Option.of(internalQualifiedElementWrapperMirrors)
				.getOrElse(List::of)
				.map(internalQualifiedElementWrapperMirror -> WsIOUtils.getAnnotationTypeValue(
						internalQualifiedElementWrapperMirror, "required", Boolean.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal qualified argument nillable of the element wrapper */
		Boolean internalQualifiedElementWrapperNillable = Option.of(internalQualifiedElementWrapperMirrors)
				.getOrElse(List::of)
				.map(internalQualifiedElementWrapperMirror -> WsIOUtils.getAnnotationTypeValue(
						internalQualifiedElementWrapperMirror, "nillable", Boolean.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal default argument name of the element wrapper */
		String internalDefaultElementWrapperName = Option.of(internalDefaultElementWrapperMirrors)
				.getOrElse(List::of)
				.map(internalDefaultElementWrapperMirror -> WsIOUtils.getAnnotationTypeValue(
						internalDefaultElementWrapperMirror, "name", String.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal default argument namespace of the element wrapper */
		String internalDefaultElementWrapperNamespace = Option.of(internalDefaultElementWrapperMirrors)
				.getOrElse(List::of)
				.map(internalDefaultElementWrapperMirror -> WsIOUtils.getAnnotationTypeValue(
						internalDefaultElementWrapperMirror, "namespace", String.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal default argument required of the element wrapper */
		Boolean internalDefaultElementWrapperRequired = Option.of(internalDefaultElementWrapperMirrors)
				.getOrElse(List::of)
				.map(internalDefaultElementWrapperMirror -> WsIOUtils.getAnnotationTypeValue(
						internalDefaultElementWrapperMirror, "required", Boolean.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal default argument nillable of the element wrapper */
		Boolean internalDefaultElementWrapperNillable = Option.of(internalDefaultElementWrapperMirrors)
				.getOrElse(List::of)
				.map(internalDefaultElementWrapperMirror -> WsIOUtils.getAnnotationTypeValue(
						internalDefaultElementWrapperMirror, "nillable", Boolean.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the external argument name of the element wrapper */
		String externalElementWrapperName = WsIOUtils.getAnnotationTypeValue(
				externalElementWrapperMirror, "name", String.class
		);

		/* Get the external argument namespace of the element wrapper */
		String externalElementWrapperNamespace = WsIOUtils.getAnnotationTypeValue(
				externalElementWrapperMirror, "namespace", String.class
		);

		/* Get the external argument required of the element wrapper */
		Boolean externalElementWrapperRequired = WsIOUtils.getAnnotationTypeValue(
				externalElementWrapperMirror, "required", Boolean.class
		);

		/* Get the external argument nillable of the element wrapper */
		Boolean externalElementWrapperNillable = WsIOUtils.getAnnotationTypeValue(
				externalElementWrapperMirror, "nillable", Boolean.class
		);

		AnnotationMirror externalAttributeMirror = WsIOUtils.getAnnotationMirror(element, XmlAttribute.class);
		Tuple2<List<AnnotationMirror>, List<AnnotationMirror>> internalTupleAttributeMirrors = WsIOUtils.getQualifiedAnnotationMirrors(
				element, WsIOAttribute.class, WsIOAttributes.class, targetIdentifier, targetType
		);

		List<AnnotationMirror> internalQualifiedAttributeMirrors = Option.of(internalTupleAttributeMirrors).map(Tuple2::_1).getOrElse(List::of);
		List<AnnotationMirror> internalDefaultAttributeMirrors = Option.of(internalTupleAttributeMirrors).map(Tuple2::_2).getOrElse(List::of);

		List<AnnotationMirror> attributeMirrors = List
				.of(internalQualifiedAttributeMirrors, internalDefaultAttributeMirrors, Option.of(externalAttributeMirror).toList())
				.filter(Objects::nonNull)
				.flatMap(Function.identity());

		/* Get the internal qualified argument name of the attribute */
		String internalQualifiedAttributeName = Option.of(internalQualifiedAttributeMirrors)
				.getOrElse(List::of)
				.map(internalQualifiedAttributeMirror -> WsIOUtils.getAnnotationTypeValue(
						internalQualifiedAttributeMirror, "name", String.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal qualified argument namespace of the attribute */
		String internalQualifiedAttributeNamespace = Option.of(internalQualifiedAttributeMirrors)
				.getOrElse(List::of)
				.map(internalQualifiedAttributeMirror -> WsIOUtils.getAnnotationTypeValue(
						internalQualifiedAttributeMirror, "namespace", String.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal qualified argument required of the attribute */
		Boolean internalQualifiedAttributeRequired = Option.of(internalQualifiedAttributeMirrors)
				.getOrElse(List::of)
				.map(internalQualifiedAttributeMirror -> WsIOUtils.getAnnotationTypeValue(
						internalQualifiedAttributeMirror, "required", Boolean.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal default argument name of the attribute */
		String internalDefaultAttributeName = Option.of(internalDefaultAttributeMirrors)
				.getOrElse(List::of)
				.map(internalDefaultAttributeMirror -> WsIOUtils.getAnnotationTypeValue(
						internalDefaultAttributeMirror, "name", String.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal default argument namespace of the attribute */
		String internalDefaultAttributeNamespace = Option.of(internalDefaultAttributeMirrors)
				.getOrElse(List::of)
				.map(internalDefaultAttributeMirror -> WsIOUtils.getAnnotationTypeValue(
						internalDefaultAttributeMirror, "namespace", String.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal default argument required of the attribute */
		Boolean internalDefaultAttributeRequired = Option.of(internalDefaultAttributeMirrors)
				.getOrElse(List::of)
				.map(internalDefaultAttributeMirror -> WsIOUtils.getAnnotationTypeValue(
						internalDefaultAttributeMirror, "required", Boolean.class
				))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the external argument name of the attribute */
		String externalAttributeName = WsIOUtils.getAnnotationTypeValue(
				externalAttributeMirror, "name", String.class
		);

		/* Get the external argument namespace of the attribute */
		String externalAttributeNamespace = WsIOUtils.getAnnotationTypeValue(
				externalAttributeMirror, "namespace", String.class
		);

		/* Get the external argument required of the attribute */
		Boolean externalAttributeRequired = WsIOUtils.getAnnotationTypeValue(
				externalAttributeMirror, "required", Boolean.class
		);

		AnnotationMirror externalTransientMirror = WsIOUtils.getAnnotationMirror(element, XmlTransient.class);
		Tuple2<List<AnnotationMirror>, List<AnnotationMirror>> internalTupleTransientMirrors = WsIOUtils.getQualifiedAnnotationMirrors(
				element, WsIOTransient.class, WsIOTransients.class, targetIdentifier, targetType
		);

		List<AnnotationMirror> internalQualifiedTransientMirrors = Option.of(internalTupleTransientMirrors).map(Tuple2::_1).getOrElse(List::of);
		List<AnnotationMirror> internalDefaultTransientMirrors = Option.of(internalTupleTransientMirrors).map(Tuple2::_2).getOrElse(List::of);

		List<AnnotationMirror> transientMirrors = List
				.of(internalQualifiedTransientMirrors, internalDefaultTransientMirrors, Option.of(externalTransientMirror).toList())
				.filter(Objects::nonNull)
				.flatMap(Function.identity());

		AnnotationMirror externalValueMirror = WsIOUtils.getAnnotationMirror(element, XmlValue.class);
		Tuple2<List<AnnotationMirror>, List<AnnotationMirror>> internalTupleValueMirrors = WsIOUtils.getQualifiedAnnotationMirrors(
				element, WsIOValue.class, WsIOValues.class, targetIdentifier, targetType
		);

		List<AnnotationMirror> internalQualifiedValueMirrors = Option.of(internalTupleValueMirrors).map(Tuple2::_1).getOrElse(List::of);
		List<AnnotationMirror> internalDefaultValueMirrors = Option.of(internalTupleValueMirrors).map(Tuple2::_2).getOrElse(List::of);

		List<AnnotationMirror> valueMirrors = List
				.of(internalQualifiedValueMirrors, internalDefaultValueMirrors, Option.of(externalValueMirror).toList())
				.filter(Objects::nonNull)
				.flatMap(Function.identity());

		AnnotationMirror externalAdapterMirror = WsIOUtils.getAnnotationMirror(element, XmlJavaTypeAdapter.class);
		Tuple2<List<AnnotationMirror>, List<AnnotationMirror>> internalTupleAdapterMirror = WsIOUtils.getQualifiedAnnotationMirrors(
				element, WsIOJavaTypeAdapter.class, WsIOJavaTypeAdapters.class, targetIdentifier, targetType
		);

		List<AnnotationMirror> internalQualifiedAdapterMirrors = Option.of(internalTupleAdapterMirror).map(Tuple2::_1).getOrElse(List::of);
		List<AnnotationMirror> internalDefaultAdapterMirrors = Option.of(internalTupleAdapterMirror).map(Tuple2::_2).getOrElse(List::of);

		List<AnnotationMirror> adapterMirrors = List
				.of(internalQualifiedAdapterMirrors, internalDefaultAdapterMirrors, Option.of(externalAdapterMirror).toList())
				.filter(Objects::nonNull)
				.flatMap(Function.identity());

		/* Get the internal qualified adapter value of the adapter */
		String internalQualifiedAdapterValue = Option.of(internalQualifiedAdapterMirrors)
				.getOrElse(List::of)
				.map(internalQualifiedAdapterMirror -> WsIOUtils.getAnnotationLiteralValue(internalQualifiedAdapterMirror, "value"))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal qualified adapter type of the adapter */
		String internalQualifiedAdapterType = Option.of(internalQualifiedAdapterMirrors)
				.getOrElse(List::of)
				.map(internalQualifiedAdapterMirror -> WsIOUtils.getAnnotationLiteralValue(internalQualifiedAdapterMirror, "type"))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal default adapter value of the adapter */
		String internalDefaultAdapterValue = Option.of(internalDefaultAdapterMirrors)
				.getOrElse(List::of)
				.map(internalDefaultAdapterMirror -> WsIOUtils.getAnnotationLiteralValue(internalDefaultAdapterMirror, "value"))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the internal default adapter type of the adapter */
		String internalDefaultAdapterType = Option.of(internalDefaultAdapterMirrors)
				.getOrElse(List::of)
				.map(internalDefaultAdapterMirror -> WsIOUtils.getAnnotationLiteralValue(internalDefaultAdapterMirror, "type"))
				.foldLeft(null, ObjectUtils::firstNonNull);

		/* Get the external adapter value of the adapter */
		String externalAdapterValue = WsIOUtils.getAnnotationLiteralValue(externalAdapterMirror, "value");

		/* Get the external adapter type of the adapter */
		String externalAdapterType = WsIOUtils.getAnnotationLiteralValue(externalAdapterMirror, "type");

		/* Get the priority param values */
		String paramName = ObjectUtils.firstNonNull(internalParamName, externalParamName, XML_EMPTY_VALUE);
		String paramPartName = ObjectUtils.firstNonNull(internalParamPartName, externalParamPartName, XML_EMPTY_VALUE);
		String paramTargetNamespace = ObjectUtils.firstNonNull(internalParamTargetNamespace, externalParamTargetNamespace, XML_EMPTY_VALUE);
		Boolean paramHeader = ObjectUtils.firstNonNull(internalParamHeader, externalParamHeader, XML_FLAG_VALUE);
		String paramMode = ObjectUtils.firstNonNull(internalParamMode, externalParamMode);

		/* Get the priority element values */
		String internalElementName = ObjectUtils.firstNonNull(internalQualifiedElementName, internalDefaultElementName);
		String internalElementNamespace = ObjectUtils.firstNonNull(internalQualifiedElementNamespace, internalDefaultElementNamespace);
		Boolean internalElementRequired = ObjectUtils.firstNonNull(internalQualifiedElementRequired, internalDefaultElementRequired);
		Boolean internalElementNillable = ObjectUtils.firstNonNull(internalQualifiedElementNillable, internalDefaultElementNillable);
		String internalElementDefaultValue = ObjectUtils.firstNonNull(internalQualifiedElementDefaultValue, internalDefaultElementDefaultValue);
		String internalElementType = ObjectUtils.firstNonNull(internalQualifiedElementType, internalDefaultElementType);

		String elementName = ObjectUtils.firstNonNull(internalElementName, externalElementName, XML_DEFAULT_VALUE);
		String elementNamespace = ObjectUtils.firstNonNull(internalElementNamespace, externalElementNamespace, XML_DEFAULT_VALUE);
		Boolean elementRequired = ObjectUtils.firstNonNull(internalElementRequired, externalElementRequired, XML_FLAG_VALUE);
		Boolean elementNillable = ObjectUtils.firstNonNull(internalElementNillable, externalElementNillable, XML_FLAG_VALUE);
		String elementDefaultValue = ObjectUtils.firstNonNull(internalElementDefaultValue, externalElementDefaultValue, XML_ZERO_VALUE);
		String elementType = ObjectUtils.firstNonNull(internalElementType, externalElementType);

		/* Get the priority element wrapper values */
		String internalElementWrapperName = ObjectUtils.firstNonNull(internalQualifiedElementWrapperName, internalDefaultElementWrapperName);
		String internalElementWrapperNamespace = ObjectUtils.firstNonNull(internalQualifiedElementWrapperNamespace, internalDefaultElementWrapperNamespace);
		Boolean internalElementWrapperRequired = ObjectUtils.firstNonNull(internalQualifiedElementWrapperRequired, internalDefaultElementWrapperRequired);
		Boolean internalElementWrapperNillable = ObjectUtils.firstNonNull(internalQualifiedElementWrapperNillable, internalDefaultElementWrapperNillable);

		String elementWrapperName = ObjectUtils.firstNonNull(internalElementWrapperName, externalElementWrapperName, XML_DEFAULT_VALUE);
		String elementWrapperNamespace = ObjectUtils.firstNonNull(internalElementWrapperNamespace, externalElementWrapperNamespace, XML_DEFAULT_VALUE);
		Boolean elementWrapperRequired = ObjectUtils.firstNonNull(internalElementWrapperRequired, externalElementWrapperRequired, XML_FLAG_VALUE);
		Boolean elementWrapperNillable = ObjectUtils.firstNonNull(internalElementWrapperNillable, externalElementWrapperNillable, XML_FLAG_VALUE);

		/* Get the priority attribute values */
		String internalAttributeName = ObjectUtils.firstNonNull(internalQualifiedAttributeName, internalDefaultAttributeName);
		String internalAttributeNamespace = ObjectUtils.firstNonNull(internalQualifiedAttributeNamespace, internalDefaultAttributeNamespace);
		Boolean internalAttributeRequired = ObjectUtils.firstNonNull(internalQualifiedAttributeRequired, internalDefaultAttributeRequired);

		String attributeName = ObjectUtils.firstNonNull(internalAttributeName, externalAttributeName, XML_DEFAULT_VALUE);
		String attributeNamespace = ObjectUtils.firstNonNull(internalAttributeNamespace, externalAttributeNamespace, XML_DEFAULT_VALUE);
		Boolean attributeRequired = ObjectUtils.firstNonNull(internalAttributeRequired, externalAttributeRequired, XML_FLAG_VALUE);

		/* Get the priority adapter values */
		String internalAdapterValue = ObjectUtils.firstNonNull(internalQualifiedAdapterValue, internalDefaultAdapterValue);
		String internalAdapterType = ObjectUtils.firstNonNull(internalQualifiedAdapterType, internalDefaultAdapterType);

		String adapterValue = ObjectUtils.firstNonNull(internalAdapterValue, externalAdapterValue);
		String adapterType = ObjectUtils.firstNonNull(internalAdapterType, externalAdapterType);

		boolean paramPresent = paramMirror != null;
		boolean elementPresent = elementMirrors != null && elementMirrors.size() > 0;
		boolean elementWrapperPresent = elementWrapperMirrors != null && elementWrapperMirrors.size() > 0;
		boolean attributePresent = attributeMirrors != null && attributeMirrors.size() > 0;
		boolean transientPresent = transientMirrors != null && transientMirrors.size() > 0;
		boolean valuePresent = valueMirrors != null && valueMirrors.size() > 0;
		boolean adapterPresent = adapterMirrors != null && adapterMirrors.size() > 0;

		return WsIOMember.of(
				element,
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
				transientPresent,
				valuePresent,
				targetIdentifier,
				targetType
		);

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
	 * Method that returns the first element not matching the specified value or the default value.
	 *
	 * @param match the value to match against
	 * @param values the list of values to check
	 * @return the first element not matching the specified value or the default value
	 */
	static String getFirstStringNonEqualsTo(String match, String... values) {
		for (String value : values) {
			if (!Objects.equals(match, value)) {
				return value;
			}
		}
		return match;
	}

	/**
	 * Method that returns the first element not matching the specified value or the default value.
	 *
	 * @param matches the list of values to match against
	 * @param or the default value
	 * @param values the list of values to check
	 * @return the first element not matching the specified value or the default value
	 */
	static String getFirstStringNonEqualsTo(List<String> matches, String or, String... values) {
		for (String value : values) {
			if (!matches.contains(value)) {
				return value;
			}
		}
		return or;
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
	 * @param element element to process
	 * @param annotation class of the annotation
	 * @param field field of the annotation
	 * @return the string value of the annotation field
	 */
	static String getAnnotationLiteralValue(Element element, Class<? extends Annotation> annotation, String field) {
		AnnotationMirror annotationMirror = getAnnotationMirror(element, annotation);
		if (annotationMirror != null) {
			return getAnnotationLiteralValue(annotationMirror, field);
		}
		return null;
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
		if (annotationValue != null) {
			return annotationValue.toString();
		}
		return null;
	}

	/**
	 * Method that returns the string value of an annotation field.
	 *
	 * @param element element to process
	 * @param annotation class of the annotation
	 * @param enumerate class of the enum
	 * @param field field of the annotation
	 * @return the string value of the annotation field
	 */
	static <T extends Enum<T>> T getAnnotationEnumValue(Element element, Class<? extends Annotation> annotation, Class<? extends T> enumerate, String field) {
		return getAnnotationEnumValue(element, annotation, enumerate, field, null);
	}

	/**
	 * Method that returns the string value of an annotation field.
	 *
	 * @param element element to process
	 * @param annotation class of the annotation
	 * @param enumerate class of the enum
	 * @param field field of the annotation
	 * @param or default enumerate value
	 * @return the string value of the annotation field
	 */
	static <T extends Enum<T>> T getAnnotationEnumValue(Element element, Class<? extends Annotation> annotation, Class<? extends T> enumerate, String field, T or) {
		AnnotationMirror annotationMirror = getAnnotationMirror(element, annotation);
		if (annotationMirror != null) {
			return getAnnotationEnumValue(annotationMirror, enumerate, field, or);
		}
		return null;
	}

	/**
	 * Method that returns the string value of an annotation field.
	 *
	 * @param mirror annotated mirror
	 * @param enumerate class of the enum
	 * @param field field of the annotation
	 * @return the string value of the annotation field
	 */
	static <T extends Enum<T>> T getAnnotationEnumValue(AnnotationMirror mirror, Class<? extends T> enumerate, String field) {
		return getAnnotationEnumValue(mirror, enumerate, field, null);
	}

	/**
	 * Method that returns the string value of an annotation field.
	 *
	 * @param mirror annotated mirror
	 * @param enumerate class of the enum
	 * @param field field of the annotation
	 * @param or default enumerate value
	 * @return the string value of the annotation field
	 */
	static <T extends Enum<T>> T getAnnotationEnumValue(AnnotationMirror mirror, Class<? extends T> enumerate, String field, T or) {
		AnnotationValue annotationValue = getAnnotationValue(mirror, field);
		if (annotationValue != null) {
			Object value = annotationValue.getValue();
			if (value instanceof VariableElement element) {
				if (element.getSimpleName() != null) {
					return WsIOUtils.toEnum(enumerate, element.getSimpleName().toString());
				}
			}
		}
		return or;
	}

	/**
	 * Method that returns the type value of an annotation field.
	 *
	 * @param element element to process
	 * @param annotation class of the annotation
	 * @param field field of the annotation
	 * @return the type value of the annotation field
	 */
	static <T> T getAnnotationTypeValue(Element element, Class<? extends Annotation> annotation, String field, Class<T> type) {
		return getAnnotationTypeValue(element, annotation, field, type, null);
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
	 * @param element element to process
	 * @param annotation class of the annotation
	 * @param field field of the annotation
	 * @param or default element to return
	 * @return the type value of the annotation field
	 */
	static <T> T getAnnotationTypeValue(Element element, Class<? extends Annotation> annotation, String field, Class<T> type, T or) {
		AnnotationMirror annotationMirror = getAnnotationMirror(element, annotation);
		if (annotationMirror != null) {
			return getAnnotationTypeValue(annotationMirror, field, type, or);
		}
		return or;
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
		if (annotationValue != null) {
			Object value = annotationValue.getValue();
			if (type.isInstance(value)) {
				return type.cast(value);
			}
		}
		return or;
	}

	/**
	 * Method that returns the type value of an annotation field.
	 *
	 * @param element element to process
	 * @param annotation class of the annotation
	 * @param field field of the annotation
	 * @return the type value of the annotation field
	 */
	static <T> java.util.List<T> getAnnotationTypeListValue(Element element, Class<? extends Annotation> annotation, String field, Class<T> type) {
		AnnotationMirror annotationMirror = getAnnotationMirror(element, annotation);
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
	 * @param element element to process
	 * @param annotation class of the annotation
	 * @return the annotation mirror of an element
	 */
	static AnnotationMirror getAnnotationMirror(Element element, Class<? extends Annotation> annotation) {
		if (element != null) {
			String annotationName = annotation.getName();
			for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
				if (annotationMirror.getAnnotationType().toString().equals(annotationName)) {
					return annotationMirror;
				}
			}
		}
		return null;
	}

	/**
	 * Method that returns the annotation mirror of an element.
	 *
	 * @param element element to process
	 * @param annotation class of the annotation
	 * @param wrapper class of the annotation
	 * @return the annotations mirror of an element
	 */
	static List<AnnotationMirror> getAnnotationMirrors(Element element, Class<? extends Annotation> annotation, Class<? extends Annotation> wrapper) {
		return getAnnotationMirrors(element, annotation, wrapper, "value");
	}

	/**
	 * Method that returns the annotation mirror of an element.
	 *
	 * @param element element to process
	 * @param annotation class of the annotation
	 * @param wrapper class of the annotation
	 * @param field name of the annotation wrapper
	 * @return the annotations mirror of an element
	 */
	static List<AnnotationMirror> getAnnotationMirrors(Element element, Class<? extends Annotation> annotation, Class<? extends Annotation> wrapper, String field) {

		if (element != null) {

			String wrapperName = wrapper.getName();
			String annotationName = annotation.getName();

			for (AnnotationMirror wrapperMirror : element.getAnnotationMirrors()) {
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
			for (AnnotationMirror wrapperMirror : element.getAnnotationMirrors()) {
				if (wrapperMirror.getAnnotationType().toString().equals(annotationName)) {
					return List.of(wrapperMirror);
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
		if (annotationMirror != null) {
			for (java.util.Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
				if (entry.getKey().getSimpleName().toString().equals(field)) {
					return entry.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * Method that returns the annotation descriptions of an element.
	 *
	 * @param element element to process
	 * @return the annotation descriptions of the element
	 */
	static Map<DeclaredType, Map<String, String>> getAnnotationDescriptions(Element element) {
		if (element != null) {
			return Stream.ofAll(element.getAnnotationMirrors())
					.toMap(annotationMirror -> {
						DeclaredType annotationType = annotationMirror.getAnnotationType();
						Map<String, String> annotationDescription = getAnnotationDescriptions(annotationMirror);
						return Map.entry(annotationType, annotationDescription);
					});
		}
		return null;
	}

	/**
	 * Method that returns the annotation descriptions of an element.
	 *
	 * @param annotationMirror annotation mirror to process
	 * @return the annotation descriptions of the element
	 */
	static Map<String, String> getAnnotationDescriptions(AnnotationMirror annotationMirror) {

		if (annotationMirror != null) {
			return HashMap.ofAll(annotationMirror.getElementValues())
					.toMap(tuple -> {

						AnnotationValue annotationValue = tuple._2();
						ExecutableElement executableElement = tuple._1();

						String fieldValue = getAnnotationDescription(annotationValue);
						String fieldName = executableElement
								.getSimpleName()
								.toString();

						return Map.entry(fieldName, fieldValue);

					}).filterValues(Objects::nonNull);
		}
		return null;

	}

	/**
	 * Method that returns the annotation description of a value.
	 *
	 * @param annotationValue the annotation value to extract the string value
	 * @return the annotation description of a value
	 */
	static String getAnnotationDescription(AnnotationValue annotationValue) {

		Object fieldValue = annotationValue.getValue();
		String fieldString = annotationValue.toString();

		/* Only process the annotation value as a list
		 * when is a list, is not empty, and one of its elements is a variable element */
		if (fieldValue instanceof java.util.List list
				&& list.size() > 0
				&& list.get(0) instanceof AnnotationValue elementValue
				&& elementValue.getValue() instanceof VariableElement) {

			@SuppressWarnings("unchecked")
			java.util.List<Object> objects = (java.util.List<Object>) list;

			String elements = Stream.ofAll(objects)
					.filter(AnnotationValue.class::isInstance)
					.map(AnnotationValue.class::cast)
					.map(WsIOUtils::getAnnotationDescription)
					.mkString(", ");

			return String.format("{%s}", elements);

		}

		if (fieldValue instanceof VariableElement variableElement) {

			TypeMirror typeMirror = variableElement.asType();

			if (typeMirror instanceof DeclaredType declaredType) {

				Element fieldElement = declaredType.asElement();
				String fieldEnum = variableElement.getSimpleName()
						.toString();

				if (fieldElement instanceof TypeElement fieldType) {
					String fieldQualified = fieldType.getQualifiedName().toString();
					return String.format(
							"%s.%s", fieldQualified, fieldEnum
					);
				}

			}

		}

		return fieldString;

	}

	/**
	 * Method that returns the annotation mirror of an element.
	 *
	 * @param element element to process
	 * @param annotation class of the annotation
	 * @param wrapper class of the annotation wrapper
	 * @param identifier pair indicating the identifier to search
	 * @param type type of the io to handle
	 * @return the annotation mirror of an element
	 */
	static Tuple2<AnnotationMirror, AnnotationMirror> getQualifiedAnnotationMirror(Element element,
																				   Class<? extends Annotation> annotation,
																				   Class<? extends Annotation> wrapper,
																				   WsIOIdentifier identifier,
																				   WsIOType type) {
		return getQualifiedAnnotationMirror(element, annotation, wrapper, identifier, type, "value");
	}

	/**
	 * Method that returns the annotation mirror of an element.
	 *
	 * @param element element to process
	 * @param annotation class of the annotation
	 * @param wrapper class of the annotation wrapper
	 * @param identifier pair indicating the identifier to search
	 * @param type type of the io to handle
	 * @param field name of the field
	 * @return the annotation mirror of an element
	 */
	static Tuple2<AnnotationMirror, AnnotationMirror> getQualifiedAnnotationMirror(Element element,
																				   Class<? extends Annotation> annotation,
																				   Class<? extends Annotation> wrapper,
																				   WsIOIdentifier identifier,
																				   WsIOType type,
																				   String field) {

		Tuple2<List<AnnotationMirror>, List<AnnotationMirror>> annotationMirrors = getQualifiedAnnotationMirrors(
				element, annotation, wrapper, identifier, type, field
		);

		if (annotationMirrors != null) {
			return Tuple.of(
					Option.of(annotationMirrors).map(Tuple2::_1).filter(Objects::nonNull).map(List::headOption).flatMap(Function.identity()).getOrNull(),
					Option.of(annotationMirrors).map(Tuple2::_2).filter(Objects::nonNull).map(List::headOption).flatMap(Function.identity()).getOrNull()
			);
		}
		return null;

	}

	/**
	 * Method that returns the annotation mirrors of an element.
	 *
	 * @param element element to process
	 * @param annotation class of the annotation
	 * @param wrapper class of the annotation wrapper
	 * @param identifier pair indicating the identifier to search
     * @param type type of the io to handle
	 * @return the annotation mirrors of an element
	 */
	static Tuple2<List<AnnotationMirror>, List<AnnotationMirror>> getQualifiedAnnotationMirrors(Element element,
																								Class<? extends Annotation> annotation,
																								Class<? extends Annotation> wrapper,
																								WsIOIdentifier identifier,
																								WsIOType type) {
		return getQualifiedAnnotationMirrors(element, annotation, wrapper, identifier, type, "value");
	}

	/**
	 * Method that returns the annotation mirrors of an element.
	 *
	 * @param element element to process
	 * @param annotation class of the annotation
	 * @param wrapper class of the annotation wrapper
	 * @param identifier pair indicating the identifier to search
	 * @param type type of the io to handle
	 * @param field name of the field
	 * @return the annotation mirrors of an element
	 */
	static Tuple2<List<AnnotationMirror>, List<AnnotationMirror>> getQualifiedAnnotationMirrors(Element element,
																								Class<? extends Annotation> annotation,
																								Class<? extends Annotation> wrapper,
																								WsIOIdentifier identifier,
																								WsIOType type,
																								String field) {

		List<AnnotationMirror> annotationMirrors = getAnnotationMirrors(element, annotation, wrapper, field);

		if (annotationMirrors != null) {

			WsIOType targetType = ObjectUtils.firstNonNull(type, WsIOType.BOTH);
			String identifierPrefix = ObjectUtils.firstNonNull(identifier != null ? identifier.getIdentifierPrefix() : null, "");
			String identifierSuffix = ObjectUtils.firstNonNull(identifier != null ? identifier.getIdentifierSuffix() : null, "");

			List<AnnotationMirror> matchingMirror = annotationMirrors
					.filter(annotationMirror -> {

						WsIOType target = getAnnotationEnumValue(annotationMirror, WsIOType.class, "target", WsIOType.BOTH);
						String prefix = getAnnotationTypeValue(annotationMirror, "prefix", String.class, XML_DEFAULT_VALUE);
						String suffix = getAnnotationTypeValue(annotationMirror, "suffix", String.class, XML_DEFAULT_VALUE);

						return identifierPrefix.equals(prefix)
								&& identifierSuffix.equals(suffix)
								&& targetType.equals(target);

					});

			List<AnnotationMirror> defaultMirror = annotationMirrors
					.filter(annotationMirror -> {

						WsIOType target = getAnnotationEnumValue(annotationMirror, WsIOType.class, "target", WsIOType.BOTH);
						String prefix = getAnnotationTypeValue(annotationMirror, "prefix", String.class, XML_DEFAULT_VALUE);
						String suffix = getAnnotationTypeValue(annotationMirror, "suffix", String.class, XML_DEFAULT_VALUE);

						boolean exact = identifierPrefix.equals(prefix)
								&& identifierSuffix.equals(suffix)
								&& targetType.equals(target);

						return !exact
								&& (identifierPrefix.equals(prefix) || XML_DEFAULT_VALUE.equals(prefix))
								&& (identifierSuffix.equals(suffix) || XML_DEFAULT_VALUE.equals(suffix))
								&& (targetType.equals(target) || WsIOType.BOTH.equals(target));

					}).sortBy(annotationMirror -> {

						WsIOType target = getAnnotationEnumValue(annotationMirror, WsIOType.class, "target", WsIOType.BOTH);
						String prefix = getAnnotationTypeValue(annotationMirror, "prefix", String.class, XML_DEFAULT_VALUE);
						String suffix = getAnnotationTypeValue(annotationMirror, "suffix", String.class, XML_DEFAULT_VALUE);

						return (identifierPrefix.equals(prefix) ? 1 : 0) + (identifierSuffix.equals(suffix) ? 1 : 0) + (targetType.equals(target) ? 1 : 0);

					}).reverse();

			return Tuple.of(matchingMirror, defaultMirror);

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
	 * Method that returns the enum from a value ignoring the case.
	 *
	 * @param enumerate enumerate to get from value
	 * @param name      name to get the enum
	 * @param <T>       type of the enumerate
	 * @return the enum of the value compared ignoring case
	 */
	static <T extends Enum<T>> T toEnum(final Class<? extends T> enumerate,
										final String name) {

		/* Search the enum by value and return if found */
		for (final T value : enumerate.getEnumConstants()) {
			if (value.name().equalsIgnoreCase(name)) {
				return value;
			}
		}

		/* Return when not found */
		return null;

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
