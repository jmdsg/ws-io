package com.fiberg.wsio.annotation;

/**
 * <p>Enum describing the type of skip.</p>
 */
public enum SkipType {

	/** Skip this element and all its childs */
	ALL,

	/** Skip this elements */
	PARENT,

	/** Skip all childs of this element, but not the element itself */
	CHILDS

}
