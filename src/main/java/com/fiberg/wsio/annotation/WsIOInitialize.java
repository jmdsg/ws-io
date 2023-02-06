package com.fiberg.wsio.annotation;

import com.fiberg.wsio.enumerate.WsIOType;

import java.lang.annotation.*;

/**
 * <p>Annotation used to specify if a parameter should be initialized.</p>
 */
@Repeatable(WsIOInitializes.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
public @interface WsIOInitialize {

	/** Prefix name of the transient */
	String prefix() default "##default";

	/** Suffix name of the transient */
	String suffix() default "##default";

	/** Target value of the transient */
	WsIOType target() default WsIOType.BOTH;

}
