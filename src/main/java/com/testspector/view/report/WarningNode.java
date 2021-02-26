package com.testspector.view.report;


import com.intellij.psi.PsiElement;

public class WarningNode extends BestPracticeViolationNode {

    private final String description;

    public WarningNode(PsiElement navigationElement, String description) {
        super(navigationElement);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
