package com.fiberg.wsio.processor;

import com.fiberg.wsio.annotation.*;
import com.fiberg.wsio.enumerate.WsIOGenerate;
import com.fiberg.wsio.enumerate.WsIOType;
import com.fiberg.wsio.handler.state.*;
import com.fiberg.wsio.handler.time.WsIOInstant;
import com.fiberg.wsio.handler.time.WsIOTime;
import com.fiberg.wsio.util.WsIOUtil;
import com.squareup.javapoet.*;
import com.squareup.javapoet.TypeSpec.Builder;
import io.vavr.*;
import io.vavr.collection.*;
import io.vavr.control.Option;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.fiberg.wsio.processor.WsIOConstant.*;

/**
 * Class that generates all classes from WsIO, it generates the clones, the messages and wrappers.
 */
class WsIOGenerator {

	/** Handles the report of errors, warnings and another */
	private final Messager messager;

	/** Receives the generated class files */
	private final Filer filer;

	/* Forwarded getter include internal */
	private static final Boolean FORWARDED_GETTER_INCLUDE_INTERNAL = true;

	/* Forwarded external getter exclude classes */
	private static final List<Class<?>> FORWARDED_EXTERNAL_GETTER_EXCLUDE_CLASSES = List.of(
			XmlJavaTypeAdapter.class, XmlAttribute.class, XmlElementWrapper.class, XmlElement.class, XmlTransient.class,  XmlValue.class
	);

	/* Forwarded internal getter exclude classes */
	private static final List<Class<?>> FORWARDED_INTERNAL_GETTER_EXCLUDE_CLASSES = List.of(
			WsIOJavaTypeAdapter.class, WsIOAttribute.class, WsIOElementWrapper.class, WsIOElement.class, WsIOTransient.class, WsIOValue.class,
			WsIOJavaTypeAdapters.class, WsIOAttributes.class, WsIOElementWrappers.class, WsIOElements.class, WsIOTransients.class, WsIOValues.class
	);

	/* Forwarded internal getter exclude classes */
	private static final List<Class<?>> FORWARDED_GETTER_EXCLUDE_CLASSES = !FORWARDED_GETTER_INCLUDE_INTERNAL
			? FORWARDED_EXTERNAL_GETTER_EXCLUDE_CLASSES
			: List.of(FORWARDED_EXTERNAL_GETTER_EXCLUDE_CLASSES, FORWARDED_INTERNAL_GETTER_EXCLUDE_CLASSES)
					.flatMap(Function.identity());

	/**
	 * @param messager handles the report of errors, warnings and another
	 * @param filer receives the generated class files
	 */
	WsIOGenerator(Messager messager, Filer filer) {
		this.messager = messager;
		this.filer = filer;
	}

