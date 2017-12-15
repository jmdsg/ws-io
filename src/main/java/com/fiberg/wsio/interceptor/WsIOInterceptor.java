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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class WsIOInterceptor {

	private WsIOInterceptor() {}

	public static void in(Class<?> endpoint, Method method, Map<String, Object> session) {

		Map<String, Object> wsio = new HashMap<>();

		wsio.put("method", method);
		wsio.put("endpoint", endpoint);

		WsIOUseTime classTime = endpoint.getAnnotation(WsIOUseTime.class);
		WsIOUseTime methodTime = method.getAnnotation(WsIOUseTime.class);
		WsIOUseTime timeWrapper = methodTime != null ? methodTime : classTime;

		WsIOUseState classState = endpoint.getAnnotation(WsIOUseState.class);
		WsIOUseState methodState = method.getAnnotation(WsIOUseState.class);
		WsIOUseState stateWrapper = methodState != null ? methodState : classState;

		if (timeWrapper != null) {
			Time.reset();
			if (timeWrapper.start()) {
				Time.addDateTimeNow("start");
			}
			wsio.put("classtime", classTime);
			wsio.put("methodtime", methodTime);
			wsio.put("time", timeWrapper);
		}

		if (stateWrapper != null) {
			State.reset();
			wsio.put("classstate", classState);
			wsio.put("methodstate", methodState);
			wsio.put("state", stateWrapper);
		}

		session.put("wsiodata", wsio);

	}

	public static void out(List<Object> objects, Map<String, Object> session) {

		@SuppressWarnings("unchecked")
		Map<String, Object> wsio = (Map<String, Object>) session.get("wsiodata");

		if (wsio != null && objects != null && objects.size() == 1) {
			Object response = objects.get(0);
			WsIOUseTime timeWrapper = (WsIOUseTime) wsio.get("time");
			if (timeWrapper != null) {
				if (!timeWrapper.middle()) {
					if (timeWrapper.start() && Time.getDateTimes().size() > 0) {
						WsIOInstant start = Time.getDateTimes().get(0);
						Time.clearDateTimes();
						Time.addDateTime(start);
					} else {
						Time.clearDateTimes();
					}
				}
				if (timeWrapper.end()) {
					Time.addDateTimeNow("end");
				}
				if (response != null && response instanceof WsIOTime) {
					Time.transfer((WsIOTime) response);
					Time.reset();
				}
			}
			WsIOUseState stateWrapper = (WsIOUseState) wsio.get("handler");
			if (stateWrapper != null) {
				if (response != null && response instanceof WsIOTime) {
					WsIOState state = (WsIOState) response;
					WsIOLanguage language = !WsIOLanguage.DEFAULT.equals(State.getDefaultLanguage())
							? (State.getDefaultLanguage())
							: (!WsIOLanguage.DEFAULT.equals(stateWrapper.language())
									? (stateWrapper.language())
									: (null));

					BiConsumer<WsIOElement, Function<WsIOElement, WsIOText>> setDefaultLanguage = (element, function) -> {
						if (function.apply(element) != null
								&& WsIOLanguage.DEFAULT.equals(function.apply(element).getLanguage())) {
							function.apply(element).setLanguage(language);
						}
					};
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

					if (State.getShowSuccessfuls() == null) {
						State.setShowSuccessfuls(stateWrapper.successful());
					}
					if (State.getShowFailures() == null) {
						State.setShowFailures(stateWrapper.failure());
					}
					if (State.getShowWarnings() == null) {
						State.setShowWarnings(stateWrapper.warning());
					}

					State.transfer(state);
					State.reset();
				}
			}
		}

	}

}
