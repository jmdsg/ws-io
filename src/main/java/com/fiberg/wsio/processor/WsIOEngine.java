package com.fiberg.wsio.processor;

import groovy.lang.GroovyShell;
import io.vavr.Predicates;
import io.vavr.collection.List;
import io.vavr.control.Option;

/**
 * Class that handle the execution of js function to obtain package names.
 */
final class WsIOEngine {

	/**
	 * Private empty constructor.
	 */
	private WsIOEngine() {  }

	/**
	 * Method that obtains the name of a package processing the js functions.
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
	 * @param packageFunc groovy function with the body to execute
	 * @return name of the package obtained after executing js methods
	 */
	static String obtainPackage(final String currentMethod, final String currentClass, final String currentPackage,
								final String packageName, final String packagePath, final String packagePrefix,
								final String packageSuffix, final String packageStart, final String packageMiddle,
								final String packageEnd, final String packageFunc) {

		/* Split package chunks and obtain name and path */
		final List<String> packageChunks = List.of(currentPackage.split("\\."));
		final String currentPackageName = packageChunks.last();
		final String currentPackagePath = packageChunks.dropRight(1)
				.mkString(".");

		/* Get actual package name */
		final String actualPackageName = Option.of(packageName)
				.filter(Predicates.noneOf("##default"::equals))
				.getOrElse(currentPackageName);

		/* Get actual package path */
		final String actualPackagePath = Option.of(packagePath)
				.filter(Predicates.noneOf("##default"::equals))
				.getOrElse(currentPackagePath);

		final GroovyShell shell = WsIOScript.createShell(
				currentMethod, currentClass, currentPackage, actualPackageName, actualPackagePath,
				packagePrefix, packageSuffix, packageStart, packageMiddle, packageEnd
		);

		return (String) shell.evaluate(packageFunc);

	}

}
