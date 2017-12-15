package com.fiberg.wsio.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotation to skip generation of the classes annotated with annotation {@link WsIOMessageWrapper}.</p>
 *
 * <p>This annotation can be used to skip the generation of classes in web methods.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface WsIOSkipMessageWrapper {

	/** Indicates the elements to be skipped */
	SkipType skip() default SkipType.ALL;

}
