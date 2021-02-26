package com.testspector.view.report;


import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeViolation;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Optional;

public abstract class BestPracticeViolationNode extends DefaultMutableTreeNode {


    protected PsiElement navigationElement = null;

    public BestPracticeViolationNode(PsiElement navigationElement) {
        super();
        this.navigationElement = navigationElement;
    }

    public BestPracticeViolationNode() {
        this(null);
    }

    public Optional<PsiElement> getNavigationElement() {
        return Optional.ofNullable(navigationElement);
    }
}
