package com.testspector.model.enums;

import java.util.Collections;
import java.util.List;

import static com.testspector.model.enums.ProgrammingLanguage.*;

public enum UnitTestFramework {

    JUNIT("JUnit", Collections.singletonList(JAVA)),
    PHP_UNIT("PHPUnit", Collections.singletonList(PHP)),
    SPOCK("Spock",Collections.singletonList(GROOVY));

    private final String displayName;
    private final List<ProgrammingLanguage> programmingLanguage;

    UnitTestFramework(String displayName, List<ProgrammingLanguage> programmingLanguage) {
        this.displayName = displayName;
        this.programmingLanguage = programmingLanguage;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<ProgrammingLanguage> getProgrammingLanguage() {
        return programmingLanguage;
    }
}
