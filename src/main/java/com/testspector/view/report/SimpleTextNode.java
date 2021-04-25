package com.testspector.view.report;


import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeViolation;

public class SimpleTextNode extends BestPracticeViolationNode {

    private final String description;

    public SimpleTextNode(PsiElement navigationElement, String description, BestPracticeViolation bestPracticeViolation) {
        super(navigationElement, bestPracticeViolation);
        this.description = description;
    }


    public String getDescription() {
        return description;
    }
}
