package com.testspector.view.report;


import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeViolation;

import java.util.List;

public class InfoNode extends BestPracticeViolationNode {

    private final String description;
    private final List<String> hints;

    public InfoNode(PsiElement navigationElement, String description, List<String> hints, BestPracticeViolation bestPracticeViolation) {
        super(navigationElement,bestPracticeViolation);
        this.description = description;
        this.hints = hints;
    }

    public List<String> getHints() {
        return hints;
    }

    public String getDescription() {
        return description;
    }
}
