package com.fiberg.wsio.processor;

import com.fiberg.wsio.annotation.*;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * Class used to hold additionals annotation values.
 */
class WsIOAdditional {

	/** Hashmap that contains the name of the addional and a tuple with the use and the ignore classes */
	private static final Map<WsIOWrapper, Tuple2<Class<? extends Annotation>, Class<? extends Annotation>>> ADDITIONALS =
			HashMap.of(
					WsIOWrapper.TIME_WRAPPER, Tuple.of(WsIOUseTime.class, WsIOIgnoreUseTime.class),
					WsIOWrapper.STATE_WRAPPER, Tuple.of(WsIOUseState.class, WsIOIgnoreUseState.class),
					WsIOWrapper.INNER_WRAPPER, Tuple.of(WsIOUseInner.class, WsIOIgnoreUseInner.class)
			);

	/**
	 * Private constructor of the class
	 */
	private WsIOAdditional() { }

	/** Annotations of the additionals */
	private Map<WsIOWrapper, Annotation> annotated;

	/** Ignores of the additionals */
	private Map<WsIOWrapper, Annotation> ignored;

	/**
	 * Getter of the ignored field.
	 *
	 * @return ignored field
	 */
	public Map<WsIOWrapper, Annotation> getIgnored() {
		return ignored;
	}

	/**
	 * Setter of the ignored field.
	 *
	 * @param ignored ignored field
	 */
	public void setIgnored(Map<WsIOWrapper, Annotation> ignored) {
		this.ignored = ignored;
	}

	/**
	 * Getter of the annotated field.
	 *
	 * @return annotated field
	 */
	public Map<WsIOWrapper, Annotation> getAnnotated() {
		return annotated;
	}

	/**
	 * Setter of the ignored field.
	 *
	 * @param annotated annotated field
	 */
	public void setAnnotated(Map<WsIOWrapper, Annotation> annotated) {
		this.annotated = annotated;
	}

	/**
	 * Method that extracts the use annotations of a type element.
	 *
	 * @param element type element
	 * @return map with the name of the additional and the use annotation
	 */
	private static Map<WsIOWrapper, Annotation> extractUse(TypeElement element) {
		return ADDITIONALS.mapValues(Tuple2::_1)
				.mapValues(annotation -> Objects.nonNull(element) ?
						element.getEnclosingElement().getAnnotation(annotation) : null);
	}

	/**
	 * Method that extracts the ignore annotations of a type element.
	 *
	 * @param element type element
	 * @return map with the name of the additional and the ignore annotation
	 */
	private static Map<WsIOWrapper, Annotation> extractIgnore(TypeElement element) {
		return ADDITIONALS.mapValues(Tuple2::_2)
				.mapValues(annotation -> Objects.nonNull(element) ?
						element.getEnclosingElement().getAnnotation(annotation) : null);
	}

	/**
	 * Static constructor method
	 *
	 * @param element type element
	 * @return the addional when element
	 */
	static WsIOAdditional of(TypeElement element) {
		WsIOAdditional additional = new WsIOAdditional();
		additional.setAnnotated(WsIOAdditional.extractUse(element));
		additional.setIgnored(WsIOAdditional.extractIgnore(element));
		return additional;
	}

	WsIOAdditional update(TypeElement element) {

		/* Check if element is null or not */
		if (Objects.nonNull(element)) {

			/* Get current annotations */
			Map<WsIOWrapper, Annotation> currentAnnotated = WsIOAdditional.extractUse(element);
			Map<WsIOWrapper, Annotation> currentIgnored = WsIOAdditional.extractIgnore(element);

			/* Merge last annotations with current, current have more priority */
			Map<WsIOWrapper, Annotation> mergedAnnotated = currentAnnotated.merge(annotated);
			Map<WsIOWrapper, Annotation> mergedIgnored = currentIgnored.merge(ignored);

			/* Create, set values and return the additional */
			WsIOAdditional additional = new WsIOAdditional();
			additional.setAnnotated(mergedAnnotated);
			additional.setIgnored(mergedIgnored);
			return additional;

		}

		/* Return this when element is null */
		return this;

	}

}
