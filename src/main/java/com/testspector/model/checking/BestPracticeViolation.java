package com.testspector.model.checking;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.testspector.model.enums.BestPractice;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BestPracticeViolation {

    private final PsiElement testMethodElement;

    private final TextRange testMethodTextRange;

    private final String problemDescription;

    private final List<String> hints;

    private final BestPractice violatedRule;

    private final List<RelatedElementWrapper> relatedElementsWrapper;

    private final String name;

    public BestPracticeViolation(String name, PsiElement testMethodElement, TextRange testMethodTextRange, String problemDescription, BestPractice violatedRule, @Nullable List<String> hints, @Nullable List<RelatedElementWrapper> relatedElementsWrapper) {
        this.testMethodElement = testMethodElement;
        this.testMethodTextRange = testMethodTextRange;
        this.problemDescription = problemDescription;
        this.hints = hints;
        this.violatedRule = violatedRule;
        this.relatedElementsWrapper = relatedElementsWrapper;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public PsiElement getTestMethodElement() {
        return testMethodElement;
    }

    public TextRange getTestMethodTextRange() {
        return testMethodTextRange;
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

    public List<RelatedElementWrapper> getRelatedElements() {
        return relatedElementsWrapper;
    }
}
