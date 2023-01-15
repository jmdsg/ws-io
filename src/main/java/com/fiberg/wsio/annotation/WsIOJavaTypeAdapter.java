package com.fiberg.wsio.annotation;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.lang.annotation.*;

/**
 * <p>Annotation used to specify the adapter of a parameter.</p>
 */
@Repeatable(WsIOJavaTypeAdapters.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
public @interface WsIOJavaTypeAdapter {

	/** Class to be used by the adapter */
	Class<? extends XmlAdapter<?, ?>> value();

	/** Type to be used by the adapter */
	Class<?> type() default XmlJavaTypeAdapter.DEFAULT.class;

	/** Prefix name of the adapter */
	String prefix() default "##default";

	/** Suffix name of the adapter */
	String suffix() default "##default";

}
