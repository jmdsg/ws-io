package com.fiberg.wsio.processor;

import com.fiberg.wsio.annotation.*;
import com.fiberg.wsio.util.WsIOUtil;
import io.vavr.*;
import io.vavr.collection.*;
import io.vavr.control.Option;
import io.vavr.control.Try;
import javassist.CtClass;
import javassist.CtMethod;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Class that gets the value of an annotation traveling all its declaring classes.
 */
public class WsIODescriptor {

	/** Descriptor of the single annotations */
	private static final Map<Class<? extends Annotation>,
				Tuple2<Class<? extends Annotation>, Function1<Annotation, SkipType>>> SINGLES = HashMap.of(
			WsIOUseTime.class, WsIODescriptor.skipSingleDescriptor(WsIOIgnoreUseTime.class, WsIOIgnoreUseTime::skip),
			WsIOUseState.class, WsIODescriptor.skipSingleDescriptor(WsIOIgnoreUseState.class, WsIOIgnoreUseState::skip),
			WsIOUseInner.class, WsIODescriptor.skipSingleDescriptor(WsIOIgnoreUseInner.class, WsIOIgnoreUseInner::skip),
			WsIOUseHideEmpty.class, WsIODescriptor.skipSingleDescriptor(WsIOIgnoreUseHideEmpty.class, WsIOIgnoreUseHideEmpty::skip),
			WsIOMessage.class, WsIODescriptor.skipSingleDescriptor(WsIOSkipMessage.class, WsIOSkipMessage::skip),
			WsIOMessageWrapper.class, WsIODescriptor.skipSingleDescriptor(WsIOSkipMessageWrapper.class, WsIOSkipMessageWrapper::skip),
			WsIOAnnotate.class, WsIODescriptor.skipSingleDescriptor(WsIOSkipAnnotate.class, WsIOSkipAnnotate::skip)
	);

	/** Descriptor of the multiple annotations */
	private static final Map<Class<? extends Annotation>,
			Tuple6<Class<? extends Annotation>, Class<? extends Annotation>, Function1<Annotation, Annotation[]>,
					Function1<Annotation, SkipType>, Function1<Annotation, Comparable<?>>, Function1<Annotation, Comparable<?>>>>
			MULTIPLES = HashMap.of(
					WsIOClone.class, WsIODescriptor.skipMultipleDescriptor(WsIOClone.class,
					WsIOClones.class, WsIOSkipClone.class, WsIOClones::value, WsIOSkipClone::skip,
					annotation -> Tuple.of(annotation.prefix(), annotation.suffix()),
					annotation -> Tuple.of(annotation.prefix(), annotation.suffix()))
			);

	/**
	 * Method that create a descriptor for the skip annotations
	 *
	 * @param skip skip class extending annotation
	 * @param function function to transform
	 * @param <S> type argument extending annotation
	 * @return tuple containing the skip descriptor of the annotation
	 */
	@SuppressWarnings("unchecked")
	private static <S extends Annotation> Tuple2<Class<? extends Annotation>, Function1<Annotation, SkipType>>
	skipSingleDescriptor(Class<S> skip, Function1<S, SkipType> function) {

		/* Return the skip class, and the skip method */
		return Tuple.of(skip, annotation -> {

			/* Check skip class and annotation are not null, and that skip is instance of annotation */
			if (skip != null && annotation != null && skip.isInstance(annotation)) {

				/* Return the function applied */
				return function.apply((S) annotation);

			}

			/* Return null by default */
			return null;

		});

	}

