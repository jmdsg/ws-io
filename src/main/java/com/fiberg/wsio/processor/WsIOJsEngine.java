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

public final class WsIOJsEngine {

	private WsIOJsEngine() {}

	public static String obtainPackage(String currentMethod, String currentClass, String currentPackage, String packageName,
	                                   String packagePath, String packagePrefix, String packageSuffix, String packageStart,
	                                   String packageMiddle, String packageEnd, String packageJs) {

		try {
			WsIOScript.loadUtilScript();
		} catch (ScriptException e) {
			throw new IllegalStateException(format("Could not load javascript functions of %s script",
					WsIOConstant.SCRIPT_UTIL_PATH));
		} catch (IOException e) {
			throw new IllegalStateException(format("Could read javascript functions of %s script",
					WsIOConstant.SCRIPT_UTIL_PATH));
		}

		try {

			InputStream inputStream = WsIOProcessor.class.getResourceAsStream(SCRIPT_GENERATOR_PATH);
			String functionTemplate = IOUtils.toString(inputStream, Charsets.UTF_8);
			Map<String, String> parameters = ImmutableMap.of(
					SCRIPT_FORMAT_NAME, SCRIPT_FUNCTION_NAME,
					SCRIPT_FORMAT_BODY, packageJs
			);
			String function = StrSubstitutor.replace(functionTemplate, parameters);
			WsIOScript.ENGINE.eval(function);

			List<String> packageChunks = List.of(currentPackage.split("\\."));
			String currentPackageName = packageChunks.last();
			String currentPackagePath = packageChunks.dropRight(1)
					.mkString(".");

			String actualPackageName = Option.of(packageName)
					.filter(Predicates.noneOf("##default"::equals))
					.getOrElse(currentPackageName);

			String actualPackagePath = Option.of(packagePath)
					.filter(Predicates.noneOf("##default"::equals))
					.getOrElse(currentPackagePath);

			return (String) WsIOScript.INVOCABLE.invokeFunction(SCRIPT_FUNCTION_NAME,
					currentMethod, currentClass, actualPackageName, actualPackagePath, packagePrefix,
					packageSuffix, packageStart, packageMiddle, packageEnd);

		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(
					format("Method %s could not be found", SCRIPT_FUNCTION_NAME), e);
		} catch (IOException e) {
			throw new IllegalStateException(
					format("Script template %s could not be found", SCRIPT_GENERATOR_PATH), e);
		} catch (ScriptException e) {
			throw new IllegalArgumentException(
					format("Package js body (%s) could not be parsed correctly", packageJs), e);
		}

	}

}