	/**
	 * Method that generates all classes from WsIO, it generates the clones, the messages and wrappers.
	 *
	 * @param messageByType       map containing type element and destination package
	 * @param cloneByGroup        map containing the identifier { prefix name - suffix name } and a set of type elements
	 * @param cloneMessageByGroup map containing type element and an inner map with method name and a tuple of { method info - destination package }
	 * @param wrapperByType       map containing type element and an inner map with method name and a tuple of { method info - destination package }
	 * @param metadataByType      map containing type element and a tuple of { package name - cases to generate - set fo field names }
	 * @return {@code true} when all classes were generated ok, {@code false} otherwise
	 */
	boolean generateClasses(Map<TypeElement, String> messageByType,
	                        Map<WsIOIdentifier, Set<WsIODestination>> cloneByGroup,
	                        Map<WsIOIdentifier, Set<WsIODestination>> cloneMessageByGroup,
	                        Map<TypeElement, Map<String, Tuple3<ExecutableElement, WsIODescriptor, String>>> wrapperByType,
	                        Map<TypeElement, Tuple3<String, Set<Case>, Map<String, Boolean>>> metadataByType) {

		/* Get all class names that are message annotated */
		Map<String, String> messageClasses = messageByType.mapKeys(element ->
				element.getQualifiedName().toString());

		/* Map of types by name */
		Map<String, TypeElement> typeByName = cloneByGroup.values()
				.flatMap(Function.identity())
				.map(WsIODestination::getElementType)
				.appendAll(messageByType.keySet())
				.appendAll(wrapperByType.keySet())
				.toSet()
				.toMap(element -> element.getQualifiedName().toString(), e -> e);

		/* Process and create each clone by group */
		List<Tuple3<TypeSpec, String, String>> cloneTypes = cloneByGroup.flatMap((clone) -> {

			/* Get identifier key and set of elements */
			WsIOIdentifier identifier = clone._1();
			Set<WsIODestination> elements = clone._2();

			/* Get identifier, clone classes types and names */
			Map<String, String> cloneClasses = cloneByGroup.getOrElse(identifier, HashSet.empty())
					.toMap(destinationDescriptor -> Map.entry(
							destinationDescriptor.getElementType().getQualifiedName().toString(),
							destinationDescriptor.getElementPackage()));

			/* Build the wrapper identifier, the clone message classes and the generate-target */
			WsIOGenerate generate = WsIOGenerate.CLONE;
			Map<String, String> cloneMessageClasses = HashMap.empty();
			WsIOIdentifier wrapper = WsIOIdentifier.of(StringUtils.EMPTY, StringUtils.EMPTY);

			/* Get prefix and suffix of identifier */
			String prefixName = identifier.getIdentifierPrefix();
			String suffixName = identifier.getIdentifierSuffix();

			/* Create the context of the generation
			 * and set the type as null since this are the simple non-prefixed and non-suffixed clones */
			WsIOType type = null;
			WsIOContext context = WsIOContext.of(
					identifier, wrapper, messageClasses, cloneClasses, cloneMessageClasses, typeByName, generate, type
			);

			/* Create a delegate for each group and type element */
			return elements.map((destinationDescriptor) -> {

				/* Extract type element and package name */
				TypeElement element = destinationDescriptor.getElementType();
				String packageName = destinationDescriptor
						.getElementPackage();

				/* Create each delegate */
				TypeSpec typeSpec = generateDelegateType(element, context);

				/* Class Name */
				String classInnerName = WsIOUtils.getFullSimpleInnerName(element);
				String className = WsIOUtil.addWrap(classInnerName, prefixName, suffixName);

				/* Tuple of type spec and package name */
				return Tuple.of(typeSpec, packageName, className);

			});

		}).filter(tuple -> Objects.nonNull(tuple._1())).toList();

		/* Process and create each message by group */
		List<Tuple3<TypeSpec, String, String>> messageTypes = messageByType.flatMap((message) -> {

			/* Get type element and package name */
			TypeElement element = message._1();
			String packageName = message._2();

			/* Prefix and suffix of request and response */
			List<WsIOType> types = List.of(WsIOType.RESPONSE, WsIOType.REQUEST);
			Map<WsIOType, WsIOIdentifier> namesByType = HashMap.of(
					WsIOType.RESPONSE, WsIOIdentifier.of(RESPONSE_PREFIX, RESPONSE_SUFFIX),
					WsIOType.REQUEST, WsIOIdentifier.of(REQUEST_PREFIX, REQUEST_SUFFIX)
			);

			/* Return the type spec for responses and requests */
			return types.map(type -> {

				/* Get the identifier using the type, the prefix and the suffix names */
				WsIOIdentifier wrapper = namesByType.get(type).get();
				String prefixName = wrapper.getIdentifierPrefix();
				String suffixName = wrapper.getIdentifierSuffix();

				/* Get identifier, clone classes types and names */
				Map<String, String> cloneClasses = cloneByGroup.getOrElse(wrapper, HashSet.empty())
						.toMap(destinationDescriptor -> Map.entry(
								destinationDescriptor.getElementType().getQualifiedName().toString(),
								destinationDescriptor.getElementPackage()));

				/* Build the main identifier, the clone message classes and the generate-target */
				WsIOGenerate generate = WsIOGenerate.MESSAGE;
				Map<String, String> cloneMessageClasses = HashMap.empty();
				WsIOIdentifier identifier = WsIOIdentifier.of(StringUtils.EMPTY, StringUtils.EMPTY);

				/* Create the context of the generation */
				WsIOContext context = WsIOContext.of(
						identifier, wrapper, messageClasses, cloneClasses, cloneMessageClasses, typeByName, generate, type
				);

				/* Create each delegate */
				TypeSpec typeSpec = generateDelegateType(element, context);

				/* Class Name */
				String classInnerName = WsIOUtils.getFullSimpleInnerName(element);
				String className = WsIOUtil.addWrap(classInnerName, prefixName, suffixName);

				/* Tuple of type spec and package name */
				return Tuple.of(typeSpec, packageName, className);

			});

		}).filter(tuple -> Objects.nonNull(tuple._1())).toList();

		/* Process and create each clone message by group */
		List<Tuple3<TypeSpec, String, String>> cloneMessageTypes = cloneMessageByGroup.flatMap((clone) -> {

			/* Get identifier key and set of elements */
			WsIOIdentifier identifier = clone._1();
			Set<WsIODestination> elements = clone._2();

			/* Get identifier, clone classes types and names */
			Map<String, String> cloneClasses = cloneByGroup.getOrElse(identifier, HashSet.empty())
					.toMap(destinationDescriptor -> Map.entry(
							destinationDescriptor.getElementType().getQualifiedName().toString(),
							destinationDescriptor.getElementPackage()));

			Map<String, String> cloneMessageClasses = cloneMessageByGroup.getOrElse(identifier, HashSet.empty())
					.toMap(destinationDescriptor -> Map.entry(
							destinationDescriptor.getElementType().getQualifiedName().toString(),
							destinationDescriptor.getElementPackage()));

			/* Create a delegate for each group and type element */
			return elements.flatMap((destinationDescriptor) -> {

				/* Extract type element and package name */
				TypeElement element = destinationDescriptor.getElementType();
				String packageName = destinationDescriptor
						.getElementPackage();

				/* Prefix and suffix of request and response */
				List<WsIOType> types = List.of(WsIOType.RESPONSE, WsIOType.REQUEST);
				Map<WsIOType, WsIOIdentifier> namesByType = HashMap.of(
						WsIOType.RESPONSE, WsIOIdentifier.of(RESPONSE_PREFIX, RESPONSE_SUFFIX),
						WsIOType.REQUEST, WsIOIdentifier.of(REQUEST_PREFIX, REQUEST_SUFFIX)
				);

				/* Return the tuple of spec package name and class name of each wrapper */
				return types.map(type -> {

					/* Get the identifier using the type and the generate-target */
					WsIOIdentifier wrapper = namesByType.get(type).get();
					WsIOGenerate generate = WsIOGenerate.CLONE_MESSAGE;

					/* Context of the generation */
					WsIOContext context = WsIOContext.of(
							identifier, wrapper, messageClasses, cloneClasses, cloneMessageClasses, typeByName, generate, type
					);

					/* Create each delegate */
					TypeSpec typeSpec = generateDelegateType(element, context);

					/* Check if elements are in sets and get name by generate */
					String elementName = element.getQualifiedName().toString();
					WsIOIdentifier names = context.getNameByGenerate(elementName);

					/* Suffix and prefix names */
					String prefixName = names.getIdentifierPrefix();
					String suffixName = names.getIdentifierSuffix();

					/* Class Name */
					String classInnerName = WsIOUtils.getFullSimpleInnerName(element);
					String className = WsIOUtil.addWrap(classInnerName, prefixName, suffixName);

					/* Tuple of type spec and package name */
					return Tuple.of(typeSpec, packageName, className);

				});

			});

		}).filter(tuple -> Objects.nonNull(tuple._1())).toList();

		/* Iterate for each type spec of clones, requests and responses */
		cloneTypes.appendAll(messageTypes).appendAll(cloneMessageTypes).forEach(tuple -> {

			/* Get type spec, package name and class name */
			TypeSpec typeSpec = tuple._1();
			String packageName = tuple._2();
			String className = tuple._3();

			/* Generate the java class */
			createJavaClass(typeSpec, packageName, className);

		});

		/* Function used to transform clone maps */
		Function1<Map<WsIOIdentifier, Set<WsIODestination>>, Map<String, Map<WsIOIdentifier, String>>> transformClones = (clone) -> clone
				.flatMap(tuple -> tuple._2()
						.map(destination -> Tuple.of(tuple._1(), destination.getElementType(), destination.getElementPackage())))
				.groupBy(Tuple3::_2)
				.mapKeys(TypeElement::getQualifiedName)
				.mapKeys(Name::toString)
				.mapValues(values -> values.toMap(Tuple3::_1, Tuple3::_3));

		/* Set with the names of the clones classes */
		Map<String, Map<WsIOIdentifier, String>> cloneClasses = transformClones.apply(cloneByGroup);

		/* Set with the names of the clones message classes */
		Map<String, Map<WsIOIdentifier, String>> cloneMessageClasses = transformClones.apply(cloneMessageByGroup);

		/* Iterate for each wrapper and generate the classes */
		wrapperByType.forEach((element, wrapper) -> {

			/* Iterate for each method name and descriptor */
			wrapper.forEach((name, descriptors) -> {

				/* Executable element, descriptor and package name */
				ExecutableElement executableElement = descriptors._1();
				WsIODescriptor operationDescriptor = descriptors._2();
				String packageName = descriptors._3();

				WsIOExecutable requestDescriptor = WsIOUtils.extractExecutableDescriptor(
						element, executableElement, operationDescriptor, WsIOType.REQUEST
				);

				WsIOExecutable responseDescriptor = WsIOUtils.extractExecutableDescriptor(
						element, executableElement, operationDescriptor, WsIOType.RESPONSE
				);

				/* Generate request and response type spec */
				TypeSpec request = generateWrapperType(
						WsIOType.REQUEST, requestDescriptor, messageClasses, cloneClasses, cloneMessageClasses, typeByName
				);
				TypeSpec response = generateWrapperType(
						WsIOType.RESPONSE, responseDescriptor, messageClasses, cloneClasses, cloneMessageClasses, typeByName
				);

				/* Get upper name of the method and class */
				String upperName = WordUtils.capitalize(name);

				/* Get final class names */
				String responseClassName = WsIOUtil.addWrap(upperName, RESPONSE_WRAPPER_PREFIX, RESPONSE_WRAPPER_SUFFIX);
				String requestClassName = WsIOUtil.addWrap(upperName, REQUEST_WRAPPER_PREFIX, REQUEST_WRAPPER_SUFFIX);

				/* Create each java class */
				createJavaClass(request, packageName, requestClassName);
				createJavaClass(response, packageName, responseClassName);

			});

		});

		/* Iterate for each type */
		metadataByType.forEach((type, info) -> {

			/* Get package name, set of cases and map with fields */
			String packageName = info._1();
			Set<Case> cases = info._2();
			Map<String, Boolean> fields = info._3();

			/* Build all the field specs */
			List<FieldSpec> constantFieldSpecs = fields.toStream()
					.flatMap(fieldInfo -> {

						/* Get field name and is static flag */
						String fieldName = fieldInfo._1();
						Boolean isStatic = fieldInfo._2();

						/* Transform the values and return the set */
						return cases.toStream()
								.map(destination ->
										Tuple.of(WsIOUtils.transformField(fieldName,
												isStatic, destination), fieldName));

					}).map(tuple -> FieldSpec.builder(ClassName.get(String.class), tuple._1(),
							Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
							.initializer("$S", tuple._2())
							.build()).toList();

			/* Generate delegate field spec */
			Option<FieldSpec> delegateFieldSpec = Option.of(type)
					.map(typeElement -> {

						/* Create generic type name */
						TypeName typeName = ParameterizedTypeName.get(ClassName.get(Class.class),
								TypeName.get(typeElement.asType()));

						/* Build and return the field */
						return FieldSpec.builder(typeName, WsIOConstant.METADATA_DELEGATE_FIELD,
								Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
								.initializer("$T.class", typeElement)
								.build();

					});

			/* Get class name, full class name and create the type spec */
			String className = WsIOUtil.addWrap(type.getSimpleName().toString(),
					WsIOConstant.METADATA_PREFIX, WsIOConstant.METADATA_SUFFIX);
			TypeSpec typeSpec = TypeSpec.classBuilder(className)
					.addFields(Stream.concat(delegateFieldSpec, constantFieldSpecs))
					.addModifiers(Modifier.PUBLIC)
					.build();

			/* Create the java class */
			createJavaClass(typeSpec, packageName, className);

		});

		/* Returns true to the processor */
		return true;

	}

	/**
	 * Method that generates the delegate types of messages and clones.
	 *
	 * @param element type element
	 * @param context context of the process
	 * @return delegate types of messages and clones.
	 */
	private TypeSpec generateDelegateType(TypeElement element,
	                                      WsIOContext context) {

		WsIOIdentifier mainIdentifier = context.getMainIdentifier();

		String prefixClassName = ObjectUtils.firstNonNull(mainIdentifier != null ? mainIdentifier.getIdentifierPrefix() : null, "");
		String suffixClassName = ObjectUtils.firstNonNull(mainIdentifier != null ? mainIdentifier.getIdentifierSuffix() : null, "");

		WsIOIdentifier wrapperIdentifier = context.getWrapperIdentifier();

		String prefixWrapperName = ObjectUtils.firstNonNull(wrapperIdentifier != null ? wrapperIdentifier.getIdentifierPrefix() : null, "");
		String suffixWrapperName = ObjectUtils.firstNonNull(wrapperIdentifier != null ? wrapperIdentifier.getIdentifierSuffix() : null, "");

		/* Full prefix and suffix names */
		String fullPrefixName = prefixWrapperName + prefixClassName;
		String fullSuffixName = suffixClassName + suffixWrapperName;

		/* Full class name and full inner class name */
		String fullInnerClassName = WsIOUtils.getFullSimpleInnerName(element);
		String fullClassName = WsIOUtil.addWrap(fullInnerClassName, fullPrefixName, fullSuffixName);

		/* Get current type variables */
		java.util.List<TypeVariableName> types = element.getTypeParameters().stream()
				.map(Object::toString)
				.map(TypeVariableName::get)
				.collect(Collectors.toList());

		/* Check the type of the type element and generate the class according to the type */
		if (ElementKind.ENUM.equals(element.getKind())) {

			/* Create empty array of methods, enum type delegate does not delegate methods just enum constants */
			java.util.List<MethodSpec> methods = new java.util.ArrayList<>();

			/* Generate the inheritance structure */
			Map<WsIOLevel, Set<DeclaredType>> inheritance = resolveInheritance(element, context);

			/* Generate the method priority from the inheritance structure */
			Map<WsIOLevel, Map<WsIOProperty, Map<String, ExecutableElement>>> methodPriorities =
					resolveMethodPriority(inheritance);

			/* Extract getters and setters from all methods with priority */
			Map<WsIOLevel, Map<String, ExecutableElement>> setterPriorities = methodPriorities
					.mapValues(map -> map.getOrElse(WsIOProperty.SETTER, HashMap.empty()));
			Map<WsIOLevel, Map<String, ExecutableElement>> getterPriorities = methodPriorities
					.mapValues(map -> map.getOrElse(WsIOProperty.GETTER, HashMap.empty()));

			/* List with all internal fields */
			List<FieldSpec> fields = inheritance.filterKeys(WsIOLevel.LOCAL::equals)
					.values()
					.flatMap(e -> e)
					.map(declaredType -> FieldSpec.builder(TypeName.get(declaredType),
							FIELD_DELEGATOR, Modifier.PRIVATE).build())
					.toList();

			/* Option with super class, only used for message generate */
			Option<TypeName> superClass = inheritance.get(WsIOLevel.CLASS_INTERNAL)
					.flatMap(Set::headOption)
					.map(declaredType -> context.getRecursiveFullTypeName(declaredType,
							true, false, true));

			/* Generate the delegate methods and add them to the set */
			List<MethodSpec> delegateMethods = generateDelegatorMethods(element,
					superClass.isDefined(), checkDelegatorOverride(getterPriorities, setterPriorities));
			methods.addAll(delegateMethods.toJavaSet());

			/* Add the modifiers to the enum */
			Predicate<Modifier> checkAbstract = Modifier.FINAL::equals;
			Modifier[] modifiers = element.getModifiers()
					.stream()
					.filter(checkAbstract.negate())
					.toArray(Modifier[]::new);

			/* Create builder with methods and modifiers */
			Builder builder = TypeSpec.enumBuilder(fullClassName)
					.addModifiers(modifiers)
					.addMethods(methods)
					.addFields(fields);

			/* Create counter to avoid creation of invalid empty enums and add each constant to the builder */
			int constants = 0;
			for (Element enclosed : element.getEnclosedElements()) {
				if (ElementKind.ENUM_CONSTANT.equals(enclosed.getKind())) {

					List<AnnotationSpec.Builder> builders = List.of();
					AnnotationMirror xmlEnumValueAdapterMirror = WsIOUtils.getAnnotationMirror(enclosed, XmlEnumValue.class);
					if (xmlEnumValueAdapterMirror != null) {
						AnnotationSpec.Builder xmlBuilder = AnnotationSpec.builder(XmlEnumValue.class);
						String xmlValue = WsIOUtils.getAnnotationLiteralValue(xmlEnumValueAdapterMirror, "value");
						if (xmlValue != null) {
							xmlBuilder = xmlBuilder.addMember("value", "$L", xmlValue);
						}
						builders = builders.append(xmlBuilder);
					}

					List<AnnotationSpec> annotations = builders.map(AnnotationSpec.Builder::build);

					builder = builder.addEnumConstant(enclosed.getSimpleName().toString(),
							TypeSpec.anonymousClassBuilder("")
									.addAnnotations(annotations)
									.build());

					constants++;

				}
			}

			/* Return the enum only when is valid (not empty) */
			return constants > 0 ? builder.build() : null;

		} else if (ElementKind.INTERFACE.equals(element.getKind())
				|| ElementKind.CLASS.equals(element.getKind())) {

			/* Generate the inheritance structure */
			Map<WsIOLevel, Set<DeclaredType>> inheritance = resolveInheritance(element, context);

			/* Generate the method priority from the inheritance structure */
			Map<WsIOLevel, Map<WsIOProperty, Map<String, ExecutableElement>>> methodPriorities =
					resolveMethodPriority(inheritance);

			/* Extract getters and setters from all methods with priority */
			Map<WsIOLevel, Map<String, ExecutableElement>> setterPriorities = methodPriorities
					.mapValues(map -> map.getOrElse(WsIOProperty.SETTER, HashMap.empty()));
			Map<WsIOLevel, Map<String, ExecutableElement>> getterPriorities = methodPriorities
					.mapValues(map -> map.getOrElse(WsIOProperty.GETTER, HashMap.empty()));

			/* Option with super class */
			Option<TypeName> superClass = inheritance.get(WsIOLevel.CLASS_INTERNAL)
					.flatMap(Set::headOption)
					.map(declaredType -> context.getRecursiveFullTypeName(
							declaredType, true, false, true));

			/* Super interfaces set */
			List<TypeName> superInterfaces = inheritance.getOrElse(WsIOLevel.INTERFACE_INTERNAL, HashSet.empty())
					.map(declaredType -> context.getRecursiveFullTypeName(
							declaredType, true, false, true))
					.toList();

			/* List with all internal fields */
			List<FieldSpec> fields = inheritance.filterKeys(WsIOLevel.LOCAL::equals)
					.values()
					.flatMap(e -> e)
					.map(declaredType -> FieldSpec.builder(TypeName.get(declaredType),
							FIELD_DELEGATOR, Modifier.PRIVATE).build())
					.toList();

			/* Get element descriptor and get use hide empties annotation */
			WsIODescriptor descriptor = WsIODescriptor.of(element);
			boolean hideEmpties = descriptor.getSingle(WsIOUseHideEmpty.class).isDefined();

			/* Generate the property methods */
			List<MethodSpec> propertyMethods = generatePropertyMethods(
					getterPriorities, setterPriorities, HashMap.empty(), WsIOConstant.GET_DELEGATOR, hideEmpties, context
			);

			/* Generate the override main methods */
			List<MethodSpec> overrideMethods = generateOverrideMethods(element, context);

			/* Generate the constructor methods */
			List<MethodSpec> constructorMethods = generateConstructorMethods(element, superClass.isDefined());

			/* Generate the delegate methods */
			List<MethodSpec> delegateMethods = generateDelegatorMethods(element,
					superClass.isDefined(), checkDelegatorOverride(getterPriorities, setterPriorities));

			/* All methods of the delegate */
			List<MethodSpec> methods = propertyMethods.appendAll(overrideMethods)
					.appendAll(constructorMethods)
					.appendAll(delegateMethods);

			/* Declare builder and check abstract predicate */
			Builder builder;
			Predicate<Modifier> checkAbstract;

			/* Check if element is of kind class */
			if (ElementKind.CLASS.equals(element.getKind())) {

				/* Create class builder and initialize predicate */
				builder = TypeSpec.classBuilder(fullClassName)
						.addFields(fields);
				checkAbstract = ign -> false;

			} else {

				/* Create interface builder and initialize predicate */
				builder = TypeSpec.interfaceBuilder(fullClassName);
				checkAbstract = Modifier.ABSTRACT::equals;

			}

			/* Create modifiers array */
			Modifier[] modifiers = element.getModifiers()
					.stream()
					.filter(checkAbstract.negate())
					.filter(Predicates.noneOf(Modifier.STATIC::equals))
					.toArray(Modifier[]::new);

			/* Add modifiers, type variables, fields, methods and interfaces */
			builder.addModifiers(modifiers)
					.addTypeVariables(types)
					.addSuperinterfaces(superInterfaces)
					.addMethods(methods);

			/* Add super class */
			if (superClass.isDefined()) {
				builder = builder.superclass(superClass.get());
			}

			/* Build and return the class builder */
			return builder.build();

		} else {

			/* Print error when the element kind is unknown */
			WsIOHandler.error(messager, element, "Unhandled element type %s", element.getKind());

		}

		/* Return null when an error occurs */
		return null;

	}

	/**
	 * Function that matches the priority level and check both getter and setter are defined.
	 *
	 * @param getters priority map of getters
	 * @param setters priority map of setters
	 * @return list containing matching getters and setters
	 */
	private List<Tuple3<ExecutableElement, ExecutableElement, WsIOLevel>> getMatchingProperties(Map<WsIOLevel, Map<String, ExecutableElement>> getters,
	                                                                                            Map<WsIOLevel, Map<String, ExecutableElement>> setters) {

		/* Function to transform the name of a getter to setter */
		Function1<String, String> getterToSetter = getter -> getter.replaceAll("^get", "set");

		/* Getter and setter names */
		Set<String> getterNames = getters.values()
				.flatMap(Map::keySet)
				.toSet();

		/* Return property names matching names and level */
		return getterNames.toList()
				.flatMap(getterName -> {

					/* Maximum level of the getter */
					WsIOLevel level = WsIOUtils.getPriorityLevelByName(getters, getterName);

					/* Setter name constructed from the getter name */
					String setterName = getterToSetter.apply(getterName);

					/* Return the tuple of executables only when exist a setter in the matching in same level */
					return setters.getOrElse(level, HashMap.empty())
							.get(setterName)
							.flatMap(setter -> getters.getOrElse(level, HashMap.empty())
									.get(getterName)
									.map(getter -> Tuple.of(getter, setter, level)));

				});

	}

	/**
	 * Method that generate the property methods of internal classes and interfaces.
	 *
	 * @param getters map containing the getters with level
	 * @param setters map containing the setters with level
	 * @param annotations annotations of the getters and setters
	 * @param delegator name of the delegator
	 * @param hideEmpties indicates if the collections and maps should be checked and ignored
	 * @param context context of the process
	 * @return property methods of internal classes and interfaces.
	 */
	private List<MethodSpec> generatePropertyMethods(Map<WsIOLevel, Map<String, ExecutableElement>> getters,
	                                                 Map<WsIOLevel, Map<String, ExecutableElement>> setters,
	                                                 Map<String, List<AnnotationSpec>> annotations,
	                                                 String delegator,
	                                                 Boolean hideEmpties,
	                                                 WsIOContext context) {

		/* Property names matching names and level */
		List<Tuple3<ExecutableElement, ExecutableElement, WsIOLevel>> properties =
				getMatchingProperties(getters, setters);

		/* Function to create method specs */
		Function3<ExecutableElement, ExecutableElement, WsIOLevel, List<MethodSpec>> createMethodSpecs = (getter, setter, level) -> {

			/* Getter, setter and property name */
			String getterName = getter.getSimpleName().toString();
			String setterName = setter.getSimpleName().toString();
			String propertyName = WsIOUtil.getterToProperty(getterName);

			/* Get return mirror type */
			TypeMirror returnType = getter.getReturnType();

			/* Get parameter mirror type */
			VariableElement parameter = setter.getParameters().get(0);
			TypeMirror parameterType = parameter.asType();

			WsIOIdentifier mainIdentifier = context.getMainIdentifier();

			String prefixClassName = ObjectUtils.firstNonNull(mainIdentifier != null ? mainIdentifier.getIdentifierPrefix() : null, "");
			String suffixClassName = ObjectUtils.firstNonNull(mainIdentifier != null ? mainIdentifier.getIdentifierSuffix() : null, "");

			WsIOType type = context.getTargetType();
			WsIOIdentifier identifier = WsIOIdentifier.of(prefixClassName, suffixClassName);
			WsIOMember getterMember = WsIOUtils.extractMemberDescriptor(getter, identifier, type);

			List<AnnotationSpec> setterXmlAnnotations = List.of();
			List<AnnotationSpec> setterForwardedAnnotations = generateForwardAnnotations(setter, null, null);

			List<AnnotationSpec> getterXmlAnnotations = generateXmlAnnotations(getterMember, null);
			List<AnnotationSpec> getterForwardedAnnotations = generateForwardAnnotations(getter, FORWARDED_GETTER_EXCLUDE_CLASSES, null);

			List<AnnotationSpec> setterCombinedAnnotations = List.of(setterXmlAnnotations, setterForwardedAnnotations)
					.filter(Objects::nonNull)
					.flatMap(Function.identity());

			List<AnnotationSpec> getterCombinedAnnotations = List.of(getterXmlAnnotations, getterForwardedAnnotations)
					.filter(Objects::nonNull)
					.flatMap(Function.identity());

			/* Current annotations */
			List<AnnotationSpec> setterAnnotations = annotations.getOrElse(setterName, setterCombinedAnnotations);
			List<AnnotationSpec> getterAnnotations = annotations.getOrElse(getterName, getterCombinedAnnotations);

			/* Return the methods generated recursively */
			return WsIODelegator.generatePropertyDelegates(returnType, parameterType,
					getter, setter, getterName, setterName, propertyName, delegator, getterName, setterName,
					getterAnnotations, setterAnnotations, level, hideEmpties, context, messager
			);

		};

		/* Return all the methods */
		return properties.flatMap(createMethodSpecs.tupled());

	}

	/**
	 * Method that generate the main override methods.
	 *
	 * @param element type element
	 * @param context context of the process
	 * @return main override methods
	 */
	private List<MethodSpec> generateOverrideMethods(TypeElement element,
	                                                 WsIOContext context) {

		/* Initialize empty set of method specs */
		List<MethodSpec> methods = List.empty();

		/* Check is the element is a class */
		if (ElementKind.CLASS.equals(element.getKind())) {

			/* Get declared type and type name */
			DeclaredType declaredType = (DeclaredType) element.asType();
			TypeName typeName = context.getRecursiveFullTypeName(declaredType,
					true, false, true);

			/* Get raw type without parameter types */
			TypeName className = typeName;
			if (className instanceof ParameterizedTypeName) {
				className = ((ParameterizedTypeName) className).rawType;
			}

			/* Generate the hash method and add it to the set */
			MethodSpec hashCode = MethodSpec.methodBuilder("hashCode")
					.addAnnotation(Override.class)
					.addModifiers(Modifier.PUBLIC)
					.returns(TypeName.INT)
					.addStatement("return $L() != null ? $L().hashCode() : 0", GET_DELEGATOR, GET_DELEGATOR)
					.build();
			methods = methods.append(hashCode);

			/* Generate the equals method and add it to the set */
			MethodSpec equals = MethodSpec.methodBuilder("equals")
					.addAnnotation(Override.class)
					.addModifiers(Modifier.PUBLIC)
					.returns(TypeName.BOOLEAN)
					.addParameter(TypeName.OBJECT, "obj")
					.beginControlFlow("if (obj != null && obj instanceof $T)", className)
					.beginControlFlow("if ($L() == null)", GET_DELEGATOR)
					.addStatement("return (($T) obj).$L() == null", typeName, GET_DELEGATOR)
					.endControlFlow()
					.beginControlFlow("else")
					.addStatement("return $L().equals((($T) obj).$L())", GET_DELEGATOR, typeName, GET_DELEGATOR)
					.endControlFlow()
					.endControlFlow()
					.addStatement("return false")
					.build();
			methods = methods.append(equals);

			/* Generate the toString method and add it to the set */
			MethodSpec toString = MethodSpec.methodBuilder("toString")
					.addAnnotation(Override.class)
					.addModifiers(Modifier.PUBLIC)
					.returns(ClassName.get(String.class))
					.addStatement("return $L() != null ? $L().toString() : \"\"", GET_DELEGATOR, GET_DELEGATOR)
					.build();
			methods = methods.append(toString);

		}

		/* Return the overridden methods */
		return methods;

	}

	/**
	 * Method that generates the constructor methods
	 *
	 * @param element element type

	 * @param hasSuper indicates if the element has a super class or not
	 * @return constructor methods
	 */
	private List<MethodSpec> generateConstructorMethods(TypeElement element,
	                                                   boolean hasSuper) {

		/* Initialize empty set of method specs */
		List<MethodSpec> methods = List.empty();

		/* Check if the element is a class */
		if (ElementKind.CLASS.equals(element.getKind())) {

			/* Get type name from type mirror */
			TypeMirror typeMirror = element.asType();
			DeclaredType declaredType = (DeclaredType) typeMirror;
			TypeName fieldType = TypeName.get(declaredType);

			/* Get the generic literal */
			String generic = Option.of(declaredType)
					.filter(ParameterizedTypeName.class::isInstance)
					.map(ParameterizedTypeName.class::cast)
					.map(parameterized -> parameterized.typeArguments)
					.filter(Objects::nonNull)
					.map(java.util.List::size)
					.filter(integer -> integer > 0)
					.map(integer -> "<>")
					.getOrElse(StringUtils.EMPTY);

			/* Create the empty constructor */
			MethodSpec.Builder emptyConstructor = MethodSpec.constructorBuilder()
					.addStatement("$L(new $T$L())", SET_DELEGATOR, fieldType, generic)
					.addModifiers(Modifier.PUBLIC);

			/* Create the parametrized constructor and add super call when the class has super */
			MethodSpec.Builder parameterConstructor = MethodSpec.constructorBuilder();
			if (hasSuper) {
				parameterConstructor.addStatement("super($L)", FIELD_DELEGATOR);
			}

			/* Objects class */
			Class<?> objectsClass = java.util.Objects.class;

			/* Add the statement code to the constructor */
			parameterConstructor
					.addStatement("$T.requireNonNull($L, \"$L is null\")",
							objectsClass, FIELD_DELEGATOR, FIELD_DELEGATOR)
					.addStatement("$L($L)", SET_DELEGATOR, FIELD_DELEGATOR)
					.addModifiers(Modifier.PUBLIC)
					.addParameter(fieldType, FIELD_DELEGATOR);

			/* Build constructors and add them to method set */
			methods = methods.append(emptyConstructor.build());
			methods = methods.append(parameterConstructor.build());

		}

		/* Return the methods */
		return methods;

	}

	/**
	 * Method that generates the delegator methods
	 *
	 * @param element type element
	 * @param hasSuper indicates if the class has super or not
	 * @param mustOverride indicates if the method delegator must be overridden or not
	 * @return delegator methods
	 */
	private List<MethodSpec> generateDelegatorMethods(TypeElement element,
	                                                  boolean hasSuper,
	                                                  boolean mustOverride) {

		/* Initialize empty set of method specs */
		List<MethodSpec> methods = List.empty();

		/* Type name of the element */
		TypeMirror typeMirror = element.asType();
		TypeName fieldType = TypeName.get(typeMirror);

		/* Annotations to the get delegate method */
		java.util.List<AnnotationSpec> getAnnotations = new java.util.ArrayList<>();
		if (mustOverride || hasSuper) {
			getAnnotations.add(AnnotationSpec.builder(Override.class).build());
		}

		/* Delegate type and method spec builders */
		TypeName delegateType = TypeName.get(element.asType());
		MethodSpec.Builder getDelegator = MethodSpec.methodBuilder(GET_DELEGATOR)
				.returns(delegateType)
				.addAnnotations(getAnnotations)
				.addModifiers(Modifier.PUBLIC);
		MethodSpec.Builder setDelegator = MethodSpec.methodBuilder(SET_DELEGATOR)
				.addParameter(delegateType, FIELD_DELEGATOR);

		/* Check the kind of the element */
		if (ElementKind.CLASS.equals(element.getKind())
				|| ElementKind.ENUM.equals(element.getKind())) {

			/* Check if the element has super or not */
			if (hasSuper) {

				/* Add getter and setter statements */
				getDelegator.addStatement("return ($T) super.$L()", fieldType, GET_DELEGATOR);
				setDelegator.addStatement("super.$L($L)", SET_DELEGATOR, FIELD_DELEGATOR);

			} else {

				/* Add getter and setter statements */
				getDelegator.addStatement("return $L", FIELD_DELEGATOR);
				setDelegator.addStatement("this.$L = $L", FIELD_DELEGATOR, FIELD_DELEGATOR);

			}

		} else {

			/* Add the abstract modifiers when the element is not a class */
			setDelegator.addModifiers(Modifier.ABSTRACT);
			getDelegator.addModifiers(Modifier.ABSTRACT);

		}

		/* Add the setter when the type is class or enum and add the getter */
		if (ElementKind.CLASS.equals(element.getKind())
				|| ElementKind.ENUM.equals(element.getKind())) {
			methods = methods.append(setDelegator.addModifiers(Modifier.PROTECTED).build());
		}
		methods = methods.append(getDelegator.build());

		/* Return the methods specs */
		return methods;

	}

	/**
	 * Method that generates the wrapper type
	 *
	 * @param type indicates if is a response or request wrapper
	 * @param executable all wrapper executable
	 * @param messageClasses map of message classes and package name
	 * @param cloneClassesByName map of cloned classes and package name by name
	 * @param cloneMessageClassesByName map of cloned message classes and package name by name
	 * @param typeByName map of types by full name
	 * @return generated wrapper type
	 */
	private TypeSpec generateWrapperType(WsIOType type,
	                                     WsIOExecutable executable,
	                                     Map<String, String> messageClasses,
	                                     Map<String, Map<WsIOIdentifier, String>> cloneClassesByName,
	                                     Map<String, Map<WsIOIdentifier, String>> cloneMessageClassesByName,
	                                     Map<String, TypeElement> typeByName) {

		/* Main executable element */
		ExecutableElement executableElement = executable.getExecutableElement();

		/* Get annotate of the executable element and separator */
		boolean skipNameSwap = Option.of(executableElement)
				.map(WsIODescriptor::of)
				.flatMap(desc -> desc.getSingle(WsIOAnnotate.class))
				.filter(WsIOAnnotate::skipNameSwap)
				.isDefined();

		String separator = skipNameSwap ? WsIOConstant.NO_SWAP_SEPARATOR : WsIOConstant.SWAP_SEPARATOR;

		/* Method and operation names */
		String methodOperationName = executable.getMethodOperationName();
		String executableName = executableElement.getSimpleName()
				.toString();

		/* Simple and full class name */
		String classSimpleName = WsIOUtils.getFirstStringNonEqualsTo(XML_EMPTY_VALUE, methodOperationName, executableName);
		String prefixTypeName = WsIOType.RESPONSE.equals(type) ? RESPONSE_WRAPPER_PREFIX : REQUEST_WRAPPER_PREFIX;
		String suffixTypeName = WsIOType.RESPONSE.equals(type) ? RESPONSE_WRAPPER_SUFFIX : REQUEST_WRAPPER_SUFFIX;
		String fullClassName = WsIOUtil.addWrap(WordUtils.capitalize(classSimpleName), prefixTypeName, suffixTypeName);

		List<Tuple4<TypeMirror, OperationIdentifier, WsIOIdentifier, WsIOMember>> descriptors = type == null ? List.of() : switch (type) {
			case REQUEST -> executable.getMemberDescriptors()
					.zipWithIndex()
					.map(tuple -> {

						/* Get the index and the parameter */
						Integer parameterIndex = tuple._2();
						WsIOMember memberDescriptor = tuple._1();

                        /* Check if the type mirror is a mirror type and the identifier descriptor */
                        Element typeElement = memberDescriptor.getTypeElement();
						TypeMirror typeMirror = memberDescriptor.getTypeMirror();
						WsIOIdentifier identifierDescriptor = memberDescriptor.getTargetIdentifier();

						Boolean attributePresent = memberDescriptor.getAttributePresent();
						Boolean elementPresent = memberDescriptor.getElementPresent();
						Boolean elementWrapperPresent = memberDescriptor.getElementWrapperPresent();

						String paramName = memberDescriptor.getParamName();
						String attributeName = memberDescriptor.getAttributeName();
						String elementName = memberDescriptor.getElementName();
						String elementWrapperName = memberDescriptor.getElementWrapperName();

						String calculatedElementName = !elementPresent
								? paramName
								: WsIOUtils.getFirstStringNonEqualsTo(List.of(XML_EMPTY_VALUE, XML_DEFAULT_VALUE), "", elementName, paramName);

						String calculatedElementWrapperName = !elementWrapperPresent
								? paramName
								: WsIOUtils.getFirstStringNonEqualsTo(List.of(XML_EMPTY_VALUE, XML_DEFAULT_VALUE), "", elementWrapperName, paramName);

						String calculatedAttributeName = !attributePresent
								? paramName
								: WsIOUtils.getFirstStringNonEqualsTo(List.of(XML_EMPTY_VALUE, XML_DEFAULT_VALUE), "", attributeName, paramName);

						String calculatedMainElementName = !elementWrapperPresent
								? calculatedElementName
								: calculatedElementWrapperName;

						String calculatedMainName = !attributePresent
								? calculatedMainElementName
								: calculatedAttributeName;

						OperationType calculatedMainElementType = !elementWrapperPresent
								? OperationType.ELEMENT
								: OperationType.WRAPPER;

						OperationType calculatedMainType = !attributePresent
								? calculatedMainElementType
								: OperationType.ATTRIBUTE;

                        Name typeName = typeElement != null ? typeElement.getSimpleName() : null;
                        String argumentName = typeName != null
                                ? typeName.toString()
                                : null;

						/* Parameter name if present otherwise default value */
						String defaultName = ObjectUtils.firstNonNull(argumentName, DEFAULT_PARAMETER + parameterIndex);
						String parameterName = WsIOUtils.getFirstStringNonEqualsTo(List.of(XML_EMPTY_VALUE, XML_DEFAULT_VALUE), "", calculatedMainName, defaultName);

						OperationIdentifier operationIdentifier = OperationIdentifier.of(
								calculatedMainType, parameterName,
								calculatedAttributeName, calculatedElementName, calculatedElementWrapperName
						);

						return Tuple.of(typeMirror, operationIdentifier, identifierDescriptor, memberDescriptor);

					});
			case RESPONSE -> {

				/* Set the member descriptor to null */
				WsIOMember memberDescriptor = null;
				TypeMirror typeMirror = executable.getReturnType();
                WsIOIdentifier identifierDescriptor = executable.getReturnIdentifier();

                Name typeName = executableElement.getSimpleName();
                String methodName = typeName != null
                        ? typeName.toString()
                        : null;

                String propertyName = methodName != null
						? WsIOUtil.getterToProperty(methodName)
						: null;

				/* Return name if present otherwise default value*/
				String resultName = executable.getResultName();
                String defaultName = ObjectUtils.firstNonNull(propertyName, DEFAULT_RESULT);
				String returnName = WsIOUtils.getFirstStringNonEqualsTo(XML_EMPTY_VALUE, resultName, defaultName);

				OperationIdentifier operationIdentifier = OperationIdentifier.of(
						OperationType.DEFAULT, returnName, null, null, null
				);

				yield List.of(
						Tuple.of(typeMirror, operationIdentifier, identifierDescriptor, memberDescriptor)
				);

			}
			default -> {
				throw new IllegalStateException(String.format("The type has an invalid type %s", type));
			}
		};

		/* Add jaxb annotations to the class */
		List<AnnotationSpec> annotations = List.empty();
		annotations = annotations.append(AnnotationSpec.builder(XmlRootElement.class).build());
		annotations = annotations.append(AnnotationSpec.builder(XmlAccessorType.class)
				.addMember("value", "$T.$L", XmlAccessType.class,
						XmlAccessType.PROPERTY.name()).build());

		/* Field, method and interface lists */
		List<FieldSpec> fields = List.empty();
		List<MethodSpec> methods = List.empty();
		List<TypeName> interfaces = List.empty();

		/* Iterate for each parameter and return type */
		List<String> fieldNames = List.empty();
		for (Tuple4<TypeMirror, OperationIdentifier, WsIOIdentifier, WsIOMember> descriptor : descriptors) {

			/* Get mirror type, type element and name/required */
			TypeMirror mirror = descriptor._1();
			OperationIdentifier operationIdentifier = descriptor._2();
			WsIOIdentifier identifierDescriptor = descriptor._3();
			WsIOMember memberDescriptor = descriptor._4();

			/* Check mirror is not a no-type */
			if (!(mirror instanceof NoType)) {

				WsIOIdentifier identifierDefault = WsIOIdentifier.of("", "");
				Boolean inversePresent = Boolean.TRUE.equals(memberDescriptor != null ? memberDescriptor.getInversePresent() : null);
				Boolean initializePresent = Boolean.TRUE.equals(
						memberDescriptor != null
								? memberDescriptor.getInitializePresent()
								: null);

				/* Identifier, prefixes and suffixes */
				String prefixClassName = ObjectUtils.firstNonNull(identifierDescriptor != null ? identifierDescriptor.getIdentifierPrefix() : null, "");
				String suffixClassName = ObjectUtils.firstNonNull(identifierDescriptor != null ? identifierDescriptor.getIdentifierSuffix() : null, "");
				String prefixWrapperName = WsIOType.RESPONSE.equals(type) ? RESPONSE_PREFIX : REQUEST_PREFIX;
				String suffixWrapperName = WsIOType.RESPONSE.equals(type) ? RESPONSE_SUFFIX : REQUEST_SUFFIX;
				WsIOGenerate generate = Objects.nonNull(identifierDescriptor) && !identifierDefault.equals(identifierDescriptor)
						? WsIOGenerate.CLONE_MESSAGE
						: WsIOGenerate.MESSAGE;

				/* Get the identifier and the wrapper identifiers */
				WsIOIdentifier identifier = WsIOIdentifier.of(prefixClassName, suffixClassName);
				WsIOIdentifier wrapper = WsIOIdentifier.of(
						prefixWrapperName, suffixWrapperName
				);

				/* Get clone classes and clone message classes */
				Map<String, String> cloneClasses = cloneClassesByName.flatMap((key, map) ->
						map.get(identifier).map(packageName -> Tuple.of(key, packageName)));
				Map<String, String> cloneMessageClasses = cloneMessageClassesByName.flatMap((key, map) ->
						map.get(identifier).map(packageName -> Tuple.of(key, packageName)));

				/* Context of the generation */
				WsIOContext context = WsIOContext.of(
						identifier, wrapper, messageClasses, cloneClasses, cloneMessageClasses, typeByName, generate, type
				);

				/* Regular and recursive type names */
				TypeName regularType = TypeName.get(mirror);
				TypeName recursiveType = context.getRecursiveFullTypeName(mirror, true, false, true);

				/* Field and internal type names */
				TypeName fieldType = !inversePresent ? regularType : recursiveType;
				TypeName internalType = !inversePresent ? recursiveType : regularType;

				/* Lower and upper names */
				String mainName = operationIdentifier.getMainName();
				String lowerMainName = WordUtils.uncapitalize(mainName);
				String upperMainName = WordUtils.capitalize(mainName);

				/* Internal and external chars */
				String internalChar = Option.when(skipNameSwap, separator).getOrElse("");
				String externalChar  = Option.when(!skipNameSwap, separator).getOrElse("");

				/* Internal and external char name */
				String internalCharName = !inversePresent ? internalChar : externalChar;
				String externalCharName = !inversePresent ? externalChar : internalChar;

				/* Internal and external getter and setter names */
				String internalGetName = String.format("get%s%s", upperMainName, internalCharName);
				String internalSetName = String.format("set%s%s", upperMainName, internalCharName);
				String externalGetName = String.format("get%s%s", upperMainName, externalCharName);
				String externalSetName = String.format("set%s%s", upperMainName, externalCharName);

				/* Assign field name and add it to the fields */
				String fieldName = WsIOConstant.DEFAULT_RESULT.equals(lowerMainName)
						? String.join(lowerMainName, separator)
						: lowerMainName;

				/* Internal and external parameter names */
				String internalParameterName = fieldName;
				String externalParameterName = fieldName;

				fieldNames = fieldNames.append(fieldName);

				/* List with the external get annotations */
				List<AnnotationSpec> externalGetAnnotations = List.of(
						AnnotationSpec.builder(XmlTransient.class).build()
				);

				/* Create fiend and add it to the set */
				FieldSpec fieldSpec = !initializePresent
						? FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE).build()
						: FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE)
								.initializer(WsIODelegator.generateFieldInitializer(mirror))
								.build();
				fields = fields.append(fieldSpec);

				/* Create return type name and get method and add it to the set */
				MethodSpec externalGet = MethodSpec.methodBuilder(externalGetName)
						.returns(fieldType)
						.addAnnotations(externalGetAnnotations)
						.addModifiers(Modifier.PUBLIC)
						.addStatement("return $L", fieldName)
						.build();
				methods = methods.append(externalGet);

				/* Create external parameter, build the method spec and add it to the set */
				ParameterSpec externalParameter = ParameterSpec.builder(fieldType, externalParameterName).build();
				MethodSpec externalSet = MethodSpec.methodBuilder(externalSetName)
						.addParameter(externalParameter)
						.addModifiers(Modifier.PUBLIC)
						.addStatement("this.$L = $L", fieldName, externalParameterName)
						.build();
				methods = methods.append(externalSet);

				/* Predicate that checks that a mirror type is valid,
				 * is not an array nor a java collection not a map */
				Predicate<TypeMirror> isValidInnerType = mirrorType -> {

					/* Check mirror is a declared type and not an array or another */
					if (mirrorType instanceof DeclaredType) {

						/* Type element and name */
						TypeElement element = (TypeElement) ((DeclaredType) mirrorType).asElement();
						String elementName = element.getQualifiedName().toString();

						/* Return true if element name is not a collection */
						return !WsIOCollection.ALL.contains(elementName);

					}

					/* Return false by default */
					return false;

				};

				/* Check if inner wrapper is defined */
				if (executable.getDescriptorWrappers().contains(WsIOWrapped.INNER_WRAPPED)
						&& isValidInnerType.test(mirror)
						&& WsIOType.RESPONSE.equals(type)) {

					/* Declared type and element type */
					DeclaredType declaredType = (DeclaredType) mirror;
					TypeElement element = (TypeElement) declaredType.asElement();

					/* Get hide empties from the type element */
					boolean hideEmpties = Option.of(element)
							.map(WsIODescriptor::of)
							.flatMap(desc -> desc.getSingle(WsIOUseHideEmpty.class)).isDefined();

					/* Generate the inheritance structure */
					Map<WsIOLevel, Set<DeclaredType>> inheritance = resolveInheritance(element, context);

					/* Generate the method priority from the inheritance structure */
					Map<WsIOLevel, Map<WsIOProperty, Map<String, ExecutableElement>>> methodPriorities =
							resolveMethodPriority(inheritance);

					/* Extract getters and setters from all methods with priority */
					Map<WsIOLevel, Map<String, ExecutableElement>> getterPriorities = methodPriorities
							.mapValues(map -> map.getOrElse(WsIOProperty.GETTER, HashMap.empty()));
					Map<WsIOLevel, Map<String, ExecutableElement>> setterPriorities = methodPriorities
							.mapValues(map -> map.getOrElse(WsIOProperty.SETTER, HashMap.empty()));

					/* Property names matching names and level */
					List<Tuple3<ExecutableElement, ExecutableElement, WsIOLevel>> properties =
							getMatchingProperties(getterPriorities, setterPriorities);

					/* Map from property to getter and setter properties */
					Function1<ExecutableElement, String> getExecutableName = exec -> exec.getSimpleName().toString();
					Map<String, Tuple4<String, String, ExecutableElement, ExecutableElement>> propertyToProperties = properties
							.map(tuple -> Tuple.of(getExecutableName.apply(tuple._1()),
									getExecutableName.apply(tuple._2()),
									tuple._1(), tuple._2()))
							.toLinkedMap(tuple -> WsIOUtil.getterToProperty(tuple._1()), tuple -> tuple);

					/* Map from property to final property, getter and setter */
					Map<String, Tuple3<String, String, String>> propertyToFinals = propertyToProperties
							.map((propertyName, propertyNames) ->
									Tuple.of(propertyName,  Tuple.of(
											String.join(propertyName, separator),
											String.join(propertyNames._1(), separator),
											String.join(propertyNames._2(), separator))))
							.filter(tuple -> skipNameSwap);

					/* Getter annotations */
					Map<String, List<AnnotationSpec>> getterAnnotations = propertyToProperties
							.toMap(tuple -> tuple._2()._1(), Tuple2::_1)
							.mapValues(propertyName -> {

								/* Getter is the first element */
								ExecutableElement getterExecutable = propertyToProperties.get(propertyName)
										.map(Tuple4::_3)
										.getOrNull();

								WsIOMember memberWrapped = WsIOUtils.extractMemberDescriptor(getterExecutable, identifierDescriptor, type);

								/* Get the forwarded and the xml annotations */
								List<AnnotationSpec> forwardedGetterAnnotations = generateForwardAnnotations(getterExecutable, FORWARDED_GETTER_EXCLUDE_CLASSES, null);
								List<AnnotationSpec> xmlGetterAnnotations = generateXmlAnnotations(memberWrapped, null);

								/* Return the combined annotations */
								return List.of(forwardedGetterAnnotations, xmlGetterAnnotations)
										.flatMap(Function.identity());

							});

					/* Setter annotations */
					Map<String, List<AnnotationSpec>> setterAnnotations = propertyToProperties
							.toMap(tuple -> tuple._2()._2(), Tuple2::_1)
							.mapValues(propertyName -> {

								/* Getter is the first element */
								ExecutableElement setterExecutable = propertyToProperties.get(propertyName)
										.map(Tuple4::_4)
										.getOrNull();

								/* Get the forwarded and return */
								return generateForwardAnnotations(setterExecutable, null, null);

							});

					/* Change the field names to the class inner fields */
					fieldNames = Option.of(propertyToFinals)
							.<Traversable<String>>map(map -> map.values().map(Tuple3::_1))
							.filter(Traversable::nonEmpty)
							.getOrElse(propertyToProperties.keySet())
							.toList();

					/* Create the property methods */
					List<MethodSpec> propertyMethods = properties.flatMap(tuple -> {

						/* Get getter, setter and level */
						ExecutableElement getter = tuple._1();
						ExecutableElement setter = tuple._2();
						WsIOLevel level = tuple._3();

                        /* Getter, setter and property name */
						String getterName = getter.getSimpleName().toString();
						String setterName = setter.getSimpleName().toString();
						String propertyName = WsIOUtil.getterToProperty(getterName);

						/* Final names for getter, setter and property */
						String finalPropertyName = propertyToFinals.get(propertyName)
								.map(Tuple3::_1).getOrElse(propertyName);
						String finalGetterName = propertyToFinals.get(propertyName)
								.map(Tuple3::_2).getOrElse(getterName);
						String finalSetterName = propertyToFinals.get(propertyName)
								.map(Tuple3::_3).getOrElse(setterName);

                        /* Get return mirror type */
						TypeMirror returnType = getter.getReturnType();

                         /* Get parameter mirror type */
						VariableElement parameter = setter.getParameters().get(0);
						TypeMirror parameterType = parameter.asType();

						/* Current annotations */
						List<AnnotationSpec> getterListAnnotations = getterAnnotations
								.getOrElse(getterName, List.empty());
						List<AnnotationSpec> setterListAnnotations = setterAnnotations
								.getOrElse(setterName, List.empty());

						/* Return the methods generated recursively */
						return WsIODelegator.generatePropertyDelegates(returnType, parameterType, getter, setter,
								finalGetterName, finalSetterName, finalPropertyName, externalGetName, getterName, setterName,
								getterListAnnotations, setterListAnnotations, level, hideEmpties, context, messager);

					});

					/* Add method of the property methods */
					methods = methods.appendAll(propertyMethods);

				} else {

					/* Flag indicating if empty collections, maps and arrays should be hidden */
					boolean methodHideEmpties = executable.getDescriptorWrappers()
							.contains(WsIOWrapped.HIDE_EMPTY_WRAPPED);

					/* List with the internal get and set annotations */
					List<AnnotationSpec> internalGetAnnotations = List.empty();
					List<AnnotationSpec> internalSetAnnotations = List.empty();

					/* Generate and append the getter annotations */
					internalGetAnnotations = internalGetAnnotations.appendAll(
							generateXmlAnnotations(memberDescriptor, operationIdentifier)
					);

					if (mirror instanceof DeclaredType declaredType) {

						/* Declared type and element type */
						TypeElement element = (TypeElement) declaredType.asElement();

						/* Generate the inheritance structure */
						Map<WsIOLevel, Set<DeclaredType>> inheritance = resolveInheritance(element, context);

						/* Generate the method priority from the inheritance structure */
						Map<WsIOLevel, Map<WsIOProperty, Map<String, ExecutableElement>>> methodPriorities =
								resolveMethodPriority(inheritance);

						/* Extract getters and setters from all methods with priority */
						Map<WsIOLevel, Map<String, ExecutableElement>> getterPriorities = methodPriorities
								.mapValues(map -> map.getOrElse(WsIOProperty.GETTER, HashMap.empty()));
						Map<WsIOLevel, Map<String, ExecutableElement>> setterPriorities = methodPriorities
								.mapValues(map -> map.getOrElse(WsIOProperty.SETTER, HashMap.empty()));

						/* Property names matching names and level */
						List<Tuple3<ExecutableElement, ExecutableElement, WsIOLevel>> properties =
								getMatchingProperties(getterPriorities, setterPriorities);

						/* Map from property to getter and setter properties */
						Function1<ExecutableElement, String> getExecutableName = exec ->
								exec.getSimpleName().toString();

						/* Get the executable of the getter only when properties match */
						ExecutableElement getterExecutable = properties.map(Tuple3::_1)
								.filter(exec -> internalGetName.equals(getExecutableName.apply(exec)))
								.getOrNull();

						/* Get the executable of the setter only when properties match */
						ExecutableElement setterExecutable = properties.map(Tuple3::_1)
								.filter(exec -> internalSetName.equals(getExecutableName.apply(exec)))
								.getOrNull();

						/* Get the forwarded annotations of the getter method */
						if (getterExecutable != null) {
							List<AnnotationSpec> forwardedGetterAnnotations = generateForwardAnnotations(getterExecutable, FORWARDED_GETTER_EXCLUDE_CLASSES, null);
							internalGetAnnotations = internalGetAnnotations.appendAll(
									forwardedGetterAnnotations
							);
						}

						/* Get the forwarded annotations of the setter method */
						if (setterExecutable != null) {
							List<AnnotationSpec> forwardedSetterAnnotations = generateForwardAnnotations(setterExecutable, null, null);
							internalSetAnnotations = internalSetAnnotations.appendAll(
									forwardedSetterAnnotations
							);
						}

					}

					/* Create internal get accessor and code block */
					String internalGetAccessor = String.format("%s()", externalGetName);
					CodeBlock internalGetBlock = !inversePresent
							? WsIODelegator.generateRecursiveTransformToInternal(mirror,
								internalType, context, internalGetAccessor, methodHideEmpties)
							: WsIODelegator.generateRecursiveTransformToExternal(fieldType,
								mirror, context, internalGetAccessor, methodHideEmpties);

					/* Create the internal get method spec and add it to the methods set */
					MethodSpec.Builder internalGetBuilder = MethodSpec.methodBuilder(internalGetName)
							.returns(internalType)
							.addAnnotations(internalGetAnnotations)
							.addModifiers(Modifier.PUBLIC);

					/* Check mirror is a primitive type or not */
					if (mirror instanceof PrimitiveType) {

						/* Add return statement of primitive type */
						internalGetBuilder = internalGetBuilder.addCode("return ", externalGetName)
								.addCode(internalGetBlock)
								.addCode(";").addCode("\n");

					} else {

						/* Add return statement of reference type */
						internalGetBuilder = internalGetBuilder.addCode("return $L() != null ? ", externalGetName)
								.addCode(internalGetBlock)
								.addCode(" : null").addCode(";").addCode("\n");

					}

					/* Build and add the method to method list */
					methods = methods.append(internalGetBuilder.build());

					/* Create external set block code */
					CodeBlock internalSetBlock = !inversePresent
							? WsIODelegator.generateRecursiveTransformToExternal(internalType,
								mirror, context, internalParameterName, methodHideEmpties)
							: WsIODelegator.generateRecursiveTransformToInternal(mirror,
								fieldType, context, internalParameterName, methodHideEmpties);

					/* Create parameter and method spec and add it to the methods */
					ParameterSpec internalParameter = ParameterSpec.builder(internalType, internalParameterName).build();
					MethodSpec.Builder internalSetBuilder = MethodSpec.methodBuilder(internalSetName)
							.addParameter(internalParameter)
							.addAnnotations(internalSetAnnotations)
							.addModifiers(Modifier.PUBLIC);

					/* Check mirror is a primitive type or not */
					if (mirror instanceof PrimitiveType) {

						/* Add set statement of primitive type */
						internalSetBuilder = internalSetBuilder.addCode("$L(", externalSetName)
								.addCode(internalSetBlock)
								.addCode(")").addCode(";").addCode("\n");

					} else {

						/* Add set statement of reference type */
						internalSetBuilder = internalSetBuilder.beginControlFlow("if ($L != null)", internalParameterName)
								.addCode("$L(", externalSetName)
								.addCode(internalSetBlock)
								.addCode(")").addCode(";").addCode("\n")
								.endControlFlow();

					}

					/* Build and add set method to the list */
					methods = methods.append(internalSetBuilder.build());

				}

			}

		}

		/* Check if the type is response and the wrapper size is greater than 0 */
		if (WsIOType.RESPONSE.equals(type) && executable.getDescriptorWrappers().size() > 0) {

			/* Get additionals */
			Tuple4<List<FieldSpec>, List<MethodSpec>, List<TypeName>, List<AnnotationSpec>> additionals =
					generateAdditionals(executable.getDescriptorWrappers(), fieldNames);

			/* Add all fields, methods, interfaces and annotations */
			fields = fields.appendAll(additionals._1());
			methods = methods.appendAll(additionals._2());
			interfaces = interfaces.appendAll(additionals._3());
			annotations = annotations.appendAll(additionals._4());

		}

		/* Check if the field or method size if greater than zero to add the type spec */
		if (fields.size() > 0 || methods.size() > 0) {

			/* Build and return the type spec and add it to the list */
			return TypeSpec.classBuilder(fullClassName)
					.addAnnotations(annotations)
					.addModifiers(Modifier.PUBLIC)
					.addSuperinterfaces(interfaces)
					.addFields(fields)
					.addMethods(methods)
					.build();

		}

		/* Return null */
		return null;

	}

