package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.*;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.JavaElementHelper;
import com.testspector.model.checking.java.junit.JUnitConstants;
import com.testspector.model.enums.BestPractice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class NoConditionalLogicJUnitCheckingStrategy implements BestPracticeCheckingStrategy {

    private final JavaElementHelper javaElementHelper;


    public NoConditionalLogicJUnitCheckingStrategy(JavaElementHelper javaElementHelper) {
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
        List<PsiStatement> statements = new ArrayList<>();
        for (PsiMethod method : methods) {
            statements.addAll(getConditionalStatements(method, method.getContainingClass()));
        }
        statements = statements.stream().distinct().collect(Collectors.toList());
        for (PsiStatement psiStatement : statements) {
            bestPracticeViolations.add(new BestPracticeViolation(
                    psiStatement,
                    psiStatement.getTextRange(),
                    psiStatement.toString() + " statement logic should not be part of the test method, it makes test hard to understand and read. Remove if statements and create separate test scenario for each branch.",
                    getCheckedBestPractice().get(0)));
        }
        return bestPracticeViolations;
    }

    private List<PsiStatement> getConditionalStatements(PsiMethod method, PsiClass psiClass) {
        List<PsiStatement> conditionalStatements = new ArrayList<>();
        PsiCodeBlock methodBody = method.getBody();
        if (methodBody != null) {
            conditionalStatements.addAll(Arrays.stream(methodBody.getStatements()).filter(psiStatement ->
                    psiStatement instanceof PsiIfStatement || psiStatement instanceof PsiForStatement || psiStatement instanceof PsiWhileStatement || psiStatement instanceof PsiSwitchStatement
            ).collect(Collectors.toList()));
        }
        List<PsiMethodCallExpression> psiMethodCallExpressions = getRelevantMethodExpression(method);
        for (PsiMethodCallExpression psiMethodCallExpression : psiMethodCallExpressions) {
            PsiMethod referencedMethod = psiMethodCallExpression.resolveMethod();
            if (referencedMethod != null && referencedMethod.getContainingClass() == psiClass) {
                conditionalStatements.addAll(getConditionalStatements(referencedMethod, psiClass));
            }
        }
        return conditionalStatements;
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
        return Collections.singletonList(BestPractice.NO_CONDITIONAL_LOGIC);
    }
}