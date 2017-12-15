package com.fiberg.wsio.annotation;

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
	String packageEnd() default "";

	/**
	 * <p>Function in javascript to generate the package name, if none is specified the default behavier is used.</p><br>
	 *
	 * <p>The function used is the following and the variable <code>packageJs</code> is used in the return of the function.</p><br>
	 *
	 * <pre>{@code
	 * var generatePackageName = function (currentMethod, className, packageName, packagePath, packagePrefix, packageSuffix, packageStart, packageMiddle, packageEnd) {
	 *     return ${packageJs};
	 * };
	 * }</pre>
	 *
	 * <p>Additionally a function called 'join' is provided to join multiple names with dot '.' sign,
	 * a function called 'backward' to get back one level in the package <br>
	 * and a function called 'forward' which starts at the begining of the package name.</p>
	 *
	 * <p>The default value is:</p>
	 * <pre>join(packageStart, packagePath, packageMiddle, packagePrefix + packageName + packageSuffix, packageEnd)</pre>
	 *
	 * <p>The definitions of the functions are the following:</p>
	 *
	 * <pre>{@code
	 * var join = function() {
	 *     var name = '';
	 *     for(var i = 0; i < arguments.length; i++) {
	 *         if (arguments[i] !== null) {
	 *             var next = arguments[i].replace(/^\.+/, '').replace(/\.+$/, '');
	 *             if (next !== '') {
	 *                 if (name !== '') {
	 *                     name += '.';
	 *                 }
	 *                 name += next;
	 *             }
	 *         }
	 *     }
	 *     return name;
	 * };
	 * }</pre>
	 *
	 * <pre>{@code
	 * var backward = function(packageName, lvl) {
	 *     lvl = (typeof lvl !== 'undefined') ? lvl : 1;
	 *     var finalName = packageName;
	 *     for (var i = 0; i < lvl; i++) {
	 *         if (packageName.lastIndexOf('.') >= 0) {
	 *             finalName = finalName.substring(0, finalName.lastIndexOf('.'));
	 *         }
	 *     }
	 *     return finalName;
	 * };
	 * }</pre>
	 *
	 * <pre>{@code
	 * var forward = function(packageName, lvl) {
	 *     lvl = (typeof lvl !== 'undefined') ? lvl : 1;
	 *     return backward(packageName, packageName.split(".").length - lvl);
	 * };
	 * }</pre>
	 *
	 * */
	String packageJs() default "join(" +
			"packageStart, " +
			"packagePath, " +
			"packageMiddle, " +
			"packagePrefix + packageName + packageSuffix, " +
			"packageEnd)";

}
