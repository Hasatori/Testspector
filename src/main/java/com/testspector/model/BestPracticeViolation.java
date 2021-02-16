package com.testspector.model;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.testspector.model.enums.BestPractice;

public class BestPracticeViolation {

    private final PsiElement psiElement;

    private final TextRange textRange;

    private final String description;

    private final BestPractice violatedRule;

    public BestPracticeViolation(PsiElement psiElement, TextRange textRange, String description, BestPractice violatedRule) {
        this.psiElement = psiElement;
        this.textRange = textRange;
        this.description = description;
        this.violatedRule = violatedRule;
    }

    public PsiElement getPsiElement() {
        return psiElement;
    }

    public TextRange getTextRange() {
        return textRange;
    }

    public String getDescription() {
        return description;
    }

    public BestPractice getViolatedRule() {
        return violatedRule;
    }
}
