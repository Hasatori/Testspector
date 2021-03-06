package com.testspector.model.checking.java.common;

import com.testspector.model.checking.java.junit.JUnitConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JavaConstants {


    public static final List<String> ASSERTION_CLASSES_CLASS_PATHS = Collections.unmodifiableList(Arrays.asList(
            JUnitConstants.JUNIT5_ASSERTIONS_CLASS_PATH,
            JUnitConstants.HAMCREST_ASSERTIONS_CLASS_PATH,
            JUnitConstants.JUNIT4_ASSERTIONS_CLASS_PATH,
            "junit.framework.TestCase",
            "org.assertj.core.api.AssertionsForClassTypes"
    ));
}
