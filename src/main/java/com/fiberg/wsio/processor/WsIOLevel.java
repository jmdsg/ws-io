package com.fiberg.wsio.processor;

/**
 * Enum containing the possible levels of inheritance.
 */
enum WsIOLevel {

	/** None, used for default  */
	NONE,

	/** Target class of the clone or message class  */
	LOCAL,

	/** Interface that is internal */
	INTERFACE_INTERNAL,

	/** Interface that is external */
	INTERFACE_EXTERNAL,

	/** Class that is internal */
	CLASS_INTERNAL,

	/** Class that is external */
	CLASS_EXTERNAL

}
