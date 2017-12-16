package com.fiberg.wsio.processor;

import com.fiberg.wsio.util.WsIOUtil;
import com.squareup.javapoet.*;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.Messager;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import java.util.Objects;

/**
 * Class to delegate the transformation from internal to external classes and vice versa.
 */
class WsIODelegator {

	/**
	 * Method that checks if two reference types of a getter and setter are recursively compatible.
	 *
	 * @param returnReference return reference type
	 * @param parameterReference parameter reference type
	 * @return {@code true} if the two reference types are recursively compatible {@code false} otherwise
	 */
	static boolean isRecursiveCompatible(ReferenceType returnReference,
	                                     ReferenceType parameterReference) {

		/* Check the instance types of the parameters */
		if (returnReference instanceof ArrayType && parameterReference instanceof ArrayType) {

			/* Get array types */
			ArrayType returnArray = (ArrayType) returnReference;
			ArrayType parameterArray = (ArrayType) parameterReference;

			/* Check if components of array are reference types */
			if (returnArray.getComponentType() instanceof ReferenceType
					&& parameterArray.getComponentType() instanceof ReferenceType) {

				/* Return the recursive result of component types of the array */
				return isRecursiveCompatible((ReferenceType) returnArray.getComponentType(),
						(ReferenceType) parameterArray.getComponentType());

			}

		} else if (returnReference instanceof DeclaredType && parameterReference instanceof DeclaredType) {

			/* Get declared types */
			DeclaredType returnDeclared = (DeclaredType) returnReference;
			DeclaredType parameterDeclared = (DeclaredType) parameterReference;

			/* Get the generics of each type */
			java.util.List<? extends TypeMirror> returnGenerics = returnDeclared.getTypeArguments();
			java.util.List<? extends TypeMirror> parameterGenerics = parameterDeclared.getTypeArguments();

			/* Check if at least one parameter has genercis */
			if (returnGenerics.size() > 0 || parameterGenerics.size() > 0) {

				/* Check generics cound match */
				if (returnGenerics.size() == parameterGenerics.size()) {

					/* Iterate for each generic */
					for (int index = 0; index < returnGenerics.size(); index++) {

						/* Complex logic is not made when types are wildcards, the type is only unwilded.
						 * Get the unwilded return and parameter types */
						TypeMirror returnGenericType = WsIOUtils.getUnwildType(returnGenerics.get(index));
						TypeMirror parameterGenericType = WsIOUtils.getUnwildType(parameterGenerics.get(index));

						/* Check if elements are reference types and check recursible if they are compatible or not */
						if (returnGenericType instanceof ReferenceType
								&& parameterGenericType instanceof ReferenceType
								&& !isRecursiveCompatible((ReferenceType) returnGenericType,
								(ReferenceType) parameterGenericType)) {

							/* Return false when a generic type is not compatible with the other in the same position */
							return false;

						}

					}

				} else {

					/* Return false when generic cound does not match */
					return false;

				}

			}

			/* Get elements of each declared type */
			Element returnElement = returnDeclared.asElement();
			Element parameterElement = parameterDeclared.asElement();

			/* Check that return and parameter elements are type elements */
			if (returnElement instanceof TypeElement
					&& parameterElement instanceof TypeElement) {

				/* Return the comparison between full names of the type elemenets */
				return StringUtils.equals(((TypeElement) returnElement).getQualifiedName().toString(),
						((TypeElement) parameterElement).getQualifiedName().toString());

			}

		}

		/* Return false by default */
		return false;

	}

