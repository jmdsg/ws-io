package com.fiberg.wsio.processor;

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;

final class WsIOScript {

	private WsIOScript() {}

	private final static ScriptEngineManager MANAGER = new ScriptEngineManager();

	final static ScriptEngine ENGINE = MANAGER.getEngineByName("JavaScript");

	final static Invocable INVOCABLE = (Invocable) ENGINE;

	private static boolean UTIL_LOADED = false;

	private static void loadScript(String script) throws ScriptException, IOException {
		InputStream inputStream = WsIOScript.class.getResourceAsStream(script);
		ENGINE.eval(IOUtils.toString(inputStream, Charsets.UTF_8));
	}

	static void loadUtilScript() throws ScriptException, IOException {
		if (!UTIL_LOADED) {
			WsIOScript.loadScript(WsIOConstant.SCRIPT_UTIL_PATH);
			UTIL_LOADED = true;
		}
	}

}
