package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.*;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaElementResolver;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.junit.JUnitConstants;
import com.testspector.model.enums.BestPractice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.testspector.model.checking.java.junit.JUnitConstants.JUNIT5_PARAMETERIZED_TEST_ABSOLUTE_PATH;

public class NoConditionalLogicJUnitCheckingStrategy implements BestPracticeCheckingStrategy {

    private final JavaElementResolver elementResolver;
    private final JavaContextIndicator contextResolver;
    private final JavaMethodResolver methodResolver;

    public NoConditionalLogicJUnitCheckingStrategy(JavaElementResolver elementResolver, JavaContextIndicator contextResolver, JavaMethodResolver methodResolver) {
        this.elementResolver = elementResolver;
        this.contextResolver = contextResolver;
        this.methodResolver = methodResolver;
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiElement psiElement) {
        return checkBestPractices(Collections.singletonList(psiElement));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiElement> psiElements) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        List<PsiMethod> methods = methodResolver.testMethodsWithAnnotations(psiElements, JUnitConstants.JUNIT_ALL_TEST_QUALIFIED_NAMES);
        for (PsiMethod method : methods) {
            List<PsiStatement> statements = elementResolver.allChildrenOfType(method, PsiStatement.class, isConditionalStatement(), contextResolver.isInTestContext());

            statements = statements.stream().distinct().collect(Collectors.toList());
            if (statements.size() > 0) {
                List<String> hints = new ArrayList<>();
                hints.add("Remove statements and create separate test scenario for each branch");
                if (Arrays.stream(method.getAnnotations()).anyMatch(psiAnnotation -> JUnitConstants.JUNIT5_TEST_QUALIFIED_NAMES.contains(psiAnnotation.getQualifiedName()))) {
                    hints.add(String.format("You are using JUnit5 so it can be solved by using %s", JUNIT5_PARAMETERIZED_TEST_ABSOLUTE_PATH));
                }
                PsiIdentifier methodIdentifier = method.getNameIdentifier();
                bestPracticeViolations.add(new BestPracticeViolation(
                                method,
                                methodIdentifier != null ? methodIdentifier.getTextRange() : method.getTextRange(),
                                "Conditional logic should not be part of the test method, it makes test hard to understand and read.",
                                hints,
                                getCheckedBestPractice().get(0),
                                statements.stream().map(statement -> (PsiElement) statement).collect(Collectors.toList())
                        )

                );
            }

        }
        return bestPracticeViolations;
    }

    Predicate<PsiStatement> isConditionalStatement() {
        return psiStatement -> psiStatement instanceof PsiIfStatement
                || psiStatement instanceof PsiForStatement
                || psiStatement instanceof PsiWhileStatement
                || psiStatement instanceof PsiSwitchStatement;
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.NO_CONDITIONAL_LOGIC);
    }
}
