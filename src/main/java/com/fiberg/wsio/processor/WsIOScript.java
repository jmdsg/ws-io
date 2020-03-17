package com.fiberg.wsio.processor;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

/**
 * Class that loads the script engine manager and evaluates javascript code.
 */
final class WsIOScript {

	/**
	 * Private empty constructor.
	 */
	private WsIOScript() {}

	private static final GroovyShell GROOVY_SHELL = WsIOScript.createShell();

	/**
	 * Method that creates the groovy shell with the specified variables.
	 *
	 * @param currentMethod name of the method
	 * @param currentClass name of the current class
	 * @param currentPackage name of the current package
	 * @param packageName name of the package
	 * @param packagePath path of the package
	 * @param packagePrefix prefix of the package
	 * @param packageSuffix suffix of the package
	 * @param packageStart start of the package
	 * @param packageMiddle middle of the package
	 * @param packageEnd end of the package
	 * @return the groovy shell with the variables set
	 */
	public static String evaluate(final String currentMethod, final String currentClass,
								  final String currentPackage, final String packageName,
								  final String packagePath, final String packagePrefix,
								  final String packageSuffix, final String packageStart,
								  final String packageMiddle, final String packageEnd,
								  final String packageFunc) {

		synchronized (GROOVY_SHELL) {

			GROOVY_SHELL.setVariable("currentMethod", currentMethod);
			GROOVY_SHELL.setVariable("currentClass", currentClass);
			GROOVY_SHELL.setVariable("currentPackage", currentPackage);
			GROOVY_SHELL.setVariable("packageName", packageName);
			GROOVY_SHELL.setVariable("packagePath", packagePath);
			GROOVY_SHELL.setVariable("packagePrefix", packagePrefix);
			GROOVY_SHELL.setVariable("packageSuffix", packageSuffix);
			GROOVY_SHELL.setVariable("packageStart", packageStart);
			GROOVY_SHELL.setVariable("packageMiddle", packageMiddle);
			GROOVY_SHELL.setVariable("packageEnd", packageEnd);

			return (String) GROOVY_SHELL.evaluate(packageFunc);

		}

	}

	/**
	 * Method that creates the groovy shell with the specified variables.
	 *
	 * @return shell ready to execute the groovy function with an evaluate
	 */
	private static GroovyShell createShell() {

		final Binding binding = new Binding();

		final GroovyShell shell = new GroovyShell(binding);

		final String join = "join = {" +
				"list -> list.findAll { str -> str != null }" +
						".collect { str -> str.replaceAll(/(^\\.+)|(\\.+$)/, '') }" +
						".findAll { str -> str != '' }" +
						".join('.')" +
		"}";

		final String forward = "forward = {" +
				"path, elements -> path.tokenize('.')" +
						".take(elements)" +
						".inject('') { acc, cur -> join([acc, cur]) }" +
		"}";

		final String backward = "backward = {" +
				"path, elements -> path.tokenize('.')" +
						".reverse()" +
						".drop(elements)" +
						".reverse()" +
						".inject('') { acc, cur -> join([acc, cur]) }" +
		"}";

		shell.parse(join).run();
		shell.parse(forward).run();
		shell.parse(backward).run();

		return shell;

	}

}
