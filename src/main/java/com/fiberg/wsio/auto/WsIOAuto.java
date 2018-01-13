package com.fiberg.wsio.auto;

import com.fiberg.wsio.annotation.WsIOAnnotate;
import com.fiberg.wsio.processor.*;
import com.fiberg.wsio.util.WsIOUtil;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.vavr.*;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import io.vavr.control.Try;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.*;
import org.apache.commons.text.WordUtils;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import java.util.Objects;

/**
 * Class used to annotate web service classes with response and request wrappers.
 */
public final class WsIOAuto {

	/**
	 * Empty private constructor.
	 */
	private WsIOAuto() {  }

	/**
	 * Method used to scan the package for annotate annotations.
	 *
	 * @param basePackage package to scan
	 */
	public static void annotate(String basePackage, Class<?> main) {

		/* Obtain all class names of the base package */
		List<String> classNames = List.ofAll(new FastClasspathScanner(basePackage)
				.scan()
				.getNamesOfAllClasses());

		/* Get default class pool */
		ClassPool pool = ClassPool.getDefault();
		pool.appendClassPath(new LoaderClassPath(main.getClassLoader()));

		/* Get all ct classes when the name starts with base package */
		Map<String, CtClass> ctClasses = classNames.toMap(className -> className,
				className -> Try.success(className)
						.filter(name -> name.startsWith(basePackage))
						.mapTry(pool::getCtClass)
						.getOrNull())
				.filterValues(Objects::nonNull);

		/* Get current wrappers of the class */
		Map<String, Map<String, Tuple2<String, Map<WsIOType, Tuple2<String, String>>>>> wrappers =
				WsIOWalker.findWrapperRecursively(ctClasses, ctClasses.values().toList());

		/* Iterate for each wrapper class */
		for (String className : wrappers.keySet()) {

			/* Check ct class is defined */
			Option<CtClass> ctClassOpt = ctClasses.get(className);
			if (ctClassOpt.isDefined()) {

				/* Get main, root class and the wrappers */
				CtClass ctClass = ctClassOpt.get();
				CtClass root = WsIOAuto.extractRootCtClass(ctClass);
				Map<String, Tuple2<String, Map<WsIOType, Tuple2<String, String>>>> wrapper =
						wrappers.getOrElse(root.getName(), HashMap.empty());

				/* Iterate for each method */
				boolean annotationAdded = false;
				for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {

					/* Define class file and class pool */
					ClassFile ccFile = ctClass.getClassFile();
					ConstPool constpool = ccFile.getConstPool();

					/* Get method info */
					Option<Tuple2<String, Map<WsIOType, Tuple2<String, String>>>> info = wrapper
							.get(ctMethod.getLongName());

					/* Get package, response and request options */
					Option<String> packageOpt = info.map(Tuple2::_1).filter(Objects::nonNull);
					Option<Tuple2<String, String>> responseOpt = info.map(Tuple2::_2)
							.flatMap(map -> map.get(WsIOType.RESPONSE));
					Option<Tuple2<String, String>> requestOpt = info.map(Tuple2::_2)
							.flatMap(map -> map.get(WsIOType.REQUEST));

					/* Check if all fields are present */
					if (packageOpt.isDefined() && responseOpt.isDefined() && requestOpt.isDefined()) {

						/* Get method descriptor and annotate annotation */
						WsIODescriptor descriptor = WsIODescriptor.of(ctClasses, ctMethod);
						Option<WsIOAnnotate> annotate = descriptor.getSingle(WsIOAnnotate.class);

						/* Check if annotate is defined and rename is enabled */
						if (annotate.isDefined() && annotate.get().nameSwap()) {

							/* Get current method annotations atribute and execute when present */
							Option.of(ctMethod.getMethodInfo())
									.map(methodInfo -> methodInfo.getAttribute(AnnotationsAttribute.visibleTag))
									.filter(AnnotationsAttribute.class::isInstance)
									.map(AnnotationsAttribute.class::cast)
									.forEach(annotationsAttribute -> {

										/* Get annotation name and current web result annotation option */
										String annotationName = WebResult.class.getCanonicalName();
										Option<Annotation> annotationOpt = Option.of(annotationsAttribute
												.getAnnotation(annotationName));

										/* Check if the annotation is defined or not */
										if (annotationOpt.isDefined()) {

											/* Change the annotation when defined */
											annotationOpt.get().getMemberValue("name")
													.accept((StringVisitor) stringMember -> {

														/* Get name or default, add the underscore and set the value */
														String name = Option.of(stringMember.getValue())
																.getOrElse(WsIOConstant.DEFAULT_RESULT);
														stringMember.setValue(String.format("%s%s", name, WsIOConstant.SWAP_SEPARATOR));

													});

											/* Remove the annotation and add it again */
											annotationsAttribute.removeAnnotation(annotationName);
											annotationsAttribute.addAnnotation(annotationOpt.get());

										} else {

											/* Create the annotation, add the member name and add the annotation to attribute */
											Annotation annotation = new Annotation(annotationName, constpool);
											annotation.addMemberValue("name",
													new StringMemberValue(String.format("%s%s", WsIOConstant.DEFAULT_RESULT,
															WsIOConstant.SWAP_SEPARATOR), ccFile.getConstPool()));
											annotationsAttribute.addAnnotation(annotation);

										}

									});

							/* Get current method parameter annotations atribute and execute when present */
							Option.of(ctMethod.getMethodInfo())
									.map(methodInfo -> methodInfo.getAttribute(ParameterAnnotationsAttribute.visibleTag))
									.filter(ParameterAnnotationsAttribute.class::isInstance)
									.map(ParameterAnnotationsAttribute.class::cast)
									.forEach(parameterAnnotationsAttribute -> {

										/* Get the annotation name */
										String annotationName = WebParam.class.getCanonicalName();

										/* Get the paramters array and list of lists */
										Annotation[][] paramArrays = parameterAnnotationsAttribute.getAnnotations();
										List<List<Annotation>> parameters = List.of(paramArrays).map(List::of);

										/* Transform the parameters */
										List<List<Annotation>> transformed = parameters.zipWithIndex().map(tuple -> {

											/* Get all annotations and index of current parameter */
											List<Annotation> argParameters = tuple._1();
											int index = tuple._2();

											/* Check if the annotation already exists */
											boolean contains = argParameters.exists(parameter ->
													annotationName.equals(parameter.getTypeName()));
											if (contains) {

												/* Iterate for each annotation */
												argParameters.forEach(parameter -> {

													/* Check if the annotatio is the searched one */
													if (annotationName.equals(parameter.getTypeName())) {

														/* Visit the member name and change its value */
														parameter.getMemberValue("name").accept((StringVisitor) stringMember -> {

															/* Get name or default, add the underscore and set the value */
															String name = Option.of(stringMember.getValue())
																	.getOrElse(String.format("%s%d", WsIOConstant.DEFAULT_PARAMETER, index));
															stringMember.setValue(String.format("%s%s", name, WsIOConstant.SWAP_SEPARATOR));

														});

													}

												});

												/* Return the modified annotations of the parameter */
												return argParameters;

											} else {

												/* Create the annotation, add the member name and add the annotation to annotations list */
												Annotation annotation = new Annotation(annotationName, constpool);
												annotation.addMemberValue("name",
														new StringMemberValue(String.format("%s%d%s", WsIOConstant.DEFAULT_PARAMETER,
																index, WsIOConstant.SWAP_SEPARATOR), ccFile.getConstPool()));
												return argParameters.append(annotation);

											}

										});

										/* Transform the list of list into a array of arrays and set the annotations */
										parameterAnnotationsAttribute.setAnnotations(transformed
												.map(argParameters -> argParameters.toJavaArray(Annotation.class))
												.toJavaArray(Annotation[].class));

									});

						}

						/* Get package name, response and request info with prefixes and suffixes */
						String packageName = packageOpt.get();
						Tuple2<String, String> response = responseOpt.get();
						Tuple2<String, String> request = requestOpt.get();

						/* Get method name and upper name to create wrapper names */
						String methodName = ctMethod.getName();
						String upperName = WordUtils.capitalize(methodName);

						/* Response and request names with prefix and suffix */
						String responseName = WsIOUtil.addWrap(upperName, response._1(), response._2());
						String requestName = WsIOUtil.addWrap(upperName, request._1(), request._2());

						/* Get full qualified response and request class names */
						String responseClass = WsIOUtil.addPrefixName(responseName, packageName);
						String requestClass = WsIOUtil.addPrefixName(requestName, packageName);

						/* Check is the response and request names are valid */
						boolean validResponse = classNames.contains(responseClass);
						boolean validRequest = classNames.contains(requestClass);
						if (validResponse || validRequest) {

							try {
								/* Get annotations attribute */
								AnnotationsAttribute attribute = Option.of(ctMethod.getMethodInfo()
										.getAttribute(AnnotationsAttribute.visibleTag))
										.filter(AnnotationsAttribute.class::isInstance)
										.map(AnnotationsAttribute.class::cast)
										.getOrElse(new AnnotationsAttribute(constpool,
												AnnotationsAttribute.visibleTag));

								/*  Check if response name is valid to be added */
								if (validResponse) {

									/* Define wrapper class name, create annotation, add the wrapper name
									 * and add the annotation to the attribute */
									String responseAnnotation = ResponseWrapper.class.getCanonicalName();
									Annotation annotation = new Annotation(responseAnnotation, constpool);
									annotation.addMemberValue("className",
											new StringMemberValue(responseClass, ccFile.getConstPool()));
									attribute.addAnnotation(annotation);

								}

								/*  Check if request name is valid to be added */
								if (validRequest) {

									/* Define wrapper class name, create annotation, add the wrapper name
									 * and add the annotation to the attribute */
									String requestAnnotation = RequestWrapper.class.getCanonicalName();
									Annotation annotation = new Annotation(requestAnnotation, constpool);
									annotation.addMemberValue("className",
											new StringMemberValue(requestClass, ccFile.getConstPool()));
									attribute.addAnnotation(annotation);

								}

								/* Add attribute to the method and set flag to true */
								ctMethod.getMethodInfo().addAttribute(attribute);
								annotationAdded = true;

							} catch (Exception e) {
								// ignore class does not exists
							}

						}

					}

				}

				/* Check if the class has been annotated or not */
				if (annotationAdded) {

					try {

						/* Compile the class */
						ctClass.toClass();

					} catch (CannotCompileException e) {

						/* Throw when the class could not be compiles */
						throw new IllegalStateException(String.format("Could not compile the class %s", ctClass), e);

					}

				}

			}

		}

	}

