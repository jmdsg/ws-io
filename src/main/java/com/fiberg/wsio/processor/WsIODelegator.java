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
	 * Method that checks if two mirror types of a getter and setter are recursively compatible.
	 *
	 * @param returnMirror return mirror type
	 * @param parameterMirror parameter mirror type
	 * @return {@code true} if the two mirror types are recursively compatible {@code false} otherwise
	 */
	static boolean isRecursiveCompatible(TypeMirror returnMirror,
	                                     TypeMirror parameterMirror) {

		/* Check the instance types of the parameters */
		if (returnMirror instanceof ArrayType && parameterMirror instanceof ArrayType) {

			/* Get array types */
			ArrayType returnArray = (ArrayType) returnMirror;
			ArrayType parameterArray = (ArrayType) parameterMirror;

			/* Return the recursive result of component types of the array */
			return isRecursiveCompatible(returnArray.getComponentType(),
					parameterArray.getComponentType());

		} else if (returnMirror instanceof DeclaredType && parameterMirror instanceof DeclaredType) {

			/* Get declared types */
			DeclaredType returnDeclared = (DeclaredType) returnMirror;
			DeclaredType parameterDeclared = (DeclaredType) parameterMirror;

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

						/* Check recursible if they are compatible or not */
						if (!isRecursiveCompatible(returnGenericType, parameterGenericType)) {

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

		} else if (returnMirror instanceof PrimitiveType && parameterMirror instanceof PrimitiveType) {

			/* Check if primitive types are equals */
			return Objects.equals(returnMirror, parameterMirror);

		}

		/* Return false by default */
		return false;

	}

	/**
	 * Method that returns the first collection implementation and the rest just internals.
	 *
	 * @param mirrorType mirror type
	 * @param useInternalTypes indicates if internal types are going to be used or not
	 * @param context context of the process
	 * @return first collection implementation and the rest just internals
	 */
	private static TypeName ensureFirstConcreteClass(TypeMirror mirrorType,
	                                                 Boolean useInternalTypes,
	                                                 WsIOContext context) {

		/* Check the type of mirror */
		if (mirrorType instanceof DeclaredType) {

			/* Get declared name, type element and name*/
			DeclaredType declaredType = (DeclaredType) mirrorType;
			TypeElement typeElement = (TypeElement) declaredType.asElement();
			String elementName = typeElement.getQualifiedName().toString();

			/* Check that the first element is a interface of map or collection */
			if (WsIOCollection.MAP_INTERFACES.contains(elementName)
					|| WsIOCollection.COLLECTION_INTERFACES.contains(elementName)) {

				/* Get the recursive type name with implementations on collection and map classes */
				TypeName implementation = context.getRecursiveFullTypeName(mirrorType,
						useInternalTypes, true, false);

				/* Check if type name is parameterized */
				if (implementation instanceof ParameterizedTypeName) {

					/* Get parameterized typa name */
					ParameterizedTypeName parameterized = (ParameterizedTypeName) implementation;

					/* Get the recursive type name without implementations only internals */
					ParameterizedTypeName internal = (ParameterizedTypeName) context.getRecursiveFullTypeName(
							mirrorType, useInternalTypes, false, false);

					/* Return first implementation */
					return ParameterizedTypeName.get(parameterized.rawType,
							internal.typeArguments.toArray(new TypeName[internal.typeArguments.size()]));

				} else {

					/* Return implementation when type name is not parameterized */
					return implementation;

				}

			}

		}

		/* Return recursive name when type is not mirror */
		return context.getRecursiveFullTypeName(mirrorType,
				useInternalTypes, false, false);

	}

	/**
	 * Method that generates the method specs delegates of a property.
	 *
	 * @param returnMirror return mirror type
	 * @param parameterMirror parameter mirror type
	 * @param getter getter method
	 * @param setter setter method
	 * @param getterName name of the getter method
	 * @param setterName name of the setter method
	 * @param propertyName name of the property method
	 * @param delegator name of the delegator
	 * @param getterAnnotations annotations of the getter
	 * @param setterAnnotations annotations of the setter
	 * @param level level of the method
	 * @param hideEmpties indicates if the collections and maps should be checked and ignored
	 * @param context context of the process
	 * @param messager messager to print the output
	 * @return the method specs delegates of a property
	 */
	static List<MethodSpec> generatePropertyDelegates(TypeMirror returnMirror,
	                                                  TypeMirror parameterMirror,
	                                                  ExecutableElement getter,
	                                                  ExecutableElement setter,
	                                                  String getterName,
	                                                  String setterName,
	                                                  String propertyName,
	                                                  String delegator,
	                                                  List<AnnotationSpec> getterAnnotations,
	                                                  List<AnnotationSpec> setterAnnotations,
	                                                  WsIOLevel level,
	                                                  Boolean hideEmpties,
	                                                  WsIOContext context,
	                                                  Messager messager) {

		/* Check that the types are compatible */
		if (isRecursiveCompatible(returnMirror, parameterMirror)) {

			/* Get full names when the class is in message or clones classes */
			TypeName returnTypeName = context.getRecursiveFullTypeName(returnMirror,
					true, false, true);
			TypeName parameterTypeName = context.getRecursiveFullTypeName(parameterMirror,
					true, false, true);

			/* Create getter builder */
			MethodSpec.Builder getterSpecBuilder = MethodSpec.methodBuilder(getterName)
					.addModifiers(Modifier.PUBLIC)
					.addAnnotations(getterAnnotations)
					.returns(returnTypeName);

			/* Create setter builder */
			MethodSpec.Builder setterSpecBuilder = MethodSpec.methodBuilder(setterName)
					.addModifiers(Modifier.PUBLIC)
					.addAnnotations(setterAnnotations)
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
				CodeBlock setCodeBlock = generateRecursiveTransformToExternal(parameterTypeName, parameterMirror,
						context, propertyName, hideEmpties);
				CodeBlock getCodeBlock = generateRecursiveTransformToInternal(returnMirror, returnTypeName,
						context, String.format("%s().%s()", delegator, getterName), hideEmpties);

				/* Check codes are non null */
				if (Objects.nonNull(setCodeBlock) && Objects.nonNull(getCodeBlock)) {

					/* Create getter method and add it to the set */
					MethodSpec getterSpec = getterSpecBuilder
							.addCode("return $L() != null ? ", delegator)
							.addCode(getCodeBlock)
							.addCode(" : null")
							.addCode(";").addCode("\n")
							.build();

					/* Create setter method and add it to the set */
					MethodSpec setterSpec = setterSpecBuilder
							.beginControlFlow("if ($L() != null && $L != null)",
									delegator, propertyName)
							.addCode("$L().$L(", delegator, setterName)
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
	 * @param external external mirror type
	 * @param internal internal type name
	 * @param context context of the process
	 * @param accessor access variable or function name
	 * @param level current recursive level
	 * @param toInternal indicates if the destiny is a internal or not
	 * @param hideEmpties indicates if the collections and maps should be checked and ignored
	 * @return recursive internal or external code block to transform.
	 */
	private static CodeBlock generateRecursiveTransform(TypeMirror external,
	                                                    TypeName internal,
	                                                    WsIOContext context,
	                                                    String accessor,
	                                                    Integer level,
	                                                    Boolean toInternal,
	                                                    Boolean hideEmpties) {

		/* Check the instance of both types */
		if (external instanceof ArrayType && internal instanceof ArrayTypeName) {

			/* Get component of each type when is an array */
			TypeMirror externalComponentType = ((ArrayType) external).getComponentType();
			TypeName internalComponentTypeName = ((ArrayTypeName) internal).componentType;

			/* Create next accesor name and get recursive code block */
			String accessorName = "component";
			String componentAccessor = String.format("%s_%d_", accessorName, level);
			CodeBlock componentBlock = generateRecursiveTransform(externalComponentType, internalComponentTypeName,
					context, componentAccessor, level + 1, toInternal, hideEmpties);

			/* Check code block is not null */
			if (Objects.nonNull(componentBlock)) {

				/* Arrays class */
				Class<?> arraysClass = java.util.Arrays.class;

				/* Get array info */
				Tuple2<TypeMirror, Integer> arrayInfo = WsIOUtils.getArrayInfo(external);

				/* Get mirror type and array count */
				TypeMirror mirror = arrayInfo._1();
				Integer dimension = arrayInfo._2();

				/* Check array count is greater than zero */
				if (dimension > 0) {

					/* Get possible element name */
					String possibleElementName = Option.of(mirror)
							.filter(DeclaredType.class::isInstance)
							.map(DeclaredType.class::cast)
							.map(DeclaredType::asElement)
							.filter(TypeElement.class::isInstance)
							.map(TypeElement.class::cast)
							.map(TypeElement::getQualifiedName)
							.map(Name::toString)
							.getOrNull();

					/* Check that the array component is internal or generic internal class  */
					if (context.isRecursiveGenericInternalType(mirror)
							|| (Objects.nonNull(possibleElementName)
							&& context.isInternalType(possibleElementName))) {

							/* Get class to cast of dimension - 1 */
							TypeName typeName = context.getRecursiveFullTypeName(mirror,
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

							/* Code block builder */
							CodeBlock.Builder builder = CodeBlock.builder();

							/* Check if hide empties if enabled */
							if (hideEmpties) {

								/* Add array check condition */
								builder = builder.add("(($L != null && $L.length > 0) ? $T.stream($L)",
										accessor, accessor, arraysClass, accessor).add("\n");

							} else {

								/* Add normal condition */
								builder = builder.add("($L != null ? $T.stream($L)",
										accessor, arraysClass, accessor).add("\n");

							}

							/* Build and return the code block for array types */
							return builder.add(".map($L -> ", componentAccessor)
									.add(componentBlock)
									.add(")").add("\n")
									.add(".<$T>toArray($T[]::new) : null)", castTypeName, destinyTypeName)
									.build();


					} else {

						/* Check if element is a collection or map and hide empties is enabled */
						if (WsIOCollection.ALL.contains(possibleElementName) && hideEmpties) {

							/* Return the code block external type */
							return CodeBlock.of("(($L != null && $L.length > 0) ? $L : null)",
									accessor, accessor, accessor);

						} else {

							/* Return the code block external type */
							return CodeBlock.of("$L", accessor);

						}

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

						List<TypeMirror> externalGenerics = List.ofAll(WsIOUtils.getClearedGenericTypes(externalDeclared));

						/* Check if is a collection or a map */
						if (WsIOCollection.COLLECTIONS.contains(externalElementName)
								&& internalGenerics.size() == 1 && externalGenerics.size() == 1) {

							/* Type name and mirror of the element */
							TypeName internalElement = internalGenerics.get(0);
							TypeMirror externalElement = externalGenerics.get(0);

							/* Create next accesor name and get recursive code block */
							String accessorName = "element";
							String elementAccessor = String.format("%s_%d_", accessorName, level);
							CodeBlock elementBlock = generateRecursiveTransform(externalElement, internalElement,
									context, elementAccessor, level + 1, toInternal, hideEmpties);

							/* Check code block is not null */
							if (Objects.nonNull(elementBlock)) {

								/* Get the implementation class name */
								String implementationName = WsIOCollection.IMPLEMENTATIONS
										.getOrDefault(externalElementName, externalElementName);
								ClassName implementationClass = ClassName.bestGuess(implementationName);

								/* Cast class type name */
								TypeName castClass = ensureFirstConcreteClass(externalDeclared,
										toInternal, context);

								/* Code block builder */
								CodeBlock.Builder builder = CodeBlock.builder();

								/* Check if hide empties if enabled */
								if (hideEmpties) {

									/* Add array check condition */
									builder = builder.add("(($L != null && $L.size() > 0) ? $L.stream()",
											accessor, accessor, accessor).add("\n");

								} else {

									/* Add normal condition */
									builder = builder.add("($L != null ? $L.stream()",
											accessor, accessor).add("\n");

								}

								/* Build and return the code block for collection types */
								return builder.add(".map($L -> ", elementAccessor)
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
							TypeMirror externalKey = externalGenerics.get(0);
							TypeMirror externalValue = externalGenerics.get(1);

							/* Accessor name and general accessor */
							String accessorName = "entry";
							String generalAccessor = String.format("%s_%d_", accessorName, level);

							/* Create next accesor key name and get recursive code block */
							String keyAccessor = generalAccessor + ".getKey()";
							CodeBlock keyBlock = generateRecursiveTransform(externalKey, internalKey,
									context, keyAccessor, level + 1, toInternal, hideEmpties);

							/* Create next accesor value name and get recursive code block */
							String valueAccessor = generalAccessor + ".getValue()";
							CodeBlock valueBlock = generateRecursiveTransform(externalValue, internalValue,
									context, valueAccessor, level + 1, toInternal, hideEmpties);

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

								/* Code block builder */
								CodeBlock.Builder builder = CodeBlock.builder();

								/* Check if hide empties if enabled */
								if (hideEmpties) {

									/* Add array check condition */
									builder = builder.add("(($L != null && $L.size() > 0) ? $L.entrySet().stream()",
											accessor, accessor, accessor).add("\n");

								} else {

									/* Add normal condition */
									builder = builder.add("($L != null ? $L.entrySet().stream()",
											accessor, accessor).add("\n");

								}

								/* Build and return the code block for map types */
								return builder.add(".map($L -> new $T<>(", generalAccessor, entryClass)
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

					/* Check if element is a collection or map and hide empties is enabled */
					if (WsIOCollection.ALL.contains(externalElementName) && hideEmpties) {

						/* Return the code block external type */
						return CodeBlock.of("(($L != null && $L.size() > 0) ? $L : null)",
								accessor, accessor, accessor);

					} else {

						/* Return the code block external type */
						return CodeBlock.of("$L", accessor);

					}

				}

			}

		} else if (external instanceof PrimitiveType) {

			/* Return the code block primitive type */
			return CodeBlock.of("$L", accessor);

		}

		/* Return null by default */
		return null;

	}

	/**
	 * Method that generates the recursive code block to transform to internal.
	 *
	 * @param fromMirror origin mirror type
	 * @param toTypeName destiny type name
	 * @param context context of the process
	 * @param accessor access variable or function name
	 * @param hideEmpties indicates if the collections and maps should be checked and ignored
	 * @return recursive internal code block to transform from external.
	 */
	static CodeBlock generateRecursiveTransformToInternal(TypeMirror fromMirror,
	                                                      TypeName toTypeName,
	                                                      WsIOContext context,
	                                                      String accessor,
	                                                      Boolean hideEmpties) {

		/* Return the transform to internal */
		return generateRecursiveTransform(fromMirror, toTypeName, context,
				accessor, 0, true, hideEmpties);

	}

	/**
	 * Method that generates the recursive code block to transform to external.
	 *
	 * @param fromTypeName origin type name
	 * @param toMirror destiny mirror type
	 * @param context context of the process
	 * @param accessor access variable or function name
	 * @param hideEmpties indicates if the collections and maps should be checked and ignored
	 * @return recursive external code block to transform from internal.
	 */
	static CodeBlock generateRecursiveTransformToExternal(TypeName fromTypeName,
	                                                      TypeMirror toMirror,
	                                                      WsIOContext context,
	                                                      String accessor,
	                                                      Boolean hideEmpties) {

		/* Return the transform to external */
		return generateRecursiveTransform(toMirror, fromTypeName, context,
				accessor, 0, false, hideEmpties);

	}

}
