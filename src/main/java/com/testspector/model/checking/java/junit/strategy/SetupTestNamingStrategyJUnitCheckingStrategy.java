package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.common.search.ElementSearchEngine;
import com.testspector.model.checking.java.common.search.ElementSearchResult;
import com.testspector.model.checking.java.junit.strategy.action.NavigateElementAction;
import com.testspector.model.enums.BestPractice;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.testspector.model.checking.java.common.search.ElementSearchResultUtils.filterResult;

public class SetupTestNamingStrategyJUnitCheckingStrategy extends JUnitBestPracticeCheckingStrategy {

    private static final String DEFAULT_PROBLEM_DESCRIPTION_MESSAGE = "The test name is more or less the same as the tested method. " +
            "This says nothing about tests scenario. You should setup a clear strategy " +
            "for naming your tests so that the person reading then knows what is tested";

    public SetupTestNamingStrategyJUnitCheckingStrategy(ElementSearchEngine elementSearchEngine, JavaContextIndicator contextIndicator, JavaMethodResolver methodResolver) {
        super(elementSearchEngine, contextIndicator, methodResolver);
    }


    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiMethod method) {
        return checkBestPractices(Collections.singletonList(method));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiMethod> methods) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        methods = methods.stream().filter(method -> method.getNameIdentifier() != null).collect(Collectors.toList());
        for (PsiMethod testMethod : methods) {
            ElementSearchResult<PsiMethodCallExpression> allTestedMethodsResult = methodResolver.allTestedMethodsMethodCalls(testMethod);
            allTestedMethodsResult = filterMethodsWithNameTooSimilarToTestMethod(testMethod,allTestedMethodsResult);
            for (PsiMethodCallExpression methodCallExpression : allTestedMethodsResult.getElementsFromAllLevels()) {
                bestPracticeViolations.add(createBestPracticeViolation(methodCallExpression));
            }
            bestPracticeViolations.addAll(createBestPracticeViolation(allTestedMethodsResult));
        }

        return bestPracticeViolations;
    }

    private ElementSearchResult<PsiMethodCallExpression> filterMethodsWithNameTooSimilarToTestMethod(PsiMethod testMethod, ElementSearchResult<PsiMethodCallExpression> allTestedMethodsResult){
        String testMethodName = testMethod.getNameIdentifier().getText();
       return filterResult(testedMethodCall -> {
            PsiMethod testedMethod = testedMethodCall.resolveMethod();
            if (testedMethod != null) {
                int minRatio = selectMinRatio(testedMethod.getName());
                return FuzzySearch.ratio(
                        testMethodName.toLowerCase(),
                        testedMethod.getName().toLowerCase()) > minRatio;
            }
            return false;
        }, allTestedMethodsResult);
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
                    if (result.getLeft().getParent() instanceof PsiMethodCallExpression && !assertionMethods.isEmpty()) {
                        bestPracticeViolations.add(createBestPracticeViolation(getMethodCallExpressionIdentifier((PsiMethodCallExpression) result.getLeft().getParent()), assertionMethods));
                    }
                    bestPracticeViolations.addAll(createBestPracticeViolation(result.getRight()));
                });
        return bestPracticeViolations;
    }

    private BestPracticeViolation createBestPracticeViolation(PsiElement element, List<PsiMethodCallExpression> testedMethods) {
        return new BestPracticeViolation(
                element,
                "Following method breaks best practice." + DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                getCheckedBestPractice().get(0),
                testedMethods.stream()
                        .map(testedMethod -> new NavigateElementAction("method call", testedMethod))
                        .collect(Collectors.toList())
        );

    }

    private BestPracticeViolation createBestPracticeViolation(PsiMethodCallExpression methodWithAlmostSameName) {
        return new BestPracticeViolation(
                getMethodCallExpressionIdentifier(methodWithAlmostSameName),
                DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                getCheckedBestPractice().get(0),
                new ArrayList<>(),
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
