package com.fiberg.wsio.annotation;

import com.fiberg.wsio.handler.state.WsIOLanguage;
import com.fiberg.wsio.handler.time.WsIOTime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotation generate a response with start and end time extending the class {@link WsIOTime}.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD })
public @interface WsIOUseState {

	boolean successful() default true;

	boolean failure() default true;

	boolean warning() default true;

	WsIOLanguage language() default WsIOLanguage.DEFAULT;

}