	/**
	 * Method that extracts the root ct class.
	 *
	 * @param ctClass ct class to extract the root class
	 * @return root ct class
	 */
	private static CtClass extractRootCtClass(CtClass ctClass) {
		try {

			/* Check ct class is not null */
			if (Objects.nonNull(ctClass)) {

				/* Get the declaring class */
				CtClass declaring = ctClass.getDeclaringClass();
				if (Objects.nonNull(declaring)) {

					/* Return recursive call to extract declaring class */
					return extractRootCtClass(declaring);

				}

			}

		} catch (NotFoundException e) {
			// ignore return current class
		}

		/* Return current class by default */
		return ctClass;

	}

	/**
	 * Functional interface for string visitor only.
	 */
	@FunctionalInterface
	private interface StringVisitor extends MemberValueVisitor {

		/**
		 * {@inheritDoc}
		 */
		default void visitAnnotationMemberValue(AnnotationMemberValue node) {

		}

		/**
		 * {@inheritDoc}
		 */
		default void visitArrayMemberValue(ArrayMemberValue node) {

		}

		/**
		 * {@inheritDoc}
		 */
		default void visitBooleanMemberValue(BooleanMemberValue node) {

		}

		/**
		 * {@inheritDoc}
		 */
		default void visitByteMemberValue(ByteMemberValue node) {

		}

		/**
		 * {@inheritDoc}
		 */
		default void visitCharMemberValue(CharMemberValue node) {

		}

		/**
		 * {@inheritDoc}
		 */
		default void visitDoubleMemberValue(DoubleMemberValue node) {

		}

		/**
		 * {@inheritDoc}
		 */
		default void visitEnumMemberValue(EnumMemberValue node) {

		}

		/**
		 * {@inheritDoc}
		 */
		default void visitFloatMemberValue(FloatMemberValue node) {

		}

		/**
		 * {@inheritDoc}
		 */
		default void visitIntegerMemberValue(IntegerMemberValue node) {

		}

		/**
		 * {@inheritDoc}
		 */
		default void visitLongMemberValue(LongMemberValue node) {

		}

		/**
		 * {@inheritDoc}
		 */
		default void visitShortMemberValue(ShortMemberValue node) {

		}

		/**
		 * {@inheritDoc}
		 */
		default void visitClassMemberValue(ClassMemberValue node) {

		}

	}

}
