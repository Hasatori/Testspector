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

import static com.testspector.model.checking.java.junit.JUnitConstants.JUNIT4_ASSERTIONS_CLASS_PATH;
import static com.testspector.model.checking.java.junit.JUnitConstants.JUNIT5_ASSERTIONS_CLASS_PATH;

public class CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy implements BestPracticeCheckingStrategy {

    private final JavaElementHelper javaElementHelper;

    public CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy(JavaElementHelper javaElementHelper) {
        this.javaElementHelper = javaElementHelper;
    }


    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiElement psiElement) {
        return this.checkBestPractices(Collections.singletonList(psiElement));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiElement> psiElements) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        List<PsiMethod> methods = javaElementHelper.getMethodsFromElementByAnnotations(psiElements, JUnitConstants.JUNIT_ALL_TEST_QUALIFIED_NAMES);
        for (PsiMethod method : methods) {
            List<PsiTryStatement> psiTryStatements = getTryStatements(method);
            if (psiTryStatements.size() > 0) {
                List<String> hints = new ArrayList<>();
                String message = "Tests should not contain try catch block. These blocks are redundant and make test harder to read and understand. In some cases it might even lead to never failing tests if we are not handling the exception properly.";
                if (Arrays.stream(method.getAnnotations()).anyMatch(psiAnnotation -> JUnitConstants.JUNIT5_TEST_QUALIFIED_NAMES.contains(psiAnnotation.getQualifiedName()))) {
                    hints.add(String.format("You are using JUnit5 so it can be solved by using %s.assertThrows() method", JUNIT5_ASSERTIONS_CLASS_PATH));
                }
                if (Arrays.stream(method.getAnnotations()).anyMatch(psiAnnotation -> JUnitConstants.JUNIT4_TEST_QUALIFIED_NAMES.contains(psiAnnotation.getQualifiedName()))) {
                    hints.add(String.format("You are using JUnit4 so it can be solved by using %s@Test(expected = Exception.class) for the test method", JUNIT4_ASSERTIONS_CLASS_PATH));
                }
                PsiIdentifier methodIdentifier = method.getNameIdentifier();
                bestPracticeViolations.add(new BestPracticeViolation(
                        method,
                        methodIdentifier != null ? methodIdentifier.getTextRange() : method.getTextRange(),
                        message,
                        hints,
                        this.getCheckedBestPractice().get(0),
                        psiTryStatements.stream().map(assertion -> (PsiElement) assertion).collect(Collectors.toList())))
                ;
            }
        }
        return bestPracticeViolations;
    }

    private List<PsiTryStatement> getTryStatements(PsiMethod method) {
        List<PsiTryStatement> tryStatements = new ArrayList<>();
        PsiCodeBlock psiCodeBlock = method.getBody();
        if (psiCodeBlock != null) {

            tryStatements.addAll(new ArrayList<>(javaElementHelper.getElementsByType(psiCodeBlock, PsiTryStatement.class)));
            List<PsiMethodCallExpression> psiMethodCallExpressions = getRelevantMethodCalls(psiCodeBlock);
            for (PsiMethodCallExpression psiMethodCallExpression : psiMethodCallExpressions) {
                PsiMethod referencedMethod = psiMethodCallExpression.resolveMethod();
                if (referencedMethod != null && javaElementHelper.isInTestContext(referencedMethod)) {
                    tryStatements.addAll(getTryStatements(referencedMethod));
                }
            }
        }
        return tryStatements;
    }

    private List<PsiMethodCallExpression> getRelevantMethodCalls(PsiElement psiElement) {
        List<PsiMethodCallExpression> psiMethodCallExpressions = new ArrayList<>();
        List<PsiElement> children = Arrays.stream(psiElement.getChildren()).collect(Collectors.toList());
        for (PsiElement child : children) {
            if (child instanceof PsiMethodCallExpression) {
                psiMethodCallExpressions.add((PsiMethodCallExpression) child);
            }
            psiMethodCallExpressions.addAll(getRelevantMethodCalls(child));
        }
        return psiMethodCallExpressions;
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.CATCH_EXCEPTIONS_USING_FRAMEWORK_TOOLS);
    }
}
