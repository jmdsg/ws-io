package com.fiberg.wsio.annotation;

import com.fiberg.wsio.processor.WsIOConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotation to generate classes with prefix '' and suffix 'Request'.</p>
 *
 * <p>Can be used in a package to mark all classes within the package or in the class to generate the class and all the inners.</p>
 * <p>Interfaces, classes and enums are generated, they must be public and in the case of classes they must have an empty constructor.</p>
 *
 * <p>Can be skipped in a class with {@link WsIOSkipMessage}.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PACKAGE, ElementType.TYPE })
public @interface WsIOMessage {

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
	 * <p>Function in groovy to generate the package name, if none is specified the default behavior is used.</p><br>
	 *
	 * <p>The function used is the evaluation of the variable <code>packageFunc</code> and it can use the following parameters:</p><br>
	 * <ul>
	 *     <li>currentMethod</li>
	 *     <li>className</li>
	 *     <li>packageName</li>
	 *     <li>packagePath</li>
	 *     <li>packagePrefix</li>
	 *     <li>packageSuffix</li>
	 *     <li>packageStart</li>
	 *     <li>packageMiddle</li>
	 *     <li>packageEnd</li>
	 * </ul>
	 *
	 * <p>Additionally a function called 'join' is provided to join multiple names (in a list) with dot '.' sign,
	 * a function called 'backward' to get back one level in the package <br>
	 * and a function called 'forward' which starts at the beginning of the package name.</p>
	 *
	 * <p>The default value is:</p>
	 * <pre>join([packageStart, packagePath, packageMiddle, packagePrefix + packageName + packageSuffix, packageEnd])</pre>
	 *
	 * <p>The definitions of the functions are the following:</p>
	 *
	 * <pre>{@code
	 *     join = {
	 *         list -> list.findAll { str -> str != null }
	 *             .collect { str -> str.replaceAll(/(^\\.+)|(\\.+$)/, '') }
	 *             .findAll { str -> str != '' }
	 *             .join('.')
	 *     }
	 * }</pre>
	 *
	 * <pre>{@code
	 *     forward = {
	 *         path, elements -> path.tokenize('.')
	 *             .take(elements)
	 *             .inject('') { acc, cur -> concat([acc, cur]) }
	 *     }
	 * }</pre>
	 *
	 * <pre>{@code
	 *     backward = {
	 *         path, elements -> path.tokenize('.')
	 *             .reverse()
	 *             .drop(elements)
	 *             .reverse()
	 *             .inject('') { acc, cur -> concat([acc, cur]) }
	 *     }
	 * }</pre>
	 *
	 * */
	String packageFunc() default "join([" +
			"packageStart, " +
			"packagePath, " +
			"packageMiddle, " +
			"packagePrefix + packageName + packageSuffix, " +
			"packageEnd" +
	"])";

}