	/**
	 * Method that generates the xml annotations of the getter method.
	 *
	 * @param executableElement element to process
	 * @param excludeClasses list of classes to exclude
	 * @param excludeNames list of names to exclude
	 * @return the xml annotations of the getter method
	 */
	private List<AnnotationSpec> generateForwardAnnotations(ExecutableElement executableElement,
															List<Class<?>> excludeClasses,
															List<String> excludeNames) {

		List<String> excludeListNames = Option.of(excludeNames).getOrElse(List::of);
		List<String> excludeListClasses = Option.of(excludeClasses)
				.getOrElse(List::of)
				.map(Class::getName);

		List<String> excludeList = List.of(excludeListNames, excludeListClasses)
				.flatMap(Function.identity());

		Map<DeclaredType, Map<String, String>> annotationDescriptions = WsIOUtils.getAnnotationDescriptions(executableElement);
		Map<DeclaredType, Map<String, String>> annotationFilteredDescriptions = Option.of(annotationDescriptions)
				.getOrElse(HashMap::empty)
				.filterKeys(annotationType -> !excludeList.contains(annotationType.toString()));

		return annotationFilteredDescriptions.toStream()
				.map(tuple -> {

					DeclaredType annotationType = tuple._1();
					Map<String, String> annotationFields = tuple._2();

					Element element = annotationType.asElement();
					if (element instanceof TypeElement typeElement) {

						ClassName className = ClassName.get(typeElement);

						AnnotationSpec.Builder builder = AnnotationSpec.builder(className);
						for (Tuple2<String, String> annotationField : annotationFields) {
							String field = annotationField._1();
							String literal = annotationField._2();
							builder = builder.addMember(field, "$L", literal);
						}

						return builder.build();

					}
					return null;

				}).filter(Objects::nonNull).toList();

	}

