package com.fiberg.wsio.annotation;

import com.fiberg.wsio.handler.time.WsIOTime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotation to generate a response with start, end and others set times extending the class {@link WsIOTime}.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD })
public @interface WsIOUseTime {

	boolean start() default true;

	boolean others() default true;

	boolean end() default true;

}
