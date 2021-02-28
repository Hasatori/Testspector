package com.testspector.view.report;


import com.intellij.psi.PsiElement;

public class WrapperNode extends BestPracticeViolationNode {

    private final String name;

    public WrapperNode(PsiElement navigationElement,String name) {
        super(navigationElement);
        this.name = name;
    }

    public WrapperNode(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
