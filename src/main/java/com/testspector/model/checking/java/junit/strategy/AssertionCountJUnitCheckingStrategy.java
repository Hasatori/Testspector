package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaElementResolver;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.junit.JUnitConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class AssertionCountJUnitCheckingStrategy implements BestPracticeCheckingStrategy<PsiMethod> {

    protected final JavaElementResolver elementResolver;
    protected final JavaContextIndicator contextIndicator;
    protected final JavaMethodResolver methodResolver;


    public AssertionCountJUnitCheckingStrategy(JavaElementResolver elementResolver, JavaContextIndicator contextIndicator, JavaMethodResolver methodResolver) {
        this.elementResolver = elementResolver;
        this.contextIndicator = contextIndicator;
        this.methodResolver = methodResolver;
    }
    protected Predicate<PsiElement> methodInTestContext() {
        return (element) ->
                element instanceof PsiMethod &&
                        (
                                contextIndicator.isInTestContext().test(element)
                        );
    }

    protected void removeGroupedAssertions(List<PsiMethodCallExpression> allAssertions) {
        List<PsiMethodCallExpression> toRemove = new ArrayList<>();
        for (PsiMethodCallExpression assertion : allAssertions) {
            toRemove.addAll(elementResolver.allChildrenOfTypeMeetingConditionWithReferences(
                    assertion,
                    PsiMethodCallExpression.class,
                    psiMethodCallExpression -> methodResolver.assertionMethod(psiMethodCallExpression).isPresent(),
                    contextIndicator.isInTestContext()));
        }
        allAssertions.removeAll(toRemove);
    }

    protected Predicate<PsiMethodCallExpression> isAssertionMethodFrom(String qualifiedName) {
        return psiMethodCallExpression -> Optional.of(psiMethodCallExpression.getMethodExpression())
                .map(psiClass -> psiClass.getQualifiedName().contains(qualifiedName))
                .orElse(false);
    }


    protected Optional<PsiReferenceExpression> firstReferenceToAssertionMethod(PsiElement element,
                                                                             PsiMethodCallExpression psiMethodCallExpression) {
        List<PsiReferenceExpression> references = elementResolver.allChildrenOfTypeMeetingConditionWithReferences(
                element,
                PsiReferenceExpression.class);
        for (PsiReferenceExpression reference : references) {
            if (!elementResolver.allChildrenOfTypeMeetingConditionWithReferences(
                    reference.getParent(),
                    PsiMethodCallExpression.class, psiMethodCallExpression1 ->
                            psiMethodCallExpression == psiMethodCallExpression1,
                    contextIndicator.isInTestContext()).isEmpty()) {
                return Optional.of(reference);
            }
        }
        return Optional.empty();
    }




    protected boolean containsHamcrestAssertion(List<PsiMethodCallExpression> allAssertionMethods) {
        return allAssertionMethods
                .stream()
                .anyMatch(isAssertionMethodFrom(JUnitConstants.HAMCREST_ASSERTIONS_CLASS_PATH));
    }

    protected boolean isJUnit5TestMethod(PsiMethod testMethod) {
        return Arrays
                .stream(testMethod.getAnnotations())
                .anyMatch(psiAnnotation ->
                        JUnitConstants.JUNIT5_TEST_QUALIFIED_NAMES.contains(psiAnnotation.getQualifiedName()));
    }

}
