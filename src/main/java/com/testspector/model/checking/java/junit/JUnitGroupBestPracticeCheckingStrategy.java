package com.testspector.model.checking.java.junit;

import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.enums.BestPractice;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JUnitGroupBestPracticeCheckingStrategy implements BestPracticeCheckingStrategy<PsiElement> {


    private final List<BestPracticeCheckingStrategy<PsiElement>> decoratedMethodSpecificStrategies;

    public JUnitGroupBestPracticeCheckingStrategy(List<BestPracticeCheckingStrategy<PsiElement>> decoratedMethodSpecificStrategies) {
        this.decoratedMethodSpecificStrategies = decoratedMethodSpecificStrategies;
    }


    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiElement psiElement) {
        return checkBestPractices(Collections.singletonList(psiElement));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiElement> psiElements) {
        return decoratedMethodSpecificStrategies.stream()
                .map(decoratedStrategy -> decoratedStrategy.checkBestPractices(psiElements))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return decoratedMethodSpecificStrategies.stream()
                .map(BestPracticeCheckingStrategy::getCheckedBestPractice)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
