package com.testspector.model.checking.java.junit;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.enums.BestPractice;

import java.util.Collections;
import java.util.List;

public class JUnitTestMethodBestPracticeCheckingStrategyAdapter implements BestPracticeCheckingStrategy<PsiElement> {


    private final BestPracticeCheckingStrategy<PsiMethod> decoratedMethodSpecificStrategy;
    private final JavaMethodResolver methodResolver;

    public JUnitTestMethodBestPracticeCheckingStrategyAdapter( BestPracticeCheckingStrategy<PsiMethod> decoratedMethodSpecificStrategy, JavaMethodResolver methodResolver) {
        this.decoratedMethodSpecificStrategy = decoratedMethodSpecificStrategy;
        this.methodResolver = methodResolver;
    }


    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiElement psiElement) {
        return checkBestPractices(Collections.singletonList(psiElement));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiElement> psiElements) {
        List<PsiMethod> methods = methodResolver.methodsWithAnnotations(psiElements, JUnitConstants.JUNIT_ALL_TEST_QUALIFIED_NAMES);
        return decoratedMethodSpecificStrategy.checkBestPractices(methods);
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return decoratedMethodSpecificStrategy.getCheckedBestPractice();
    }
}
