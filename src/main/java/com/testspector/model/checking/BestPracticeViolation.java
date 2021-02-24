package com.testspector.model.checking;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.testspector.model.enums.BestPractice;

public class BestPracticeViolation {

    private final PsiElement psiElement;

    private final TextRange textRange;

    private final String problemDescription;

    private final String hintDescription;

    private final BestPractice violatedRule;

    public BestPracticeViolation(PsiElement psiElement, TextRange textRange, String problemDescription, BestPractice violatedRule) {
        this(psiElement,textRange,problemDescription,null, violatedRule);
    }

    public BestPracticeViolation(PsiElement psiElement, TextRange textRange, String problemDescription, String hintDescription, BestPractice violatedRule) {
        this.psiElement = psiElement;
        this.textRange = textRange;
        this.problemDescription = problemDescription;
        this.hintDescription = hintDescription;
        this.violatedRule = violatedRule;
    }

    public PsiElement getPsiElement() {
        return psiElement;
    }

    public TextRange getTextRange() {
        return textRange;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public String getHintDescription() {
        return hintDescription;
    }

    public BestPractice getViolatedRule() {
        return violatedRule;
    }
}
