package com.testspector.model.checking.java.common;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJvmMember;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.testspector.model.checking.java.junit.JUnitConstants;

import java.util.*;
import java.util.function.Predicate;

public class JavaMethodCallExpressionResolver {

    public static final List<String> ASSERTION_CLASS_PATHS = Collections.unmodifiableList(Arrays.asList(
            JUnitConstants.JUNIT5_ASSERTIONS_CLASS_PATH,
            JUnitConstants.HAMCREST_ASSERTIONS_CLASS_PATH,
            JUnitConstants.JUNIT4_ASSERTIONS_CLASS_PATH,
            "junit.framework.TestCase",
            "org.assertj.core.api.AssertionsForClassTypes"
    ));

    private final JavaContextIndicator contextResolver;

    public JavaMethodCallExpressionResolver(JavaContextIndicator contextIndicator) {
        this.contextResolver = contextIndicator;
    }

    public List<PsiMethodCallExpression> assertionCallExpressions(PsiElement psiElement) {
        List<PsiMethodCallExpression> assertionMethods = new ArrayList<>();
        for (PsiElement child : psiElement.getChildren()) {
            if (child instanceof PsiMethodCallExpression) {
                PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) child;
                if (this.isAssertionMethodCallExpression().test(methodCallExpression)) {
                    assertionMethods.add(methodCallExpression);
                } else {
                    PsiMethod referencedMethod = methodCallExpression.resolveMethod();
                    if (referencedMethod != null && contextResolver.isInTestContext(referencedMethod)) {
                        assertionMethods.addAll(assertionCallExpressions(referencedMethod));
                    }
                }
            }
            assertionMethods.addAll(assertionCallExpressions(child));
        }
        return assertionMethods;
    }

    private Predicate<PsiMethodCallExpression> isAssertionMethodCallExpression() {
        return psiMethodCallExpression -> Optional.ofNullable(psiMethodCallExpression.resolveMethod())
                .map(PsiJvmMember::getContainingClass)
                .map(psiClass -> ASSERTION_CLASS_PATHS.contains(psiClass.getQualifiedName()))
                .orElse(false);
    }
}
