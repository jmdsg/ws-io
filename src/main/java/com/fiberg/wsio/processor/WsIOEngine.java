package com.fiberg.wsio.processor;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import io.vavr.Predicates;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StrSubstitutor;

import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.fiberg.wsio.processor.WsIOConstant.*;
import static java.lang.String.format;

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
	 * @param currentClass name of the current lcass
	 * @param currentPackage name of the current package
	 * @param packageName name of the package
	 * @param packagePath path of the package
	 * @param packagePrefix prefix of the package
	 * @param packageSuffix suffix of the package
	 * @param packageStart start of the package
	 * @param packageMiddle middle of the package
	 * @param packageEnd end of the package
	 * @param packageJs javascript piece to generate package name
	 * @return name of the package obtained after executing js methods
	 */
	static String obtainPackage(String currentMethod, String currentClass, String currentPackage, String packageName,
	                            String packagePath, String packagePrefix, String packageSuffix, String packageStart,
	                            String packageMiddle, String packageEnd, String packageJs) {

		try {

			/* Load the script manager to process js methods */
			WsIOScript.loadUtilScript();

		} catch (ScriptException e) {

			/* Throw when an error processing the script occurs */
			throw new IllegalStateException(format("Could not load javascript functions of %s script",
					WsIOConstant.SCRIPT_UTIL_PATH));

		} catch (IOException e) {

			/* Throw when an io exeception occurs */
			throw new IllegalStateException(format("Could read javascript functions of %s script",
					WsIOConstant.SCRIPT_UTIL_PATH));

		}

		try {

			/* Get the input stream of the script template and transform it to string */
			InputStream inputStream = WsIOProcessor.class.getResourceAsStream(SCRIPT_GENERATOR_PATH);
			String functionTemplate = IOUtils.toString(inputStream, Charsets.UTF_8);

			/* Define the parameters to be used in the template */
			Map<String, String> parameters = ImmutableMap.of(
					SCRIPT_FORMAT_NAME, SCRIPT_FUNCTION_NAME,
					SCRIPT_FORMAT_BODY, packageJs
			);

			/* Replace the parameters in the template and evaluate the js function */
			String function = StrSubstitutor.replace(functionTemplate, parameters);
			WsIOScript.ENGINE.eval(function);

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
			return (String) WsIOScript.INVOCABLE.invokeFunction(SCRIPT_FUNCTION_NAME,
					currentMethod, currentClass, actualPackageName, actualPackagePath, packagePrefix,
					packageSuffix, packageStart, packageMiddle, packageEnd);

		} catch (NoSuchMethodException e) {

			/* Throw when the method does not exists */
			throw new IllegalStateException(
					format("Method %s could not be found", SCRIPT_FUNCTION_NAME), e);

		} catch (IOException e) {

			/* Throw when an io exeception occurs */
			throw new IllegalStateException(
					format("Script template %s could not be found", SCRIPT_GENERATOR_PATH), e);

		} catch (ScriptException e) {

			/* Throw when an error processing the script occurs */
			throw new IllegalArgumentException(
					format("Package js body (%s) could not be parsed correctly", packageJs), e);
		}

	}

}
