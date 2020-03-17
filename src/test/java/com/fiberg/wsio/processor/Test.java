package com.fiberg.wsio.processor;

import groovy.lang.*;

public class Test {
    public static void main(String[] args) {
        final GroovyShell shell = WsIOScript.createShell(
                "a", "B", "c",
                "d", "e", "f",
                "g", "h", "i", "j"
        );
        final String packageFunc = "join([" +
                "packageStart, " +
                "packagePath, " +
                "packageMiddle, " +
                "packagePrefix + packageName + packageSuffix, " +
                "packageEnd" +
        "])";
        assert "h.e.i.fdg.j".equals(shell.evaluate(packageFunc));
    }
}
