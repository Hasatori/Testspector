package com.testspector.model.checking.java.junit.singlebestpracticestrategy;

import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.BestPracticeCheckingStrategy;

import java.util.List;

public class NoGlobalStaticPropertiesJUnitCheckingStrategy implements BestPracticeCheckingStrategy {

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiElement psiElement) {
        return null;
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiElement> psiElements) {
        return null;
    }
}
