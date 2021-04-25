package com.testspector.view.report;


import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeViolation;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Optional;

public abstract class BestPracticeViolationNode extends DefaultMutableTreeNode {


    protected BestPracticeViolation bestPracticeViolation;
    protected PsiElement navigationElement = null;

    public BestPracticeViolationNode(PsiElement navigationElement, BestPracticeViolation bestPracticeViolation) {
        super();
        this.navigationElement = navigationElement;
        this.bestPracticeViolation = bestPracticeViolation;
    }

    public BestPracticeViolationNode(BestPracticeViolation bestPracticeViolation) {
        this(null, bestPracticeViolation);
    }

    public Optional<PsiElement> getNavigationElement() {
        return Optional.ofNullable(navigationElement);
    }

    public BestPracticeViolation getBestPracticeViolation() {
        return bestPracticeViolation;
    }
}
