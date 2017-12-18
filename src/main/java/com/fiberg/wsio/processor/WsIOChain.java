package com.fiberg.wsio.processor;

import com.fiberg.wsio.annotation.*;
import io.vavr.Function1;
import io.vavr.Predicates;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import org.apache.commons.lang3.ObjectUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * Class that gets the value of an annotation traveling all its declaring classes.
 */
class WsIOChain {

	/** Descriptor of the annotations */
	private static final Map<Class<? extends Annotation>,
				Tuple2<Class<? extends Annotation>, Function1<Annotation, SkipType>>> ANNOTATIONS = HashMap.of(
			WsIOUseTime.class, WsIOChain.skipDescriptor(WsIOIgnoreUseTime.class, WsIOIgnoreUseTime::skip),
			WsIOUseState.class, WsIOChain.skipDescriptor(WsIOIgnoreUseState.class, WsIOIgnoreUseState::skip),
			WsIOUseInner.class, WsIOChain.skipDescriptor(WsIOIgnoreUseInner.class, WsIOIgnoreUseInner::skip),
			WsIOUseHideEmpty.class, WsIOChain.skipDescriptor(WsIOIgnoreUseHideEmpty.class, WsIOIgnoreUseHideEmpty::skip)
	);

	/**
	 * Method that create a descriptor for the skip annotations
	 *
	 * @param clazz main class extending annotation
	 * @param function function to transform
	 * @param <T> type argument extending annotation
	 * @return tuple containing the skip descriptor of the annotation
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Annotation> Tuple2<Class<? extends Annotation>, Function1<Annotation, SkipType>>
	skipDescriptor(Class<T> clazz, Function1<T, SkipType> function) {
		return Tuple.of(clazz, annotation -> {
			if (clazz != null && annotation != null && clazz.isInstance(annotation)) {
				return function.apply((T) annotation);
			}
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
	public static <T extends Annotation> Option<T> get(Map<Class<? extends Annotation>, ? extends Annotation> annotations,
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
	 * Method that extracts the info for all elements.
	 *
	 * @param element type, package or executable element
	 * @param annotation class of the annotation
	 * @return list of tuples with annotation and skip type of all recursive methods
	 */
	private static List<Tuple2<Annotation, SkipType>> chain(Element element,
	                                                        Class<? extends Annotation> annotation) {

		/* Get enclosing element and current tupe info */
		Element enclosing = element.getEnclosingElement();
		List<Tuple2<Annotation, SkipType>> current = current(element, annotation);

		/* Check enclosing element is type or package to keep calling recursively */
		if (enclosing instanceof TypeElement
				|| enclosing instanceof PackageElement) {

			/* Return the recursive call of the function and append current info in the end of the list */
			return chain(enclosing, annotation).appendAll(current);

		}

		/* Return just current info */
		return current;

	}

	/**
	 * Method that extracts the info for current element.
	 *
	 * @param element type, package or executable element
	 * @param annotation class of the annotation
	 * @return tuple with annotation and skip type
	 */
	private static List<Tuple2<Annotation, SkipType>> current(Element element,
	                                                          Class<? extends Annotation> annotation) {

		/* Get annotation, and skip type from function call, finally filter tuple of nulls and return */
		return List.of(Tuple.of(
				element.getAnnotation(annotation),
				ANNOTATIONS.get(annotation)
						.map(tuple -> tuple._2().apply(element.getAnnotation(tuple._1())))
						.getOrNull()
				));

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

		/* Return default empty */
		return Option.none();

	}

	/**
	 * Method that creates a chain starting from specified element.
	 *
	 * @param element package, type or executable element to extract chain
	 * @return chain from the element
	 */
	static WsIOChain of(Element element) {

		/* Create chain, initialize annotations and return */
		WsIOChain chain = new WsIOChain();
		chain.annotations = ANNOTATIONS.keySet().flatMap(clazz ->
				WsIOChain.resolve(chain(element, clazz))
						.map(annotation -> Tuple.of(clazz, annotation)))
				.toMap(e -> e);
		return chain;

	}

	/**
	 * Private empty constructor
	 */
	private WsIOChain() {  }

	/** Map containing the annotations */
	private Map<Class<? extends Annotation>, ? extends Annotation> annotations;

	/**
	 * Getter for the field annotations.
	 *
	 * @return annotations map
	 */
	public Map<Class<? extends Annotation>, ? extends Annotation> getAnnotations() {
		return annotations;
	}

	/**
	 * Method that returns the annotation wrapper in a option.
	 *
	 * @param clazz class of the annoation
	 * @param <T> type argument of the annotation
	 * @return option containing the possible annotation
	 */
	public <T extends Annotation> Option<T> get(Class<T> clazz) {

		/* Return annotation option */
		return WsIOChain.get(annotations, clazz);

	}

}
