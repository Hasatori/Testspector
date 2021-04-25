package com.testspector.view.report;


import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeViolation;

public class WarningNode extends BestPracticeViolationNode {

    private final String description;

    public WarningNode(PsiElement navigationElement, String description, BestPracticeViolation bestPracticeViolation) {
        super(navigationElement,bestPracticeViolation);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
