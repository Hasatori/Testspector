package com.testspector.model.checking.java.junit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class JUnitConstants {

    private JUnitConstants() {

    }

    public static final String JUNIT5_ASSERTIONS_CLASS_PATH = "org.junit.jupiter.api.Assertions";
    public static final String JUNIT4_ASSERTIONS_CLASS_PATH = "org.junit.Assert";
    public static final String HAMCREST_ASSERTIONS_CLASS_PATH = "org.hamcrest.MatcherAssert";
    public static final String JUNIT4_TEST_QUALIFIED_NAME =  "org.junit.Test";
    public static final List<String> JUNIT4_TEST_QUALIFIED_NAMES = Collections.singletonList(
           JUNIT4_TEST_QUALIFIED_NAME
    );


    public static final String JUNIT5_PARAMETERIZED_TEST_ABSOLUTE_PATH = "org.junit.jupiter.params.ParameterizedTest";
    public static final List<String> JUNIT5_TEST_QUALIFIED_NAMES = Collections.unmodifiableList(Arrays.asList(
            "org.junit.jupiter.api.Test",
            JUNIT5_PARAMETERIZED_TEST_ABSOLUTE_PATH,
            "org.junit.jupiter.api.RepeatedTest"
    ));

    public static final List<String> JUNIT_ALL_TEST_QUALIFIED_NAMES = Collections.unmodifiableList(
            Stream.concat(
                    JUNIT4_TEST_QUALIFIED_NAMES.stream(),
                    JUNIT5_TEST_QUALIFIED_NAMES.stream()
            ).collect(Collectors.toList()));

    public static final List<String> JUNIT_ALL_PACKAGES_QUALIFIED_NAMES = Collections.unmodifiableList(Arrays.asList(
            "org.junit.jupiter.api",
            "org.junit.jupiter.params",
            "org.junit"
    ));
}
