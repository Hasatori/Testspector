package com.testspector.model.checking.java.junit.singlebestpracticestrategy;

import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.junit.JUnitSingleBestPracticeCheckingStrategy;

import java.util.List;

public class NoSimpleTestsJUnitCheckingStrategy implements JUnitSingleBestPracticeCheckingStrategy {

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiElement psiElement) {
        return null;
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiElement> psiElements) {
        return null;
    }
}
