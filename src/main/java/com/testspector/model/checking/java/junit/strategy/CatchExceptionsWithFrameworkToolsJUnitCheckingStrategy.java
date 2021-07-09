package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiTryStatement;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.common.search.ElementSearchEngine;
import com.testspector.model.checking.java.common.search.ElementSearchResult;
import com.testspector.model.checking.java.common.search.QueriesRepository;
import com.testspector.model.checking.java.junit.strategy.action.NavigateElementAction;
import com.testspector.model.checking.java.junit.strategy.action.RemoveTryCatchStatementAction;
import com.testspector.model.enums.BestPractice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.testspector.model.checking.java.junit.JUnitConstants.*;

public class CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy implements BestPracticeCheckingStrategy<PsiMethod> {

    private final ElementSearchEngine elementSearchEngine;
    private final JavaContextIndicator contextResolver;
    private final JavaMethodResolver methodResolver;
    private static final String DEFAULT_PROBLEM_DESCRIPTION_MESSAGE = "Tests should not contain try catch block. " +
            "These blocks are redundant and make test harder to read and understand. " +
            "In some cases it might even lead to never failing tests " +
            "if we are not handling the exception properly.";

    public CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy(ElementSearchEngine elementSearchEngine, JavaContextIndicator contextResolver, JavaMethodResolver methodResolver) {
        this.elementSearchEngine = elementSearchEngine;
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

            ElementSearchResult<PsiTryStatement> psiTryStatementsElementSearchResult = elementSearchEngine
                    .findByQuery(testMethod, QueriesRepository.FIND_ALL_TRY_STATEMENTS);
            for (PsiTryStatement psiTryStatement : psiTryStatementsElementSearchResult.getElementsFromAllLevels()) {
                bestPracticeViolations.add(createBestPracticeViolation(testMethod, psiTryStatement));
            }
            bestPracticeViolations.addAll(createBestPracticeViolation(psiTryStatementsElementSearchResult));


        }
        return bestPracticeViolations;
    }


    private BestPracticeViolation createBestPracticeViolation(PsiMethod testMethod, PsiTryStatement psiTryStatement) {
        List<String> hints = new ArrayList<>();
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
                psiTryStatement,
                DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                this.getCheckedBestPractice().get(0),
                Collections.singletonList(new RemoveTryCatchStatementAction(psiTryStatement)));
    }

    private List<BestPracticeViolation> createBestPracticeViolation(ElementSearchResult<PsiTryStatement> elementSearchResult) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        elementSearchResult.getReferencedResults()
                .forEach(result -> {
                    List<PsiTryStatement> tryStatements = result.getRight().getElementsFromAllLevels();
                    if (!tryStatements.isEmpty()) {
                        bestPracticeViolations.add(createBestPracticeViolation(result.getLeft(), tryStatements));
                    }
                    bestPracticeViolations.addAll(createBestPracticeViolation(result.getRight()));
                });
        return bestPracticeViolations;
    }

    private BestPracticeViolation createBestPracticeViolation(PsiReference reference, List<PsiTryStatement> tryStatements) {
        return new BestPracticeViolation(
                reference.getElement(),
                "Following method breaks best practice. " + DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                getCheckedBestPractice().get(0),
                tryStatements.stream()
                        .map(tryStatement -> new NavigateElementAction("try catch statement", tryStatement))
                        .collect(Collectors.toList())
        );

    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.CATCH_TESTED_EXCEPTIONS_USING_FRAMEWORK_TOOLS);
    }
}
