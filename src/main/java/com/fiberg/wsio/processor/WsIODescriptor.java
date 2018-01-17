package com.fiberg.wsio.processor;

import com.fiberg.wsio.annotation.*;
import com.fiberg.wsio.util.WsIOUtil;
import io.vavr.*;
import io.vavr.collection.*;
import io.vavr.control.Option;
import io.vavr.control.Try;
import javassist.CtClass;
import javassist.CtMember;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

/**
 * Class that gets the value of an annotation traveling all its declaring classes.
 */
public class WsIODescriptor {

	/** Descriptor of the single annotations */
	private static final Map<Class<? extends Annotation>, SingleDescriptor> SINGLES = HashMap.ofEntries(
			WsIODescriptor.describeSingle(WsIOUseTime.class, WsIOIgnoreUseTime.class, WsIOIgnoreUseTime::skip),
			WsIODescriptor.describeSingle(WsIOUseState.class, WsIOIgnoreUseState.class, WsIOIgnoreUseState::skip),
			WsIODescriptor.describeSingle(WsIOUseInner.class, WsIOIgnoreUseInner.class, WsIOIgnoreUseInner::skip),
			WsIODescriptor.describeSingle(WsIOUseHideEmpty.class, WsIOIgnoreUseHideEmpty.class, WsIOIgnoreUseHideEmpty::skip),
			WsIODescriptor.describeSingle(WsIOMessage.class, WsIOSkipMessage.class, WsIOSkipMessage::skip),
			WsIODescriptor.describeSingle(WsIOMessageWrapper.class, WsIOSkipMessageWrapper.class, WsIOSkipMessageWrapper::skip),
			WsIODescriptor.describeSingle(WsIOAnnotate.class, WsIOSkipAnnotate.class, WsIOSkipAnnotate::skip),
			WsIODescriptor.describeSingle(WsIOMetadata.class, WsIOSkipMetadata.class, WsIOSkipMetadata::skip)
	);

	/** Descriptor of the repeatables annotations */
	private static final Map<Class<? extends Annotation>, RepeatableDescriptor> REPEATABLES = HashMap.ofEntries(
			WsIODescriptor.describeRepeatable(WsIOClone.class, WsIOClones.class, WsIOSkipClone.class, WsIOSkipClones.class,
					WsIOClones::value, WsIOSkipClones::value,
					annotation -> Tuple.of(annotation.prefix(), annotation.suffix()),
					annotation -> Tuple.of(annotation.prefix(), annotation.suffix()),
					WsIOSkipClone::skip)
	);

	/**
	 * Method that checks the arguments received and transforms the function.
	 *
	 * @param function function to transform
	 * @param clazz    class of the annotation
	 * @param <A>      type of the annotation
	 * @param <R>      type of the return
	 * @return the function that receives an annotation
	 */
	private static <A extends Annotation, R> Function1<Annotation, R> describeCheck(
			final Function1<A, R> function,
			final Class<A> clazz) {

		return annotation -> {

			/* Check class is not null, annotaiton is not null and is instance of the class */
			if (clazz != null && annotation != null && clazz.isInstance(annotation)) {

				/* Return the function applied */
				@SuppressWarnings({ "unchecked" })
				final R result = function.apply((A) annotation);
				return result;

			}

			/* Return null by default */
			return null;

		};

	}

	/**
	 * Method that create a descriptor for the single skip annotations.
	 *
	 * @param mainClass     main class extending annotation
	 * @param skipClass     skip class extending annotation
	 * @param skipExtractor function to transform
	 * @param <S>           type argument of the skip class extending annotation
	 * @return tuple containing the main class and the descriptor
	 */
	private static <M extends Annotation, S extends Annotation> Tuple2<Class<M>, SingleDescriptor> describeSingle(
			final Class<M> mainClass,
			final Class<S> skipClass,
			final Function1<S, SkipType> skipExtractor) {

		/* Return the skip class, and the skip method */
		return Tuple.of(mainClass, new SingleDescriptor(mainClass, skipClass,
				WsIODescriptor.describeCheck(skipExtractor, skipClass)));

	}

