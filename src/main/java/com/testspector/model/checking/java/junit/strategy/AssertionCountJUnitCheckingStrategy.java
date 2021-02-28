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

import static com.testspector.model.checking.java.junit.JUnitConstants.ASSERTION_CLASS_PATHS;

public class AssertionCountJUnitCheckingStrategy implements BestPracticeCheckingStrategy {

    private final JavaElementHelper javaElementHelper;

    public static final List<String> GROUP_ASSERTION_NAMES = Collections.unmodifiableList(Arrays.asList(
            "assertAll"
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
            List<PsiMethodCallExpression> assertionMethods = getAssertionsMethods(method);
            PsiIdentifier methodIdentifier = method.getNameIdentifier();
            if (assertionMethods.isEmpty()) {
                bestPracticeViolations.add(new BestPracticeViolation(
                        method,
                        methodIdentifier != null ? methodIdentifier.getTextRange() : method.getTextRange(),
                        "Test should contain at least one assertion method!",
                        BestPractice.AT_LEAST_ONE_ASSERTION
                ));
            }
            if (assertionMethods.size() > 1) {
                List<String> hints = new ArrayList<>();
                String message = "Test should contain only one assertion method!";
                if (Arrays.stream(method.getAnnotations()).anyMatch(psiAnnotation -> JUnitConstants.JUNIT5_TEST_QUALIFIED_NAMES.contains(psiAnnotation.getQualifiedName()))) {
                    hints.add(String.format("You are using JUnit5 so it can be solved by wrapping multiple assertions into %s.assertAll() method", JUnitConstants.JUNIT5_ASSERTIONS_CLASS_PATH));
                }
                if (assertionMethods.stream().anyMatch(isAssertionMethodFrom(JUnitConstants.HAMCREST_ASSERTIONS_CLASS_PATH))) {
                    hints.add("you can use hamcrest org.hamcrest.core.Every or org.hamcrest.core.AllOf matchers");
                }

                bestPracticeViolations.add(new BestPracticeViolation(
                        method,
                        methodIdentifier != null ? methodIdentifier.getTextRange() : method.getTextRange(),
                        message,
                        hints,
                        BestPractice.ONLY_ONE_ASSERTION,
                        assertionMethods.stream().map(assertion -> (PsiElement) assertion).collect(Collectors.toList())));


            }
        }
        return bestPracticeViolations;
    }

    private List<PsiMethodCallExpression> getAssertionsMethods(PsiMethod psiMethod) {
        List<PsiMethodCallExpression> methodCallExpressions = new ArrayList<>();
        PsiCodeBlock psiCodeBlock = psiMethod.getBody();
        if (psiCodeBlock != null) {

            methodCallExpressions.addAll(javaElementHelper.getImmediateChildrenOfType(psiCodeBlock, PsiExpressionStatement.class)
                    .stream()
                    .map(psiExpression -> javaElementHelper.getImmediateChildrenOfType(psiExpression, PsiMethodCallExpression.class))
                    .flatMap(Collection::stream)
                    .filter(isAssertionMethod())
                    .collect(Collectors.toList()));
            List<PsiMethodCallExpression> psiMethodCallExpressions = getRelevantMethodExpression(psiCodeBlock);
            for (PsiMethodCallExpression psiMethodCallExpression : psiMethodCallExpressions) {
                PsiMethod referencedMethod = psiMethodCallExpression.resolveMethod();
                if (referencedMethod != null && javaElementHelper.isInTestContext(referencedMethod)) {
                    methodCallExpressions.addAll(getAssertionsMethods(referencedMethod));
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
            boolean shouldContinue = true;
            if (child instanceof PsiMethodCallExpression) {
                psiMethodCallExpressions.add((PsiMethodCallExpression) child);
                PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) child;
                PsiMethod method = methodCallExpression.resolveMethod();
                if (method != null && GROUP_ASSERTION_NAMES.contains(method.getName()) && method.getContainingClass() != null && ASSERTION_CLASS_PATHS.contains(method.getContainingClass().getQualifiedName())) {
                    shouldContinue = false;
                }
            }
            if (shouldContinue) {
                psiMethodCallExpressions.addAll(getRelevantMethodExpression(child));
            }

        }
        return psiMethodCallExpressions;
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.AT_LEAST_ONE_ASSERTION);
    }
}
