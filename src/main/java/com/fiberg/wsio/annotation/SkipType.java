package com.fiberg.wsio.annotation;

/**
 * <p>Enum describing the type of skip.</p>
 */
public enum SkipType {

	/** Skip this element and all its children */
	ALL,

	/** Skip this elements */
	CURRENT,

	/** Skip all children of this element, but not the element itself */
	CHILDREN,

}
