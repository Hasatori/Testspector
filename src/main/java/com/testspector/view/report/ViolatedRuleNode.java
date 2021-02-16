package com.testspector.view.report;


import com.testspector.model.checking.BestPracticeViolation;

public class ViolatedRuleNode extends BestPracticeViolationNode {


    public ViolatedRuleNode(BestPracticeViolation bestPracticeViolation) {
        super(bestPracticeViolation);
    }
}
