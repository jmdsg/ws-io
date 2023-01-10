package com.fiberg.wsio.annotation;

import com.fiberg.wsio.processor.WsIOConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotation to generate classes with prefix 'Request' and suffix 'Wrapper'.</p>
 *
 * <p>Classes are generated for each interface marked with {@link jakarta.jws.WebService}.</p>
 * <p>A class time is generated for each method containing {@link jakarta.jws.WebMethod} and only if the method contain parameters.</p>
 *
 * <p>Can be skipped in a class with {@link WsIOSkipMessageWrapper}.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD })
public @interface WsIOMessageWrapper {

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
	 *     <li>currentClass</li>
	 *     <li>currentPackage</li>
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
	 * <pre>
	 * join([
	 *     packageStart,
	 *     packagePath,
	 *     packageMiddle,
	 *     packagePrefix + packageName + packageSuffix,
	 *     currentClass.toLowerCase(),
	 *     packageEnd
	 * ])
	 * </pre>
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
			"currentClass.toLowerCase(), " +
			"packageEnd" +
	"])";

}