	/**
	 * Method that generates the xml annotations of the getter method.
	 *
	 * @param memberDescriptor member descriptor
	 * @param operationIdentifier operation identifier info
	 * @return the xml annotations of the getter method
	 */
	private List<AnnotationSpec> generateXmlAnnotations(WsIOMember memberDescriptor,
														OperationIdentifier operationIdentifier) {

		List<AnnotationSpec.Builder> builders = List.of();

		if (memberDescriptor != null) {

			if (memberDescriptor.getTransientPresent()) {

				AnnotationSpec.Builder builder = AnnotationSpec.builder(XmlTransient.class);

				builders = builders.append(builder);

			} else {

				if (memberDescriptor.getAdapterPresent()) {

					AnnotationSpec.Builder builder = AnnotationSpec.builder(XmlJavaTypeAdapter.class);

					if (memberDescriptor.getAdapterValue() != null) {
						builder = builder.addMember("value", "$L", memberDescriptor.getAdapterValue());
					}

					if (memberDescriptor.getAdapterType() != null) {
						builder = builder.addMember("type", "$L", memberDescriptor.getAdapterType());
					}

					builders = builders.append(builder);

				}

				if (memberDescriptor.getValuePresent()) {

					AnnotationSpec.Builder builder = AnnotationSpec.builder(XmlValue.class);

					builders = builders.append(builder);

				}

				if (memberDescriptor.getAttributePresent()) {

					AnnotationSpec.Builder builder = AnnotationSpec.builder(XmlAttribute.class);

					if (operationIdentifier != null) {
						if (operationIdentifier.getAttributeName() != null && !XML_DEFAULT_VALUE.equals(operationIdentifier.getAttributeName())) {
							builder = builder.addMember("name", "$S", operationIdentifier.getAttributeName());
						}
					} else {
						if (memberDescriptor.getAttributeName() != null && !XML_DEFAULT_VALUE.equals(memberDescriptor.getAttributeName())) {
							builder = builder.addMember("name", "$S", memberDescriptor.getAttributeName());
						}
					}

					if (memberDescriptor.getAttributeNamespace() != null && !XML_DEFAULT_VALUE.equals(memberDescriptor.getAttributeNamespace())) {
						builder = builder.addMember("namespace", "$S", memberDescriptor.getAttributeNamespace());
					}

					if (memberDescriptor.getAttributeRequired() != null && !XML_FLAG_VALUE.equals(memberDescriptor.getAttributeRequired())) {
						builder = builder.addMember("required", "$L", memberDescriptor.getAttributeRequired());
					}

					builders = builders.append(builder);

				} else if (memberDescriptor.getElementWrapperPresent() || memberDescriptor.getElementPresent()) {

					if (memberDescriptor.getElementPresent()) {

						AnnotationSpec.Builder builder = AnnotationSpec.builder(XmlElement.class);

						if (operationIdentifier != null) {
							if (operationIdentifier.getElementName() != null && !XML_DEFAULT_VALUE.equals(operationIdentifier.getElementName())) {
								builder = builder.addMember("name", "$S", operationIdentifier.getElementName());
							}
						} else {
							if (memberDescriptor.getElementName() != null && !XML_DEFAULT_VALUE.equals(memberDescriptor.getElementName())) {
								builder = builder.addMember("name", "$S", memberDescriptor.getElementName());
							}
						}

						if (memberDescriptor.getElementNamespace() != null && !XML_DEFAULT_VALUE.equals(memberDescriptor.getElementNamespace())) {
							builder = builder.addMember("namespace", "$S", memberDescriptor.getElementNamespace());
						}

						if (memberDescriptor.getElementDefaultValue() != null && !XML_ZERO_VALUE.equals(memberDescriptor.getElementDefaultValue())) {
							builder = builder.addMember("defaultValue", "$S", memberDescriptor.getElementDefaultValue());
						}

						if (memberDescriptor.getElementRequired() != null && !XML_FLAG_VALUE.equals(memberDescriptor.getElementRequired())) {
							builder = builder.addMember("required", "$L", memberDescriptor.getElementRequired());
						}

						if (memberDescriptor.getElementNillable() != null && !XML_FLAG_VALUE.equals(memberDescriptor.getElementNillable())) {
							builder = builder.addMember("nillable", "$L", memberDescriptor.getElementNillable());
						}

						if (memberDescriptor.getElementType() != null) {
							builder = builder.addMember("type", "$L", memberDescriptor.getElementType());
						}

						builders = builders.append(builder);

					}

					if (memberDescriptor.getElementWrapperPresent()) {

						AnnotationSpec.Builder builder = AnnotationSpec.builder(XmlElementWrapper.class);

						if (operationIdentifier != null) {
							if (operationIdentifier.getElementWrapperName() != null && !XML_DEFAULT_VALUE.equals(operationIdentifier.getElementWrapperName())) {
								builder = builder.addMember("name", "$S", operationIdentifier.getElementWrapperName());
							}
						} else {
							if (memberDescriptor.getElementWrapperName() != null && !XML_DEFAULT_VALUE.equals(memberDescriptor.getElementWrapperName())) {
								builder = builder.addMember("name", "$S", memberDescriptor.getElementWrapperName());
							}
						}

						if (memberDescriptor.getElementNamespace() != null && !XML_DEFAULT_VALUE.equals(memberDescriptor.getElementNamespace())) {
							builder = builder.addMember("namespace", "$S", memberDescriptor.getElementNamespace());
						}

						if (memberDescriptor.getElementRequired() != null && !XML_FLAG_VALUE.equals(memberDescriptor.getElementRequired())) {
							builder = builder.addMember("required", "$L", memberDescriptor.getElementRequired());
						}

						if (memberDescriptor.getElementNillable() != null && !XML_FLAG_VALUE.equals(memberDescriptor.getElementNillable())) {
							builder = builder.addMember("nillable", "$L", memberDescriptor.getElementNillable());
						}

						builders = builders.append(builder);

					}

				}

			}

		}

		return builders.map(AnnotationSpec.Builder::build);

	}