	/**
	 * Method that returns the first collection implementation and the rest just internals.
	 *
	 * @param referenceType reference type
	 * @param useInternalTypes indicates if internal types are going to be used or not
	 * @param context context of the process
	 * @return first collection implementation and the rest just internals
	 */
	private static TypeName ensureFirstConcreteClass(ReferenceType referenceType,
	                                                 Boolean useInternalTypes,
	                                                 WsIOContext context) {

		/* Check the type of reference */
		if (referenceType instanceof DeclaredType) {

			/* Get declared name, type element and name*/
			DeclaredType declaredType = (DeclaredType) referenceType;
			TypeElement typeElement = (TypeElement) declaredType.asElement();
			String elementName = typeElement.getQualifiedName().toString();

			/* Check that the first element is a interface of map or collection */
			if (WsIOCollection.MAP_INTERFACES.contains(elementName)
					|| WsIOCollection.COLLECTION_INTERFACES.contains(elementName)) {

				/* Get the recursive type name with implementations on collection and map classes */
				TypeName implementation = context.getRecursiveFullTypeName(referenceType,
						useInternalTypes, true, false);

				/* Check if type name is parameterized */
				if (implementation instanceof ParameterizedTypeName) {

					/* Get parameterized typa name */
					ParameterizedTypeName parameterized = (ParameterizedTypeName) implementation;

					/* Get the recursive type name without implementations only internals */
					ParameterizedTypeName internal = (ParameterizedTypeName) context.getRecursiveFullTypeName(
							referenceType, useInternalTypes, false, false);

					/* Return first implementation */
					return ParameterizedTypeName.get(parameterized.rawType,
							internal.typeArguments.toArray(new TypeName[internal.typeArguments.size()]));

				} else {

					/* Return implementation when type name is not parameterized */
					return implementation;

				}

			}

		}

		/* Return recursive name when type is not declared */
		return context.getRecursiveFullTypeName(referenceType,
				useInternalTypes, false, false);

	}

	/**
	 * Method that generates the method specs delegates of a property.
	 *
	 * @param returnReference return reference type
	 * @param parameterReference parameter reference type
	 * @param getter getter method
	 * @param setter setter method
	 * @param getterName name of the getter method
	 * @param setterName name of the setter method
	 * @param propertyName name of the property method
	 * @param level level of the method
	 * @param context context of the process
	 * @param messager messager to print the output
	 * @return the method specs delegates of a property
	 */
	static List<MethodSpec> generatePropertyDelegates(ReferenceType returnReference,
	                                                  ReferenceType parameterReference,
	                                                  ExecutableElement getter,
	                                                  ExecutableElement setter,
	                                                  String getterName,
	                                                  String setterName,
	                                                  String propertyName,
	                                                  WsIOLevel level,
	                                                  WsIOContext context,
	                                                  Messager messager) {

		/* Check that the types are compatible */
		if (isRecursiveCompatible(returnReference, parameterReference)) {

			/* Get full names when the class is in message or clones classes */
			TypeName returnTypeName = context.getRecursiveFullTypeName(returnReference,
					true, false, true);
			TypeName parameterTypeName = context.getRecursiveFullTypeName(parameterReference,
					true, false, true);

			/* Create getter builder */
			MethodSpec.Builder getterSpecBuilder = MethodSpec.methodBuilder(getterName)
					.addModifiers(Modifier.PUBLIC)
					.returns(returnTypeName);

			/* Create setter builder */
			MethodSpec.Builder setterSpecBuilder = MethodSpec.methodBuilder(setterName)
					.addModifiers(Modifier.PUBLIC)
					.addParameter(parameterTypeName, propertyName);

			/* Check if the property is to be delegated or not */
			boolean isDelegated = WsIOLevel.LOCAL.equals(level)
					|| WsIOLevel.INTERFACE_EXTERNAL.equals(level)
					|| WsIOLevel.CLASS_EXTERNAL.equals(level);

			/* Check the abstract modifiers of getter and setter */
			if (getter.getModifiers().contains(Modifier.ABSTRACT)
					&& setter.getModifiers().contains(Modifier.ABSTRACT)
					&& isDelegated) {

				/* Add abstract modifiers, builder and return getter and setter methods */
				return List.of(getterSpecBuilder.addModifiers(Modifier.ABSTRACT).build(),
						setterSpecBuilder.addModifiers(Modifier.ABSTRACT).build());

			} else if (!getter.getModifiers().contains(Modifier.ABSTRACT)
					&& !setter.getModifiers().contains(Modifier.ABSTRACT)
					&& isDelegated) {

				/* Recursive set and get code blocks */
				CodeBlock setCodeBlock = generateRecursiveTransformToExternal(parameterTypeName, parameterReference,
						context, propertyName);
				CodeBlock getCodeBlock = generateRecursiveTransformToInternal(returnReference, returnTypeName,
						context, String.format("%s().%s()", WsIOConstant.GET_DELEGATOR, getterName));

				/* Check codes are non null */
				if (Objects.nonNull(setCodeBlock) && Objects.nonNull(getCodeBlock)) {

					/* Create getter method and add it to the set */
					MethodSpec getterSpec = getterSpecBuilder
							.addCode("return $L() != null ? ", WsIOConstant.GET_DELEGATOR)
							.addCode(getCodeBlock)
							.addCode(" : null")
							.addCode(";").addCode("\n")
							.build();

					/* Create setter method and add it to the set */
					MethodSpec setterSpec = setterSpecBuilder
							.beginControlFlow("if ($L() != null && $L != null)",
									WsIOConstant.GET_DELEGATOR, propertyName)
							.addCode("$L().$L(", WsIOConstant.GET_DELEGATOR, setterName)
							.addCode(setCodeBlock)
							.addCode(")").addCode(";").addCode("\n")
							.endControlFlow()
							.build();

					/* return getter and setter methods */
					return List.of(getterSpec, setterSpec);

				}

			} else if (isDelegated) {

						/* Print the error message when the getter and setter abstract modifiers does not match */
				WsIOHandler.error(messager, "Methods of the element %s and %s, should have getter %s and setter %s" +
								" of the same type, abstract or non abstract",
						returnTypeName, parameterTypeName, getter, setter);

			}

		}

		/* Return empty list by default and when are not types are not compatible */
		return List.empty();

	}

