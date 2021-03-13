package com.testspector.model.checking.java.junit;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.crate.BestPracticeViolation;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.enums.BestPractice;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JUnitGroupMethodBestPracticeCheckingStrategyAdapter implements BestPracticeCheckingStrategy<PsiElement> {


    private final List<BestPracticeCheckingStrategy<PsiMethod>> decoratedMethodSpecificStrategies;
    private final JavaMethodResolver methodResolver;

    public JUnitGroupMethodBestPracticeCheckingStrategyAdapter(List<BestPracticeCheckingStrategy<PsiMethod>> decoratedMethodSpecificStrategies, JavaMethodResolver methodResolver) {
        this.decoratedMethodSpecificStrategies = decoratedMethodSpecificStrategies;
        this.methodResolver = methodResolver;
    }


    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiElement psiElement) {
        return checkBestPractices(Collections.singletonList(psiElement));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiElement> psiElements) {
        List<PsiMethod> methods = methodResolver.testMethodsWithAnnotations(psiElements, JUnitConstants.JUNIT_ALL_TEST_QUALIFIED_NAMES);
        return decoratedMethodSpecificStrategies.stream()
                .map(decoratedStrategy -> decoratedStrategy.checkBestPractices(methods))
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