	/**
	 * Method that generates the fields, methods, interfaces and annotations of wrapper additionals.
	 *
	 * @param wrappers list of wrappers
	 * @param fieldNames names of the fields
	 * @return fields, methods, interfaces and annotations of wrapper additionals
	 */
	private Tuple4<List<FieldSpec>, List<MethodSpec>, List<TypeName>, List<AnnotationSpec>> generateAdditionals(Set<WsIOWrapped> wrappers,
	                                                                                                            List<String> fieldNames) {

		/* Flag indicating if empty collections, maps and arrays should be hidden */
		boolean hideEmpties = wrappers.contains(WsIOWrapped.HIDE_EMPTY_WRAPPED);

		/* Fields, methods and interfaces */
		List<FieldSpec> fields = List.empty();
		List<MethodSpec> methods = List.empty();
		List<TypeName> interfaces = List.empty();
		List<AnnotationSpec> annotations = List.empty();

		/* Declare function to create the members */
		Function5<String, TypeName, String, List<AnnotationSpec>, List<AnnotationSpec>,
				Tuple2<List<FieldSpec>, List<MethodSpec>>> createMember =
				(fieldName, typeName, getCheck, getAnnotations, setAnnotations) -> {

					/* Declare current sets to hold the fields and methods */
					List<FieldSpec> currentFields = List.empty();
					List<MethodSpec> currentMethods = List.empty();

					/* Get the upper name */
					String upperName = WordUtils.capitalize(fieldName);

					/* Create the field and add it to the set */
					currentFields = currentFields.append(FieldSpec.builder(typeName, fieldName, Modifier.PRIVATE).build());

					/* Get possible class name and check if is a collection or an array */
					String possibleClassName = Option.of(typeName)
							.filter(ParameterizedTypeName.class::isInstance)
							.map(ParameterizedTypeName.class::cast)
							.map(parameterized -> parameterized.rawType)
							.orElse(Option.of(typeName)
									.filter(ClassName.class::isInstance)
									.map(ClassName.class::cast))
							.map(ClassName::reflectionName)
							.getOrNull();
					boolean isArray = typeName instanceof ArrayTypeName;
					boolean isCollection = WsIOCollection.ALL.contains(possibleClassName);

					/* Declare the code block and check if the get check is empty or not */
					CodeBlock getCode;
					if (StringUtils.isNotBlank(getCheck)) {

						/* Create the upper check name and initialize the get code block */
						String upperCheck = WordUtils.capitalize(getCheck);

						/* Create code builder and check if hice mode is enabled and if is an array or collection */
						CodeBlock.Builder getCodeBuilder = CodeBlock.builder();
						if (hideEmpties && isArray) {

							/* Simple show condition check */
							getCodeBuilder = getCodeBuilder.beginControlFlow("if ($T.$L.equals(get$L()) " +
											"&& $L != null && $L.length > 0)",
									Boolean.class, "TRUE", upperCheck, fieldName, fieldName);

						} else if (hideEmpties && isCollection) {

							/* Simple show condition check */
							getCodeBuilder = getCodeBuilder.beginControlFlow("if ($T.$L.equals(get$L()) " +
											"&& $L != null && $L.size() > 0)",
									Boolean.class, "TRUE", upperCheck, fieldName, fieldName);

						} else {

							/* Simple show condition check */
							getCodeBuilder = getCodeBuilder.beginControlFlow("if ($T.$L.equals(get$L()))",
									Boolean.class, "TRUE", upperCheck);

						}

						/* Builder and add the rest of the get code */
						getCode = getCodeBuilder.addStatement("return $L", fieldName)
								.endControlFlow()
								.beginControlFlow("else")
								.addStatement("return null")
								.endControlFlow()
								.build();

					} else {

						if (hideEmpties && isArray) {

							/* Initialize the get code block */
							getCode = CodeBlock.builder()
									.addStatement("return ($L != null && $L.length > 0 ? $L : null)",
											fieldName, fieldName, fieldName)
									.build();

						} else if (hideEmpties && isCollection) {

							/* Initialize the get code block */
							getCode = CodeBlock.builder()
									.addStatement("return ($L != null && $L.size() > 0 ? $L : null)",
											fieldName, fieldName, fieldName)
									.build();

						} else {

							/* Initialize the get code block */
							getCode = CodeBlock.builder()
									.addStatement("return $L", fieldName)
									.build();

						}

					}

					/* Add get method to the set */
					currentMethods = currentMethods.append(MethodSpec.methodBuilder("get" + upperName)
							.addAnnotation(Override.class)
							.addAnnotations(getAnnotations)
							.addModifiers(Modifier.PUBLIC)
							.returns(typeName)
							.addCode(getCode)
							.build());

					/* Add set method to the set */
					currentMethods = currentMethods.append(MethodSpec.methodBuilder("set" + upperName)
							.addAnnotations(setAnnotations)
							.addAnnotation(Override.class)
							.addModifiers(Modifier.PUBLIC)
							.addParameter(typeName, fieldName)
							.addStatement("this.$L = $L", fieldName, fieldName)
							.build());

					/* Return the tuple with current fields and methods and the flag in true */
					return Tuple.of(currentFields, currentMethods);

				};

		/* Function to create member without set */
		Function4<String, TypeName, String, List<AnnotationSpec>, Tuple2<List<FieldSpec>, List<MethodSpec>>>
				createMemberNoSet = createMember.reversed()
				.apply(List.empty())
				.reversed();

		/* Function to create element field and method spec with element annotations */
		Function2<TypeName, Tuple3<String, String, String>, Tuple2<List<FieldSpec>, List<MethodSpec>>> createElement =
				(type, stateName) -> createMemberNoSet.apply(stateName._2(), type, stateName._3(),
						List.of(AnnotationSpec.builder(XmlElement.class).build()));

		/* Function to create element field and method spec with attribute annotations */
		Function2<TypeName, Tuple3<String, String, String>, Tuple2<List<FieldSpec>, List<MethodSpec>>> createAttribute =
				(type, stateName) -> createMemberNoSet.apply(stateName._2(), type, stateName._3(),
						List.of(AnnotationSpec.builder(XmlAttribute.class).build()));

		/* Function to create element field and method spec with element and wrapper annotations */
		Function2<TypeName, Tuple3<String, String, String>, Tuple2<List<FieldSpec>, List<MethodSpec>>> createWrapper =
				(type, stateName) -> createMemberNoSet.apply(stateName._1(), type, stateName._3(),
						List.of(AnnotationSpec.builder(XmlElement.class)
										.addMember("name", "$S", stateName._2()).build(),
								AnnotationSpec.builder(XmlElementWrapper.class)
										.addMember("name", "$S", stateName._1()).build()));

		/* Function to create element field and method spec with transient annotations */
		Function2<TypeName, Tuple3<String, String, String>, Tuple2<List<FieldSpec>, List<MethodSpec>>> createTransient =
				(type, stateName) -> createMemberNoSet.apply(stateName._1(), type, stateName._3(),
						List.of(AnnotationSpec.builder(XmlTransient.class).build()));

		/* List with time names */
		List<Tuple3<String, String, String>> timeNames = List.of(
				Tuple.of("times", "time", null));

		/* List with state names */
		List<Tuple3<String, String, String>> stateNames = List.of(
				Tuple.of("identifiers", "identifier", null),
				Tuple.of("messages", "message", null),
				Tuple.of("descriptions", "description", null),
				Tuple.of("types", "type", null),
				Tuple.of("status", "status", null),
				Tuple.of("details", "detail", null),
				Tuple.of("successfulItems", "successful", "showSuccessfulItems"),
				Tuple.of("failureItems", "failure", "showFailureItems"),
				Tuple.of("warningItems", "warning", "showWarningItems"),
				Tuple.of("showSuccessfulItems", "showSuccessful", null),
				Tuple.of("showFailureItems", "showFailure", null),
				Tuple.of("showWarningItems", "showWarning", null));

		/* Next line counter to format the code */
		Integer nextLine = 0;

		/* Declare the list of orders */
		List<String> orders = List.empty();

		/* Check if the time wrapper is defined and add it to orders */
		if (wrappers.contains(WsIOWrapped.TIME_WRAPPED)) {
			nextLine++;
			orders = orders.appendAll(timeNames.map(Tuple3::_1));
		}

		/* Check if the state wrapper is defined and add the first 6 to orders */
		if (wrappers.contains(WsIOWrapped.STATE_WRAPPED)) {
			nextLine += 6;
			orders = orders.appendAll(stateNames.map(Tuple3::_2).take(6));
		}

		/* Add all field names */
		orders = orders.appendAll(fieldNames);

		/* Check if the state wrapper is defined, skip the first 6 and add the next 3 elements to the orders */
		if (wrappers.contains(WsIOWrapped.STATE_WRAPPED)) {
			orders = orders.appendAll(stateNames.map(Tuple3::_1).drop(6).take(3));
		}

		/* Create the name format */
		String format = Stream.of("$S")
				.cycle(orders.size())
				.intersperse(", ")
				.prepend("{ ")
				.append(" }")
				.insert(2 * nextLine + 1, wrappers.contains(WsIOWrapped.STATE_WRAPPED) ? "\n" :"")
				.mkString();

		/* Add the annotation to the list */
		annotations = annotations.append(AnnotationSpec.builder(XmlType.class)
				.addMember("propOrder", format, orders.toJavaArray()).build());

		/* Check if time wrapper is going to be added or not */
		if (wrappers.contains(WsIOWrapped.TIME_WRAPPED)) {

			/* Declare the index and call the function to generate the fields and methods */
			Option<Tuple2<List<FieldSpec>, List<MethodSpec>>> results = timeNames
					.map(createWrapper.apply(ParameterizedTypeName.get(ClassName.get(java.util.List.class),
							ClassName.get(WsIOInstant.class))))
					.headOption();

			/* Add the fields and methods */
			fields = fields.appendAll(results.toList().flatMap(Tuple2::_1));
			methods = methods.appendAll(results.toList().flatMap(Tuple2::_2));

			/* Add the annotation */
			interfaces = interfaces.append(ClassName.get(WsIOTime.class));

		}

		/* Check if state wrapper is going to be added or not */
		if (wrappers.contains(WsIOWrapped.STATE_WRAPPED)) {

			/* Get all results */
			List<Tuple2<List<FieldSpec>, List<MethodSpec>>> results = List.of(
					createElement.apply(ClassName.get(String.class)),
					createElement.apply(ClassName.get(WsIOText.class)),
					createElement.apply(ClassName.get(WsIOText.class)),
					createElement.apply(ClassName.get(String.class)),
					createAttribute.apply(ClassName.get(WsIOStatus.class)),
					createAttribute.apply(ClassName.get(WsIODetail.class)),
					createWrapper.apply(ParameterizedTypeName.get(ClassName.get(java.util.List.class),
							ClassName.get(WsIOItem.class))),
					createWrapper.apply(ParameterizedTypeName.get(ClassName.get(java.util.List.class),
							ClassName.get(WsIOItem.class))),
					createWrapper.apply(ParameterizedTypeName.get(ClassName.get(java.util.List.class),
							ClassName.get(WsIOItem.class))),
					createTransient.apply(ClassName.get(Boolean.class)),
					createTransient.apply(ClassName.get(Boolean.class)),
					createTransient.apply(ClassName.get(Boolean.class)))
					.zip(stateNames)
					.map(tuple -> tuple._1().apply(tuple._2()));

			/* Add all results to the fields and methods */
			fields = fields.appendAll(results.flatMap(Tuple2::_1));
			methods = methods.appendAll(results.flatMap(Tuple2::_2));

			/* Add the interface to the set */
			interfaces = interfaces.append(ClassName.get(WsIOState.class));

		}

		/* Return fields, methods, interfaces and annotations */
		return Tuple.of(fields, methods, interfaces, annotations);

	}

