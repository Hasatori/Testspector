package com.testspector.view.report;


public enum GroupBy {
    FILES("Files"),
    BEST_PRACTICE("Best practice");

    private final String displayName;

    GroupBy(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}

