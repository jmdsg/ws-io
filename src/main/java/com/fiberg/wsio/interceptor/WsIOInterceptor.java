package com.fiberg.wsio.interceptor;

import com.fiberg.wsio.annotation.WsIOUseState;
import com.fiberg.wsio.annotation.WsIOUseTime;
import com.fiberg.wsio.handler.WsIOHandler.State;
import com.fiberg.wsio.handler.WsIOHandler.Time;
import com.fiberg.wsio.handler.state.WsIOElement;
import com.fiberg.wsio.handler.state.WsIOLanguage;
import com.fiberg.wsio.handler.state.WsIOState;
import com.fiberg.wsio.handler.state.WsIOText;
import com.fiberg.wsio.handler.time.WsIOInstant;
import com.fiberg.wsio.handler.time.WsIOTime;
import io.vavr.Predicates;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Class used to intercept the petitions and responses and add the additionals.
 */
public final class WsIOInterceptor {

	/**
	 * Private empty contructor.
	 */
	private WsIOInterceptor() {  }

	/**
	 * In interceptor method, used to add the initial info for the out interceptor.
	 * This method also resets the current values for additionals and add the start date when is enabled.
	 *
	 * @param endpoint endpoint class of the method
	 * @param method method to extract the info
	 * @param session map containing the session info
	 */
	public static void in(Class<?> endpoint, Method method, java.util.Map<String, Object> session) {

		/* Create the hash map and add the method and endpoint info */
		java.util.Map<WsIOData, Object> wsio = new java.util.HashMap<>();
		wsio.put(WsIOData.METHOD, method);
		wsio.put(WsIOData.ENDPOINT, endpoint);

		/* Time and state annotations with priority */
		WsIOUseTime time = WsIOInterceptor.extractAnnotation(method, WsIOUseTime.class);
		WsIOUseState state = WsIOInterceptor.extractAnnotation(method, WsIOUseState.class);

		/* Check time annotation is defined */
		if (Objects.nonNull(time)) {

			/* Reset current values */
			Time.reset();

			/* Check start is enabled and add current date time to the additional */
			if (time.start()) {
				Time.addDateTimeNow(time.startName());
			}

			/* Add time annotation to the session */
			wsio.put(WsIOData.TIME, time);

		}

		/* Check state annotation is defined */
		if (Objects.nonNull(state)) {

			/* Reset current values */
			State.reset();

			/* Add state annotation to the session */
			wsio.put(WsIOData.STATE, state);

		}

		/* Put the data map in the current session map */
		session.put(WsIOData.class.getCanonicalName(), wsio);

	}

