package com.fiberg.wsio.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotation used to specify if a parameter is an attribute.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
public @interface WsIOAttribute {

    /** Name of the attribute */
    String name() default "##default";

    /** Required of the attribute */
    boolean required() default false;

    /** Namespace of the attribute */
    String namespace() default "##default";

}
