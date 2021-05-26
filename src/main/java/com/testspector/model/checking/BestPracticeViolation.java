package com.testspector.model.checking;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.PsiElement;
import com.testspector.model.enums.BestPractice;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BestPracticeViolation {

    private final PsiElement testMethodElement;

    private final String problemDescription;

    private final BestPractice violatedBestPractice;

    private final List<PsiElement> relatedElements;

    private final List<LocalQuickFix> quickFixes;

    private final List<String> hints;

    public BestPracticeViolation(PsiElement testMethodElement, String problemDescription, BestPractice violatedBestPractice, List<PsiElement> relatedElements, List<LocalQuickFix> quickFixes, List<String> hints) {
        this.testMethodElement = testMethodElement;
        this.problemDescription = problemDescription;
        this.violatedBestPractice = violatedBestPractice;
        this.relatedElements = relatedElements;
        this.quickFixes = quickFixes;
        this.hints = hints;
    }

    public BestPracticeViolation(PsiElement testMethodElement, String problemDescription, BestPractice violatedBestPractice, List<PsiElement> relatedElements, List<LocalQuickFix> quickFixes) {
        this(testMethodElement,problemDescription,violatedBestPractice,relatedElements,quickFixes,null);
    }

    public PsiElement getTestMethodElement() {
        return testMethodElement;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public BestPractice getViolatedBestPractice() {
        return violatedBestPractice;
    }

    public List<PsiElement> getRelatedElements() {
        return relatedElements;
    }

    public List<LocalQuickFix> getQuickFixes() {
        return quickFixes;
    }

    @Nullable
    public List<String> getHints() {
        return hints;
    }
}