	/**
	 * Method used to add the additional info to the output objects.
	 *
	 * @param objects list of objects to add the additionals
	 * @param session map containing the session info
	 */
	public static void out(java.util.List<Object> objects, java.util.Map<String, Object> session) {

		/* Get current wsio data */
		@SuppressWarnings("unchecked")
		java.util.Map<WsIOData, Object> wsio = (java.util.Map<WsIOData, Object>)
				session.get(WsIOData.class.getCanonicalName());

		/* Check wsio and object list are not null, check the size of objects is 1 */
		if (Objects.nonNull(wsio) && Objects.nonNull(objects) && objects.size() == 1) {

			/* Get response object */
			Object response = objects.get(0);

			/* Get time annotation and check if is non null */
			WsIOUseTime useTime = (WsIOUseTime) wsio.get(WsIOData.TIME);
			if (Objects.nonNull(useTime)) {

				/* Check response if instance of ws time */
				if (response instanceof WsIOTime) {

					/* Check end is enabled and add current date time to the additional */
					if (useTime.start()) {
						Time.addDateTimeNow(useTime.endName());
					}

					/* Get time list, create inmmutable list and clear all dates */
					java.util.List<WsIOInstant> timeList = Time.getDateTimes();
					List<WsIOInstant> times = Option.of(timeList)
							.toList()
							.flatMap(e -> e);
					Time.clearDateTimes();

					/* Filter not enabled date times */
					Predicate<String> isStart = str -> StringUtils.equals(str, useTime.startName());
					Predicate<String> isEnd = str -> StringUtils.equals(str, useTime.endName());
					Predicate<String> isOther = Predicates.noneOf(isStart, isEnd);

					/* Enabled predicates */
					List<Predicate<String>> enabled = List.of(
							Option.when(useTime.start(), isStart),
							Option.when(useTime.end(), isEnd),
							Option.when(useTime.others(), isOther)
					).flatMap(e -> e);

					/* Disabled predicates */
					List<Predicate<String>> disabled = List.of(isStart, isEnd, isOther)
							.filter(Predicates.noneOf(enabled::contains))
							.map(Predicate::negate);

					/* Enabled predicates joined with or */
					Predicate<String> initial = Predicates.anyOf(
							WsIOInterceptor.toJavaArray(enabled, Predicate.class));

					/* Initial enabled predicates joined with and to all disabled negated */
					Predicate<String> isValid = Predicates.allOf(
							WsIOInterceptor.toJavaArray(disabled.append(initial), Predicate.class));
					Predicate<WsIOInstant> isValidInstant = instant -> isValid.test(instant.getId());

					/* Add java list date times and transfer info to object */
					Time.setDateTimes(times.filter(isValidInstant).toJavaList());
					Time.transfer((WsIOTime) response);

				}

				/* Reset times */
				Time.reset();

			}

			/* Get time annotation and check if is non null */
			WsIOUseState useState = (WsIOUseState) wsio.get(WsIOData.STATE);
			if (Objects.nonNull(useState)) {

				/* Check response if instance of ws state */
				if (response instanceof WsIOState) {

					/* Get priority languages where programmatic is more important than the annotated */
					Option<WsIOLanguage> programmatic = Option.of(State.getDefaultLanguage());
					Option<WsIOLanguage> annotated = Option.of(useState.language());
					Option<WsIOLanguage> priority = programmatic.orElse(annotated);

					/* Get the language */
					WsIOLanguage language = priority.filter(
							Predicates.noneOf(WsIOLanguage.DEFAULT::equals)).getOrNull();

					/* Bi consumer to assign the language to the elements with default */
					BiConsumer<WsIOElement, Function<WsIOElement, WsIOText>> setDefaultLanguage = (element, function) -> {

						/* Check element is not null and language is set to default */
						if (Objects.nonNull(function.apply(element))
								&& WsIOLanguage.DEFAULT.equals(function.apply(element).getLanguage())) {
							function.apply(element).setLanguage(language);
						}

					};

					/* Set default languages to successfuls, failures and warnings */
					State.getSuccessfuls().forEach(successful -> {
						setDefaultLanguage.accept(successful, WsIOElement::getMessage);
						setDefaultLanguage.accept(successful, WsIOElement::getDescription);
					});
					State.getFailures().forEach(failure -> {
						setDefaultLanguage.accept(failure, WsIOElement::getMessage);
						setDefaultLanguage.accept(failure, WsIOElement::getDescription);
					});
					State.getWarnings().forEach(warning -> {
						setDefaultLanguage.accept(warning, WsIOElement::getMessage);
						setDefaultLanguage.accept(warning, WsIOElement::getDescription);
					});

					/* Set state shows for successfuls, failures and warnings */
					if (Objects.isNull(State.getShowSuccessfuls())) {
						State.setShowSuccessfuls(useState.successful());
					}
					if (Objects.isNull(State.getShowFailures())) {
						State.setShowFailures(useState.failure());
					}
					if (Objects.isNull(State.getShowWarnings())) {
						State.setShowWarnings(useState.warning());
					}

					/* Transfer state to response */
					State.transfer((WsIOState) response);

				}

				/* Reset states */
				State.reset();

			}

		}

	}

	/**
	 * Method to transform vavr list to java array.
	 *
	 * @param list vavr list
	 * @param clazz raw class
	 * @param <R> type argument without generics
	 * @param <G> type argument with generics
	 * @return java array of the list
	 */
	@SuppressWarnings("unchecked")
	private static <R, G extends R> G[] toJavaArray(List<G> list, Class<R> clazz) {

		/* Return the java array */
		return list.toJavaArray((Class<G>) clazz);

	}

}
