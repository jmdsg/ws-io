package com.fiberg.wsio.processor;

import groovy.lang.GroovyShell;

public class Test {
    public static void main(String[] args) {
        final GroovyShell shell = WsIOScript.createShell(
                "a", "B", "c",
                "d", null, "f",
                "g", "h", "i", "j"
        );
        final String packageFunc = "join([" +
                "packageStart, " +
                "packagePath, " +
                "packageMiddle, " +
                "packagePrefix + packageName + packageSuffix, " +
                "packageEnd" +
        "])";
        System.out.println(shell.evaluate(packageFunc));
    }
}
