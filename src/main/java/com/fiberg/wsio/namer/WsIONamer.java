package com.fiberg.wsio.namer;

import java.util.Arrays;
import java.util.stream.Collectors;

@FunctionalInterface
public interface WsIONamer {

    String generate(
            final String methodName, final String className, final String packageName,
            final String packagePath, final String packagePrefix, final String packageSuffix,
            final String packageStart, final String packageMiddle, final String packageEnd
    );

    default String concat(final String... packagePaths) {
        return Arrays.stream(packagePaths)
                .map(packagePath -> packagePath.replaceAll("^\\.+", ""))
                .map(packagePath -> packagePath.replaceAll("\\.+$", ""))
                .filter(packagePath -> !packagePath.equals(""))
                .collect(Collectors.joining("."));
    }

    default String backward(final String packageName) {
        return backward(packageName, 1);
    }

    default String forward(final String packageName) {
        return forward(packageName, 1);
    }

    default String backward(final String packageName, int level) {

        if (level < 0) {
            throw new IllegalArgumentException(
                    String.format("Level must be greater or equals than 0, current value is %s", level)
            );
        }

        final String[] packagePaths = packageName.split("\\.");

        if (packagePaths.length <= level) return "";
        else {
            return Arrays.stream(packagePaths)
                    .limit(packagePaths.length - level)
                    .reduce("", this::concat);
        }

    }

    default String forward(final String packageName, int level) {

        if (level < 0) {
            throw new IllegalArgumentException(
                    String.format("Level must be greater or equals than 0, current value is %s", level)
            );
        }

        final String[] packagePaths = packageName.split("\\.");

        final int backward = packagePaths.length - level;
        final int cappedBackward = Math.max(0, backward);

        return backward(packageName, cappedBackward);

    }

    class Default implements WsIONamer {

        @Override
        public String generate(
                final String methodName, final String className, final String packageName,
                final String packagePath, final String packagePrefix, final String packageSuffix,
                final String packageStart, final String packageMiddle, final String packageEnd) {

            return concat(
                    packageStart, packagePath, packageMiddle,
                    String.join("", packagePrefix, packageName, packageSuffix),
                    packageEnd
            );

        }

    }

}
