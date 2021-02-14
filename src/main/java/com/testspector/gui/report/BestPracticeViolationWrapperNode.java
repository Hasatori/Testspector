package com.testspector.gui.report;


import com.testspector.checking.BestPracticeViolation;

public class BestPracticeViolationWrapperNode extends BestPracticeViolationNode {

    private final String name;

    public BestPracticeViolationWrapperNode(BestPracticeViolation bestPracticeViolation) {
        super(bestPracticeViolation);
        this.name = bestPracticeViolation.getPsiElement().toString();
    }

    public String getName() {
        return this.name;
    }
}
