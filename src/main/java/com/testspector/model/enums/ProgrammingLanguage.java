package com.testspector.model.enums;

public enum ProgrammingLanguage {

    JAVA("Java"),
    PHP("Php"),
    C_PLUS_PLUS("C++"),
    C_SHARP("C#"),
    JAVASCRIPT("Javascript");

    private final String displayName;

    ProgrammingLanguage(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
