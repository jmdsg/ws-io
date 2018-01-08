package com.fiberg.wsio.processor;

import com.fiberg.wsio.annotation.WsIOAnnotate;
import com.fiberg.wsio.annotation.WsIOUseHideEmpty;
import com.fiberg.wsio.handler.state.*;
import com.fiberg.wsio.handler.time.WsIOInstant;
import com.fiberg.wsio.handler.time.WsIOTime;
import com.fiberg.wsio.util.WsIOUtil;
import com.squareup.javapoet.*;
import com.squareup.javapoet.TypeSpec.Builder;
import io.vavr.*;
import io.vavr.collection.*;
import io.vavr.control.Option;
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
import javax.xml.bind.annotation.*;
import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.fiberg.wsio.processor.WsIOConstant.*;

/**
 * Class that generates all classes from WsIO, it generates the clones, the messages and wrappers.
 */
class WsIOGenerator {

	/** Handles the report of errors, warnings and anothers */
	private Messager messager;

	/** Receives the generated class files */
	private Filer filer;

	/**
	 * @param messager handles the report of errors, warnings and anothers
	 * @param filer receives the generated class files
	 */
	WsIOGenerator(Messager messager, Filer filer) {
		this.messager = messager;
		this.filer = filer;
	}

	/**
	 * Method that generates all classes from WsIO, it generates the clones, the messages and wrappers.
	 *
	 * @param messageByType map containing type element and destination package
	 * @param cloneByGroup map containing the identifier { prefix name - suffix name } and a set of type elements
	 * @param cloneMessageByGroup map containing type element and a inner map with method name and a tuple of { method info - destination package }
	 * @param wrapperByType map containing type element and a inner map with method name and a tuple of { method info - destination package }
	 * @return {@code true} when all classes were generated ok, {@code false} otherwise
	 */
	boolean generateClasses(Map<TypeElement, String> messageByType,
	                        Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> cloneByGroup,
	                        Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>> cloneMessageByGroup,
	                        Map<TypeElement, Map<String, Tuple2<WsIOInfo, String>>> wrapperByType) {

		/* Get all class names that are message annotated */
		Map<String, String> messageClasses = messageByType.mapKeys(element ->
				element.getQualifiedName().toString());

		/* Map of types by name */
		Map<String, TypeElement> typeByName = cloneByGroup.values()
				.flatMap(e -> e)
				.map(Tuple2::_1)
				.appendAll(messageByType.keySet())
				.appendAll(wrapperByType.keySet())
				.toSet()
				.toMap(element -> element.getQualifiedName().toString(), e -> e);

		/* Process and create each clone by group */
		List<Tuple3<TypeSpec, String, String>> cloneTypes = cloneByGroup.flatMap((clone) -> {

			/* Get identifier key and set of elements */
			Tuple2<String, String> key = clone._1();
			Set<Tuple2<TypeElement, String>> elements = clone._2();

			/* Get prefix and suffix of identifier */
			String prefixName = key._1();
			String suffixName = key._2();

			/* Get identifier, clone classes types and names */
			Tuple2<String, String> identifier = Tuple.of(prefixName, suffixName);
			Map<String, String> cloneClasses = cloneByGroup.getOrElse(identifier, HashSet.empty())
					.toMap(tuple -> tuple.map1(type -> type.getQualifiedName().toString()));

			/* Context of the generation */
			WsIOContext context = new WsIOContext(prefixName, suffixName,
					StringUtils.EMPTY, StringUtils.EMPTY, messageClasses, cloneClasses,
					HashMap.empty(), typeByName, WsIOGenerate.CLONE);

			/* Create a delegate for each group and type element */
			return elements.map((tuple) -> {

				/* Extract type lement and package name */
				TypeElement element = tuple._1();
				String packageName = tuple._2();

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
			List<Tuple2<String, String>> names = List.of(
					Tuple.of(RESPONSE_PREFIX, RESPONSE_SUFFIX),
					Tuple.of(REQUEST_PREFIX, REQUEST_SUFFIX)
			);

			/* Return the type spec for responses and requests */
			return names.map(name -> {

				/* Get prefix and suffix names */
				String prefixName = name._1();
				String suffixName = name._2();

				/* Get identifier, clone classes types and names */
				Tuple2<String, String> identifier = Tuple.of(prefixName, suffixName);
				Map<String, String> cloneClasses = cloneByGroup.getOrElse(identifier, HashSet.empty())
						.toMap(tuple -> tuple.map1(type -> type.getQualifiedName().toString()));

				/* Context of the generation */
				WsIOContext context = new WsIOContext(StringUtils.EMPTY, StringUtils.EMPTY, prefixName, suffixName,
						messageClasses, cloneClasses, HashMap.empty(), typeByName, WsIOGenerate.MESSAGE);

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
			Tuple2<String, String> key = clone._1();
			Set<Tuple2<TypeElement, String>> elements = clone._2();

			/* Get prefix and suffix of identifier */
			String prefixClassName = key._1();
			String suffixClassName = key._2();

			/* Get identifier, clone classes types and names */
			Tuple2<String, String> identifier = Tuple.of(prefixClassName, suffixClassName);
			Map<String, String> cloneClasses = cloneByGroup.getOrElse(identifier, HashSet.empty())
					.toMap(tuple -> tuple.map1(type -> type.getQualifiedName().toString()));
			Map<String, String> cloneMessageClasses = cloneMessageByGroup.getOrElse(identifier, HashSet.empty())
					.toMap(tuple -> tuple.map1(type -> type.getQualifiedName().toString()));

			/* Create a delegate for each group and type element */
			return elements.flatMap((tuple) -> {

				/* Extract type lement and package name */
				TypeElement element = tuple._1();
				String packageName = tuple._2();

				/* Prefix and suffix wrapper names of request and response */
				List<Tuple2<String, String>> wrappers = List.of(
						Tuple.of(RESPONSE_PREFIX, RESPONSE_SUFFIX),
						Tuple.of(REQUEST_PREFIX, REQUEST_SUFFIX)
				);

				/* Return the tuple of spec package name and class name of each wrapper */
				return wrappers.map(wrapper -> {

					/* Get prefix and suffix of identifier */
					String prefixWrapperName = wrapper._1();
					String suffixWrapperName = wrapper._2();

					/* Context of the generation */
					WsIOContext context = new WsIOContext(prefixClassName, suffixClassName, prefixWrapperName,
							suffixWrapperName, messageClasses, cloneClasses,
							cloneMessageClasses, typeByName, WsIOGenerate.CLONE_MESSAGE);

					/* Create each delegate */
					TypeSpec typeSpec = generateDelegateType(element, context);

					/* Check if elements are in sets and get name by generate */
					String elementName = element.getQualifiedName().toString();
					Tuple2<String, String> names = context.getNameByGenerate(elementName);

					/* Suffix and prefix names */
					String prefixName = names._1();
					String suffixName = names._2();

					/* Class Name */
					String classInnerName = WsIOUtils.getFullSimpleInnerName(element);
					String className = WsIOUtil.addWrap(classInnerName, prefixName, suffixName);

					/* Tuple of type spec and package name */
					return Tuple.of(typeSpec, packageName, className);

				});

			});

		}).filter(tuple -> Objects.nonNull(tuple._1())).toList();

		/* Iterate for each type spec of clones, requests and reponses */
		cloneTypes.appendAll(messageTypes).appendAll(cloneMessageTypes).forEach(tuple -> {

			/* Get type spec, package name and class name */
			TypeSpec typeSpec = tuple._1();
			String packageName = tuple._2();
			String className = tuple._3();

			/* Generate the java class */
			createJavaClass(typeSpec, packageName, className);

		});

		/* Function used to transform clone maps */
		Function1<Map<Tuple2<String, String>, Set<Tuple2<TypeElement, String>>>,
				Map<String, Map<Tuple2<String, String>, String>>> transformClones =
				(clone) -> clone.flatMap(tuple -> tuple._2().map(info
						-> Tuple.of(tuple._1()._1(), tuple._1()._2(), info._1(), info._2())))
						.groupBy(Tuple4::_3)
						.mapKeys(TypeElement::getQualifiedName)
						.mapKeys(Name::toString)
						.mapValues(values -> values.toMap(tuple -> Tuple.of(tuple._1(), tuple._2()), Tuple4::_4));

		/* Set with the names of the clones classes */
		Map<String, Map<Tuple2<String, String>, String>> cloneClasses = transformClones.apply(cloneByGroup);

		/* Set with the names of the clones message classes */
		Map<String, Map<Tuple2<String, String>, String>> cloneMessageClasses = transformClones.apply(cloneMessageByGroup);

		/* Iterate for each wrapper and generate the classes */
		wrapperByType.forEach((element, wrapper) -> {

			/* Iterate for each method name and info */
			wrapper.forEach((name, infos) -> {

				/* Info and package name */
				WsIOInfo info = infos._1();
				String packageName = infos._2();

				/* Generate request and response type spec */
				TypeSpec request = generateWrapperType(WsIOType.REQUEST, info,
						messageClasses, cloneClasses, cloneMessageClasses, typeByName);
				TypeSpec response = generateWrapperType(WsIOType.RESPONSE, info,
						messageClasses, cloneClasses, cloneMessageClasses, typeByName);

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

		/* Full prefix and suffix names */
		String fullPrefixName = context.getPrefixWrapperName() + context.getPrefixClassName();
		String fullSuffixName = context.getSuffixClassName() + context.getSuffixWrapperName();

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

			/* Generate the method priotity from the inheritance structure */
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

			/* Create counter to avoid creation of invalid empty enums and add each contant to the builder */
			int constants = 0;
			for (Element enclosed : element.getEnclosedElements()) {
				if (ElementKind.ENUM_CONSTANT.equals(enclosed.getKind())) {
					builder = builder.addEnumConstant(enclosed.getSimpleName().toString());
					constants++;
				}
			}

			/* Return the enum only when is valid (not empty) */
			return constants > 0 ? builder.build() : null;

		} else if (ElementKind.INTERFACE.equals(element.getKind())
				|| ElementKind.CLASS.equals(element.getKind())) {

			/* Generate the inheritance structure */
			Map<WsIOLevel, Set<DeclaredType>> inheritance = resolveInheritance(element, context);

			/* Generate the method priotity from the inheritance structure */
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
					.map(declaredType -> context.getRecursiveFullTypeName(declaredType,
							true, false, true));

			/* Super interfaces set */
			List<TypeName> superInterfaces = inheritance.getOrElse(WsIOLevel.INTERFACE_INTERNAL, HashSet.empty())
					.map(declaredType -> context.getRecursiveFullTypeName(declaredType,
							true, false, true))
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
			List<MethodSpec> propertyMethods = generatePropertyMethods(getterPriorities, setterPriorities,
					HashMap.empty(), WsIOConstant.GET_DELEGATOR, hideEmpties, context);

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

				/* Create intrface builder and initialize predicate */
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

					/* Maximun level of the getter */
					WsIOLevel level = WsIOUtils.getPriorityLevelByName(getters, getterName);

					/* Setter name contructed from the getter name */
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

			/* Current annotations */
			List<AnnotationSpec> getterAnnotations = annotations.getOrElse(getterName, List.empty());
			List<AnnotationSpec> setterAnnotations = annotations.getOrElse(setterName, List.empty());

			/* Return the methods generated recursively */
			return WsIODelegator.generatePropertyDelegates(returnType, parameterType,
					getter, setter, getterName, setterName, propertyName, delegator, getterName, setterName,
					getterAnnotations, setterAnnotations, level, hideEmpties, context, messager);

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

		/* Return the overrided methods */
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

			/* Create the parametrized contructor and add super call when the class has super */
			MethodSpec.Builder parameterConstructor = MethodSpec.constructorBuilder();
			if (hasSuper) {
				parameterConstructor.addStatement("super($L)", FIELD_DELEGATOR);
			}

			/* Objects class */
			Class<?> objectsClass = java.util.Objects.class;

			/* Add the statement code to the construcotr */
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
	 * @param mustOverride indicates if the method delegator must be overrided or not
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
	 * @param info all wrapper info
	 * @param messageClasses map of message classes and package name
	 * @param cloneClassesByName map of cloned classes and package name by name
	 * @param cloneMessageClassesByName map of cloned message classes and package name by name
	 * @param typeByName map of types by full name
	 * @return generated wrapper type
	 */
	private TypeSpec generateWrapperType(WsIOType type,
	                                     WsIOInfo info,
	                                     Map<String, String> messageClasses,
	                                     Map<String, Map<Tuple2<String, String>, String>> cloneClassesByName,
	                                     Map<String, Map<Tuple2<String, String>, String>> cloneMessageClassesByName,
	                                     Map<String, TypeElement> typeByName) {

		/* Main executable element */
		ExecutableElement executableElement = info.getExecutableElement();

		/* Get annotate of the executable element and separator */
		boolean nameSwap = Option.of(executableElement)
				.map(WsIODescriptor::of)
				.flatMap(desc -> desc.getSingle(WsIOAnnotate.class))
				.filter(WsIOAnnotate::nameSwap)
				.isDefined();
		String separator = nameSwap ? WsIOConstant.SWAP_SEPARATOR : WsIOConstant.NO_SWAP_SEPARATOR;

		/* Method and operation names */
		String methodName = executableElement.getSimpleName().toString();
		String operationName = info.getOperationName();

		/* Simple and full class name */
		String classSimpleName = StringUtils.isNotBlank(operationName) ? operationName : methodName;
		String prefixTypeName = WsIOType.RESPONSE.equals(type) ? RESPONSE_WRAPPER_PREFIX : REQUEST_WRAPPER_PREFIX;
		String suffixTypeName = WsIOType.RESPONSE.equals(type) ? RESPONSE_WRAPPER_SUFFIX : REQUEST_WRAPPER_SUFFIX;
		String fullClassName = WsIOUtil.addWrap(WordUtils.capitalize(classSimpleName), prefixTypeName, suffixTypeName);

		/* List of descriptors with types, elements and names */
		List<Tuple3<TypeMirror, String, Tuple2<String, String>>> descriptors = getWrapperDescriptors(type, info);

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
		for (Tuple3<TypeMirror, String, Tuple2<String, String>> descriptor : descriptors) {

			/* Get mirror type, type element and name */
			TypeMirror mirror = descriptor._1();
			String name = descriptor._2();

			/* Check mirror is not a notype */
			if (!(mirror instanceof NoType)) {

				/* Qualifier, prefixes and suffixes */
				Tuple2<String, String> qualifier = descriptor._3();
				String prefixClassName = Objects.nonNull(qualifier) ? qualifier._1() : StringUtils.EMPTY;
				String suffixClassName = Objects.nonNull(qualifier) ? qualifier._2() : StringUtils.EMPTY;
				String prefixWrapperName = WsIOType.RESPONSE.equals(type) ? RESPONSE_PREFIX : REQUEST_PREFIX;
				String suffixWrapperName = WsIOType.RESPONSE.equals(type) ? RESPONSE_SUFFIX : REQUEST_SUFFIX;
				WsIOGenerate generate = Objects.nonNull(qualifier) ? WsIOGenerate.CLONE_MESSAGE : WsIOGenerate.MESSAGE;

				/* Get identifier and get clone classes and clone message classes */
				Tuple2<String, String> identifier = Tuple.of(prefixClassName, suffixClassName);
				Map<String, String> cloneClasses = cloneClassesByName.flatMap((key, map) ->
						map.get(identifier).map(packageName -> Tuple.of(key, packageName)));
				Map<String, String> cloneMessageClasses = cloneMessageClassesByName.flatMap((key, map) ->
						map.get(identifier).map(packageName -> Tuple.of(key, packageName)));

				/* Context of the generation */
				WsIOContext context = new WsIOContext(prefixClassName, suffixClassName,
						prefixWrapperName, suffixWrapperName, messageClasses, cloneClasses,
						cloneMessageClasses, typeByName, generate);

				/* Recursive full type name */
				TypeName internalType = context.getRecursiveFullTypeName(mirror,
						true, false, true);

				/* Lower and upper names */
				String lowerName = WordUtils.uncapitalize(name);
				String upperName = WordUtils.capitalize(name);

				/* Internal and external chars */
				String internalChar = Option.when(!nameSwap, separator).getOrElse("");
				String externalChar  = Option.when(nameSwap, separator).getOrElse("");

				/* Internal and external getter and setter names */
				String internalGetName = String.format("get%s%s", upperName, internalChar);
				String internalSetName = String.format("set%s%s", upperName, internalChar);
				String externalGetName = String.format("get%s%s", upperName, externalChar);
				String externalSetName = String.format("set%s%s", upperName, externalChar);

				/* Internal and external parameter names */
				String internalParameterName = WsIOConstant.DEFAULT_RESULT.equals(lowerName) ?
						String.format("%s%s", lowerName, separator) : lowerName;
				String externalParameterName = WsIOConstant.DEFAULT_RESULT.equals(lowerName) ?
						String.format("%s%s", lowerName, separator) : lowerName;

				/* Assign field name and add it to the fields */
				String fieldName = WsIOConstant.DEFAULT_RESULT.equals(lowerName) ?
						String.format("%s%s", lowerName, separator) : lowerName;
				fieldNames = fieldNames.append(fieldName);

				/* List with the external get annotations */
				List<AnnotationSpec> externalGetAnnotations = List.of(
						AnnotationSpec.builder(XmlTransient.class).build()
				);

				/* Create fiend and add it to the set */
				TypeName fieldType = TypeName.get(mirror);
				FieldSpec fieldSpec = FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE).build();
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
				if (info.getWrappers().contains(WsIOWrapper.INNER_WRAPPER)
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

					/* Generate the method priotity from the inheritance structure */
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
					Function1<ExecutableElement, String> getExecutableName = executable ->
							executable.getSimpleName().toString();
					Map<String, Tuple2<String, String>> propertyToProperties = properties
							.map(tuple -> Tuple.of(getExecutableName.apply(tuple._1()),
									getExecutableName.apply(tuple._2())))
							.toLinkedMap(tuple -> WsIOUtil.getterToProperty(tuple._1()), tuple -> tuple);

					/* Map from property to final property, getter and setter */
					Map<String, Tuple3<String, String, String>> propertyToFinals = propertyToProperties
							.map((propertyName, propertyNames) ->
									Tuple.of(propertyName,  Tuple.of(
											String.format("%s%s", propertyName, separator),
											String.format("%s%s", propertyNames._1(), separator),
											String.format("%s%s", propertyNames._2(), separator))))
							.filter(tuple -> !nameSwap);

					/* Getter annotations */
					Map<String, List<AnnotationSpec>> getterAnnotations = propertyToProperties
							.toMap(tuple -> tuple._2()._1(), Tuple2::_1)
							.mapValues(propertyName -> List.of(AnnotationSpec.builder(XmlElement.class)
									.addMember("name", "$S", propertyName).build()));

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
						List<AnnotationSpec> setterListAnnotations = List.empty();

						/* Return the methods generated recursively */
						return WsIODelegator.generatePropertyDelegates(returnType, parameterType, getter, setter,
								finalGetterName, finalSetterName, finalPropertyName, externalGetName, getterName, setterName,
								getterListAnnotations, setterListAnnotations, level, hideEmpties, context, messager);

					});

					/* Add method of the property methods */
					methods = methods.appendAll(propertyMethods);

				} else {

					/* Flag indicating if empty collections, maps and arrays should be hidden */
					boolean methodHideEmpties = info.getWrappers().contains(WsIOWrapper.HIDE_EMPTY_WRAPPER);

					/* List with the internal get annotations */
					List<AnnotationSpec> internalGetAnnotations = List.empty();
					internalGetAnnotations = internalGetAnnotations.append(AnnotationSpec.builder(XmlElement.class)
							.addMember("name", "$S", lowerName).build());

					/* Create internal get accessor and code block */
					String internalGetAccessor = String.format("%s()", externalGetName);
					CodeBlock internalGetBlock = WsIODelegator.generateRecursiveTransformToInternal(mirror,
							internalType, context, internalGetAccessor, methodHideEmpties);

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
					CodeBlock internalSetBlock = WsIODelegator.generateRecursiveTransformToExternal(internalType,
							mirror, context, internalParameterName, methodHideEmpties);

					/* Create parameter and method spec and add it to the methods */
					ParameterSpec internalParameter = ParameterSpec.builder(internalType, internalParameterName).build();
					MethodSpec.Builder internalSetBuilder = MethodSpec.methodBuilder(internalSetName)
							.addParameter(internalParameter)
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
		if (WsIOType.RESPONSE.equals(type) && info.getWrappers().size() > 0) {

			/* Get additionals */
			Tuple4<List<FieldSpec>, List<MethodSpec>, List<TypeName>, List<AnnotationSpec>> additionals =
					generateAdditionals(info.getWrappers(), fieldNames);

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
	 * Method that generates the fields, methods, interfaces and annotations of wrapper additionals.
	 *
	 * @param wrappers list of wrapper enums
	 * @param fieldNames names of the fields
	 * @return fields, methods, interfaces and annotations of wrapper additionals
	 */
	private Tuple4<List<FieldSpec>, List<MethodSpec>, List<TypeName>, List<AnnotationSpec>> generateAdditionals(Set<WsIOWrapper> wrappers,
	                                                                                                            List<String> fieldNames) {

		/* Flag indicating if empty collections, maps and arrays should be hidden */
		boolean hideEmpties = wrappers.contains(WsIOWrapper.HIDE_EMPTY_WRAPPER);

		/* Fields, methods and interfaces */
		List<FieldSpec> fields = List.empty();
		List<MethodSpec> methods = List.empty();
		List<TypeName> interfaces = List.empty();
		List<AnnotationSpec> annotations = List.empty();

		/* Declare function to create the menbers */
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
							.map(pameterized -> pameterized.rawType)
							.orElse(Option.of(typeName)
									.filter(ClassName.class::isInstance)
									.map(ClassName.class::cast))
							.map(ClassName::reflectionName)
							.getOrNull();
					boolean isArray = typeName instanceof ArrayTypeName;
					boolean isCollection = WsIOCollection.ALL.contains(possibleClassName);

					/* Declare the code block and check if get check is empty or not */
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
				Tuple.of("successfuls", "successful", "showSuccessfuls"),
				Tuple.of("failures", "failure", "showFailures"),
				Tuple.of("warnings", "warning", "showWarnings"),
				Tuple.of("showSuccessfuls", "showSuccessful", null),
				Tuple.of("showFailures", "showFailure", null),
				Tuple.of("showWarnings", "showWarning", null));

		/* Next line counter to format the code */
		Integer nextLine = 0;

		/* Declare the list of orders */
		List<String> orders = List.empty();

		/* Check if the time wrapper is defined and add it to orders */
		if (wrappers.contains(WsIOWrapper.TIME_WRAPPER)) {
			nextLine++;
			orders = orders.appendAll(timeNames.map(Tuple3::_1));
		}

		/* Check if the state wrapper is defined and add the first 6 to orders */
		if (wrappers.contains(WsIOWrapper.STATE_WRAPPER)) {
			nextLine += 6;
			orders = orders.appendAll(stateNames.map(Tuple3::_2).take(6));
		}

		/* Add all field names */
		orders = orders.appendAll(fieldNames);

		/* Check if the state wrapper is defined, skip the first 6 and add the next 3 elements to the orders */
		if (wrappers.contains(WsIOWrapper.STATE_WRAPPER)) {
			orders = orders.appendAll(stateNames.map(Tuple3::_1).drop(6).take(3));
		}

		/* Create the name format */
		String format = Stream.of("$S")
				.cycle(orders.size())
				.intersperse(", ")
				.prepend("{ ")
				.append(" }")
				.insert(2 * nextLine + 1, wrappers.contains(WsIOWrapper.STATE_WRAPPER) ? "\n" :"")
				.mkString();

		/* Add the annotation to the list */
		annotations = annotations.append(AnnotationSpec.builder(XmlType.class)
				.addMember("propOrder", format, orders.toJavaArray()).build());

		/* Chck if time wrapper is going to be added or not */
		if (wrappers.contains(WsIOWrapper.TIME_WRAPPER)) {

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

		/* Chck if state wrapper is going to be added or not */
		if (wrappers.contains(WsIOWrapper.STATE_WRAPPER)) {

			/* Get all results */
			List<Tuple2<List<FieldSpec>, List<MethodSpec>>> results = List.of(
					createElement.apply(ClassName.get(String.class)),
					createElement.apply(ClassName.get(WsIOText.class)),
					createElement.apply(ClassName.get(WsIOText.class)),
					createElement.apply(ClassName.get(String.class)),
					createAttribute.apply(ClassName.get(WsIOStatus.class)),
					createAttribute.apply(ClassName.get(WsIODetail.class)),
					createWrapper.apply(ParameterizedTypeName.get(ClassName.get(java.util.List.class),
							ClassName.get(WsIOElement.class))),
					createWrapper.apply(ParameterizedTypeName.get(ClassName.get(java.util.List.class),
							ClassName.get(WsIOElement.class))),
					createWrapper.apply(ParameterizedTypeName.get(ClassName.get(java.util.List.class),
							ClassName.get(WsIOElement.class))),
					createTransient.apply(ClassName.get(Boolean.class)),
					createTransient.apply(ClassName.get(Boolean.class)),
					createTransient.apply(ClassName.get(Boolean.class)))
					.zip(stateNames)
					.map(tuple -> tuple._1().apply(tuple._2()));

			/* Add all results to fields and methods */
			fields = fields.appendAll(results.flatMap(Tuple2::_1));
			methods = methods.appendAll(results.flatMap(Tuple2::_2));

			/* Add the interface to the set */
			interfaces = interfaces.append(ClassName.get(WsIOState.class));

		}

		/* Return fields, methods, interfaces and annotations */
		return Tuple.of(fields, methods, interfaces, annotations);

	}

	/**
	 * Method that create the descriptors of reponse and request types.
	 *
	 * @param type indicates if is a response or request wrapper
	 * @param info all wrapper info
	 * @return descriptors of reponse and request types.
	 */
	private List<Tuple3<TypeMirror, String, Tuple2<String, String>>> getWrapperDescriptors(WsIOType type,
	                                                                                       WsIOInfo info) {

		/* List to fill with types, elements and names */
		List<Tuple3<TypeMirror, String, Tuple2<String, String>>> descriptors = List.empty();

		/* Check if the type is response or request */
		if (WsIOType.RESPONSE.equals(type)) {

			/* Check if the type mirror is a mirror type */
			TypeMirror typeMirror = info.getReturnType();
			if (Objects.nonNull(typeMirror)) {

				/* Return name if present otherwise default value*/
				String returnName = info.getReturnName();
				String result = StringUtils.isNotBlank(returnName) ? returnName : DEFAULT_RESULT;

				/* Get qualifier from annotation */
				Tuple2<String, String> qualifier = info.getReturnQualifier();

				/* Fill the type, element and name */
				Tuple3<TypeMirror, String, Tuple2<String, String>> descriptor =
						Tuple.of(typeMirror, result, qualifier);
				descriptors = descriptors.append(descriptor);

			}

		} else if (WsIOType.REQUEST.equals(type)) {

			/* Iterate for each parameter */
			for (int index = 0; index < info.getParameterTypes().size(); index++) {

				/* Check if the type mirror is a mirror type */
				TypeMirror typeMirror = info.getParameterTypes().get(index);
				if (Objects.nonNull(typeMirror)) {

					/* Parameter name if present otherwise default value */
					String defaultName = DEFAULT_PARAMETER + index;
					String parameterName = info.getParameterNames().get(index);
					String argument = StringUtils.isNotBlank(parameterName) ? parameterName : defaultName;

					/* Get qualifier from annotation */
					Tuple2<String, String> qualifier = info.getParameterQualifiers().get(index);

					/* Fill the type, element and name */
					Tuple3<TypeMirror, String, Tuple2<String, String>> descriptor =
							Tuple.of(typeMirror, argument, qualifier);
					descriptors = descriptors.append(descriptor);

				}

			}

		}

		/* Return descriptors */
		return descriptors;

	}

	/**
	 * Method that return if the delegator should be overriden or not
	 *
	 * @param getters map of getters with priority levels
	 * @param setters map of setters with priority levels
	 * @return {@code true} if the delegator should be overriden {@code false} otherwise
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

			/* Get delegator maximun level */
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

		/* Create the root delegate element if is non null and instance of declared type */
		Option<DeclaredType> rootOpt = Option.of(rootType)
				.filter(DeclaredType.class::isInstance)
				.map(DeclaredType.class::cast);

		/* Create inheritance map with local class */
		Map<WsIOLevel, Set<DeclaredType>> inheritances = rootOpt.toMap(val -> WsIOLevel.LOCAL, HashSet::of);

		/* Create the super delegate element */
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
				WsIOGenerate generate = context.getGenerate();
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

		/* Add the element if is non null and instance of declared type */
		inheritances = inheritances.merge(superOpt.toMap(extractDeclaredClassInterface.apply(true), HashSet::of));

		/* Return current local and super class merged with non null super interfaces instance of declared type */
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

					/* Fcuntion to group by executable name */
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

		/* Check if non of the parameters is null */
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

}
