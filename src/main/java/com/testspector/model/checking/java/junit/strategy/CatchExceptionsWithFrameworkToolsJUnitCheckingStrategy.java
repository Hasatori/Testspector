package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.*;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaElementResolver;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.enums.BestPractice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.testspector.model.checking.java.junit.JUnitConstants.*;

public class CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy implements BestPracticeCheckingStrategy<PsiMethod> {

    private final JavaElementResolver elementResolver;
    private final JavaContextIndicator contextResolver;
    private final JavaMethodResolver methodResolver;

    public CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy(JavaElementResolver elementResolver, JavaContextIndicator contextResolver, JavaMethodResolver methodResolver) {
        this.elementResolver = elementResolver;
        this.contextResolver = contextResolver;
        this.methodResolver = methodResolver;
    }


    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiMethod method) {
        return this.checkBestPractices(Collections.singletonList(method));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiMethod> methods) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        for (PsiMethod testMethod : methods) {
            List<PsiTryStatement> psiTryStatements = elementResolver
                    .allChildrenOfTypeWithReferences(
                            testMethod,
                            PsiTryStatement.class,
                            contextResolver.isInTestContext());
            if (psiTryStatements.size() > 0) {
                bestPracticeViolations.add(createBestPracticeViolation(testMethod, psiTryStatements));
            }
        }
        return bestPracticeViolations;
    }

    private Optional<PsiReferenceExpression> firstReferenceToTryStatement(PsiElement element, PsiTryStatement statement) {
        List<PsiReferenceExpression> references = elementResolver.allChildrenOfTypeMeetingConditionWithReferences(
                element,
                PsiReferenceExpression.class);
        for (PsiReferenceExpression reference : references) {
            if (!elementResolver.allChildrenOfTypeMeetingConditionWithReferences(
                    reference.getParent(),
                    PsiStatement.class,
                    psiStatement -> statement == psiStatement,
                    contextResolver.isInTestContext()).isEmpty()) {
                return Optional.of(reference);
            }
        }
        return Optional.empty();
    }

    private BestPracticeViolation createBestPracticeViolation(PsiMethod testMethod, List<PsiTryStatement> psiTryStatements) {
        List<String> hints = new ArrayList<>();
        String message = "Tests should not contain try catch block. " +
                "These blocks are redundant and make test harder to read and understand. " +
                "In some cases it might even lead to never failing tests " +
                "if we are not handling the exception properly.";
        hints.add("If catching an exception is not part of a test then just delete it.");
        if (methodResolver.methodHasAnyOfAnnotations(testMethod, JUNIT5_TEST_QUALIFIED_NAMES)) {
            hints.add(String.format("If catching an exception is part of a test " +
                            "then since you are using JUnit5 it can be solved by using %s.assertThrows() method"
                    , JUNIT5_ASSERTIONS_CLASS_PATH));
        }
        if (methodResolver.methodHasAnyOfAnnotations(testMethod, JUNIT4_TEST_QUALIFIED_NAMES)) {
            hints.add(String.format(
                    "If catching an exception is part of a test then since you are using JUnit4 " +
                            "it can be solved by using @%s.Test(expected = Exception.class) for the test method"
                    , JUNIT4_ASSERTIONS_CLASS_PATH));
        }
        return new BestPracticeViolation(
                testMethod,
                message,
                this.getCheckedBestPractice().get(0),
                psiTryStatements.stream().map(statement -> (PsiElement) statement).collect(Collectors.toList()),
                null);
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.CATCH_TESTED_EXCEPTIONS_USING_FRAMEWORK_TOOLS);
    }
}
