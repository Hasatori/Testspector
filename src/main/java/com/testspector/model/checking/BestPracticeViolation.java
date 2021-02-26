package com.testspector.model.checking;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.testspector.model.enums.BestPractice;

import java.util.List;

public class BestPracticeViolation {

    private final PsiElement psiElement;

    private final TextRange textRange;

    private final String problemDescription;

    private final List<String> hints;

    private final BestPractice violatedRule;

    private final List<PsiElement> errorElements;

    public BestPracticeViolation(PsiElement psiElement, TextRange textRange, String problemDescription, BestPractice violatedRule) {
        this(psiElement,textRange,problemDescription,null, violatedRule,null);
    }

    public BestPracticeViolation(PsiElement psiElement, TextRange textRange, String problemDescription, List<String> hints, BestPractice violatedRule, List<PsiElement> errorElements) {
        this.psiElement = psiElement;
        this.textRange = textRange;
        this.problemDescription = problemDescription;
        this.hints = hints;
        this.violatedRule = violatedRule;
        this.errorElements = errorElements;
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

    public List<String> getHints() {
        return hints;
    }

    public BestPractice getViolatedRule() {
        return violatedRule;
    }

    public List<PsiElement> getErrorElements() {
        return errorElements;
    }
}
