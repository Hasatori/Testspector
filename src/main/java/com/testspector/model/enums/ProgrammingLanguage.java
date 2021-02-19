package com.testspector.model.enums;

public enum ProgrammingLanguage {

    JAVA("Java"),
    PHP("Php"),
    TYPESCRIPT("Typescript");

    private final String displayName;

    ProgrammingLanguage(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