	/**
	 * Method that create a descriptor for the repeatable skip annotations.
	 *
	 * @param mainClass                 main class extending annotation
	 * @param mainRepeatableClass       main repeatable class extending annotation
	 * @param skipClass                 skip class extending annotation
	 * @param skipRepeatableClass       skip repeatable class extending annotation
	 * @param mainRepeatableExtractor   main repeatable extractor
	 * @param skipRepeatableExtractor   skip repeatable extractor
	 * @param mainComparator            main annotation comparator
	 * @param skipComparator            skip annotation comparator
	 * @param skipExtractor             skip annotation extractor
	 * @param <M>                       type argument extending the main annotation
	 * @param <MR>                      type argument extending the main repeatable annotation
	 * @param <S>                       type argument extending the skip annotation
	 * @param <SR>                      type argument extending the skip repeatable annotation
	 * @return tuple containing the main class and the descriptor
	 */
	@SuppressWarnings({ "unchecked" })
	private static <M extends Annotation, S extends Annotation, MR extends Annotation, SR extends Annotation>
	Tuple2<Class<M>, RepeatableDescriptor> describeRepeatable(
			final Class<M> mainClass,
			final Class<MR> mainRepeatableClass,
			final Class<S> skipClass,
			final Class<SR> skipRepeatableClass,
			final Function1<MR, M[]> mainRepeatableExtractor,
			final Function1<SR, S[]> skipRepeatableExtractor,
			final Function1<M, Comparable<?>> mainComparator,
			final Function1<S, Comparable<?>> skipComparator,
			final Function1<S, SkipType> skipExtractor) {

		/* Return the annotation class, the skip function and the comparator function */
		return Tuple.of(mainClass,
				new RepeatableDescriptor(mainClass, mainRepeatableClass, skipClass, skipRepeatableClass,
						WsIODescriptor.describeCheck(mainRepeatableExtractor, mainRepeatableClass)
								.andThen(a -> (Annotation[]) a),
						WsIODescriptor.describeCheck(skipRepeatableExtractor, skipRepeatableClass)
								.andThen(a -> (Annotation[]) a),
						WsIODescriptor.describeCheck(mainComparator, mainClass),
						WsIODescriptor.describeCheck(skipComparator, skipClass),
						WsIODescriptor.describeCheck(skipExtractor, skipClass)
				));

	}

