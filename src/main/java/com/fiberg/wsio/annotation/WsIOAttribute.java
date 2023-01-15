package com.fiberg.wsio.annotation;

import java.lang.annotation.*;

/**
 * <p>Annotation used to specify if a parameter is an attribute.</p>
 */
@Repeatable(WsIOAttributes.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
public @interface WsIOAttribute {

    /** Name of the attribute */
    String name() default "##default";

    /** Required of the attribute */
    boolean required() default false;

    /** Namespace of the attribute */
    String namespace() default "##default";

    /** Prefix name of the attribute */
    String prefix() default "##default";

    /** Suffix name of the attribute */
    String suffix() default "##default";

}
