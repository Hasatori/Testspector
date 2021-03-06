package com.testspector.model.checking;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.testspector.model.enums.BestPractice;

import java.util.List;

public class BestPracticeViolation {

    private final PsiElement testMethodElement;

    private final TextRange testMethodTextRange;

    private final String problemDescription;

    private final List<String> hints;

    private final BestPractice violatedRule;

    private final List<RelatedElementWrapper> relatedElementWrappers;

    private final String name;

    public BestPracticeViolation(String name, PsiElement testMethodElement, TextRange testMethodTextRange, String problemDescription, BestPractice violatedRule) {
        this(name, testMethodElement, testMethodTextRange,problemDescription,null, violatedRule,null);

    }

    public BestPracticeViolation(String name, PsiElement testMethodElement, TextRange testMethodTextRange, String problemDescription, List<String> hints, BestPractice violatedRule, List<RelatedElementWrapper> relatedElementWrappers) {
        this.testMethodElement = testMethodElement;
        this.testMethodTextRange = testMethodTextRange;
        this.problemDescription = problemDescription;
        this.hints = hints;
        this.violatedRule = violatedRule;
        this.relatedElementWrappers = relatedElementWrappers;
        this.name = name;
    }

    public BestPracticeViolation(String name, PsiElement testMethodElement, TextRange testMethodTextRange, String problemDescription, BestPractice violatedRule, List<RelatedElementWrapper> relatedElementWrappers) {
        this(name, testMethodElement, testMethodTextRange,problemDescription,null, violatedRule, relatedElementWrappers);
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
        return relatedElementWrappers;
    }
}
