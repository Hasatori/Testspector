package com.testspector.enums;


import java.util.Collections;
import java.util.List;

public enum BestPractice {

    TEST_ONLY_PUBLIC_BEHAVIOUR("", ""),
    NO_SIMPLE_TESTS("", ""),
    AT_LEAST_ONE_ASSERTION("", ""),
    ONLY_ONE_ASSERTION("", ""),
    NO_GLOBAL_STATIC_PROPERTIES("", ""),
    CREATE_CUSTOM_DATA_AND_SOURCES("", ""),
    SETUP_A_TEST_NAMING_STRATEGY("", ""),
    CATCH_EXCEPTIONS_USING_FRAMEWORK_TOOLS("", ""),
    NO_CONDITIONAL_LOGIC("", ""),
    THREE_PHASE_TEST_STRUCTURE("", "");

    static {
        ONLY_ONE_ASSERTION.relatedRules = Collections.singletonList(AT_LEAST_ONE_ASSERTION);
        AT_LEAST_ONE_ASSERTION.relatedRules = Collections.singletonList(ONLY_ONE_ASSERTION);
    }

    private final String displayName;
    private final String definition;
    private List<BestPractice> relatedRules;


    BestPractice(String displayName, String definition) {
        this.displayName = displayName;
        this.definition = definition;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefinition() {
        return definition;
    }

    public List<BestPractice> getRelatedRules() {
        return relatedRules;
    }
}
