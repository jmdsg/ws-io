package com.fiberg.wsio.processor;

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class that loads the script engine manager and evaluates javascript code.
 */
final class WsIOScript {

	/**
	 * Private empty constructor.
	 */
	private WsIOScript() {}

	/** Script engine manager */
	private final static ScriptEngineManager MANAGER = new ScriptEngineManager();

	/** Script engine */
	final static ScriptEngine ENGINE = MANAGER.getEngineByName("JavaScript");

	/** Javascript invicable */
	final static Invocable INVOCABLE = (Invocable) ENGINE;

	/** Flag that indicates is the script engine is loaded or not */
	private static boolean UTIL_LOADED = false;

	/**
	 * Method that loads the input string as the script.
	 *
	 * @param script input string as the script.
	 * @throws ScriptException when an error processing the script code occurs
	 * @throws IOException when a io error occurs
	 */
	private static void loadScript(String script) throws ScriptException, IOException {
		InputStream inputStream = WsIOScript.class.getResourceAsStream(script);
		ENGINE.eval(IOUtils.toString(inputStream, Charsets.UTF_8));
	}

	/**
	 * Method that loads the util script from the path.
	 *
	 * @throws ScriptException when an error processing the script code occurs
	 * @throws IOException when an error reading script occurs
	 */
	static void loadUtilScript() throws ScriptException, IOException {
		if (!UTIL_LOADED) {
			WsIOScript.loadScript(WsIOConstant.SCRIPT_UTIL_PATH);
			UTIL_LOADED = true;
		}
	}

}
