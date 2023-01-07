package com.fiberg.wsio.auto;

import com.fiberg.wsio.annotation.WsIOAnnotate;
import com.fiberg.wsio.processor.*;
import com.fiberg.wsio.util.WsIOUtil;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import io.vavr.*;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import io.vavr.control.Try;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.xml.ws.RequestWrapper;
import jakarta.xml.ws.ResponseWrapper;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.asm.MemberAttributeExtension;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationSource;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.AnnotationVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import org.apache.commons.text.WordUtils;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.function.Function;

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

		ScanResult scanResult = new ClassGraph()
				.verbose()
				.enableAllInfo()
				.acceptPackages(basePackage)
				.scan();

		/* Obtain all class names of the base package */
		ClassLoader classLoader = main.getClassLoader();
		List<String> classNames = List.ofAll(scanResult.getAllClasses()
				.stream()
				.map(ClassInfo::getName));

		/* Get all types when the name starts with base package */
		TypePool pool = TypePool.Default.of(classLoader);
		Map<String, TypeDescription> types = classNames.toMap(className -> className,
				className -> Try.success(className)
						.filter(name -> name.startsWith(basePackage))
						.mapTry(pool::describe)
						.mapTry(TypePool.Resolution::resolve)
						.getOrNull())
				.filterValues(Objects::nonNull);

		/* Get current wrappers of the class */
		Map<String, Map<String, Tuple2<String, Map<WsIOType, Tuple2<String, String>>>>> wrappers =
				WsIOWalker.findWrapperRecursively(types.values().toList());

		/* Iterate for each wrapper class */
		List<DynamicType.Unloaded<Object>> uploads = wrappers.keySet()
				.toList()
				.flatMap(className -> {

					/* Check ct class is defined */
					Option<TypeDescription> typeOpt = types.get(className);
					if (!typeOpt.isDefined()) return Stream.of();
					else {

						/* Get main, root class and the wrappers */
						TypeDescription type = typeOpt.get();
						String typeName = type.getName();
						Map<String, Tuple2<String, Map<WsIOType, Tuple2<String, String>>>> wrapper =
								wrappers.getOrElse(typeName, HashMap.empty());

						/* Iterate for each method */
						List<Tuple2<MethodDescription, Map<Class<? extends Annotation>, List<AnnotationDescription>>>>
						annotated = List.ofAll(type.getDeclaredMethods())
								.flatMap(method -> {

									/* Get method info */
									Option<Tuple2<String, Map<WsIOType, Tuple2<String, String>>>> info = wrapper
											.get(method.toString());

									/* Get package, response and request options */
									Option<String> packageOpt = info.map(Tuple2::_1).filter(Objects::nonNull);
									Option<Tuple2<String, String>> responseOpt = info.map(Tuple2::_2)
											.flatMap(map -> map.get(WsIOType.RESPONSE));
									Option<Tuple2<String, String>> requestOpt = info.map(Tuple2::_2)
											.flatMap(map -> map.get(WsIOType.REQUEST));

									/* Check if all fields are present */
									if (packageOpt.isDefined() && responseOpt.isDefined() && requestOpt.isDefined()) {

										/* Get method descriptor and annotate annotation */
										WsIODescriptor descriptor = WsIODescriptor.of(method);
										Map<Class<? extends Annotation>, List<AnnotationDescription>>
										webResultAnnotation = getWebAnnotation(
												method, descriptor
										);

										/* Get package name, response and request info with prefixes and suffixes */
										String packageName = packageOpt.get();
										Tuple2<String, String> response = responseOpt.get();
										Tuple2<String, String> request = requestOpt.get();
										Map<Class<? extends Annotation>, List<AnnotationDescription>>
										wrapperAnnotation = getWrapperAnnotation(
												method, packageName, response, request, classNames
										);

										Map<Class<? extends Annotation>, List<AnnotationDescription>> descriptions =
												Stream.of(webResultAnnotation, wrapperAnnotation)
														.flatMap(Function.identity())
														.toMap(Function.identity());

										return List.of(
												Tuple.of(method, descriptions)
										);

									}

									return List.of();

								});

						List<Tuple2<MethodDescription, Map<Class<? extends Annotation>, List<AnnotationDescription>>>>
						filtered = annotated.filter(tuple -> !tuple._2().isEmpty());

						if (filtered.isEmpty()) return List.of();
						else {

							List<ElementMatcher.Junction<MethodDescription>> junctions = filtered.map(Tuple2::_1)
									.map(descriptor -> {

										String name = descriptor.getName();
										TypeDescription.Generic returnType = descriptor.getReturnType();
										List<ParameterDescription> parameterDescriptions = List.ofAll(
												descriptor.getParameters()
														.asDefined()
										);

										List<TypeDescription.Generic> argumentTypes = parameterDescriptions
												.map(ParameterDescription::getType);

										return ElementMatchers.named(name)
												.and(ElementMatchers.returnsGeneric(returnType))
												.and(ElementMatchers.takesGenericArguments(argumentTypes.toJavaList()));

									});

							DynamicType.Builder<Object> builder = new ByteBuddy()
									.redefine(type, ClassFileLocator.ForClassLoader.of(classLoader));

							DynamicType.Builder<Object> addAnnotationVisitorBuilder = Stream.range(0, filtered.size())
									.foldLeft(builder, (next, index) -> {

										MemberAttributeExtension.ForMethod forMethod = new MemberAttributeExtension.ForMethod();
										Map<Class<? extends Annotation>, List<AnnotationDescription>>
										annotations = filtered.get(index)._2();

										MemberAttributeExtension.ForMethod methodVisitForMethod = forMethod
												.annotateMethod(annotations
														.filterKeys(key -> !WebParam.class.equals(key))
														.values()
														.flatMap(Function.identity())
														.toJavaList());

										Map<Class<? extends Annotation>, List<AnnotationDescription>>
										paramAnnotations = annotations.filterKeys(WebParam.class::equals)
												.filterValues(values -> !values.isEmpty());

										MemberAttributeExtension.ForMethod paramVisitForMethod = paramAnnotations.keySet()
												.foldLeft(methodVisitForMethod, (visitor, annotation) -> {

													List<AnnotationDescription> params = paramAnnotations.get(annotation)
															.getOrElse(List::of);

													return params.zipWithIndex()
															.foldLeft(visitor, (visit, tuple) -> {

																int param = tuple._2();
																AnnotationDescription description = tuple._1();

																return visit.annotateParameter(param, description);

															});

												});

										return next.visit(paramVisitForMethod.on(junctions.get(index)));

									});

							// this must be last since the visits are performed in inverse order
							DynamicType.Builder<Object> removeAnnotationVisitorBuilder = addAnnotationVisitorBuilder
									.visit(new AsmVisitorWrapper.ForDeclaredMethods()
									.method(ElementMatchers.any(), new AsmVisitorWrapper.ForDeclaredMethods.MethodVisitorWrapper() {
										@Override
										public MethodVisitor wrap(TypeDescription instrumentedType,
																  MethodDescription instrumentedMethod,
																  MethodVisitor methodVisitor,
																  Implementation.Context implementationContext,
																  TypePool typePool,
																  int writerFlags,
																  int readerFlags) {

											int index = junctions.indexWhere(junction -> junction.matches(instrumentedMethod));
											Map<Class<? extends Annotation>, List<AnnotationDescription>> map =
													filtered.get(index)._2();

											// erase the current annotations only when there are new ones defined
											Set<String> methodNames = map.filterValues(values -> !values.isEmpty())
													.keySet()
													.filter(key -> !WebParam.class.equals(key))
													.map(Type::getDescriptor);

											// erase the current annotations only when there are new ones defined
											Set<String> paramNames = map.filterValues(values -> !values.isEmpty())
													.keySet()
													.filter(WebParam.class::equals)
													.map(Type::getDescriptor);

											return new MethodVisitor(Opcodes.ASM5, methodVisitor) {
												@Override
												public AnnotationVisitor visitParameterAnnotation(
														int parameter,
														String desc,
														boolean visible) {
													if (paramNames.contains(desc)) {
														return null;
													}
													return super.visitParameterAnnotation(parameter, desc, visible);
												}
												@Override
												public AnnotationVisitor visitAnnotation(String desc,
																						 boolean visible) {
													if (methodNames.contains(desc)) {
														return null;
													}
													return super.visitAnnotation(desc, visible);
												}
											};

										}
									}));

							DynamicType.Unloaded<Object> unloaded = removeAnnotationVisitorBuilder.make();

							return List.of(unloaded);

						}

					}

				});

		if (uploads.size() >= 1) {

			DynamicType.Unloaded<Object> first = uploads.get(0);
			List<DynamicType.Unloaded<Object>> tail = uploads.tail();

			// load the classes and the includes
			first.include(tail.toJavaList())
					.load(classLoader, ClassLoadingStrategy.Default.INJECTION);

		}

	}

	private static Map<Class<? extends Annotation>, List<AnnotationDescription>>
	getWrapperAnnotation(MethodDescription method,
						 String packageName,
						 Tuple2<String, String> response,
						 Tuple2<String, String> request,
						 List<String> classNames) {

		/* Get method name and upper name to create wrapper names */
		String methodName = method.getName();
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

		if (!validResponse && !validRequest) return HashMap.empty();
		else {

			Class<? extends java.lang.annotation.Annotation> requestAnnotationClass = RequestWrapper.class;
			Class<? extends java.lang.annotation.Annotation> responseAnnotationClass = ResponseWrapper.class;

			Option<Tuple2<Class<? extends Annotation>, String>> requestAnnotationOpt = !validRequest
					? Option.none()
					: Option.of(Tuple.of(requestAnnotationClass, requestClass));

			Option<Tuple2<Class<? extends Annotation>, String>> responseAnnotationOpt = !validResponse
					? Option.none()
					: Option.of(Tuple.of(responseAnnotationClass, responseClass));

			Map<Class<? extends Annotation>, String> validAnnotations = HashMap.ofEntries(
					Stream.concat(requestAnnotationOpt, responseAnnotationOpt)
			);

			return validAnnotations
					.map((annotationClass, className) -> Tuple.of(annotationClass, List.of(
							AnnotationDescription.Builder.ofType(annotationClass)
									.define("className", className)
									.build()
					)));

		}

	}

	private static Map<Class<? extends Annotation>, List<AnnotationDescription>>
	getWebAnnotation(MethodDescription method,
					 WsIODescriptor descriptor) {

		/* Check if annotate is defined and rename is enabled */
		Option<WsIOAnnotate> annotate = descriptor.getSingle(WsIOAnnotate.class);
		if (annotate.isDefined() && annotate.get().nameSwap()) {

			/* Get annotation name and current web result annotation option */
			Class<WebParam> paramAnnotationClass = WebParam.class;
			Class<WebResult> resultAnnotationClass = WebResult.class;
			AnnotationDescription.Loadable<WebResult> resultLoadable = method.getDeclaredAnnotations()
					.ofType(resultAnnotationClass);

			Option<WebResult> resultValueOpt = Option.of(resultLoadable)
					.flatMap(loadable -> Try.of(loadable::load)
							.toOption());

			String resultName = resultValueOpt.map(WebResult::name)
					.getOrElse(WsIOConstant.DEFAULT_RESULT);

			String resultContent = String.format("%s%s", resultName, WsIOConstant.SWAP_SEPARATOR);
			Option<AnnotationDescription> resultAnnotationOpt = Option.of(resultAnnotationClass)
					.map(AnnotationDescription.Builder::ofType)
					.map(builder -> builder.define("name", resultContent))
					.map(builder -> resultValueOpt.isDefined()
							? builder.define("header", resultValueOpt.get().header())
							: builder)
					.map(builder -> resultValueOpt.isDefined()
							? builder.define("targetNamespace", resultValueOpt.get().targetNamespace())
							: builder)
					.map(builder -> resultValueOpt.isDefined()
							? builder.define("partName", resultValueOpt.get().partName())
							: builder)
					.map(AnnotationDescription.Builder::build);

			List<ParameterDescription.InDefinedShape> parameters = List.ofAll(
					method.getParameters().asDefined()
			);

			List<AnnotationDescription> resultAnnotations = resultAnnotationOpt.toList();
			List<AnnotationDescription> paramAnnotations = parameters
					.map(parameter -> Option.of(parameter)
							.map(AnnotationSource::getDeclaredAnnotations)
							.map(source -> source.ofType(paramAnnotationClass))
							.filter(Objects::nonNull)
							.map(AnnotationDescription.Loadable::load))
					.zipWithIndex()
					.map(tuple -> {

						int index = tuple._2();
						Option<WebParam> paramValueOpt = tuple._1();
						String paramName = paramValueOpt.map(WebParam::name)
								.getOrElse(() -> String.format("%s%d", WsIOConstant.DEFAULT_PARAMETER, index));

						String paramContent = String.format("%s%s", paramName, WsIOConstant.SWAP_SEPARATOR);
						Option<AnnotationDescription> paramAnnotation = Option.of(paramAnnotationClass)
								.map(AnnotationDescription.Builder::ofType)
								.map(builder -> builder.define("name", paramContent))
								.map(builder -> paramValueOpt.isDefined()
										? builder.define("header", paramValueOpt.get().header())
										: builder)
								.map(builder -> paramValueOpt.isDefined()
										? builder.define("targetNamespace", paramValueOpt.get().targetNamespace())
										: builder)
								.map(builder -> paramValueOpt.isDefined()
										? builder.define("partName", paramValueOpt.get().partName())
										: builder)
								.map(builder -> paramValueOpt.isDefined()
										? builder.define("mode", paramValueOpt.get().mode())
										: builder)
								.map(AnnotationDescription.Builder::build);

						return paramAnnotation.get();

					});

			return HashMap.of(
					resultAnnotationClass, resultAnnotations,
					paramAnnotationClass, paramAnnotations
			);

		}

		return HashMap.empty();

	}

}
