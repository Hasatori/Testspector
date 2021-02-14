package com.testspector.gui.report;


import com.testspector.checking.BestPracticeViolation;

import javax.swing.tree.DefaultMutableTreeNode;

public abstract class BestPracticeViolationNode extends DefaultMutableTreeNode {

    private BestPracticeViolation bestPracticeViolation;

    public BestPracticeViolationNode(BestPracticeViolation bestPracticeViolation) {
        super();
        this.bestPracticeViolation = bestPracticeViolation;
    }

    public BestPracticeViolation getBestPracticeViolation() {
        return bestPracticeViolation;
    }
}
