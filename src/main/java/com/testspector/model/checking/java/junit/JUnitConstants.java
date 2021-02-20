package com.testspector.model.checking.java.junit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class JUnitConstants {

    private JUnitConstants(){

    }

    public static final List<String> TEST_QUALIFIED_NAMES = Collections.unmodifiableList(Arrays.asList(
            "org.junit.Test",
            "org.junit.jupiter.api.Test",
            "org.junit.jupiter.params.ParameterizedTest",
            "org.junit.jupiter.api.RepeatedTest"
    ));

    public static final List<String> JUNIT_PACKAGES_QUALIFIED_NAMES = Collections.unmodifiableList(Arrays.asList(
            "org.junit.jupiter.api",
            "org.junit.jupiter.params",
            "org.junit"
    ));
}
