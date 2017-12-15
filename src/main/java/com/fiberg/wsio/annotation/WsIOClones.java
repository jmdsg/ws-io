package com.fiberg.wsio.annotation;

import java.lang.annotation.*;

/**
 * <p>Annotation used to allow multiple clone annotations {@link WsIOClone}.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PACKAGE, ElementType.TYPE })
public @interface WsIOClones {

	WsIOClone[] value();

}