	/**
	 * Method that return if the delegator should be overridden or not
	 *
	 * @param getters map of getters with priority levels
	 * @param setters map of setters with priority levels
	 * @return {@code true} if the delegator should be overridden {@code false} otherwise
	 */
	private boolean checkDelegatorOverride(Map<WsIOLevel, Map<String, ExecutableElement>> getters,
	                                       Map<WsIOLevel, Map<String, ExecutableElement>> setters) {

		/* Predicate that filter the map keys */
		Predicate<WsIOLevel> filterKey = key -> WsIOLevel.CLASS_INTERNAL.equals(key)
				|| WsIOLevel.INTERFACE_INTERNAL.equals(key);

		/* Getter names */
		Set<String> getterNames = getters.filterKeys(filterKey)
				.values()
				.flatMap(Map::keySet)
				.toSet();

		/* Check if the delegator getter is present */
		if (getterNames.contains(GET_DELEGATOR)) {

			/* Get delegator maximum level */
			WsIOLevel level = WsIOUtils.getPriorityLevelByNameFromExclusive(getters, GET_DELEGATOR, WsIOLevel.LOCAL);

			/* Getter and setter executable options */
			Option<ExecutableElement> getterOpt = getters.getOrElse(level, HashMap.empty())
					.get(GET_DELEGATOR);
			Option<ExecutableElement> setterOpt = setters.getOrElse(level, HashMap.empty())
					.get(SET_DELEGATOR);

			/* Check if the getter and setter are defined */
			if (getterOpt.isDefined() && setterOpt.isDefined()) {

				/* Getter and setters */
				ExecutableElement getter = getterOpt.get();
				ExecutableElement setter = setterOpt.get();

				/* Get type mirrors of return and parameter type */
				TypeMirror returnType = getter.getReturnType();
				VariableElement parameter = setter.getParameters().get(0);
				TypeMirror parameterType = parameter.asType();

				/* Check that types are declared types */
				if (returnType instanceof DeclaredType
						&& parameterType instanceof DeclaredType) {

					/* Get declared types */
					DeclaredType returnDeclaredType = (DeclaredType) returnType;
					DeclaredType parameterDeclaredType = (DeclaredType) parameterType;

					/* Return true if the return and parameter types are compatible */
					if (WsIODelegator.isRecursiveCompatible(returnDeclaredType, parameterDeclaredType)) {
						return true;
					}

				}

			}

		}

		/* Return false if the types are not found or does not match */
		return false;

	}

