package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.*;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.common.search.ElementSearchEngine;
import com.testspector.model.checking.java.common.search.ElementSearchResult;
import com.testspector.model.checking.java.junit.strategy.action.NavigateElementAction;
import com.testspector.model.enums.BestPractice;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SetupTestNamingStrategyJUnitCheckingStrategy implements BestPracticeCheckingStrategy<PsiMethod> {

    private final ElementSearchEngine elementSearchEngine;
    private final JavaMethodResolver methodResolver;
    private final JavaContextIndicator contextIndicator;

    private static final String DEFAULT_PROBLEM_DESCRIPTION_MESSAGE = "The test name is more or less the same as the tested method. " +
            "This says nothing about tests scenario. You should setup a clear strategy " +
            "for naming your tests so that the person reading then knows what is tested";

    public SetupTestNamingStrategyJUnitCheckingStrategy(ElementSearchEngine elementSearchEngine, JavaMethodResolver methodResolver, JavaContextIndicator contextIndicator) {
        this.elementSearchEngine = elementSearchEngine;
        this.methodResolver = methodResolver;
        this.contextIndicator = contextIndicator;
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiMethod method) {
        return checkBestPractices(Collections.singletonList(method));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiMethod> methods) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        for (PsiMethod testMethod : methods) {
            PsiIdentifier nameIdentifier = testMethod.getNameIdentifier();
            if (nameIdentifier != null) {
                String testMethodName = nameIdentifier.getText();
                ElementSearchResult<PsiMethodCallExpression> allTestedMethodsResult = methodResolver.allTestedMethodsMethodCalls(testMethod);
                allTestedMethodsResult = removeTestedMethodsWithDifferentName(testMethodName, allTestedMethodsResult);
                for (PsiMethodCallExpression methodCallExpression : allTestedMethodsResult.getElementsFromAllLevels()) {
                    bestPracticeViolations.add(createBestPracticeViolation(methodCallExpression));
                }
                bestPracticeViolations.addAll(createBestPracticeViolation(allTestedMethodsResult));

            }
        }

        return bestPracticeViolations;
    }

    private ElementSearchResult<PsiMethodCallExpression> removeTestedMethodsWithDifferentName(String testMethodName, ElementSearchResult<PsiMethodCallExpression> allTestedMethodsResult) {
        List<PsiMethodCallExpression> notToRemove = allTestedMethodsResult.getElementsOfCurrentLevel().stream()
                .filter(testedMethodCall -> {
                    PsiMethod testedMethod = testedMethodCall.resolveMethod();
                    if (testedMethod != null) {
                        int minRatio = selectMinRatio(testedMethod.getName());
                        return FuzzySearch.ratio(
                                testMethodName.toLowerCase(),
                                testedMethod.getName().toLowerCase()) > minRatio;
                    }
                    return false;
                }).collect(Collectors.toList());
        List<Pair<PsiReferenceExpression, ElementSearchResult<PsiMethodCallExpression>>> referencedElements = new ArrayList<>();
        for (Pair<PsiReferenceExpression, ElementSearchResult<PsiMethodCallExpression>> referencedResult : allTestedMethodsResult.getReferencedResults()) {
            ElementSearchResult<PsiMethodCallExpression> newReferencedResult = removeTestedMethodsWithDifferentName(testMethodName, referencedResult.getRight());
            referencedElements.add(Pair.of(referencedResult.getLeft(), newReferencedResult));
        }
        return new ElementSearchResult<>(referencedElements, notToRemove);
    }

    private int selectMinRatio(String testedMethodName) {
        int testedMethodNameLength = testedMethodName.length();
        if (testedMethodNameLength == 1) {
            return 28;
        } else if (testedMethodNameLength == 2) {
            return 43;
        } else if (testedMethodNameLength > 2 && testedMethodNameLength <= 5) {
            return 50;
        } else {
            return 70;
        }
    }

    private List<BestPracticeViolation> createBestPracticeViolation(ElementSearchResult<PsiMethodCallExpression> elementSearchResult) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        elementSearchResult.getReferencedResults()
                .forEach(result -> {
                    List<PsiMethodCallExpression> assertionMethods = result.getRight().getElementsFromAllLevels();
                    if (!assertionMethods.isEmpty()) {
                        if (result.getLeft().getParent() instanceof PsiMethodCallExpression) {
                            bestPracticeViolations.add(createBestPracticeViolation("Following method breaks best practice. ", result.getLeft(), assertionMethods));
                        } else {
                            bestPracticeViolations.add(createBestPracticeViolation(result.getLeft(), assertionMethods));
                        }
                    }
                    bestPracticeViolations.addAll(createBestPracticeViolation(result.getRight()));
                });
        return bestPracticeViolations;
    }


    private BestPracticeViolation createBestPracticeViolation(PsiReference reference, List<PsiMethodCallExpression> elements) {
        return createBestPracticeViolation("", reference, elements);
    }

    private BestPracticeViolation createBestPracticeViolation(String descriptionPrefix, PsiReference reference, List<PsiMethodCallExpression> testedMethods) {
        return new BestPracticeViolation(
                reference.getElement(),
                descriptionPrefix + DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                getCheckedBestPractice().get(0),
                testedMethods.stream()
                        .map(testedMethod -> new NavigateElementAction("method call", testedMethod))
                        .collect(Collectors.toList())
        );

    }

    private BestPracticeViolation createBestPracticeViolation(PsiMethodCallExpression methodWithAlmostSameName) {
        return new BestPracticeViolation(
                methodWithAlmostSameName.getMethodExpression(),
                DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                getCheckedBestPractice().get(0),
                null,
                Arrays.asList(
                        "Possible strategy: 'doingSomeOperationGeneratesSomeResult'",
                        "Possible strategy: 'someResultOccursUnderSomeCondition'",
                        "Possible strategy: 'given-when-then'",
                        "Possible strategy: 'givenSomeContextWhenDoingSomeBehaviorThenSomeResultOccurs'",
                        "Possible strategy: 'whatIsTested_conditions_expectedResult'",
                        "Chosen naming strategy is subjective. The key thing to remember is that name of the " +
                                "test should say: What is tests, What are the conditions, What is expected result"
                ));
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.SETUP_A_TEST_NAMING_STRATEGY);
    }
}