	/**
	 * Method that returns the annotation wrapper in a option.
	 *
	 * @param annotations map of annotations
	 * @param clazz       class of the annoation
	 * @param <T>         type argument of the annotation
	 * @return option containing the possible annotation
	 */
	private static <T extends Annotation> Option<T> getSingle(final Map<Class<? extends Annotation>, ? extends Annotation> annotations,
	                                                          final Class<T> clazz) {

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
	 * @param clazz       class of the annoation
	 * @param <T>         type argument of the annotation
	 * @return option containing the possible annotation
	 */
	private static <T extends Annotation> Map<Comparable<?>, T> getMultiple(
			final Map<Class<? extends Annotation>, Map<Comparable<?>, ? extends Annotation>> annotations,
			final Class<T> clazz) {

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
	 * Method that resolves the hierarchy of annotations.
	 *
	 * @param hierarchy list of hierarchy annotations and skips
	 * @return option with the annotation
	 */
	private static Option<Annotation> resolve(final List<Tuple2<Annotation, SkipType>> hierarchy) {

		/* Get last index and iterate for each element of hierarchy in reverse order */
		final int lastIndex = hierarchy.size() - 1;
		for (int index = lastIndex; index >= 0; index--) {

			/* Check tuple is not null */
			if (Objects.nonNull(hierarchy.get(index))) {

				/* Get current annotation and skip */
				final Annotation annotation = hierarchy.get(index)._1();
				final SkipType skip = hierarchy.get(index)._2();

				/* Check if annotation is null and the if skip is defined */
				if (SkipType.ALL.equals(skip)
						|| (SkipType.CHILDS.equals(skip) && index != lastIndex)
						|| (SkipType.CURRENT.equals(skip) && index == lastIndex)) {

					/* Break to return default value */
					break;

				} else if (Objects.nonNull(annotation)) {

					/* Return current annotation */
					return Option.of(annotation);

				}

			}

		}

		/* Return default empty */
		return Option.none();

	}

	/**
	 * Method that extracts the info for annotated element.
	 *
	 * @param annotated annotated method or class
	 * @return list of tuples with annotation and skip type of all recursive annotated elements
	 */
	private static Map<Class<? extends Annotation>, List<Tuple2<Annotation, SkipType>>>
	extractAnnotationInfos(final Annotated annotated) {

		/* Map with all classes info */
		return SINGLES.keySet().toMap(annot -> annot,
				annot -> WsIODescriptor.extractElementHierarchy(annotated).map(elem ->
						WsIODescriptor.extractElementAnnotationInfo(elem, annot)));

	}

	/**
	 * Method that extracts all elements of current.
	 *
	 * @param annotated annotated class
	 * @return list of annotated elements
	 */
	private static List<Annotated> extractElementHierarchy(final Annotated annotated) {

		/* Return the list of elements */
		return Stream.iterate(annotated, Annotated::getEnclosingAnnotated)
				.takeWhile(Objects::nonNull)
				.reverse()
				.toList();

	}

	/**
	 * Method that extracts the info for current annotated element.
	 *
	 * @param annotated  annotated element
	 * @param annotation class of the annotation
	 * @return tuple with annotation and skip type
	 */
	private static Tuple2<Annotation, SkipType> extractElementAnnotationInfo(final Annotated annotated,
	                                                                         final Class<? extends Annotation> annotation) {

		/* Get annotation, and skip type from function call, finally filter tuple of nulls and return */
		return Tuple.of(annotated.getAnnotation(annotation),
				SINGLES.get(annotation)
						.map(single -> single.getSkipExtractor()
								.apply(annotated.getAnnotation(single.getSkipClass())))
						.getOrNull());

	}

	/**
	 * Method that extracts the info for annotated element.
	 *
	 * @param annotated annotated method or class
	 * @return list of tuples with annotation and skip type of all recursive annotated elements
	 */
	private static Map<Class<? extends Annotation>, Map<Comparable<?>, List<Tuple2<Annotation, SkipType>>>>
	extractAnnotationsInfos(final Annotated annotated) {

		/* Return annotations info */
		return extractAnnotationsInfos(annotated, WsIODescriptor::extractElementHierarchy,
				WsIODescriptor::extractElementAnnotationsInfo);

	}

	/**
	 * Method that extracts the info for current annotated element.
	 *
	 * @param annotated  annotated element
	 * @param annotation class of the annotation
	 * @return tuple with annotation and skip type
	 */
	private static Map<Comparable<?>, Tuple2<Annotation, SkipType>> extractElementAnnotationsInfo(
			final Annotated annotated,
			final Class<? extends Annotation> annotation) {

		/* Return the annotations info */
		return Map.narrow(REPEATABLES.get(annotation)
				.map(repeatable -> {

					/* Main and skip annotations */
					final Map<Comparable<?>, Annotation> mains = Stream.ofAll(annotated.getAnnotationsByType(annotation))
							.toMap(repeatable.getMainComparator(), a -> a);
					final Map<Comparable<?>, Annotation> skips = Stream.ofAll(annotated.getAnnotationsByType(repeatable.getSkipClass()))
							.toMap(repeatable.getSkipComparator(), a -> a);

					/* Get all the keys, then then the annotation of mains and the skip from skips */
					return Stream.concat(mains.keySet(), skips.keySet())
							.toMap(c -> c, c -> Tuple.of(mains.get(c).getOrNull(),
									repeatable.getSkipExtractor().apply(skips.get(c).getOrNull())));

				}).getOrElse(HashMap.empty()));

	}

	/**
	 * Method that extracts the info for all generics.
	 *
	 * @param element   type, package or executable generic
	 * @param hierarchy hierarchy function
	 * @param extractor extractor function
	 * @param <E>       type argument of the extractor
	 * @return list of tuples with annotation and skip type of all recursive methods
	 */
	private static <E> Map<Class<? extends Annotation>, Map<Comparable<?>, List<Tuple2<Annotation, SkipType>>>>
	extractAnnotationsInfos(final E element,
	                        final Function1<E, List<E>> hierarchy,
	                        final Function2<E, Class<? extends Annotation>, Map<Comparable<?>, Tuple2<Annotation, SkipType>>> extractor) {

		/* Map with all classes info */
		return REPEATABLES.keySet().toMap(annot -> annot,
				annot -> {

					/* Get all annotations for each element */
					final List<Map<Comparable<?>, Tuple2<Annotation, SkipType>>> annotations = hierarchy.apply(element)
							.map(elem -> extractor.apply(elem, annot));

					/* Get all keys */
					final Set<Comparable<?>> keys = annotations.flatMap(Map::keySet).toSet();

					/* Return the map of annotation class and the info by comparable */
					return keys.toMap(comparable -> comparable,
							comparable -> annotations.map(map -> map.get(comparable).getOrNull()));

				});

	}

	/**
	 * Transform to annotated the element.
	 *
	 * @param element element
	 * @return the annotated
	 */
	private static Annotated toAnnotated(final Element element) {

		/* Create the annotated element */
		return new AnnotatedImpl(element::getAnnotation,
				annotation -> Arrays.asList(element.getAnnotationsByType(annotation)),
				() -> Option.of(element).map(WsIODescriptor::toAnnotated).getOrNull());

	}

	/**
	 * Transform to annotated the annotated element.
	 *
	 * @param annotated annotated element
	 * @return the annotated
	 */
	private static Annotated toAnnotated(final AnnotatedElement annotated) {

		/* Create the annotated element */
		return new AnnotatedImpl(annotated::getAnnotation,
				annotation -> Arrays.asList(annotated.getAnnotationsByType(annotation)),
				() -> Option.of(annotated).map(element -> {

					/* Check the type of the element */
					if (element instanceof Method) {

						/* Return the declaring class of a method */
						return ((Method) element).getDeclaringClass();

					} else if (element instanceof Class) {

						/* Get declaring class and check if is null or not */
						final Class<?> declaring = ((Class) element).getDeclaringClass();
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

				}).filter(Objects::nonNull)
						.map(WsIODescriptor::toAnnotated)
						.getOrNull());

	}

	/**
	 * Transform to annotated the ct member.
	 *
	 * @param pool     pool of classes to search
	 * @param ctMember ct member
	 * @return the annotated
	 */
	private static Annotated toAnnotated(final Map<String, CtClass> pool, final CtMember ctMember) {

		/* Extract and return the annotated element */
		return toAnnotated(
				ctMember,
				pool,
				ct -> null,
				CtMember::getAnnotation,
				CtMember::getAnnotations,
				CtMember::getDeclaringClass);

	}

	/**
	 * Transform to annotated the ct class.
	 *
	 * @param pool    pool of classes to search
	 * @param ctClass ct class
	 * @return the annotated
	 */
	private static Annotated toAnnotated(final Map<String, CtClass> pool, final CtClass ctClass) {

		/* Extract and return the annotated element */
		return toAnnotated(
				ctClass,
				pool,
				ct -> Option.of(ct)
						.filter(current -> !"package-info".equals(current.getSimpleName()))
						.map(CtClass::getPackageName)
						.map(packageName -> WsIOUtil.addPrefixName("package-info", packageName))
						.getOrNull(),
				CtClass::getAnnotation,
				CtClass::getAnnotations,
				CtClass::getDeclaringClass);

	}

	/**
	 * Transform to annotated the ct type.
	 *
	 * @param ct             ct method or class
	 * @param pool           pool of classes to search
	 * @param getName        function to extract the name
	 * @param getAnnotation  checked function to extract annotation
	 * @param getAnnotations checked function to extract annotations
	 * @param getEnclosing   checked function that get enclosing element
	 * @param <CT>           type argument of ct method or class
	 * @return the annotated
	 */
	private static <CT> Annotated toAnnotated(final CT ct,
	                                          final Map<String, CtClass> pool,
	                                          final Function1<CT, String> getName,
	                                          final CheckedFunction2<CT, Class, Object> getAnnotation,
	                                          final CheckedFunction1<CT, Object[]> getAnnotations,
	                                          final CheckedFunction1<CT, CtClass> getEnclosing) {

		/* Return the annotated implementation */
		return new AnnotatedImpl(annotation -> {

			/* Try to obtain the annotation and check if is instance of the annotation class */
			return Try.of(() -> getAnnotation.apply(ct, annotation))
					.filter(annotation::isInstance)
					.map(annotation::cast)
					.getOrNull();

		}, annotation -> {

			/* Get all annotations, filter by instance, cast and then return the java list */
			return Try.of(() -> getAnnotations.apply(ct))
					.toOption().filter(Objects::nonNull)
					.map(Stream::of)
					.getOrElse(Stream::empty)
					.filter(annotation::isInstance)
					.map(annotation::cast)
					.toJavaList();

		}, () -> {

			/* Call function to return enclosing */
			return Try.of(() -> getEnclosing.apply(ct))
					.toOption().filter(Objects::nonNull)
					.orElse(Option.of(ct)
							.map(getName)
							.filter(Objects::nonNull)
							.flatMap(pool::get))
					.map(current -> toAnnotated(pool, current))
					.getOrNull();

		});

	}

	/**
	 * Method that creates a descriptor starting from specified element.
	 *
	 * @param pool     pool of classes to search
	 * @param ctMember ct member
	 * @return descriptor from the element
	 */
	public static WsIODescriptor of(final Map<String, CtClass> pool,
	                                 final CtMember ctMember) {
		return of(toAnnotated(pool, ctMember));
	}

	/**
	 * Method that creates a descriptor starting from specified element.
	 *
	 * @param pool    pool of classes to search
	 * @param ctClass ct class
	 * @return descriptor from the element
	 */
	public static WsIODescriptor of(final Map<String, CtClass> pool,
	                                 final CtClass ctClass) {
		return of(toAnnotated(pool, ctClass));
	}

	/**
	 * Method that creates a descriptor starting from specified element.
	 *
	 * @param element package, type or executable element to extract descriptor
	 * @return descriptor from the element
	 */
	public static WsIODescriptor of(final Element element) {
		return of(toAnnotated(element));
	}

	/**
	 * Method that creates a descriptor starting from specified element.
	 *
	 * @param annotated package, type or executable element to extract descriptor
	 * @return descriptor from the element
	 */
	public static WsIODescriptor of(final AnnotatedElement annotated) {
		return of(toAnnotated(annotated));
	}

	/**
	 * Method that creates a descriptor starting from specified generic.
	 *
	 * @param annotated annotated element to create the descriptor
	 * @return descriptor from the element
	 */
	private static WsIODescriptor of(final Annotated annotated) {

		/* Create descriptor */
		final WsIODescriptor descriptor = new WsIODescriptor();

		/* Initialize single elements */
		descriptor.singles = extractAnnotationInfos(annotated)
				.mapValues(WsIODescriptor::resolve)
				.flatMap((key, value) ->
						value.map(annotation -> Tuple.of(key, annotation)));

		/* Initialize multiple elements */
		descriptor.multiples = extractAnnotationsInfos(annotated)
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
	 * @param <T>   type argument of the annotation
	 * @return option containing the possible annotation
	 */
	public <T extends Annotation> Option<T> getSingle(final Class<T> clazz) {

		/* Return annotation option */
		return getSingle(singles, clazz);

	}

	/**
	 * Method that returns the annotation wrapper in a option.
	 *
	 * @param clazz class of the annoation
	 * @param <T>   type argument of the annotation
	 * @return option containing the possible annotation
	 */
	public <T extends Annotation> Map<Comparable<?>, T> getMultiple(final Class<T> clazz) {

		/* Return annotation option */
		return getMultiple(multiples, clazz);

	}

	/**
	 * Class used to represent an annotated element.
	 */
	private static class AnnotatedImpl implements Annotated {

		/** Get annotation function */
		private final Function1<Class<? extends Annotation>, ? extends Annotation> getAnnotation;

		/** Get annotations by type function */
		private final Function1<Class<? extends Annotation>, java.util.List<? extends Annotation>> getAnnotationsByType;

		/** Get enclosing annotated function */
		private final Function0<Annotated> getEnclosingAnnotated;

		/**
		 * Constructor of the annotated class.
		 *
		 * @param getAnnotation         get annotation function
		 * @param getAnnotationsByType  get annotations by type function
		 * @param getEnclosingAnnotated get enclosing annotated function
		 */
		private AnnotatedImpl(final Function1<Class<? extends Annotation>, ? extends Annotation> getAnnotation,
		                      final Function1<Class<? extends Annotation>, java.util.List<? extends Annotation>> getAnnotationsByType,
		                      final Function0<Annotated> getEnclosingAnnotated) {
			this.getAnnotation = getAnnotation;
			this.getAnnotationsByType = getAnnotationsByType;
			this.getEnclosingAnnotated = getEnclosingAnnotated;
		}

		/**
		 * {@inheritDoc}
		 */
		public <T extends Annotation> T getAnnotation(Class<T> annotation) {

			/* Check if annotation is null or not */
			if (annotation != null) {

				/* Get result object and check if is instance of the class */
				final Object object = getAnnotation.apply(annotation);
				if (annotation.isInstance(object)) {

					/* Cast and return */
					@SuppressWarnings({ "unchecked" })
					final T result = (T) object;
					return result;

				}

			}

			/* Return null by default */
			return null;

		}

		/**
		 * {@inheritDoc}
		 */
		public <T extends Annotation> java.util.List<T> getAnnotationsByType(Class<T> annotation) {

			/* Check if annotation is null or not */
			if (annotation != null) {

				/* Return the java list */
				return Option.of(getAnnotationsByType.apply(annotation))
						.map(Stream::ofAll)
						.getOrElse(Stream::empty)
						.filter(annotation::isInstance)
						.map(annotation::cast)
						.toJavaList();

			}

			/* Return empty list by default */
			return Collections.emptyList();

		}

		/**
		 * {@inheritDoc}
		 */
		public Annotated getEnclosingAnnotated() {
			return getEnclosingAnnotated.apply();
		}

	}

	/**
	 * Interface used to represent an annotated element.
	 */
	private interface Annotated {

		/**
		 * Method to extract the annotation of an element.
		 *
		 * @param anotation class of the annotation
		 * @param <T>       annotation type argument
		 * @return annotation of the specified type
		 */
		<T extends Annotation> T getAnnotation(Class<T> anotation);

		/**
		 * Method to extract the annotations of an element by type.
		 *
		 * @param anotation class of the annotation
		 * @param <T>       annotation type argument
		 * @return list with all annotations of the specified type
		 */
		<T extends Annotation> java.util.List<T> getAnnotationsByType(Class<T> anotation);

		/**
		 * Method that returns the enclosing annotated.
		 *
		 * @return the enclosing annotated
		 */
		Annotated getEnclosingAnnotated();

	}

	/**
	 * Single descriptor class to hold the necesary values.
	 */
	static class SingleDescriptor {

		/** Main class */
		private final Class<? extends Annotation> mainClass;

		/** Skip class */
		private final Class<? extends Annotation> skipClass;

		/** Skip extractor */
		private final Function1<Annotation, SkipType> skipExtractor;

		/**
		 * All args constructor.
		 *
		 * @param mainClass     main class
		 * @param skipClass   skip class
		 * @param skipExtractor skip extractor
		 */
		private SingleDescriptor(final Class<? extends Annotation> mainClass,
		                         final Class<? extends Annotation> skipClass,
		                         final Function1<Annotation, SkipType> skipExtractor) {
			this.mainClass = mainClass;
			this.skipClass = skipClass;
			this.skipExtractor = skipExtractor;
		}

		/**
		 * Getter of the main class.
		 *
		 * @return main class
		 */
		public Class<? extends Annotation> getMainClass() {
			return mainClass;
		}

		/**
		 * Getter of the skip class.
		 *
		 * @return skip class
		 */
		public Class<? extends Annotation> getSkipClass() {
			return skipClass;
		}

		/**
		 * Getter of the skip extractor.
		 *
		 * @return skip extractor
		 */
		public Function1<Annotation, SkipType> getSkipExtractor() {
			return skipExtractor;
		}

	}

	/**
	 * Repeatable descriptor class to hold the necesary values.
	 */
	static class RepeatableDescriptor {

		/** Main class */
		private final Class<? extends Annotation> mainClass;

		/** Main repeatable class */
		private final Class<? extends Annotation> mainRepeatableClass;

		/** Skip class */
		private final Class<? extends Annotation> skipClass;

		/** Skip repeatable class */
		private final Class<? extends Annotation> skipRepeatableClass;

		/** Main repeatable extractor function */
		private final Function1<Annotation, Annotation[]> mainRepeatableExtractor;

		/** Skip repeatable extractor function */
		private final Function1<Annotation, Annotation[]> skipRepeatableExtractor;

		/** Main comparator function */
		private final Function1<Annotation, Comparable<?>> mainComparator;

		/** Skip comparator function */
		private final Function1<Annotation, Comparable<?>> skipComparator;

		/** Skip extractor function */
		private final Function1<Annotation, SkipType> skipExtractor;

		/**
		 * All args constructor.
		 *
		 * @param mainClass                 main class
		 * @param mainRepeatableClass       main repeatable class
		 * @param skipClass               skip class
		 * @param skipRepeatableClass     skip repeatable class
		 * @param mainRepeatableExtractor   main repeatable extractor function
		 * @param skipRepeatableExtractor skip repeatable extractor function
		 * @param mainComparator            main comparator function
		 * @param skipComparator          skip comparator function
		 * @param skipExtractor             skip extractor function
		 */
		public RepeatableDescriptor(final Class<? extends Annotation> mainClass,
		                            final Class<? extends Annotation> mainRepeatableClass,
		                            final Class<? extends Annotation> skipClass,
		                            final Class<? extends Annotation> skipRepeatableClass,
		                            final Function1<Annotation, Annotation[]> mainRepeatableExtractor,
		                            final Function1<Annotation, Annotation[]> skipRepeatableExtractor,
		                            final Function1<Annotation, Comparable<?>> mainComparator,
		                            final Function1<Annotation, Comparable<?>> skipComparator,
		                            final Function1<Annotation, SkipType> skipExtractor) {
			this.mainClass = mainClass;
			this.mainRepeatableClass = mainRepeatableClass;
			this.skipClass = skipClass;
			this.skipRepeatableClass = skipRepeatableClass;
			this.mainRepeatableExtractor = mainRepeatableExtractor;
			this.skipRepeatableExtractor = skipRepeatableExtractor;
			this.mainComparator = mainComparator;
			this.skipComparator = skipComparator;
			this.skipExtractor = skipExtractor;
		}

		/**
		 * Getter of the main class.
		 *
		 * @return the main class
		 */
		public Class<? extends Annotation> getMainClass() {
			return mainClass;
		}

		/**
		 * Getter of the main repeatable class.
		 *
		 * @return the main repeatable class
		 */
		public Class<? extends Annotation> getMainRepeatableClass() {
			return mainRepeatableClass;
		}

		/**
		 * Getter of the skip class.
		 *
		 * @return the skip class
		 */
		public Class<? extends Annotation> getSkipClass() {
			return skipClass;
		}

		/**
		 * Getter of the skip repeatable class.
		 *
		 * @return the skip repeatable class
		 */
		public Class<? extends Annotation> getSkipRepeatableClass() {
			return skipRepeatableClass;
		}

		/**
		 * Getter of the main repeatable extractor.
		 *
		 * @return the main repeatable extractor
		 */
		public Function1<Annotation, Annotation[]> getMainRepeatableExtractor() {
			return mainRepeatableExtractor;
		}

		/**
		 * Getter of the skip repeatable extractor.
		 *
		 * @return the skip repeatable extractor
		 */
		public Function1<Annotation, Annotation[]> getSkipRepeatableExtractor() {
			return skipRepeatableExtractor;
		}

		/**
		 * Getter of the main comparator.
		 *
		 * @return the main comparator
		 */
		public Function1<Annotation, Comparable<?>> getMainComparator() {
			return mainComparator;
		}

		/**
		 * Getter of the skip comparator.
		 *
		 * @return the skip comparator
		 */
		public Function1<Annotation, Comparable<?>> getSkipComparator() {
			return skipComparator;
		}

		/**
		 * Getter of the skip extractor.
		 *
		 * @return the skip extractor
		 */
		public Function1<Annotation, SkipType> getSkipExtractor() {
			return skipExtractor;
		}

	}

}