	/**
	 * Method that create a descriptor for the skip annotations
	 *
	 * @param main main class extending annotation
	 * @param multiple multiple class extending annotation
	 * @param skip skip class extending annotation
	 * @param multipleFunction multiple function to transform
	 * @param skipFunction skip function to transform
	 * @param mainComparator main annotation comparator
	 * @param skipComparator skip annotation comparator
	 * @param <A> type argument extending the annotation
	 * @param <M> type argument extending the multiple annotation
	 * @param <S> type argument extending the skip annotation
	 * @return tuple containing the skip descriptor of the annotation
	 */
	@SuppressWarnings("unchecked")
	private static <A extends Annotation, M extends Annotation, S extends Annotation> Tuple6<Class<? extends Annotation>,
			Class<? extends Annotation>, Function1<Annotation, Annotation[]>, Function1<Annotation, SkipType>,
			Function1<Annotation, Comparable<?>>, Function1<Annotation, Comparable<?>>> skipMultipleDescriptor(
					Class<A> main,
					Class<M> multiple,
					Class<S> skip,
					Function1<M, A[]> multipleFunction,
					Function1<S, SkipType> skipFunction,
					Function1<A, Comparable<?>> mainComparator,
					Function1<S, Comparable<?>> skipComparator) {

		/* Return the annotation class, the skip function and the comparator function */
		return Tuple.of(multiple, skip,
				annotation -> {

					/* Check multiple class and annotation are not null, and that skip is instance of annotation */
					if (multiple != null && annotation != null && multiple.isInstance(annotation)) {

						/* Return the function applied */
						return multipleFunction.apply((M) annotation);

					}

					/* Return null by default */
					return new Annotation[0];

				}, annotation -> {

					/* Check skip class and annotation are not null, and that skip is instance of annotation */
					if (skip != null && annotation != null && skip.isInstance(annotation)) {

						/* Return the function applied */
						return skipFunction.apply((S) annotation);

					}

					/* Return null by default */
					return null;

				}, annotation -> {

					/* Check main class and annotation are not null, and that main is instance of annotation */
					if (main != null && annotation != null && main.isInstance(annotation)) {

						/* Return the comparator applied */
						return mainComparator.apply((A) annotation);

					}

					/* Return null by default */
					return null;

				}, annotation -> {

					/* Check skip class and annotation are not null, and that main is instance of annotation */
					if (skip != null && annotation != null && skip.isInstance(annotation)) {

						/* Return the comparator applied */
						return skipComparator.apply((S) annotation);

					}

					/* Return null by default */
					return null;

				});

	}

	/**
	 * Method that returns the annotation wrapper in a option.
	 *
	 * @param annotations map of annotations
	 * @param clazz class of the annoation
	 * @param <T> type argument of the annotation
	 * @return option containing the possible annotation
	 */
	private static <T extends Annotation> Option<T> getSingle(Map<Class<? extends Annotation>, ? extends Annotation> annotations,
	                                                          Class<T> clazz) {

		/* Check class is not null */
		if (Objects.nonNull(clazz)) {

			/* Return the annotation only if is instance of the specified class */
			return annotations.get(clazz)
					.filter(clazz::isInstance)
					.map(clazz::cast);

		} else {

			/* Return empty option */
			return Option.none();

		}

	}

	/**
	 * Method that returns the annotation wrapper in a option.
	 *
	 * @param annotations map of annotations
	 * @param clazz class of the annoation
	 * @param <T> type argument of the annotation
	 * @return option containing the possible annotation
	 */
	private static <T extends Annotation> Map<Comparable<?>, T> getMultiple(
			Map<Class<? extends Annotation>, Map<Comparable<?>, ? extends Annotation>> annotations,
			Class<T> clazz) {

		/* Check class is not null */
		if (Objects.nonNull(clazz)) {

			/* Return the annotation only if is instance of the specified class */
			return annotations.get(clazz).getOrElse(HashMap.empty())
					.filterValues(clazz::isInstance)
					.mapValues(clazz::cast);

		} else {

			/* Return empty map */
			return HashMap.empty();

		}

	}

	/**
	 * Method that extracts the info for annotated element.
	 *
	 * @param annotated annotated method or class
	 * @return list of tuples with annotation and skip type of all recursive annotated elements
	 */
	private static Map<Class<? extends Annotation>, Map<Comparable<?>, List<Tuple2<Annotation, SkipType>>>>
	extractAnnotationsInfos(Annotated annotated) {

		/* Return annotations info */
		return extractAnnotationsInfos(annotated, WsIODescriptor::extractElementHierarchy,
				WsIODescriptor::extractElementAnnotationsInfo);

	}

