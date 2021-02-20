package com.testspector.model.checking.java.junit;

import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class JUnitBestPracticeCheckingStrategy implements BestPracticeCheckingStrategy{

    private final List<JUnitSingleBestPracticeCheckingStrategy> checkingStrategies;

    public JUnitBestPracticeCheckingStrategy(List<JUnitSingleBestPracticeCheckingStrategy> checkingStrategies) {
        this.checkingStrategies = checkingStrategies;

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
