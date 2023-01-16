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
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.*;
import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;
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
			return method.getSimpleName().toString().equals(info.getExecutableName())
					&& method.getParameters().stream()
					.map(VariableElement::asType)
					.collect(Collectors.toList())
					.equals(info.getMemberInfos()
							.map(WsIOMemberInfo::getTypeMirror))
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
		return extractExecutableInfo(executable, descriptor, null);
	}

	/**
	 * Method that extracts the method and additional info.
	 *
	 * @param executableElement executable element
	 * @param executableDescriptor object with all element annotations
	 * @param qualifierInfo pair data to specify the target qualifier
	 * @return object containing all the info required for the generation of a wrapper element.
	 */
	static WsIOExecutableInfo extractExecutableInfo(ExecutableElement executableElement,
													WsIODescriptor executableDescriptor,
													WsIOQualifierInfo qualifierInfo) {

		AnnotationMirror webMethodMirror = WsIOUtils.getAnnotationMirror(executableElement, WebMethod.class);
		AnnotationMirror webResultMirror = WsIOUtils.getAnnotationMirror(executableElement, WebResult.class);
		AnnotationMirror xmlElementMirror = WsIOUtils.getAnnotationMirror(executableElement, XmlElement.class);
		AnnotationMirror xmlElementWrapperMirror = WsIOUtils.getAnnotationMirror(executableElement, XmlElementWrapper.class);

		/* Get operation name, action and exclude */
		String methodOperationName = WsIOUtils.getAnnotationTypeValue(webMethodMirror, "operationName", String.class, XML_EMPTY_VALUE);
		String methodAction = WsIOUtils.getAnnotationTypeValue(webMethodMirror, "action", String.class, XML_EMPTY_VALUE);
		Boolean methodExclude = WsIOUtils.getAnnotationTypeValue(webMethodMirror, "exclude", Boolean.class, XML_FLAG_VALUE);

		/* Get result name, part name, target namespace and result header */
		String resultName = WsIOUtils.getAnnotationTypeValue(webResultMirror, "name", String.class, XML_EMPTY_VALUE);
		String resultPartName = WsIOUtils.getAnnotationTypeValue(webResultMirror, "partName", String.class, XML_EMPTY_VALUE);
		String resultTargetNamespace = WsIOUtils.getAnnotationTypeValue(webResultMirror, "targetNamespace", String.class, XML_EMPTY_VALUE);
		Boolean resultHeader = WsIOUtils.getAnnotationTypeValue(webResultMirror, "header", Boolean.class, XML_FLAG_VALUE);

		/* Get wrapper name, namespace, nillable and required */
		String elementName = WsIOUtils.getAnnotationTypeValue(xmlElementMirror, "name", String.class, XML_DEFAULT_VALUE);
		String elementNamespace = WsIOUtils.getAnnotationTypeValue(xmlElementMirror, "namespace", String.class, XML_DEFAULT_VALUE);
		Boolean elementNillable = WsIOUtils.getAnnotationTypeValue(xmlElementMirror, "nillable", Boolean.class, XML_FLAG_VALUE);
		Boolean elementRequired = WsIOUtils.getAnnotationTypeValue(xmlElementMirror, "required", Boolean.class, XML_FLAG_VALUE);
		String elementDefaultValue = WsIOUtils.getAnnotationTypeValue(xmlElementMirror, "defaultValue", String.class, XML_ZERO_VALUE);
		String elementType = WsIOUtils.getAnnotationLiteralValue(xmlElementMirror, "type");

		/* Get wrapper name, namespace, nillable and required */
		String elementWrapperName = WsIOUtils.getAnnotationTypeValue(xmlElementWrapperMirror, "name", String.class, XML_DEFAULT_VALUE);
		String elementWrapperNamespace = WsIOUtils.getAnnotationTypeValue(xmlElementWrapperMirror, "namespace", String.class, XML_DEFAULT_VALUE);
		Boolean elementWrapperNillable = WsIOUtils.getAnnotationTypeValue(xmlElementWrapperMirror, "nillable", Boolean.class, XML_FLAG_VALUE);
		Boolean elementWrapperRequired = WsIOUtils.getAnnotationTypeValue(xmlElementWrapperMirror, "required", Boolean.class, XML_FLAG_VALUE);

		/* Get the present indicator */
		boolean methodPresent = webMethodMirror != null;
		boolean resultPresent = webResultMirror != null;
		boolean elementPresent = xmlElementMirror != null;
		boolean elementWrapperPresent = xmlElementWrapperMirror != null;

		/* Get return type and the prefix and suffix of ws qualifier */
		TypeMirror returnType = executableElement.getReturnType();
		WsIOQualifierInfo returnQualifierInfo = Option.of(WsIOUtils.getAnnotationMirror(executableElement, WsIOQualifier.class))
				.map(mirror -> WsIOQualifierInfo.of(
						WsIOUtils.getAnnotationTypeValue(mirror, "prefix", String.class, ""),
						WsIOUtils.getAnnotationTypeValue(mirror, "suffix", String.class, "")))
				.getOrNull();

		/* Get operation and method names */
		String executableName = executableElement.getSimpleName().toString();

		List<WsIOMemberInfo> memberInfos = List.ofAll(executableElement.getParameters())
				.map(variableElement -> WsIOUtils.extractMemberInfo(variableElement, qualifierInfo));

		/* Extract the use annotations that are defined */
		Set<WsIOWrapped> descriptorWrappers = WsIOWrapped.ANNOTATIONS
				.filterValues(annotation -> Option.of(executableDescriptor)
						.flatMap(desc -> desc.getSingle(annotation))
						.isDefined())
				.keySet();

		/* Return the info */
		return WsIOExecutableInfo.of(
				executableElement, executableName,
				methodPresent, methodOperationName, methodAction, methodExclude,
				resultPresent, resultName, resultPartName, resultTargetNamespace, resultHeader,
				elementPresent, elementName, elementNamespace, elementRequired, elementNillable, elementDefaultValue, elementType,
				elementWrapperPresent, elementWrapperName, elementWrapperNamespace, elementWrapperRequired, elementWrapperNillable,
				returnType, returnQualifierInfo,
				memberInfos,
				descriptorWrappers
		);

	}

	/**
	 * Method that returns the full member info of an element.
	 *
	 * @param element type element
	 * @return the full member info of an element
	 */
	static WsIOMemberInfo extractMemberInfo(Element element) {
		return extractMemberInfo(element, null);
	}

	/**
	 * Method that returns the full member info of an element.
	 *
	 * @param element type element
	 * @param qualifier pair data to specify the target qualifier
	 * @return the full member info of an element
	 */
	static WsIOMemberInfo extractMemberInfo(Element element, WsIOQualifierInfo qualifier) {

		// Get the type mirror of the variable
		TypeMirror type = element.asType();

		WsIOQualifierInfo qualifierInitialized = Objects.requireNonNullElseGet(qualifier, () -> WsIOQualifierInfo.of("", ""));

		AnnotationMirror internalParamMirror = WsIOUtils.getAnnotationMirror(element, WsIOParam.class);
		AnnotationMirror externalParamMirror = WsIOUtils.getAnnotationMirror(element, WebParam.class);
		AnnotationMirror paramMirror = ObjectUtils.firstNonNull(
				internalParamMirror, externalParamMirror
		);

		/* Get the internal argument name of the param */
		String internalParamName = WsIOUtils.getAnnotationTypeValue(
				internalParamMirror, "name", String.class, XML_EMPTY_VALUE
		);

		/* Get the internal argument part name of the param */
		String internalParamPartName = WsIOUtils.getAnnotationTypeValue(
				internalParamMirror, "partName", String.class, XML_EMPTY_VALUE
		);

		/* Get the internal argument target name space of the param */
		String internalParamTargetNamespace = WsIOUtils.getAnnotationTypeValue(
				internalParamMirror, "targetNamespace", String.class, XML_EMPTY_VALUE
		);

		/* Get the internal argument mode of the param */
		String internalParamMode = WsIOUtils.getAnnotationLiteralValue(internalParamMirror, "mode");

		/* Get the internal argument header of the param */
		Boolean internalParamHeader = WsIOUtils.getAnnotationTypeValue(
				internalParamMirror, "header", Boolean.class, XML_FLAG_VALUE
		);

		/* Get the external argument names of the param */
		String externalParamName = WsIOUtils.getAnnotationTypeValue(
				externalParamMirror, "name", String.class, XML_EMPTY_VALUE
		);

		/* Get the external argument part name of the param */
		String externalParamPartName = WsIOUtils.getAnnotationTypeValue(
				externalParamMirror, "partName", String.class, XML_EMPTY_VALUE
		);

		/* Get the external argument target name space of the param */
		String externalParamTargetNamespace = WsIOUtils.getAnnotationTypeValue(
				externalParamMirror, "targetNamespace", String.class, XML_EMPTY_VALUE
		);

		/* Get the external argument mode of the param */
		String externalParamMode = WsIOUtils.getAnnotationLiteralValue(externalParamMirror, "mode");

		/* Get the external argument header of the param */
		Boolean externalParamHeader = WsIOUtils.getAnnotationTypeValue(
				externalParamMirror, "header", Boolean.class, XML_FLAG_VALUE
		);

		AnnotationMirror externalElementMirror = WsIOUtils.getAnnotationMirror(element, XmlElement.class);
		Tuple2<AnnotationMirror, AnnotationMirror> internalTupleElementMirrors = WsIOUtils.getQualifiedAnnotationMirror(element, WsIOElement.class, WsIOElements.class, qualifierInitialized);

		AnnotationMirror internalQualifiedElementMirror = Option.of(internalTupleElementMirrors).map(Tuple2::_1).getOrNull();
		AnnotationMirror internalDefaultElementMirror = Option.of(internalTupleElementMirrors).map(Tuple2::_2).getOrNull();

		AnnotationMirror elementMirror = ObjectUtils.firstNonNull(
				internalQualifiedElementMirror, internalDefaultElementMirror, externalElementMirror
		);

		/* Get the internal qualified argument name of the element */
		String internalQualifiedElementName = WsIOUtils.getAnnotationTypeValue(
				internalQualifiedElementMirror, "name", String.class, XML_DEFAULT_VALUE
		);

		/* Get the internal qualified argument namespace of the element */
		String internalQualifiedElementNamespace = WsIOUtils.getAnnotationTypeValue(
				internalQualifiedElementMirror, "namespace", String.class, XML_DEFAULT_VALUE
		);

		/* Get the internal qualified argument required of the element */
		Boolean internalQualifiedElementRequired = WsIOUtils.getAnnotationTypeValue(
				internalQualifiedElementMirror, "required", Boolean.class, XML_FLAG_VALUE
		);

		/* Get the internal qualified argument nillable of the element */
		Boolean internalQualifiedElementNillable = WsIOUtils.getAnnotationTypeValue(
				internalQualifiedElementMirror, "nillable", Boolean.class, XML_FLAG_VALUE
		);

		/* Get the internal qualified argument default value of the element */
		String internalQualifiedElementDefaultValue = WsIOUtils.getAnnotationTypeValue(
				internalQualifiedElementMirror, "defaultValue", String.class, XML_ZERO_VALUE
		);

		/* Get the internal qualified argument type of the element */
		String internalQualifiedElementType = WsIOUtils.getAnnotationLiteralValue(internalQualifiedElementMirror, "type");

		/* Get the internal default argument name of the element */
		String internalDefaultElementName = WsIOUtils.getAnnotationTypeValue(
				internalDefaultElementMirror, "name", String.class, XML_DEFAULT_VALUE
		);

		/* Get the internal default argument namespace of the element */
		String internalDefaultElementNamespace = WsIOUtils.getAnnotationTypeValue(
				internalDefaultElementMirror, "namespace", String.class, XML_DEFAULT_VALUE
		);

		/* Get the internal default argument required of the element */
		Boolean internalDefaultElementRequired = WsIOUtils.getAnnotationTypeValue(
				internalDefaultElementMirror, "required", Boolean.class, XML_FLAG_VALUE
		);

		/* Get the internal default argument nillable of the element */
		Boolean internalDefaultElementNillable = WsIOUtils.getAnnotationTypeValue(
				internalDefaultElementMirror, "nillable", Boolean.class, XML_FLAG_VALUE
		);

		/* Get the internal default argument default value of the element */
		String internalDefaultElementDefaultValue = WsIOUtils.getAnnotationTypeValue(
				internalDefaultElementMirror, "defaultValue", String.class, XML_ZERO_VALUE
		);

		/* Get the internal default argument type of the element */
		String internalDefaultElementType = WsIOUtils.getAnnotationLiteralValue(internalDefaultElementMirror, "type");

		/* Get the external argument name of the element */
		String externalElementName = WsIOUtils.getAnnotationTypeValue(
				externalElementMirror, "name", String.class, XML_DEFAULT_VALUE
		);

		/* Get the external argument namespace of the element */
		String externalElementNamespace = WsIOUtils.getAnnotationTypeValue(
				externalElementMirror, "namespace", String.class, XML_DEFAULT_VALUE
		);

		/* Get the external argument required of the element */
		Boolean externalElementRequired = WsIOUtils.getAnnotationTypeValue(
				externalElementMirror, "required", Boolean.class, XML_FLAG_VALUE
		);

		/* Get the external argument nillable of the element */
		Boolean externalElementNillable = WsIOUtils.getAnnotationTypeValue(
				externalElementMirror, "nillable", Boolean.class, XML_FLAG_VALUE
		);

		/* Get the external argument default value of the element */
		String externalElementDefaultValue = WsIOUtils.getAnnotationTypeValue(
				externalElementMirror, "defaultValue", String.class, XML_ZERO_VALUE
		);

		/* Get the external argument type of the element */
		String externalElementType = WsIOUtils.getAnnotationLiteralValue(externalElementMirror, "type");

		AnnotationMirror externalElementWrapperMirror = WsIOUtils.getAnnotationMirror(element, XmlElementWrapper.class);
		Tuple2<AnnotationMirror, AnnotationMirror> internalTupleElementWrapperMirrors = WsIOUtils.getQualifiedAnnotationMirror(
				element, WsIOElementWrapper.class, WsIOElementWrappers.class, qualifierInitialized
		);

		AnnotationMirror internalQualifiedElementWrapperMirror = Option.of(internalTupleElementWrapperMirrors).map(Tuple2::_1).getOrNull();
		AnnotationMirror internalDefaultElementWrapperMirror = Option.of(internalTupleElementWrapperMirrors).map(Tuple2::_2).getOrNull();

		AnnotationMirror elementWrapperMirror = ObjectUtils.firstNonNull(
				internalQualifiedElementWrapperMirror, internalDefaultElementWrapperMirror, externalElementWrapperMirror
		);

		/* Get the internal qualified argument name of the element wrapper */
		String internalQualifiedElementWrapperName = WsIOUtils.getAnnotationTypeValue(
				internalQualifiedElementWrapperMirror, "name", String.class, XML_DEFAULT_VALUE
		);

		/* Get the internal qualified argument namespace of the element wrapper */
		String internalQualifiedElementWrapperNamespace = WsIOUtils.getAnnotationTypeValue(
				internalQualifiedElementWrapperMirror, "namespace", String.class, XML_DEFAULT_VALUE
		);

		/* Get the internal qualified argument required of the element wrapper */
		Boolean internalQualifiedElementWrapperRequired = WsIOUtils.getAnnotationTypeValue(
				internalQualifiedElementWrapperMirror, "required", Boolean.class, XML_FLAG_VALUE
		);

		/* Get the internal qualified argument nillable of the element wrapper */
		Boolean internalQualifiedElementWrapperNillable = WsIOUtils.getAnnotationTypeValue(
				internalQualifiedElementWrapperMirror, "nillable", Boolean.class, XML_FLAG_VALUE
		);

		/* Get the internal default argument name of the element wrapper */
		String internalDefaultElementWrapperName = WsIOUtils.getAnnotationTypeValue(
				internalDefaultElementWrapperMirror, "name", String.class, XML_DEFAULT_VALUE
		);

		/* Get the internal default argument namespace of the element wrapper */
		String internalDefaultElementWrapperNamespace = WsIOUtils.getAnnotationTypeValue(
				internalDefaultElementWrapperMirror, "namespace", String.class, XML_DEFAULT_VALUE
		);

		/* Get the internal default argument required of the element wrapper */
		Boolean internalDefaultElementWrapperRequired = WsIOUtils.getAnnotationTypeValue(
				internalDefaultElementWrapperMirror, "required", Boolean.class, XML_FLAG_VALUE
		);

		/* Get the internal default argument nillable of the element wrapper */
		Boolean internalDefaultElementWrapperNillable = WsIOUtils.getAnnotationTypeValue(
				internalDefaultElementWrapperMirror, "nillable", Boolean.class, XML_FLAG_VALUE
		);

		/* Get the external argument name of the element wrapper */
		String externalElementWrapperName = WsIOUtils.getAnnotationTypeValue(
				externalElementWrapperMirror, "name", String.class, XML_DEFAULT_VALUE
		);

		/* Get the external argument namespace of the element wrapper */
		String externalElementWrapperNamespace = WsIOUtils.getAnnotationTypeValue(
				externalElementWrapperMirror, "namespace", String.class, XML_DEFAULT_VALUE
		);

		/* Get the external argument required of the element wrapper */
		Boolean externalElementWrapperRequired = WsIOUtils.getAnnotationTypeValue(
				externalElementWrapperMirror, "required", Boolean.class, XML_FLAG_VALUE
		);

		/* Get the external argument nillable of the element wrapper */
		Boolean externalElementWrapperNillable = WsIOUtils.getAnnotationTypeValue(
				externalElementWrapperMirror, "nillable", Boolean.class, XML_FLAG_VALUE
		);

		AnnotationMirror externalAttributeMirror = WsIOUtils.getAnnotationMirror(element, XmlAttribute.class);
		Tuple2<AnnotationMirror, AnnotationMirror> internalTupleAttributeMirrors = WsIOUtils.getQualifiedAnnotationMirror(
				element, WsIOAttribute.class, WsIOAttributes.class, qualifierInitialized
		);

		AnnotationMirror internalQualifiedAttributeMirror = Option.of(internalTupleAttributeMirrors).map(Tuple2::_1).getOrNull();
		AnnotationMirror internalDefaultAttributeMirror = Option.of(internalTupleAttributeMirrors).map(Tuple2::_2).getOrNull();

		AnnotationMirror attributeMirror = ObjectUtils.firstNonNull(
				internalQualifiedAttributeMirror, internalDefaultAttributeMirror, externalAttributeMirror
		);

		/* Get the internal qualified argument name of the attribute */
		String internalQualifiedAttributeName = WsIOUtils.getAnnotationTypeValue(
				internalQualifiedAttributeMirror, "name", String.class, XML_DEFAULT_VALUE
		);

		/* Get the internal qualified argument namespace of the attribute */
		String internalQualifiedAttributeNamespace = WsIOUtils.getAnnotationTypeValue(
				internalQualifiedAttributeMirror, "namespace", String.class, XML_DEFAULT_VALUE
		);

		/* Get the internal qualified argument required of the attribute */
		Boolean internalQualifiedAttributeRequired = WsIOUtils.getAnnotationTypeValue(
				internalQualifiedAttributeMirror, "required", Boolean.class, XML_FLAG_VALUE
		);

		/* Get the internal default argument name of the attribute */
		String internalDefaultAttributeName = WsIOUtils.getAnnotationTypeValue(
				internalDefaultAttributeMirror, "name", String.class, XML_DEFAULT_VALUE
		);

		/* Get the internal default argument namespace of the attribute */
		String internalDefaultAttributeNamespace = WsIOUtils.getAnnotationTypeValue(
				internalDefaultAttributeMirror, "namespace", String.class, XML_DEFAULT_VALUE
		);

		/* Get the internal default argument required of the attribute */
		Boolean internalDefaultAttributeRequired = WsIOUtils.getAnnotationTypeValue(
				internalDefaultAttributeMirror, "required", Boolean.class, XML_FLAG_VALUE
		);

		/* Get the external argument name of the attribute */
		String externalAttributeName = WsIOUtils.getAnnotationTypeValue(
				externalAttributeMirror, "name", String.class, XML_DEFAULT_VALUE
		);

		/* Get the external argument namespace of the attribute */
		String externalAttributeNamespace = WsIOUtils.getAnnotationTypeValue(
				externalAttributeMirror, "namespace", String.class, XML_DEFAULT_VALUE
		);

		/* Get the external argument required of the attribute */
		Boolean externalAttributeRequired = WsIOUtils.getAnnotationTypeValue(
				externalAttributeMirror, "required", Boolean.class, XML_FLAG_VALUE
		);

		AnnotationMirror externalTransientMirror = WsIOUtils.getAnnotationMirror(element, XmlTransient.class);
		Tuple2<AnnotationMirror, AnnotationMirror> internalTupleTransientMirrors = WsIOUtils.getQualifiedAnnotationMirror(
				element, WsIOTransient.class, WsIOTransients.class, qualifierInitialized
		);

		AnnotationMirror internalQualifiedTransientMirror = Option.of(internalTupleTransientMirrors).map(Tuple2::_1).getOrNull();
		AnnotationMirror internalDefaultTransientMirror = Option.of(internalTupleTransientMirrors).map(Tuple2::_2).getOrNull();

		AnnotationMirror transientMirror = ObjectUtils.firstNonNull(
				internalQualifiedTransientMirror, internalDefaultTransientMirror, externalTransientMirror
		);

		AnnotationMirror externalValueMirror = WsIOUtils.getAnnotationMirror(element, XmlValue.class);
		Tuple2<AnnotationMirror, AnnotationMirror> internalTupleValueMirrors = WsIOUtils.getQualifiedAnnotationMirror(
				element, WsIOValue.class, WsIOValues.class, qualifierInitialized
		);

		AnnotationMirror internalQualifiedValueMirror = Option.of(internalTupleValueMirrors).map(Tuple2::_1).getOrNull();
		AnnotationMirror internalDefaultValueMirror = Option.of(internalTupleValueMirrors).map(Tuple2::_2).getOrNull();

		AnnotationMirror valueMirror = ObjectUtils.firstNonNull(
				internalQualifiedValueMirror, internalDefaultValueMirror, externalValueMirror
		);

		AnnotationMirror externalAdapterMirror = WsIOUtils.getAnnotationMirror(element, XmlJavaTypeAdapter.class);
		Tuple2<AnnotationMirror, AnnotationMirror> internalTupleAdapterMirror = WsIOUtils.getQualifiedAnnotationMirror(
				element, WsIOJavaTypeAdapter.class, WsIOJavaTypeAdapters.class, qualifierInitialized
		);

		AnnotationMirror internalQualifiedAdapterMirror = Option.of(internalTupleAdapterMirror).map(Tuple2::_1).getOrNull();
		AnnotationMirror internalDefaultAdapterMirror = Option.of(internalTupleAdapterMirror).map(Tuple2::_2).getOrNull();

		AnnotationMirror adapterMirror = ObjectUtils.firstNonNull(
				internalQualifiedAdapterMirror, internalDefaultAdapterMirror, externalAdapterMirror
		);

		/* Get the internal qualified adapter value of the adapter */
		String internalQualifiedAdapterValue = WsIOUtils.getAnnotationLiteralValue(internalQualifiedAdapterMirror, "value");

		/* Get the internal qualified adapter type of the adapter */
		String internalQualifiedAdapterType = WsIOUtils.getAnnotationLiteralValue(internalQualifiedAdapterMirror, "type");

		/* Get the internal default adapter value of the adapter */
		String internalDefaultAdapterValue = WsIOUtils.getAnnotationLiteralValue(internalDefaultAdapterMirror, "value");

		/* Get the internal default adapter type of the adapter */
		String internalDefaultAdapterType = WsIOUtils.getAnnotationLiteralValue(internalDefaultAdapterMirror, "type");

		/* Get the external adapter value of the adapter */
		String externalAdapterValue = WsIOUtils.getAnnotationLiteralValue(externalAdapterMirror, "value");

		/* Get the external adapter type of the adapter */
		String externalAdapterType = WsIOUtils.getAnnotationLiteralValue(externalAdapterMirror, "type");

		/* Get the priority param values */
		String paramName = WsIOUtils.getFirstStringNonEqualsTo(XML_EMPTY_VALUE, internalParamName, externalParamName);
		String paramPartName = WsIOUtils.getFirstStringNonEqualsTo(XML_EMPTY_VALUE, internalParamPartName, externalParamPartName);
		String paramTargetNamespace = WsIOUtils.getFirstStringNonEqualsTo(XML_EMPTY_VALUE, internalParamTargetNamespace, externalParamTargetNamespace);
		String paramMode = StringUtils.firstNonBlank(internalParamMode, externalParamMode);
		Boolean paramHeader = ObjectUtils.firstNonNull(internalParamHeader, externalParamHeader);

		/* Get the priority element values */
		String internalElementName = WsIOUtils.getFirstStringNonEqualsTo(XML_DEFAULT_VALUE, internalQualifiedElementName, internalDefaultElementName);
		String internalElementNamespace = WsIOUtils.getFirstStringNonEqualsTo(XML_DEFAULT_VALUE, internalQualifiedElementNamespace, internalDefaultElementNamespace);
		Boolean internalElementRequired = ObjectUtils.firstNonNull(internalQualifiedElementRequired, internalDefaultElementRequired);
		Boolean internalElementNillable = ObjectUtils.firstNonNull(internalQualifiedElementNillable, internalDefaultElementNillable);
		String internalElementDefaultValue = ObjectUtils.firstNonNull(internalQualifiedElementDefaultValue, internalDefaultElementDefaultValue);
		String internalElementType = ObjectUtils.firstNonNull(internalQualifiedElementType, internalDefaultElementType);
		String elementName = getFirstStringNonEqualsTo(XML_DEFAULT_VALUE, internalElementName, externalElementName);
		String elementNamespace = getFirstStringNonEqualsTo(XML_DEFAULT_VALUE, internalElementNamespace, externalElementNamespace);
		Boolean elementRequired = ObjectUtils.firstNonNull(internalElementRequired, externalElementRequired);
		Boolean elementNillable = ObjectUtils.firstNonNull(internalElementNillable, externalElementNillable);
		String elementDefaultValue = ObjectUtils.firstNonNull(internalElementDefaultValue, externalElementDefaultValue);
		String elementType = ObjectUtils.firstNonNull(internalElementType, externalElementType);

		/* Get the priority element wrapper values */
		String internalElementWrapperName = WsIOUtils.getFirstStringNonEqualsTo(XML_DEFAULT_VALUE, internalQualifiedElementWrapperName, internalDefaultElementWrapperName);
		String internalElementWrapperNamespace = WsIOUtils.getFirstStringNonEqualsTo(XML_DEFAULT_VALUE, internalQualifiedElementWrapperNamespace, internalDefaultElementWrapperNamespace);
		Boolean internalElementWrapperRequired = ObjectUtils.firstNonNull(internalQualifiedElementWrapperRequired, internalDefaultElementWrapperRequired);
		Boolean internalElementWrapperNillable = ObjectUtils.firstNonNull(internalQualifiedElementWrapperNillable, internalDefaultElementWrapperNillable);
		String elementWrapperName = WsIOUtils.getFirstStringNonEqualsTo(XML_DEFAULT_VALUE, internalElementWrapperName, externalElementWrapperName);
		String elementWrapperNamespace = WsIOUtils.getFirstStringNonEqualsTo(XML_DEFAULT_VALUE, internalElementWrapperNamespace, externalElementWrapperNamespace);
		Boolean elementWrapperRequired = ObjectUtils.firstNonNull(internalElementWrapperRequired, externalElementWrapperRequired);
		Boolean elementWrapperNillable = ObjectUtils.firstNonNull(internalElementWrapperNillable, externalElementWrapperNillable);

		/* Get the priority attribute values */
		String internalAttributeName = WsIOUtils.getFirstStringNonEqualsTo(XML_DEFAULT_VALUE, internalQualifiedAttributeName, internalDefaultAttributeName);
		String internalAttributeNamespace = WsIOUtils.getFirstStringNonEqualsTo(XML_DEFAULT_VALUE, internalQualifiedAttributeNamespace, internalDefaultAttributeNamespace);
		Boolean internalAttributeRequired = ObjectUtils.firstNonNull(internalQualifiedAttributeRequired, internalDefaultAttributeRequired);
		String attributeName = WsIOUtils.getFirstStringNonEqualsTo(XML_DEFAULT_VALUE, internalAttributeName, externalAttributeName);
		String attributeNamespace = WsIOUtils.getFirstStringNonEqualsTo(XML_DEFAULT_VALUE, internalAttributeNamespace, externalAttributeNamespace);
		Boolean attributeRequired = ObjectUtils.firstNonNull(internalAttributeRequired, externalAttributeRequired);

		/* Get the priority adapter values */
		String internalAdapterValue = StringUtils.firstNonBlank(internalQualifiedAdapterValue, internalDefaultAdapterValue);
		String internalAdapterType = StringUtils.firstNonBlank(internalQualifiedAdapterType, internalDefaultAdapterType);
		String adapterValue = StringUtils.firstNonBlank(internalAdapterValue, externalAdapterValue);
		String adapterType = StringUtils.firstNonBlank(internalAdapterType, externalAdapterType);

		boolean paramPresent = paramMirror != null;
		boolean elementPresent = elementMirror != null;
		boolean elementWrapperPresent = elementWrapperMirror != null;
		boolean attributePresent = attributeMirror != null;
		boolean transientPresent = transientMirror != null;
		boolean valuePresent = valueMirror != null;
		boolean adapterPresent = adapterMirror != null;

		/* Get the ws io implicit qualifier prefix */
		String qualifierImplicitPrefix = WsIOUtils.getAnnotationTypeValue(
				element, WsIOQualifier.class, "prefix", String.class, ""
		);

		/* Get the ws io implicit qualifier suffix */
		String qualifierImplicitSuffix = WsIOUtils.getAnnotationTypeValue(
				element, WsIOQualifier.class, "suffix", String.class, ""
		);

		/* Get the ws io explicit qualifier prefix */
		String qualifierExplicitPrefix = qualifierInitialized.getQualifierPrefix();

		/* Get the ws io explicit qualifier suffix */
		String qualifierExplicitSuffix = qualifierInitialized.getQualifierSuffix();

		/* Get the ws io qualifier prefix */
		String qualifierPrefix = WsIOUtils.getFirstStringNonEqualsTo("", qualifierExplicitPrefix, qualifierImplicitPrefix);

		/* Get the ws io qualifier suffix */
		String qualifierSuffix = WsIOUtils.getFirstStringNonEqualsTo("", qualifierExplicitSuffix, qualifierImplicitSuffix);

		/* Get the qualifier info */
		WsIOQualifierInfo qualifierInfo = WsIOQualifierInfo.of(qualifierPrefix, qualifierSuffix);

		return WsIOMemberInfo.of(
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
				qualifierInfo
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
		if (target != null) {
			String annotationName = annotation.getName();
			for (AnnotationMirror annotationMirror : target.getAnnotationMirrors()) {
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

		if (target != null) {

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
			for (AnnotationMirror wrapperMirror : target.getAnnotationMirrors()) {
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
	 * @param target element to process
	 * @return the annotation descriptions of the element
	 */
	static Map<DeclaredType, Map<String, String>> getAnnotationDescriptions(Element target) {
		if (target != null) {
			return Stream.ofAll(target.getAnnotationMirrors())
					.toMap(annotationMirror -> {

						DeclaredType annotationType = annotationMirror.getAnnotationType();
						Map<String, String> annotationDescription = HashMap.ofAll(annotationMirror.getElementValues())
								.toMap(tuple -> {

									AnnotationValue annotationValue = tuple._2();
									ExecutableElement executableElement = tuple._1();

									String fieldValue = annotationValue.toString();
									String fieldName = executableElement
											.getSimpleName()
											.toString();

									return Map.entry(fieldName, fieldValue);

								});

						return Map.entry(annotationType, annotationDescription);

					});
		}
		return null;
	}

	/**
	 * Method that returns the annotation mirror of an element.
	 *
	 * @param target element to process
	 * @param annotation class of the annotation
	 * @param wrapper class of the annotation wrapper
	 * @param qualifier pair indicating the qualifier to search
	 * @return the annotation mirror of an element
	 */
	static Tuple2<AnnotationMirror, AnnotationMirror> getQualifiedAnnotationMirror(Element target,
																				   Class<? extends Annotation> annotation,
																				   Class<? extends Annotation> wrapper,
																				   WsIOQualifierInfo qualifier) {
		return getQualifiedAnnotationMirror(target, annotation, wrapper, qualifier, "value");
	}

	/**
	 * Method that returns the annotation mirror of an element.
	 *
	 * @param target element to process
	 * @param annotation class of the annotation
	 * @param wrapper class of the annotation wrapper
	 * @param qualifier pair indicating the qualifier to search
	 * @param field name of the field
	 * @return the annotation mirror of an element
	 */
	static Tuple2<AnnotationMirror, AnnotationMirror> getQualifiedAnnotationMirror(Element target,
																				   Class<? extends Annotation> annotation,
																				   Class<? extends Annotation> wrapper,
																				   WsIOQualifierInfo qualifier,
																				   String field) {

		Tuple2<List<AnnotationMirror>, List<AnnotationMirror>> annotationMirrors = getQualifiedAnnotationMirrors(
				target, annotation, wrapper, qualifier, field
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
	 * @param target element to process
	 * @param annotation class of the annotation
	 * @param wrapper class of the annotation wrapper
	 * @param qualifier pair indicating the qualifier to search
	 * @return the annotation mirrors of an element
	 */
	static Tuple2<List<AnnotationMirror>, List<AnnotationMirror>> getQualifiedAnnotationMirrors(Element target,
																								Class<? extends Annotation> annotation,
																								Class<? extends Annotation> wrapper,
																								WsIOQualifierInfo qualifier) {
		return getQualifiedAnnotationMirrors(target, annotation, wrapper, qualifier, "value");
	}

	/**
	 * Method that returns the annotation mirrors of an element.
	 *
	 * @param target element to process
	 * @param annotation class of the annotation
	 * @param wrapper class of the annotation wrapper
	 * @param qualifier pair indicating the qualifier to search
	 * @param field name of the field
	 * @return the annotation mirrors of an element
	 */
	static Tuple2<List<AnnotationMirror>, List<AnnotationMirror>> getQualifiedAnnotationMirrors(Element target,
																								Class<? extends Annotation> annotation,
																								Class<? extends Annotation> wrapper,
																								WsIOQualifierInfo qualifier,
																								String field) {

		List<AnnotationMirror> annotationMirrors = getAnnotationMirrors(target, annotation, wrapper, field);

		if (annotationMirrors != null) {

			String qualifierPrefix = ObjectUtils.firstNonNull(qualifier != null ? qualifier.getQualifierPrefix() : null, "");
			String qualifierSuffix = ObjectUtils.firstNonNull(qualifier != null ? qualifier.getQualifierSuffix() : null, "");

			List<AnnotationMirror> matchingMirror = annotationMirrors
					.filter(annotationMirror -> {

						String prefix = getAnnotationTypeValue(annotationMirror, "prefix", String.class, XML_DEFAULT_VALUE);
						String suffix = getAnnotationTypeValue(annotationMirror, "suffix", String.class, XML_DEFAULT_VALUE);

						return qualifierPrefix.equals(prefix)
								&& qualifierSuffix.equals(suffix);

					});

			List<AnnotationMirror> defaultMirror = annotationMirrors
					.filter(annotationMirror -> {

						String prefix = getAnnotationTypeValue(annotationMirror, "prefix", String.class, XML_DEFAULT_VALUE);
						String suffix = getAnnotationTypeValue(annotationMirror, "suffix", String.class, XML_DEFAULT_VALUE);

						boolean exact = qualifierPrefix.equals(prefix)
								&& qualifierSuffix.equals(suffix);

						return !exact
								&& (qualifierPrefix.equals(prefix) || XML_DEFAULT_VALUE.equals(prefix))
								&& (qualifierSuffix.equals(suffix) || XML_DEFAULT_VALUE.equals(suffix));

					}).sortBy(annotationMirror -> {

						String prefix = getAnnotationTypeValue(annotationMirror, "prefix", String.class, XML_DEFAULT_VALUE);
						String suffix = getAnnotationTypeValue(annotationMirror, "suffix", String.class, XML_DEFAULT_VALUE);

						return (qualifierPrefix.equals(prefix) ? 1 : 0) + (qualifierSuffix.equals(suffix) ? 1 : 0);

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
