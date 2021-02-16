package com.testspector.model.checking.java.junit;

import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.JavaBestPracticeCheckingStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class JUnitBestPracticeCheckingStrategy extends JavaBestPracticeCheckingStrategy {

    private final List<BestPracticeCheckingStrategy> checkingStrategies;

    public JUnitBestPracticeCheckingStrategy() {
        checkingStrategies = new ArrayList<>();
        checkingStrategies.add(new NoSimpleTestsJunitBestPracticeCheckingStrategy());

    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiElement psiElement) {
        return checkingStrategies.stream()
                .map(bestPracticeCheckingStrategy -> bestPracticeCheckingStrategy.checkBestPractices(psiElement))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiElement> psiElements) {
        return psiElements.stream().map(this::checkBestPractices)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
