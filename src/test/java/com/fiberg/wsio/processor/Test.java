package com.fiberg.wsio.processor;

public class Test {
    public static void main(String[] args) {

        final String packageFunc = "join([" +
                "packageStart, " +
                "packagePath, " +
                "packageMiddle, " +
                "packagePrefix + packageName + packageSuffix, " +
                "packageEnd" +
        "])";

        final String result = WsIOScript.evaluate(
                "a", "B", "c",
                "d", "e", "f",
                "g", "h", "i", "j",
                packageFunc
        );

        assert "h.e.i.fdg.j".equals(result);

    }
}
