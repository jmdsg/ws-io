package com.fiberg.wsio.interceptor;

import com.fiberg.wsio.annotation.WsIOUseState;
import com.fiberg.wsio.annotation.WsIOUseTime;
import com.fiberg.wsio.handler.WsIOHandler.State;
import com.fiberg.wsio.handler.WsIOHandler.Time;
import com.fiberg.wsio.handler.state.WsIOItem;
import com.fiberg.wsio.handler.state.WsIOLanguage;
import com.fiberg.wsio.handler.state.WsIOState;
import com.fiberg.wsio.handler.state.WsIOText;
import com.fiberg.wsio.handler.time.WsIOInstant;
import com.fiberg.wsio.handler.time.WsIOTime;
import com.fiberg.wsio.processor.WsIODescriptor;
import io.vavr.Predicates;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

/**
 * Class used to intercept the petitions and responses and add the additional.
 */
public final class WsIOInterceptor {

	/** Start name */
	public static final String START = "start";

	/** End name */
	public static final String END = "end";

	/**
	 * Private empty constructor.
	 */
	private WsIOInterceptor() {  }

	/**
	 * In interceptor method, used to add the initial info for the out interceptor.
	 * This method also resets the current values for additional and add the start date when is enabled.
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

		/* Get descriptor to extract time and state annotations with priority */
		WsIODescriptor descriptor = WsIODescriptor.of(method);
		WsIOUseTime time = descriptor.getSingle(WsIOUseTime.class).getOrNull();
		WsIOUseState state = descriptor.getSingle(WsIOUseState.class).getOrNull();

		/* Reset current values */
		Time.reset();
		State.reset();

		/* Add date time start */
		Time.addDateTimeNow(START);

		/* Add time and state annotation to the session */
		wsio.put(WsIOData.TIME, time);
		wsio.put(WsIOData.STATE, state);

		/* Set time and state enabled or disabled */
		Time.setEnabled(Objects.nonNull(time));
		State.setEnabled(Objects.nonNull(state));

		/* Put the data map in the current session map */
		session.put(WsIOData.class.getCanonicalName(), wsio);

	}

	/**
	 * Method used to add the additional info to the output objects.
	 *
	 * @param objects list of objects to add the additional
	 * @param session map containing the session info
	 */
	public static void out(java.util.List<Object> objects, java.util.Map<String, Object> session) {

		/* Get current ws-io data */
		@SuppressWarnings({ "unchecked" })
		java.util.Map<WsIOData, Object> wsIO = (java.util.Map<WsIOData, Object>)
				session.get(WsIOData.class.getCanonicalName());

		/* Check ws-io and object list are not null, check the size of objects is 1 */
		if (Objects.nonNull(wsIO) && Objects.nonNull(objects) && objects.size() == 1) {

			/* Get response object */
			Object response = objects.get(0);

			/* Get time annotation and check if is non-null */
			WsIOUseTime useTime = (WsIOUseTime) wsIO.get(WsIOData.TIME);
			if (Objects.nonNull(useTime)) {

				/* Check response if instance of ws time */
				if (response instanceof WsIOTime) {

					/* Check end is enabled and add current date time to the additional */
					if (useTime.start()) {
						Time.addDateTimeNow(END);
					}

					/* Get time list, create immutable list and clear all dates */
					java.util.List<WsIOInstant> timeList = Time.getTimes();
					List<WsIOInstant> times = Option.of(timeList)
							.toList()
							.flatMap(e -> e);
					Time.clearTimes();

					/* Filter not enabled date times */
					Predicate<String> isStart = str -> StringUtils.equals(str, START);
					Predicate<String> isEnd = str -> StringUtils.equals(str, END);
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
					Time.setTimes(times.filter(isValidInstant).toJavaList());
					Time.transfer((WsIOTime) response);

				}

			}

			/* Get time annotation and check if is non-null */
			WsIOUseState useState = (WsIOUseState) wsIO.get(WsIOData.STATE);
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
					BiConsumer<WsIOItem, Function<WsIOItem, WsIOText>> setDefaultLanguage = (element, function) -> {

						/* Check element is not null and language is set to default */
						if (Objects.nonNull(function.apply(element))
								&& WsIOLanguage.DEFAULT.equals(function.apply(element).getLanguage())) {
							function.apply(element).setLanguage(language);
						}

					};

					/* Set default languages to successfuls, failures and warnings */
					if (Objects.nonNull(State.getSuccessfuls())) {
						State.getSuccessfuls().forEach(successful -> {
							setDefaultLanguage.accept(successful, WsIOItem::getMessage);
							setDefaultLanguage.accept(successful, WsIOItem::getDescription);
						});
					}
					if (Objects.nonNull(State.getFailures())) {
						State.getFailures().forEach(failure -> {
							setDefaultLanguage.accept(failure, WsIOItem::getMessage);
							setDefaultLanguage.accept(failure, WsIOItem::getDescription);
						});
					}
					if (Objects.nonNull(State.getWarnings())) {
						State.getWarnings().forEach(warning -> {
							setDefaultLanguage.accept(warning, WsIOItem::getMessage);
							setDefaultLanguage.accept(warning, WsIOItem::getDescription);
						});
					}

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

			}

			/* Reset current values */
			Time.reset();
			State.reset();

		}

	}

	/**
	 * Method to transform vavr list to java array.
	 *
	 * @param list vavr list
	 * @param type raw class
	 * @param <R> type argument without generics
	 * @param <G> type argument with generics
	 * @return java array of the list
	 */
	@SuppressWarnings({ "unchecked" })
	private static <R, G extends R> G[] toJavaArray(List<G> list, Class<R> type) {

		Class<G> target = (Class<G>) type;
		IntFunction<G[]> creator = length -> (G[]) Array.newInstance(target, length);

		/* Return the java array */
		return list.toJavaArray(creator);

	}

}
