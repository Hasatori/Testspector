package com.testspector.view.report;


import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeViolation;

public class WrapperNode extends BestPracticeViolationNode {

    private final String name;

    public WrapperNode(PsiElement navigationElement, String name, BestPracticeViolation bestPracticeViolation) {
        super(navigationElement,bestPracticeViolation);
        this.name = name;
    }

    public WrapperNode(String name,BestPracticeViolation bestPracticeViolation) {
        super(bestPracticeViolation);
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
