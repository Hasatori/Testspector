package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.*;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.common.search.ElementSearchEngine;
import com.testspector.model.checking.java.common.search.ElementSearchResult;
import com.testspector.model.checking.java.common.search.QueriesRepository;
import com.testspector.model.checking.java.junit.strategy.action.*;
import com.testspector.model.enums.BestPractice;

import java.util.*;
import java.util.stream.Collectors;

public class CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy extends JUnitBestPracticeCheckingStrategy {


    private static final String DEFAULT_PROBLEM_DESCRIPTION_MESSAGE = "Tests should not contain try catch block. " +
            "These blocks are redundant and make test harder to read and understand. " +
            "In some cases it might even lead to never failing tests " +
            "if we are not handling the exception properly.";

    public CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy(ElementSearchEngine elementSearchEngine, JavaContextIndicator contextIndicator, JavaMethodResolver methodResolver) {
        super(elementSearchEngine, contextIndicator, methodResolver);
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
            boolean usingJUnit5 = areJUnit5ClassesAvailable(testMethod);
            boolean usingJUnit4 = areJUnit4ClassesAvailable(testMethod);
            for (PsiTryStatement psiTryStatement : psiTryStatementsElementSearchResult.getElementsFromAllLevels()) {
                bestPracticeViolations.add(createBestPracticeViolation(testMethod, psiTryStatement,usingJUnit5,usingJUnit4));
            }
            bestPracticeViolations.addAll(createBestPracticeViolation(psiTryStatementsElementSearchResult));


        }
        return bestPracticeViolations;
    }

    private boolean noMethodCallThrowsAnyOfCaughtExceptions(ElementSearchResult<PsiMethodCallExpression> productionMethodCalls, List<PsiType> caughtTypes) {
        return productionMethodCalls
                .getElementsFromAllLevels()
                .stream()
                .map(PsiCall::resolveMethod)
                .filter(Objects::nonNull)
                .noneMatch(method -> Arrays.stream(method.getThrowsList().getReferencedTypes())
                        .anyMatch(psiClassType -> caughtTypes.stream().anyMatch(psiClassType::isAssignableFrom)));
    }

    private boolean catchTypesContainAssertFail(PsiTryStatement psiTryStatement) {
        return Arrays.stream(psiTryStatement.getCatchSections())
                .anyMatch(psiCatchSection -> elementSearchEngine
                        .findByQuery(psiCatchSection, QueriesRepository.FIND_ALL_ASSERTION_METHOD_CALL_EXPRESSIONS)
                        .getElementsFromAllLevels().stream().anyMatch(psiMethodCallExpression -> Optional
                                .ofNullable(psiMethodCallExpression.resolveMethod())
                                .map(PsiMethod::getName)
                                .map(name -> name.toLowerCase().contains("fail"))
                                .isPresent()));
    }

    private BestPracticeViolation createBestPracticeViolation(PsiMethod testMethod, PsiTryStatement psiTryStatement,boolean usingJUnit5,boolean usingJUnit4) {
        List<String> hints = new ArrayList<>();
        List<Action<BestPracticeViolation>> actions = new ArrayList<>();
        ElementSearchResult<PsiMethodCallExpression> methodCallsThrowingAnyException = elementSearchEngine.findByQuery(psiTryStatement.getTryBlock(), QueriesRepository.FIND_ALL_METHOD_CALL_EXPRESSIONS_THROWING_ANY_EXCEPTION_WITHOUT_REFERENCES);
        ElementSearchResult<PsiMethodCallExpression> assertions = elementSearchEngine.findByQuery(testMethod, QueriesRepository.FIND_ALL_ASSERTION_METHOD_CALL_EXPRESSIONS);
        List<PsiType> caughtTypes = Arrays.stream(psiTryStatement.getCatchSections()).map(PsiCatchSection::getCatchType).collect(Collectors.toList());
        if (noMethodCallThrowsAnyOfCaughtExceptions(methodCallsThrowingAnyException, caughtTypes)) {
            actions.add(new RemoveTryCatchStatementAction(psiTryStatement, false));
        } else {
            actions.add(new RemoveTryCatchStatementAction(psiTryStatement, true));
        }
        HashMap<PsiType, List<PsiMethodCallExpression>> exceptionTestMethodsMap = new HashMap<>();
        caughtTypes.forEach(catchType -> methodCallsThrowingAnyException.getElementsOfCurrentLevel().forEach(methodCallThrowingException -> {
            if (Optional.ofNullable(methodCallThrowingException.resolveMethod())
                    .map(PsiMethod::getThrowsList)
                    .map(psiReferenceList -> Arrays.asList(psiReferenceList.getReferencedTypes()))
                    .stream()
                    .anyMatch(psiClassTypes -> psiClassTypes.stream().anyMatch(catchType::isAssignableFrom))) {
                List<PsiMethodCallExpression> methodCallExpressions = Optional
                        .ofNullable(exceptionTestMethodsMap.get(catchType))
                        .orElse(new ArrayList<>());
                boolean contains = false;
                for (Map.Entry<PsiType, List<PsiMethodCallExpression>> entry : exceptionTestMethodsMap.entrySet()) {
                    PsiType key = entry.getKey();
                    List<PsiMethodCallExpression> value = entry.getValue();
                    contains = value.contains(methodCallThrowingException);
                    if (contains) {
                        break;
                    }
                }
                if (!contains) {
                    methodCallExpressions.add(methodCallThrowingException);
                    exceptionTestMethodsMap.put(catchType, methodCallExpressions);
                }
            }
        }));
        if (exceptionTestMethodsMap.isEmpty()) {
            actions.add(new RemoveTryCatchStatementAction(psiTryStatement, true));
        } else if (usingJUnit5) {
            actions.add(new ReplaceTryCatchWithAssertThrows(psiTryStatement, exceptionTestMethodsMap));
            actions.add(new ReplaceTryCatchWithAssertDoesNotThrow(psiTryStatement));
        } else if (usingJUnit4) {
            if (exceptionTestMethodsMap.size() > 1) {
                actions.add(new TestExceptionUsingExpectedJUnit4Test(testMethod, psiTryStatement, exceptionTestMethodsMap.keySet().stream().findFirst().get()));
            }
        }

        return new
                BestPracticeViolation(
                psiTryStatement,
                DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                this.getCheckedBestPractice().get(0),
                actions,
                hints
        );
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