	/**
	 * Method that generates a map containing the inheritance structure
	 *
	 * @param element element to extract the types
	 * @param context context of the generation
	 * @return map containing the inheritance structure
	 */
	private Map<WsIOLevel, Set<DeclaredType>> resolveInheritance(TypeElement element,
	                                                             WsIOContext context) {

		/* Get root type, super class and super interfaces */
		TypeMirror rootType = element.asType();
		TypeMirror superType = element.getSuperclass();
		List<TypeMirror> interTypes = List.ofAll(element.getInterfaces());

		/* Create the root delegate element if is non-null and instance of declared type */
		Option<DeclaredType> rootOpt = Option.of(rootType)
				.filter(DeclaredType.class::isInstance)
				.map(DeclaredType.class::cast);

		/* Create inheritance map with local class */
		Map<WsIOLevel, Set<DeclaredType>> inheritances = rootOpt.toMap(val -> WsIOLevel.LOCAL, HashSet::of);

		/* Create the super-delegate element */
		Option<DeclaredType> superOpt = Option.of(superType)
				.filter(DeclaredType.class::isInstance)
				.map(DeclaredType.class::cast)
				.filter(declaredType -> Option.of(declaredType)
						.map(DeclaredType::asElement)
						.filter(TypeElement.class::isInstance)
						.map(TypeElement.class::cast)
						.map(TypeElement::getQualifiedName)
						.map(Name::toString)
						.exists(Predicates.noneOf(Object.class.getCanonicalName()::equals,
								Enum.class.getCanonicalName()::equals)));

		/* Function that checks if the class is external of internal */
		Function2<Boolean, DeclaredType, WsIOLevel> extractDeclaredClassInterface = (isClass, type) -> {
			if (type.asElement() instanceof TypeElement) {

				/* Type element and name */
				TypeElement typeElement = (TypeElement) type.asElement();
				String name = typeElement.getQualifiedName().toString();

				/* Check if the element class name is in clone and message classes */
				boolean inMessage = context.getMessageClasses().keySet().contains(name);
				boolean inClone = context.getCloneClasses().keySet().contains(name);

				/* Check generate type and in message in clone */
				WsIOGenerate generate = context.getTargetGenerate();
				if ((WsIOGenerate.MESSAGE.equals(generate) && inMessage)
						|| (WsIOGenerate.CLONE.equals(generate) && inClone)
						|| (WsIOGenerate.CLONE_MESSAGE.equals(generate) && inMessage && inClone)) {

					/* Check if is a class or interface */
					if (isClass) {

						/* Return class internal */
						return WsIOLevel.CLASS_INTERNAL;

					} else {

						/* Return interface internal */
						return WsIOLevel.INTERFACE_INTERNAL;

					}

				} else {

					/* Check if is a class or interface */
					if (isClass) {

						/* Return class external */
						return WsIOLevel.CLASS_EXTERNAL;

					} else {

						/* Return interface external */
						return WsIOLevel.INTERFACE_EXTERNAL;

					}

				}

			}

			/* Return none by default */
			return WsIOLevel.NONE;

		};

		/* Add the element if is non-null and instance of declared type */
		inheritances = inheritances.merge(superOpt.toMap(extractDeclaredClassInterface.apply(true), HashSet::of));

		/* Return current local and super class merged with non-null super interfaces instance of declared type */
		return inheritances.merge(interTypes
				.filter(DeclaredType.class::isInstance)
				.map(DeclaredType.class::cast)
				.toSet()
				.groupBy(extractDeclaredClassInterface.apply(false)));

	}

