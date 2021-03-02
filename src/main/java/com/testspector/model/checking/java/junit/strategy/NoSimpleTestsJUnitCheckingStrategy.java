package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.*;
import com.intellij.psi.impl.search.PsiSearchHelperImpl;
import com.intellij.psi.util.PsiTypesUtil;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.JavaElementHelper;
import com.testspector.model.checking.java.junit.JUnitConstants;
import com.testspector.model.enums.BestPractice;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NoSimpleTestsJUnitCheckingStrategy implements BestPracticeCheckingStrategy {


    private final JavaElementHelper javaElementHelper;

    public NoSimpleTestsJUnitCheckingStrategy(JavaElementHelper javaElementHelper) {
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
           List<PsiMethod> simpleMethods = javaElementHelper.getTestedMethods(method)
                    .stream()
                    .filter(isMethodSimple(method))
                    .collect(Collectors.toList());
            PsiIdentifier methodIdentifier = method.getNameIdentifier();
            if (simpleMethods.size() > 0) {
                bestPracticeViolations.add(new BestPracticeViolation(
                        method,
                        methodIdentifier != null ? methodIdentifier.getTextRange() : method.getTextRange(),
                        "Simple tests for getter and setters are redundant and can be deleted. These methods do not represent complex logic and therefore does not need to be tested.",
                        Collections.singletonList("Delete simple tests"),
                        this.getCheckedBestPractice().get(0),
                        simpleMethods.stream().map(assertion -> (PsiElement) assertion).collect(Collectors.toList())));
            }
        }
        return bestPracticeViolations;
    }

    private Predicate<PsiMethod> isMethodSimple(PsiMethod testingMethod) {
        return method -> {
            if (isSimpleGetter().test(method)) {
                PsiClass getterReferenceClass = PsiTypesUtil.getPsiClass(javaElementHelper.getAllChildrenOfType(testingMethod, PsiReferenceExpression.class)
                        .stream()
                        .filter(psiReferenceExpression -> psiReferenceExpression.resolve() == method).findFirst()
                        .map(psiReferenceExpression -> javaElementHelper.getImmediateChildrenOfType(psiReferenceExpression, PsiReferenceExpression.class))
                        .map(psiReferenceExpressions -> psiReferenceExpressions.size() >= 1 ? psiReferenceExpressions.get(0) : null)
                        .map(PsiReference::resolve)
                        .filter(element -> element instanceof PsiLocalVariable)
                        .map(element -> (PsiLocalVariable) element)
                        .map(PsiVariable::getType).orElse(null));
                return getterReferenceClass == method.getContainingClass();
            }
            return false;
        };
    }

    private Predicate<PsiMethod> isSimpleGetter() {
        return method -> {
            Optional<PsiElement> returnCandidate = javaElementHelper.getFirstChildIgnoring(Objects.requireNonNull(method.getBody()), Arrays.asList(PsiJavaToken.class, PsiWhiteSpace.class));
            if (returnCandidate.isPresent() && returnCandidate.get() instanceof PsiReturnStatement) {
                return javaElementHelper.getImmediateChildrenOfType(returnCandidate.get(), PsiReferenceExpression.class)
                        .stream()
                        .map(PsiReference::resolve)
                        .anyMatch(element -> element instanceof PsiField);
            }
            return false;
        };
    }



    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.NO_SIMPLE_TESTS);
    }


}
