package com.fiberg.wsio.annotation;

import com.fiberg.wsio.namer.WsIONamer;
import com.fiberg.wsio.processor.WsIOConstant;

import java.lang.annotation.*;

/**
 * <p>Annotation used to generate the metadata for a class.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PACKAGE, ElementType.TYPE })
public @interface WsIOMetadata {

	/** Package name to replace the old, if not specified old package name is used. */
	String packageName() default "##default";

	/** Package path to replace the old, if not specified old package path is used. */
	String packagePath() default "##default";

	/** Package prefix to be appended to the package name */
	String packagePrefix() default "";

	/** Package suffix to be appended to the package name */
	String packageSuffix() default "";

	/** Package start to be appended in the start package path, it does not need to end with dot '.' */
	String packageStart() default "";

	/** Package middle to be appended before package name, it does not need to start or end with dot '.' */
	String packageMiddle() default "";

	/** Package end to be appended after package name, it does not need to start with dot '.' */
	String packageEnd() default WsIOConstant.PACKAGE_END;

	/**
	 * <p>Class that must have public without argument constructor.</p><br>
	 * <p>The class passed must implement the {@link com.fiberg.wsio.namer.WsIONamer} interface.</p>
	 *
	 * <p>The default behavior is:</p>
	 * <pre>join(packageStart, packagePath, packageMiddle, packagePrefix + packageName + packageSuffix, packageEnd)</pre>
	 *
	 * <p>Where <pre>join(...)</pre> performs a join with the '.' character.</p>
	 *
	 * */
	Class<? extends WsIONamer> packageNamer() default WsIONamer.Default.class;

	/** Cases to generate the fields */
	Case[] cases() default { Case.UPPER };

	/** Indicates if the metadata for static fields show be generated */
	boolean staticFields() default false;

}
