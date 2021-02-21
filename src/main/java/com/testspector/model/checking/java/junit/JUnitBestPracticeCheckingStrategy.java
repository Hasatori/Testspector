package com.testspector.model.checking.java.junit;

import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.UnitTestFrameworkComponent;
import com.testspector.model.enums.UnitTestFramework;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class JUnitBestPracticeCheckingStrategy implements BestPracticeCheckingStrategy, UnitTestFrameworkComponent {

    private final List<BestPracticeCheckingStrategy> checkingStrategies;

    public JUnitBestPracticeCheckingStrategy(List<BestPracticeCheckingStrategy> checkingStrategies) {
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

    @Override
    public UnitTestFramework getUnitTestFramework() {
        return UnitTestFramework.JUNIT;
    }
}
