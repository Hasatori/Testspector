package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.*;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.JavaElementHelper;
import com.testspector.model.checking.java.junit.JUnitConstants;
import com.testspector.model.enums.BestPractice;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AssertionCountJUnitCheckingStrategy implements BestPracticeCheckingStrategy {

    private final JavaElementHelper javaElementHelper;


    private static final String JUNIT5_ASSERTIONS_CLASS_PATH = "org.junit.jupiter.api.Assertions";
    private static final String HAMCREST_ASSERTIONS_CLASS_PATH = "org.hamcrest.MatcherAssert";
    private static final List<String> ASSERTION_CLASS_PATHS = Collections.unmodifiableList(Arrays.asList(
            JUNIT5_ASSERTIONS_CLASS_PATH,
            HAMCREST_ASSERTIONS_CLASS_PATH,
            "org.junit.Assert"
    ));

    public AssertionCountJUnitCheckingStrategy(JavaElementHelper javaElementHelper) {
        this.javaElementHelper = javaElementHelper;
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiElement psiElement) {
        return checkBestPractices(Collections.singletonList(psiElement));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiElement> psiElements) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        List<PsiMethod> methods = javaElementHelper.getMethodsFromElementByAnnotations(psiElements, JUnitConstants.JUNIT_ALL_TEST_QUALIFIED_NAMES);
        for (PsiMethod method : methods) {
            List<PsiMethodCallExpression> assertionMethods = getAssertionsMethods(method, method.getContainingClass());
            if (assertionMethods.isEmpty()) {
                bestPracticeViolations.add(new BestPracticeViolation(
                        method,
                        method.getTextRange(),
                        "Test should contain at least one assertion method!",
                        BestPractice.AT_LEAST_ONE_ASSERTION
                ));
            }
            if (assertionMethods.size() > 1) {
                StringBuilder hintMessageBuilder = new StringBuilder();
                String message = "Test should contain only one assertion method!";
                if (Arrays.stream(method.getAnnotations()).anyMatch(psiAnnotation -> JUnitConstants.JUNIT5_TEST_QUALIFIED_NAMES.contains(psiAnnotation.getQualifiedName()))) {
                    hintMessageBuilder.append(String.format("You are using JUnit5 so it can be solved by wrapping multiple assertions into %s.assertAll() method", JUNIT5_ASSERTIONS_CLASS_PATH));
                }
                if (assertionMethods.stream().anyMatch(isAssertionMethodFrom(HAMCREST_ASSERTIONS_CLASS_PATH))) {
                    if (hintMessageBuilder.length() != 0) {
                        hintMessageBuilder.append(" or ");
                    }
                    hintMessageBuilder.append("you can use hamcrest org.hamcrest.core.Every or org.hamcrest.core.AllOf matchers");
                }

                bestPracticeViolations.add(new BestPracticeViolation(
                        method,
                        method.getTextRange(),
                        message,
                        hintMessageBuilder.length() != 0 ? hintMessageBuilder.toString() : null,
                        BestPractice.ONLY_ONE_ASSERTION
                ));


            }
        }
        return bestPracticeViolations;
    }

    private List<PsiMethodCallExpression> getAssertionsMethods(PsiMethod psiMethod, PsiClass psiClass) {
        List<PsiMethodCallExpression> methodCallExpressions = new ArrayList<>();
        PsiCodeBlock psiCodeBlock = psiMethod.getBody();
        if (psiCodeBlock != null) {

            methodCallExpressions.addAll(javaElementHelper.getElementsByType(psiCodeBlock, PsiExpressionStatement.class)
                    .stream()
                    .map(psiExpression -> javaElementHelper.getElementsByType(psiExpression, PsiMethodCallExpression.class))
                    .flatMap(Collection::stream)
                    .filter(isAssertionMethod())
                    .collect(Collectors.toList()));
            List<PsiMethodCallExpression> psiMethodCallExpressions = getRelevantMethodExpression(psiCodeBlock);
            for (PsiMethodCallExpression psiMethodCallExpression : psiMethodCallExpressions) {
                PsiMethod referencedMethod = psiMethodCallExpression.resolveMethod();
                if (referencedMethod != null && referencedMethod.getContainingClass() == psiClass) {
                    methodCallExpressions.addAll(getAssertionsMethods(referencedMethod, psiClass));
                }
            }
        }
        return methodCallExpressions;
    }

    private Predicate<PsiMethodCallExpression> isAssertionMethodFrom(String qualifiedName) {
        return psiMethodCallExpression -> Optional.ofNullable(psiMethodCallExpression.resolveMethod())
                .map(PsiJvmMember::getContainingClass)
                .map(psiClass -> qualifiedName.equals(psiClass.getQualifiedName()))
                .orElse(false);
    }

    private Predicate<PsiMethodCallExpression> isAssertionMethod() {
        return psiMethodCallExpression -> Optional.ofNullable(psiMethodCallExpression.resolveMethod())
                .map(PsiJvmMember::getContainingClass)
                .map(psiClass -> ASSERTION_CLASS_PATHS.contains(psiClass.getQualifiedName()))
                .orElse(false);
    }

    private List<PsiMethodCallExpression> getRelevantMethodExpression(PsiElement psiElement) {
        List<PsiMethodCallExpression> psiMethodCallExpressions = new ArrayList<>();
        List<PsiElement> children = Arrays.stream(psiElement.getChildren()).collect(Collectors.toList());
        for (PsiElement child : children) {
            if (child instanceof PsiMethodCallExpression) {
                psiMethodCallExpressions.add((PsiMethodCallExpression) child);
            }
            psiMethodCallExpressions.addAll(getRelevantMethodExpression(child));
        }
        return psiMethodCallExpressions;
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.AT_LEAST_ONE_ASSERTION);
    }
}
