package com.fiberg.wsio.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to skip generation of the classes annotated with annotation {@link WsIOMessage}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface WsIOSkipMessage {

	/** Indicates the elements to be skipped */
	SkipType skip() default SkipType.ALL;

}