	/**
	 * Method that generates the recursive code block to transform to internal or external.
	 *
	 * @param external external reference type
	 * @param internal internal type name
	 * @param context context of the process
	 * @param accessor access variable or function name
	 * @param level current recursive level
	 * @param toInternal indicates if the destiny is a internal or not
	 * @return recursive internal or external code block to transform.
	 */
	private static CodeBlock generateRecursiveTransform(ReferenceType external,
	                                                    TypeName internal,
	                                                    WsIOContext context,
	                                                    String accessor,
	                                                    Integer level,
	                                                    Boolean toInternal) {

		/* Check the instance of both types */
		if (external instanceof ArrayType && internal instanceof ArrayTypeName) {

			/* Get component of each type when is an array */
			TypeMirror externalComponentType = ((ArrayType) external).getComponentType();
			TypeName internalComponentTypeName = ((ArrayTypeName) internal).componentType;

			/* Check external type is a reference type */
			if (externalComponentType instanceof ReferenceType) {

				/* Create next accesor name and get recursive code block */
				String accessorName = "component";
				String componentAccessor = String.format("%s_%d_", accessorName, level);
				ReferenceType externalComponentReference = (ReferenceType) externalComponentType;
				CodeBlock componentBlock = generateRecursiveTransform(externalComponentReference, internalComponentTypeName,
						context, componentAccessor, level + 1, toInternal);

				/* Check code block is not null */
				if (Objects.nonNull(componentBlock)) {

					/* Arrays class */
					Class<?> arraysClass = java.util.Arrays.class;

					/* Get array info */
					Tuple2<ReferenceType, Integer> arrayInfo = WsIOUtils.getArrayInfo(external);

					/* Get declared type and array count */
					ReferenceType referenceType = arrayInfo._1();
					Integer dimension = arrayInfo._2();

					/* Check array count is greater than zero */
					if (dimension > 0) {

						/* Get class to cast of dimension - 1 */
						TypeName typeName = context.getRecursiveFullTypeName(referenceType,
								toInternal, false, false);
						TypeName castTypeName = WsIOUtils.getArrayType(typeName, dimension - 1);

						/* Declare destiny class name and check if is parameterized or not */
						TypeName destinyClassName;
						if (typeName instanceof ParameterizedTypeName) {

							/* Extract the raw type of a parameterized type */
							destinyClassName = ((ParameterizedTypeName) typeName).rawType;

						} else {

							/* Destiny type name when is not parameterized */
							destinyClassName = typeName;

						}

						/* Get destiny array */
						TypeName destinyTypeName = WsIOUtils.getArrayType(destinyClassName, dimension - 1);

						/* Build and return the code block for array types */
						return CodeBlock.builder()
								.add("($L != null ? $T.stream($L)", accessor, arraysClass, accessor).add("\n")
								.add(".map($L -> ", componentAccessor)
								.add(componentBlock)
								.add(")").add("\n")
								.add(".<$T>toArray($T[]::new) : null)", castTypeName, destinyTypeName)
								.build();

					}

				}

			}

		} else if (external instanceof DeclaredType) {

			/* Get declared type and check element is type element */
			DeclaredType externalDeclared = (DeclaredType) external;
			if (externalDeclared.asElement() instanceof TypeElement) {

				/* Get component of each type when is a collection */
				TypeElement externalTypeElement = (TypeElement) externalDeclared.asElement();
				String externalElementName = externalTypeElement.getQualifiedName().toString();

				/* Check type of external */
				if (WsIOCollection.ALL.contains(externalElementName) &&
						context.isRecursiveGenericInternalType(externalDeclared)) {

					/* Check if internal is parameterized */
					if (internal instanceof ParameterizedTypeName) {

						ParameterizedTypeName internalParameterizedType = (ParameterizedTypeName) internal;
						List<TypeName> internalGenerics = List.ofAll(WsIOUtils.getClearedGenericTypes(internalParameterizedType));

						List<ReferenceType> externalGenerics = List.ofAll(WsIOUtils.getClearedGenericTypes(externalDeclared));

						/* Check if is a collection or a map */
						if (WsIOCollection.COLLECTIONS.contains(externalElementName)
								&& internalGenerics.size() == 1 && externalGenerics.size() == 1) {

							/* Type name and mirror of the element */
							TypeName internalElement = internalGenerics.get(0);
							ReferenceType externalElement = externalGenerics.get(0);

							/* Create next accesor name and get recursive code block */
							String accessorName = "element";
							String elementAccessor = String.format("%s_%d_", accessorName, level);
							CodeBlock elementBlock = generateRecursiveTransform(externalElement, internalElement,
									context, elementAccessor, level + 1, toInternal);

							/* Check code block is not null */
							if (Objects.nonNull(elementBlock)) {

								/* Get the implementation class name */
								String implementationName = WsIOCollection.IMPLEMENTATIONS
										.getOrDefault(externalElementName, externalElementName);
								ClassName implementationClass = ClassName.bestGuess(implementationName);

								/* Cast class type name */
								TypeName castClass = ensureFirstConcreteClass(externalDeclared,
										toInternal, context);

								/* Build and return the code block for collection types */
								return CodeBlock.builder()
										.add("($L != null ? $L.stream()", accessor, accessor).add("\n")
										.add(".map($L -> ", elementAccessor)
										.add(elementBlock)
										.add(")").add("\n")
										.add(".<$T>collect($T::new, $T::add, $T::addAll) : null)", castClass,
												implementationClass, implementationClass, implementationClass)
										.build();

							}

						} else if (WsIOCollection.MAPS.contains(externalElementName)
								&& internalGenerics.size() == 2 && externalGenerics.size() == 2) {

							/* Type names of key and value */
							TypeName internalKey = internalGenerics.get(0);
							TypeName internalValue = internalGenerics.get(1);

							/* Type mirrors of key and value */
							ReferenceType externalKey = externalGenerics.get(0);
							ReferenceType externalValue = externalGenerics.get(1);

							/* Accessor name and general accessor */
							String accessorName = "entry";
							String generalAccessor = String.format("%s_%d_", accessorName, level);

							/* Create next accesor key name and get recursive code block */
							String keyAccessor = generalAccessor + ".getKey()";
							CodeBlock keyBlock = generateRecursiveTransform(externalKey, internalKey,
									context, keyAccessor, level + 1, toInternal);

							/* Create next accesor value name and get recursive code block */
							String valueAccessor = generalAccessor + ".getValue()";
							CodeBlock valueBlock = generateRecursiveTransform(externalValue, internalValue,
									context, valueAccessor, level + 1, toInternal);

							/* Check code blocks are not null */
							if (Objects.nonNull(keyBlock) && Objects.nonNull(valueBlock)) {

								/* Get the implementation class name */
								String implementationName = WsIOCollection.IMPLEMENTATIONS
										.getOrDefault(externalElementName, externalElementName);
								ClassName implementationClass = ClassName.bestGuess(implementationName);

								/* Cast class type name */
								TypeName castClass = ensureFirstConcreteClass(externalDeclared,
										toInternal, context);

								/* Entry class to create simple entries */
								Class<?> entryClass = java.util.AbstractMap.SimpleEntry.class;

								/* Build and return the code block for map types */
								return CodeBlock.builder()
										.add("($L != null ? $L.entrySet().stream()", accessor, accessor).add("\n")
										.add(".map($L -> new $T<>(", generalAccessor, entryClass)
										.add(keyBlock)
										.add(", ").add("\n")
										.add(valueBlock)
										.add(")").add(")").add("\n")
										.add(".<$T>collect($T::new, (map__, entry__) -> ", castClass, implementationClass).add("\n")
										.add("map__.put(entry__.getKey(), entry__.getValue()), $T::putAll) : null)",
												implementationClass)
										.build();

							}

						}

					}

				} else if (context.isInternalType(externalElementName)) {

					/* Check if is to internal or not */
					if (toInternal) {

						/* Check the kind of the element */
						if (ElementKind.CLASS.equals(externalTypeElement.getKind())) {

							/* Get the generic literal */
							String generic = Option.of(externalDeclared)
									.filter(ParameterizedTypeName.class::isInstance)
									.map(ParameterizedTypeName.class::cast)
									.map(parameterized -> parameterized.typeArguments)
									.filter(Objects::nonNull)
									.map(java.util.List::size)
									.filter(integer -> integer > 0)
									.map(integer -> "<>")
									.getOrElse(StringUtils.EMPTY);

							/* Return the code block when type is internal to internal*/
							return CodeBlock.of("($L != null ? new $T$L($L) : null)",
									accessor, internal, generic, accessor);

						} else if (ElementKind.INTERFACE.equals(externalTypeElement.getKind())) {

							/* Get candidates class names and package separeted joined by dot and them separated with commas */
							Set<String> classes = context.getCandidateForceClasses(externalTypeElement);
							String classesString = classes
									.map(str -> String.format("\"%s\"", str))
									.mkString(", \n");

							/* Arrays and generator class */
							Class<?> arraysClass = java.util.Arrays.class;
							TypeName generatorClass = ClassName.get(WsIOUtil.class);

							/* Return the code block when type is internal to external*/
							return CodeBlock.of("($L != null ? $T.$L($L, \n" +
											"$T.class, $T.asList($L)) : null)",
									accessor, generatorClass, WsIOConstant.GENERATOR_FORCE, accessor,
									internal, arraysClass, classesString);

						} else if (ElementKind.ENUM.equals(externalTypeElement.getKind())) {

							/* Return the code block when type is internal to external*/
							return CodeBlock.of("($L != null ? $T.valueOf($L.name()) : null)", accessor,
									internal, accessor);

						}

					} else {

						/* Check the kind of the element */
						if (ElementKind.INTERFACE.equals(externalTypeElement.getKind())
								|| ElementKind.CLASS.equals(externalTypeElement.getKind())) {

							/* Return the code block when type is internal to external*/
							return CodeBlock.of("($L != null ? $L.$L() : null)", accessor,
									accessor, WsIOConstant.GET_DELEGATOR);

						} else if (ElementKind.ENUM.equals(externalTypeElement.getKind())) {

							/* Return the code block when type is internal to external*/
							return CodeBlock.of("($L != null ? $T.valueOf($L.name()) : null)", accessor,
									externalTypeElement, accessor);

						}

					}

				} else {

					/* Return the code block external type */
					return CodeBlock.of("$L", accessor);

				}

			}

		}

		/* Return null by default */
		return null;

	}

	/**
	 * Method that generates the recursive code block to transform to internal.
	 *
	 * @param fromReference origin reference type
	 * @param toTypeName destiny type name
	 * @param context context of the process
	 * @param accessor access variable or function name
	 * @return recursive internal code block to transform from external.
	 */
	static CodeBlock generateRecursiveTransformToInternal(ReferenceType fromReference,
	                                                      TypeName toTypeName,
	                                                      WsIOContext context,
	                                                      String accessor) {

		/* Return the transform to internal */
		return generateRecursiveTransform(fromReference, toTypeName, context, accessor, 0, true);

	}

	/**
	 * Method that generates the recursive code block to transform to external.
	 *
	 * @param fromTypeName origin type name
	 * @param toReference destiny reference type
	 * @param context context of the process
	 * @param accessor access variable or function name
	 * @return recursive external code block to transform from internal.
	 */
	static CodeBlock generateRecursiveTransformToExternal(TypeName fromTypeName,
	                                                      ReferenceType toReference,
	                                                      WsIOContext context,
	                                                      String accessor) {

		/* Return the transform to external */
		return generateRecursiveTransform(toReference, fromTypeName, context, accessor, 0, false);

	}

}