	/**
	 * Method that returns a map with getters and setters by priority.
	 *
	 * @param inheritance map containing the inheritance structure
	 * @return a map with getters and setters by priority
	 */
	private Map<WsIOLevel, Map<WsIOProperty, Map<String, ExecutableElement>>> resolveMethodPriority(Map<WsIOLevel, Set<DeclaredType>> inheritance) {

		/* Return the map with getters and setters by priority */
		return inheritance.filterKeys(Predicates.noneOf(WsIOLevel.NONE::equals))
				.mapValues(types -> types.map(DeclaredType::asElement)
						.filter(TypeElement.class::isInstance)
						.map(TypeElement.class::cast))
				.mapValues(types -> {

					/* All methods of the type recursively */
					Map<Integer, Set<ExecutableElement>> methods = types.map(WsIOUtils::getRecursiveExecutableElementsWithLevel)
							.fold(HashMap.empty(), (map1, map2) -> map1.merge(map2, Set::addAll));

					/* Functions to flat the map */
					Function2<Integer, Set<ExecutableElement>, Set<Tuple2<Integer, ExecutableElement>>> toFlatTupleStream =
							(priority, executables) -> executables.map(executable -> Tuple.of(priority, executable));

					/* Function to group by executable name */
					Function2<Integer, ExecutableElement, String> groupByName = (priority, executable) ->
							executable.getSimpleName().toString();

					Set<Tuple2<Integer, ExecutableElement>> properties = methods.flatMap(toFlatTupleStream.tupled()).toSet();

					/* Function to extract executables by priority */
					Function1<Predicate<ExecutableElement>, Map<String, ExecutableElement>> getProperty = (filter) ->
							properties.filter(tuple -> filter.test(tuple._2()))
							.groupBy(groupByName.tupled())
							.mapValues(executables -> executables.toSortedSet(Comparator.comparing(Tuple2::_1)).head())
							.mapValues(Tuple2::_2);

					/* Extract getters and setters by priority */
					Map<String, ExecutableElement> setters = getProperty.apply(WsIOUtils::isSetter);
					Map<String, ExecutableElement> getters = getProperty.apply(WsIOUtils::isGetter);

					/* Return the map of getters and setters */
					return HashMap.of(WsIOProperty.SETTER, setters, WsIOProperty.GETTER, getters);

				});

	}

	/**
	 * Method that generates the type spec
	 *
	 * @param typeSpec type spec
	 * @param packageName package name of the class
	 * @param className class name of the class
	 */
	private void createJavaClass(TypeSpec typeSpec,
	                             String packageName,
	                             String className) {

		/* Check if none of the parameters is null */
		if (Objects.nonNull(typeSpec)
				&& Objects.nonNull(packageName)
				&& Objects.nonNull(className)) {

			try {

				/* Create source file and open the writer */
				JavaFileObject jfo = filer.createSourceFile(WsIOUtil.addPrefixName(className, packageName));
				Writer writer = jfo.openWriter();

				/* Build the class and write to the file */
				JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
				javaFile.writeTo(writer);

				/* Close the writer */
				writer.close();

			} catch (IOException e) {

				/* Print the error when the class could not be generated */
				WsIOHandler.error(messager, e, "Class could not be written");

			}

		}

	}

	private enum OperationType {
		DEFAULT,
		WRAPPER,
		ELEMENT,
		ATTRIBUTE;
	}

	private static class OperationIdentifier {
		private OperationType operationType;
		private String mainName;
		private String attributeName;
		private String elementName;
		private String elementWrapperName;
		static OperationIdentifier of(OperationType operationType,
									  String mainName,
									  String attributeName,
									  String elementName,
									  String elementWrapperName) {
			return new OperationIdentifier(operationType, mainName, attributeName, elementName, elementWrapperName);
		}
		OperationIdentifier(OperationType operationType,
							String mainName,
							String attributeName,
							String elementName,
							String elementWrapperName) {
			this.operationType = operationType;
			this.mainName = mainName;
			this.attributeName = attributeName;
			this.elementName = elementName;
			this.elementWrapperName = elementWrapperName;
		}
		public OperationType getOperationType() {
			return operationType;
		}
		public void setOperationType(OperationType operationType) {
			this.operationType = operationType;
		}
		public String getAttributeName() {
			return attributeName;
		}
		public void setAttributeName(String attributeName) {
			this.attributeName = attributeName;
		}
		public String getMainName() {
			return mainName;
		}
		public void setMainName(String mainName) {
			this.mainName = mainName;
		}
		public String getElementName() {
			return elementName;
		}
		public void setElementName(String elementName) {
			this.elementName = elementName;
		}
		public String getElementWrapperName() {
			return elementWrapperName;
		}
		public void setElementWrapperName(String elementWrapperName) {
			this.elementWrapperName = elementWrapperName;
		}
	}

}