	/**
	 * Method that extracts the info for all elements.
	 *
	 * @param element type, package or executable element
	 * @return list of tuples with annotation and skip type of all recursive methods
	 */
	private static Map<Class<? extends Annotation>, Map<Comparable<?>, List<Tuple2<Annotation, SkipType>>>>
	extractAnnotationsInfos(Element element) {

		/* Return annotations info */
		return extractAnnotationsInfos(element, WsIODescriptor::extractElementHierarchy,
				WsIODescriptor::extractElementAnnotationsInfo);

	}

	/**
	 * Method that extracts the info for all annotated elements.
	 *
	 * @param annotated type, package or executable annotated element
	 * @return list of tuples with annotation and skip type of all recursive methods
	 */
	private static Map<Class<? extends Annotation>, Map<Comparable<?>, List<Tuple2<Annotation, SkipType>>>>
	extractAnnotationsInfos(AnnotatedElement annotated) {

		/* Return annotations info */
		return extractAnnotationsInfos(annotated, WsIODescriptor::extractElementHierarchy,
				WsIODescriptor::extractElementAnnotationsInfo);

	}

	/**
	 * Method that extracts the info for all generics.
	 *
	 * @param element type, package or executable generic
	 * @param hierarchy hierarchy function
	 * @param extractor extractor function
	 * @param <E> type argument of the extractor
	 * @return list of tuples with annotation and skip type of all recursive methods
	 */
	private static <E> Map<Class<? extends Annotation>, Map<Comparable<?>, List<Tuple2<Annotation, SkipType>>>>
	extractAnnotationsInfos(E element,
	                        Function1<E, List<E>> hierarchy,
	                        Function2<E, Class<? extends Annotation>, Map<Comparable<?>, Tuple2<Annotation, SkipType>>> extractor) {

		/* Map with all classes info */
		return MULTIPLES.keySet().toMap(annot -> annot,
				annot -> {

					/* Get all annotations for each element */
					List<Map<Comparable<?>, Tuple2<Annotation, SkipType>>> annotations = hierarchy.apply(element)
							.map(elem -> extractor.apply(elem, annot));

					/* Get all keys */
					Set<Comparable<?>> keys = annotations.flatMap(Map::keySet).toSet();

					/* Return the map of annotation class and the info by comparable */
					return keys.toMap(comparable -> comparable,
							comparable -> annotations.map(map -> map.get(comparable).getOrNull()));

				});

	}

	/**
	 * Method that extracts the info for annotated element.
	 *
	 * @param annotated annotated method or class
	 * @return list of tuples with annotation and skip type of all recursive annotated elements
	 */
	private static Map<Class<? extends Annotation>, List<Tuple2<Annotation, SkipType>>>
	extractAnnotationInfos( Annotated annotated) {

		/* Map with all classes info */
		return SINGLES.keySet().toMap(annot -> annot,
				annot -> extractElementHierarchy(annotated).map(elem ->
						WsIODescriptor.extractElementAnnotationInfo(elem, annot)));

	}

	/**
	 * Method that extracts the info for all elements.
	 *
	 * @param element type, package or executable element
	 * @return list of tuples with annotation and skip type of all recursive methods
	 */
	private static Map<Class<? extends Annotation>, List<Tuple2<Annotation, SkipType>>>
	extractAnnotationInfos(Element element) {

		/* Map with all classes info */
		return SINGLES.keySet().toMap(annot -> annot,
				annot -> extractElementHierarchy(element).map(elem ->
						WsIODescriptor.extractElementAnnotationInfo(elem, annot)));

	}

	/**
	 * Method that extracts the info for all annotated elements.
	 *
	 * @param annotated type, package or executable element
	 * @return list of tuples with annotation and skip type of all recursive methods
	 */
	private static Map<Class<? extends Annotation>, List<Tuple2<Annotation, SkipType>>>
	extractAnnotationInfos(AnnotatedElement annotated) {

		/* Map with all classes info */
		return SINGLES.keySet().toMap(annot -> annot,
				annot -> extractElementHierarchy(annotated).map(elem ->
						WsIODescriptor.extractElementAnnotationInfo(elem, annot)));

	}

