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
	 * @return shell ready to execute the groovy function with an evaluate
	 */
	public static GroovyShell createShell(final String currentMethod, final String currentClass,
										  final String currentPackage, final String packageName,
										  final String packagePath, final String packagePrefix,
										  final String packageSuffix, final String packageStart,
										  final String packageMiddle, final String packageEnd) {

		final Binding binding = new Binding();

		binding.setVariable("currentMethod", currentMethod);
		binding.setVariable("currentClass", currentClass);
		binding.setVariable("currentPackage", currentPackage);
		binding.setVariable("packageName", packageName);
		binding.setVariable("packagePath", packagePath);
		binding.setVariable("packagePrefix", packagePrefix);
		binding.setVariable("packageSuffix", packageSuffix);
		binding.setVariable("packageStart", packageStart);
		binding.setVariable("packageMiddle", packageMiddle);
		binding.setVariable("packageEnd", packageEnd);

		final GroovyShell shell = new GroovyShell(binding);

		final String join = "join = {" +
				"list -> list.collect { str -> str.replaceAll(/(^\\.+)|(\\.+$)/, '') }" +
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
