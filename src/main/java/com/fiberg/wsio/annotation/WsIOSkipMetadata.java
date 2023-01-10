package com.fiberg.wsio.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to skip metadata generation {@link WsIOMetadata}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface WsIOSkipMetadata {

	/** Indicates the elements to be skipped */
	SkipType skip() default SkipType.ALL;

}
