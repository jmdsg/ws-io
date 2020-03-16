package com.fiberg.wsio.processor;

import com.fiberg.wsio.namer.WsIONamer;
import io.vavr.Predicates;
import io.vavr.collection.List;
import io.vavr.control.Option;

import java.lang.reflect.InvocationTargetException;

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
	 * @param packageNamer instantiable class that generates the package name
	 * @return name of the package obtained after executing js methods
	 */
	static String obtainPackage(String currentMethod, String currentClass, String currentPackage, String packageName,
	                            String packagePath, String packagePrefix, String packageSuffix, String packageStart,
	                            String packageMiddle, String packageEnd, Class<? extends WsIONamer> packageNamer) {

		try {

			/* Instance of the ws io namer */
			final WsIONamer wsIONamer = packageNamer.getConstructor()
					.newInstance();

			/* Split package chunks and obtain name and path */
			List<String> packageChunks = List.of(currentPackage.split("\\."));
			String currentPackageName = packageChunks.last();
			String currentPackagePath = packageChunks.dropRight(1)
					.mkString(".");

			/* Get actual package name */
			String actualPackageName = Option.of(packageName)
					.filter(Predicates.noneOf("##default"::equals))
					.getOrElse(currentPackageName);

			/* Get actual package path */
			String actualPackagePath = Option.of(packagePath)
					.filter(Predicates.noneOf("##default"::equals))
					.getOrElse(currentPackagePath);

			/* Invoke the function and return the result */
			return wsIONamer.generate(
					currentMethod, currentClass, actualPackageName, actualPackagePath,
					packagePrefix, packageSuffix, packageStart, packageMiddle, packageEnd
			);

		} catch (final InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {

			/* Throw when the class could not be instantiated */
			throw new IllegalStateException(
					String.format("The namer class that implements %s could not be instantiated", WsIONamer.class.getName()), e
			);

		}

	}

}
