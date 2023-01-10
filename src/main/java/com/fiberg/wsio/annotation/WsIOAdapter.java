package com.fiberg.wsio.annotation;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotation used to specify the adapter of a parameter.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface WsIOAdapter {

	/** Adapter class to be used by the parameter */
	Class<? extends XmlAdapter<?, ?>> value();

}
