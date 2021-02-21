package com.testspector.model.enums;


import java.util.Collections;
import java.util.List;

import static com.testspector.model.utils.Constants.WEB_PAGE_BEST_PRACTICES_ULR;

public enum BestPractice {


    TEST_ONLY_PUBLIC_BEHAVIOUR("Test only public behaviour", ""),
    NO_SIMPLE_TESTS("No simple tests", ""),
    AT_LEAST_ONE_ASSERTION("At least one assertion", ""),
    ONLY_ONE_ASSERTION("Only one assertion", ""),
    NO_GLOBAL_STATIC_PROPERTIES("No global static properties", ""),
    CREATE_CUSTOM_DATA_AND_SOURCES("Create custom data and sources", ""),
    SETUP_A_TEST_NAMING_STRATEGY("Setup a test naming strategy", ""),
    CATCH_EXCEPTIONS_USING_FRAMEWORK_TOOLS("Catch exceptions using frameworks tools", ""),
    NO_CONDITIONAL_LOGIC("No conditional logic", ""),
    THREE_PHASE_TEST_STRUCTURE("Three phase test structure", "");

    static {
        ONLY_ONE_ASSERTION.relatedRules = Collections.singletonList(AT_LEAST_ONE_ASSERTION);
        AT_LEAST_ONE_ASSERTION.relatedRules = Collections.singletonList(ONLY_ONE_ASSERTION);
    }

    private final String displayName;
    private final String definition;
    private final String webPageHyperlink;
    private List<BestPractice> relatedRules = Collections.emptyList();


    BestPractice(String displayName, String definition) {
        this.displayName = displayName;
        this.definition = definition;
        this.webPageHyperlink = String.format("%s#%s", WEB_PAGE_BEST_PRACTICES_ULR, this.name().toLowerCase());
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

    public String getWebPageHyperlink() {
        return webPageHyperlink;
    }
}
