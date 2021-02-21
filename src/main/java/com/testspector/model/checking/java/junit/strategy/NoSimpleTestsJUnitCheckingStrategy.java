package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.enums.BestPractice;

import java.util.Collections;
import java.util.List;

public class NoSimpleTestsJUnitCheckingStrategy implements BestPracticeCheckingStrategy {

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiElement psiElement) {
        return null;
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiElement> psiElements) {
        return null;
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.NO_SIMPLE_TESTS);
    }
}