	/**
	 * Method that extracts the ct annotated from a ct method.
	 *
	 * @param pool pool of classes to search
	 * @param ctMethod ct method
	 * @return the annotated element
	 */
	private static Annotated extractCtAnnotated(Map<String, CtClass> pool, CtMethod ctMethod) {

		/* Extract and return the annotated element */
		return extractCtAnnotated(ctMethod, pool, ct -> null, CtMethod::getAnnotation,
				CtMethod::getDeclaringClass, ct -> extractCtAnnotated(pool, ct));

	}

	/**
	 * Method that extracts the ct annotated from a ct class.
	 *
	 * @param pool pool of classes to search
	 * @param ctClass ct class
	 * @return the annotated element
	 */
	private static Annotated extractCtAnnotated(Map<String, CtClass> pool, CtClass ctClass) {

		/* Extract and return the annotated element */
		return extractCtAnnotated(ctClass, pool, ct -> {

					/* Get package name and return package info name*/
					String packageName = ct.getPackageName();
					return WsIOUtil.addPrefixName("package-info", packageName);

				}, CtClass::getAnnotation, CtClass::getDeclaringClass, ct -> extractCtAnnotated(pool, ct));

	}

	/**
	 * Method that extracts the ct annotated from a ct method or class.
	 *
	 * @param ct ct method or class
	 * @param pool pool of classes to search
	 * @param getName function to extract the name
	 * @param getAnnotation checked function to extract annotations
	 * @param getEnclosing checked function that get enclosing element
	 * @param transformEnclosing function that transform enclosing element to annotated
	 * @param <CT> type argument of ct method or class
	 * @param <ENC> type argument of enclosing element
	 * @return the annotated element
	 */
	private static <CT, ENC> Annotated extractCtAnnotated(CT ct,
	                                                      Map<String, ENC> pool,
	                                                      Function1<CT, String> getName,
	                                                      CheckedFunction2<CT, Class, Object> getAnnotation,
	                                                      CheckedFunction1<CT, ENC> getEnclosing,
	                                                      Function1<ENC, Annotated> transformEnclosing) {

		/* Create a new annotated object */
		return new Annotated() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			@SuppressWarnings("unchecked")
			public <T extends Annotation> T getAnnotation(Class<T> anotation) {

				/* Try to obtain the annotation and check if is instance of the annotation class */
				return Try.of(() -> getAnnotation.apply(ct, anotation))
						.filter(anotation::isInstance)
						.map(anotation::cast)
						.getOrNull();

			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public <T extends Annotation> T[] getAnnotationsByType(Class<T> anotation) {

				/* Get the annotation info, try to get the multiple annotation, check is instance of multiple,
				 * then apply multiple function and check each element is instance of the main class.
				 * Finally transform the stream to array return it or an empty array */
				return MULTIPLES.get(anotation)
						.map(tuple -> Try.of(() -> getAnnotation.apply(ct, tuple._2()))
								.filter(tuple._2()::isInstance)
								.map(tuple._2()::cast)
								.map(tuple._3())
								.toStream()
								.flatMap(Stream::of)
								.filter(anotation::isInstance)
								.<T>map(anotation::cast)
								.toJavaArray(anotation))
						.getOrElse(() -> Stream.<T>empty()
								.toJavaArray(anotation));

			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public Annotated getEnclosingAnnotated() {

				/* Call function to return enclosing */
				return Try.of(() -> getEnclosing.apply(ct))
						.toOption().filter(Objects::nonNull)
						.orElse(Option.of(ct)
								.map(getName)
								.filter(Objects::nonNull)
								.flatMap(pool::get))
						.map(transformEnclosing)
						.getOrNull();

			}

		};

	}

	/**
	 * Method that extracts all elements of current.
	 *
	 * @param annotated annotated class
	 * @return list of annotated elements
	 */
	private static List<Annotated> extractElementHierarchy(Annotated annotated) {

		/* Return the list of elements */
		return Stream.iterate(annotated, Annotated::getEnclosingAnnotated)
				.takeWhile(Objects::nonNull)
				.reverse()
				.toList();

	}

	/**
	 * Method that extracts all elements of current.
	 *
	 * @param element type, package or executable element
	 * @return list of elements
	 */
	private static List<Element> extractElementHierarchy(Element element) {

		/* Return the list of elements */
		return Stream.iterate(element, Element::getEnclosingElement)
				.takeWhile(Objects::nonNull)
				.reverse()
				.toList();

	}

	/**
	 * Method that extracts all annotated of current.
	 *
	 * @param annotated type, package or executable annotated element
	 * @return list of annotateds
	 */
	private static List<AnnotatedElement> extractElementHierarchy(AnnotatedElement annotated) {

		/* Return the list of annotated elements */
		return Stream.iterate(annotated, element -> {

			/* Check the type of the element */
			if (element instanceof Method) {

				/* Return the declaring class of a method */
				return ((Method) element).getDeclaringClass();

			} else if (element instanceof Class) {

				/* Get declaring class and check if is null or not */
				Class<?> declaring = ((Class) element).getDeclaringClass();
				if (Objects.isNull(declaring)) {

					/* Return the package of the class */
					return ((Class) element).getPackage();

				} else {

					/* Return the declaring class */
					return declaring;

				}

			} else {

				/* Return null by default */
				return null;

			}

		}).takeWhile(Objects::nonNull).reverse().toList();

	}

	/**
	 * Method that extracts the info for current annotated element.
	 *
	 * @param annotated annotated element
	 * @param annotation class of the annotation
	 * @return tuple with annotation and skip type
	 */
	private static Map<Comparable<?>, Tuple2<Annotation, SkipType>> extractElementAnnotationsInfo(
			Annotated annotated,
			Class<? extends Annotation> annotation) {

		/* Return the map of multiples */
		return extractElementAnnotationsInfo(annotated, annotation, Annotated::getAnnotationsByType);

	}

	/**
	 * Method that extracts the info for current element.
	 *
	 * @param element type, package or executable element
	 * @param annotation class of the annotation
	 * @return tuple with annotation and skip type
	 */
	private static Map<Comparable<?>, Tuple2<Annotation, SkipType>> extractElementAnnotationsInfo(
			Element element,
			Class<? extends Annotation> annotation) {

		/* Return the map of multiples */
		return extractElementAnnotationsInfo(element, annotation, Element::getAnnotationsByType);

	}

	/**
	 * Method that extracts the info for current annotated element.
	 *
	 * @param annotated type, package or executable annotated element
	 * @param annotation class of the annotation
	 * @return tuple with annotation and skip type
	 */
	private static Map<Comparable<?>, Tuple2<Annotation, SkipType>> extractElementAnnotationsInfo(
			AnnotatedElement annotated,
			Class<? extends Annotation> annotation) {

		/* Return the map of multiples */
		return extractElementAnnotationsInfo(annotated, annotation, AnnotatedElement::getAnnotationsByType);

	}

	/**
	 * Method that extracts the info for current generic.
	 *
	 * @param element type, package or executable generic
	 * @param annotation class of the annotation
	 * @param extractor extractor function
	 * @param <E> type argument of the extractor
	 * @return tuple with annotation and skip type
	 */
	private static <E> Map<Comparable<?>, Tuple2<Annotation, SkipType>> extractElementAnnotationsInfo(
			E element,
			Class<? extends Annotation> annotation,
			Function2<E, Class<? extends Annotation>, ? extends Annotation[]> extractor) {

		/* Return the annotations info */
		return Map.narrow(MULTIPLES.get(annotation)
				.map(tuple -> Stream.of(extractor.apply(element, annotation))
						.toMap(annot -> tuple._5().apply(annot),
								annot -> Tuple.of(annot,
										tuple._4().apply(Stream.of(extractor.apply(element, tuple._1()))
												.toMap(tuple._6(), e -> e)
												.get(tuple._5().apply(annot))
												.getOrNull())
										)))
				.getOrElse(HashMap.empty()));

	}

	/**
	 * Method that extracts the info for current annotated element.
	 *
	 * @param annotated annotated element
	 * @param annotation class of the annotation
	 * @return tuple with annotation and skip type
	 */
	private static Tuple2<Annotation, SkipType> extractElementAnnotationInfo(Annotated annotated,
	                                                                         Class<? extends Annotation> annotation) {

		/* Return annotation info */
		return extractElementAnnotationInfo(annotated, annotation, Annotated::getAnnotation);

	}

	/**
	 * Method that extracts the info for current element.
	 *
	 * @param element type, package or executable element
	 * @param annotation class of the annotation
	 * @return tuple with annotation and skip type
	 */
	private static Tuple2<Annotation, SkipType> extractElementAnnotationInfo(Element element,
	                                                                         Class<? extends Annotation> annotation) {

		/* Return annotation info */
		return extractElementAnnotationInfo(element, annotation, Element::getAnnotation);

	}

	/**
	 * Method that extracts the info for current annotated element.
	 *
	 * @param annotated type, package or executable annotated element
	 * @param annotation class of the annotation
	 * @return tuple with annotation and skip type
	 */
	private static Tuple2<Annotation, SkipType> extractElementAnnotationInfo(AnnotatedElement annotated,
	                                                                         Class<? extends Annotation> annotation) {

		/* Return annotation info */
		return extractElementAnnotationInfo(annotated, annotation, AnnotatedElement::getAnnotation);

	}

	/**
	 * Method that extracts the info for current annotated generic.
	 *
	 * @param element type, package or executable generic
	 * @param annotation class of the annotation
	 * @param extractor extractor function
	 * @param <E> type argument of the extractor
	 * @return tuple with annotation and skip type
	 */
	private static <E> Tuple2<Annotation, SkipType> extractElementAnnotationInfo(
			E element,
			Class<? extends Annotation> annotation,
			Function2<E, Class<? extends Annotation>, ? extends Annotation> extractor) {

		/* Get annotation, and skip type from function call, finally filter tuple of nulls and return */
		return Tuple.of(extractor.apply(element, annotation),
				SINGLES.get(annotation)
						.map(tuple -> tuple._2().apply(extractor.apply(element, tuple._1())))
						.getOrNull());

	}

	/**
	 * Method that resolves the hierarchy of annotations.
	 *
	 * @param hierarchy list of hierarchy annotations and skips
	 * @return option with the annotation
	 */
	private static Option<Annotation> resolve(List<Tuple2<Annotation, SkipType>> hierarchy) {

		/* Get last index and iterate for each element of hierarchy in reverse order */
		int lastIndex = hierarchy.size() - 1;
		for (int index = lastIndex; index >= 0; index--) {

			/* Check tuple is not null */
			if (Objects.nonNull(hierarchy.get(index))) {

				/* Get current annotation and skip */
				Annotation annotation = hierarchy.get(index)._1();
				SkipType skip = hierarchy.get(index)._2();

				/* Check if annotation is null and the if skip is defined */
				if (Objects.nonNull(annotation)) {

					/* Return current annotation */
					return Option.of(annotation);

				} else if (SkipType.CHILDS.equals(skip)
						|| SkipType.ALL.equals(skip)
						|| (SkipType.CURRENT.equals(skip) && index == lastIndex)) {

					/* Break to return default value */
					break;

				}

			}

		}

		/* Return default empty */
		return Option.none();

	}

	/**
	 * Method that creates a descriptor starting from specified element.
	 *
	 * @param pool pool of classes to search
	 * @param ctMethod ct method
	 * @return descriptor from the element
	 */
	public static WsIODescriptor of(Map<String, CtClass> pool, CtMethod ctMethod) {

		/* Return of element type */
		return of(extractCtAnnotated(pool, ctMethod), WsIODescriptor::extractAnnotationInfos,
				WsIODescriptor::extractAnnotationsInfos);

	}

	/**
	 * Method that creates a descriptor starting from specified element.
	 *
	 * @param pool pool of classes to search
	 * @param ctClass ct class
	 * @return descriptor from the element
	 */
	public static WsIODescriptor of(Map<String, CtClass> pool, CtClass ctClass) {

		/* Return of element type */
		return of(extractCtAnnotated(pool, ctClass), WsIODescriptor::extractAnnotationInfos,
				WsIODescriptor::extractAnnotationsInfos);

	}

	/**
	 * Method that creates a descriptor starting from specified element.
	 *
	 * @param element package, type or executable element to extract descriptor
	 * @return descriptor from the element
	 */
	public static WsIODescriptor of(Element element) {

		/* Return of element type */
		return of(element, WsIODescriptor::extractAnnotationInfos,
				WsIODescriptor::extractAnnotationsInfos);

	}

	/**
	 * Method that creates a descriptor starting from specified element.
	 *
	 * @param annotated package, type or executable element to extract descriptor
	 * @return descriptor from the element
	 */
	public static WsIODescriptor of(AnnotatedElement annotated) {

		/* Return of annotated type */
		return of(annotated, WsIODescriptor::extractAnnotationInfos,
				WsIODescriptor::extractAnnotationsInfos);

	}

	/**
	 * Method that creates a descriptor starting from specified generic.
	 *
	 * @param element package, type or executable element to extract descriptor
	 * @param singleExtractor single extractor function
	 * @param multipleExtractor multiple extractor function
	 * @param <E> type argument of the extractor
	 * @return descriptor from the element
	 */
	private static <E> WsIODescriptor of(
			E element,
			Function1<E, Map<Class<? extends Annotation>, List<Tuple2<Annotation, SkipType>>>> singleExtractor,
			Function1<E, Map<Class<? extends Annotation>, Map<Comparable<?>, List<Tuple2<Annotation, SkipType>>>>> multipleExtractor) {

		/* Create descriptor */
		WsIODescriptor descriptor = new WsIODescriptor();

		/* Initialize single elements */
		descriptor.singles = singleExtractor.apply(element)
				.mapValues(WsIODescriptor::resolve)
				.flatMap((key, value) ->
						value.map(annotation -> Tuple.of(key, annotation)));

		/* Initialize multiple elements */
		descriptor.multiples = multipleExtractor.apply(element)
				.mapValues(map -> map.mapValues(WsIODescriptor::resolve)
						.flatMap((key, value) ->
								value.map(annotation -> Tuple.of(key, annotation))));

		/* Return descriptor */
		return descriptor;

	}

	/**
	 * Private empty constructor
	 */
	private WsIODescriptor() {  }

	/** Map containing the annotations */
	private Map<Class<? extends Annotation>, ? extends Annotation> singles;

	/** Map containing the annotations */
	private Map<Class<? extends Annotation>, Map<Comparable<?>, ? extends Annotation>> multiples;

	/**
	 * Getter for the field singles.
	 *
	 * @return singles map
	 */
	public Map<Class<? extends Annotation>, ? extends Annotation> getSingles() {
		return singles;
	}

	/**
	 * Getter for the field multiples.
	 *
	 * @return multiples map
	 */
	public Map<Class<? extends Annotation>, Map<Comparable<?>, ? extends Annotation>> getMultiples() {
		return multiples;
	}

	/**
	 * Method that returns the annotation wrapper in a option.
	 *
	 * @param clazz class of the annoation
	 * @param <T> type argument of the annotation
	 * @return option containing the possible annotation
	 */
	public <T extends Annotation> Option<T> getSingle(Class<T> clazz) {

		/* Return annotation option */
		return WsIODescriptor.getSingle(singles, clazz);

	}

	/**
	 * Method that returns the annotation wrapper in a option.
	 *
	 * @param clazz class of the annoation
	 * @param <T> type argument of the annotation
	 * @return option containing the possible annotation
	 */
	public <T extends Annotation> Map<Comparable<?>, T> getMultiple(Class<T> clazz) {

		/* Return annotation option */
		return WsIODescriptor.getMultiple(multiples, clazz);

	}

	/**
	 * Class used to represent an annotated element.
	 */
	private interface Annotated {

		/**
		 * Method to extract the annotation of an element.
		 *
		 * @param anotation class of the annotation
		 * @param <T> annotation type argument
		 * @return annotation of the specified type
		 */
		<T extends Annotation> T getAnnotation(Class<T> anotation);

		/**
		 * Method to extract the annotations of an element by type.
		 *
		 * @param anotation class of the annotation
		 * @param <T> annotation type argument
		 * @return array with all annotations of the specified type
		 */
		<T extends Annotation> T[] getAnnotationsByType(Class<T> anotation);

		/**
		 * Method that returns the enclosing annotated.
		 *
		 * @return the enclosing annotated
		 */
		Annotated getEnclosingAnnotated();

	}

}
