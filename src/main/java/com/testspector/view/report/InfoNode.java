package com.testspector.view.report;


import com.intellij.psi.PsiElement;

public class InfoNode extends BestPracticeViolationNode {

    private final String description;

    public InfoNode(PsiElement navigationElement, String description) {
        super(navigationElement);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
