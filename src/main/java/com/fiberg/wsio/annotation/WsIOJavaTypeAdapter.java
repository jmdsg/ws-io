package com.fiberg.wsio.annotation;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotation used to specify the adapter of a parameter.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface WsIOJavaTypeAdapter {

	/** Class to be used by the adapter */
	Class<? extends XmlAdapter<?, ?>> value();

	/** Type to be used by the adapter */
	Class<?> type() default XmlJavaTypeAdapter.DEFAULT.class;

}
