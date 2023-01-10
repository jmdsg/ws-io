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
enum WsIOWrapped {

	/** Time wrapper use annotation */
	TIME_WRAPPED,

	/** State wrapper use annotation */
	STATE_WRAPPED,

	/** Inner wrapper use annotation */
	INNER_WRAPPED,

	/** Hide empty wrapper use annotation */
	HIDE_EMPTY_WRAPPED;

	/** Map with each annotation enum with its annotation class */
	static final Map<WsIOWrapped, Class<? extends Annotation>> ANNOTATIONS = HashMap.of(
			WsIOWrapped.TIME_WRAPPED, WsIOUseTime.class,
			WsIOWrapped.STATE_WRAPPED, WsIOUseState.class,
			WsIOWrapped.INNER_WRAPPED, WsIOUseInner.class,
			WsIOWrapped.HIDE_EMPTY_WRAPPED, WsIOUseHideEmpty.class
	);

}
