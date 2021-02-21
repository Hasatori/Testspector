package com.testspector.model.checking;

import com.intellij.psi.PsiElement;
import com.testspector.model.enums.BestPractice;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class GroupBestPracticeCheckingStrategyDecorator implements BestPracticeCheckingStrategy {


    private final List<BestPracticeCheckingStrategy> bestPracticeCheckingStrategies;


    public GroupBestPracticeCheckingStrategyDecorator(List<BestPracticeCheckingStrategy> bestPracticeCheckingStrategies) {
        this.bestPracticeCheckingStrategies = bestPracticeCheckingStrategies;
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiElement psiElement) {
        return bestPracticeCheckingStrategies.stream()
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

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return bestPracticeCheckingStrategies.stream()
                .map(BestPracticeCheckingStrategy::getCheckedBestPractice)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

}
