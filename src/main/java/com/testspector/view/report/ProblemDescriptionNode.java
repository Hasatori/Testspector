package com.testspector.view.report;


import com.intellij.psi.PsiElement;

public class ProblemDescriptionNode extends BestPracticeViolationNode {

    private final String description;

    public ProblemDescriptionNode(PsiElement navigationElement, String description) {
        super(navigationElement);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
