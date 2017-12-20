package com.fiberg.wsio.annotation;

import com.fiberg.wsio.auto.WsIOAuto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotation used to auto annotate webservice classes.</p>
 *
 * </p>The class {@link WsIOAuto} must be called before jaxb generates schema and wsdl.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD })
public @interface WsIOAnnotate {

	/** Indicates that web param and web result will be renamed */
	boolean nameSwap() default true;

}
