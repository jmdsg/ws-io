package com.fiberg.wsio.processor;

import com.fiberg.wsio.annotation.WsIOUseHideEmpty;
import com.fiberg.wsio.annotation.WsIOUseInner;
import com.fiberg.wsio.annotation.WsIOUseState;
import com.fiberg.wsio.annotation.WsIOUseTime;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

import java.lang.annotation.Annotation;

/**
 * Enum containing all possible wrapper use annotations.
 */
enum WsIOWrapper {

	/** Time wrapper use annotation */
	TIME_WRAPPER,

	/** State wrapper use annotation */
	STATE_WRAPPER,

	/** Inner wrapper use annotation */
	INNER_WRAPPER,

	/** Hide empty wrapper use annotation */
	HIDE_EMPTY_WRAPPER;

	/** Map with each annotation enum with its annotation class */
	static final Map<WsIOWrapper, Class<? extends Annotation>> ANNOTATIONS = HashMap.of(
			WsIOWrapper.TIME_WRAPPER, WsIOUseTime.class,
			WsIOWrapper.STATE_WRAPPER, WsIOUseState.class,
			WsIOWrapper.INNER_WRAPPER, WsIOUseInner.class,
			WsIOWrapper.HIDE_EMPTY_WRAPPER, WsIOUseHideEmpty.class
	);

}
