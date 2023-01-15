package com.fiberg.wsio.annotation;

import java.lang.annotation.*;

/**
 * <p>Annotation used to allow multiple clone annotations {@link WsIOAttribute}.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
public @interface WsIOAttributes {

	WsIOAttribute[] value();

}
