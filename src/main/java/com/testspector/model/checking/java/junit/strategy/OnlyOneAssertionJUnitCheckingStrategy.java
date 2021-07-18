package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReference;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.common.search.ElementSearchEngine;
import com.testspector.model.checking.java.common.search.ElementSearchResult;
import com.testspector.model.checking.java.common.search.QueriesRepository;
import com.testspector.model.checking.java.junit.JUnitConstants;
import com.testspector.model.checking.java.junit.strategy.action.NavigateElementAction;
import com.testspector.model.checking.java.junit.strategy.action.WrapAllAssertionsIntoAssertAll;
import com.testspector.model.enums.BestPractice;

import java.util.*;
import java.util.stream.Collectors;

public class OnlyOneAssertionJUnitCheckingStrategy extends AssertionCountJUnitCheckingStrategy {

    private static final String DEFAULT_PROBLEM_DESCRIPTION_MESSAGE = "Test should fail for only one reason. " +
            "Using multiple assertions in JUnit leads to that if " +
            "one assertion fails other will not be executed and " +
            "therefore you will not get overview of all problems.";

    public OnlyOneAssertionJUnitCheckingStrategy(ElementSearchEngine elementSearchEngine, JavaContextIndicator contextIndicator, JavaMethodResolver methodResolver) {
        super(elementSearchEngine, contextIndicator, methodResolver);
    }


    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiMethod checkedElement) {
        return checkBestPractices(Collections.singletonList(checkedElement));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiMethod> methods) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        for (PsiMethod testMethod : methods) {
            ElementSearchResult<PsiMethodCallExpression> allAssertionMethodsResult =
                    elementSearchEngine.findByQuery(
                            testMethod, QueriesRepository.FIND_ALL_ASSERTION_METHOD_CALL_EXPRESSIONS);
            allAssertionMethodsResult = removeGroupedAssertions(allAssertionMethodsResult);
            List<PsiMethodCallExpression> allAssertionMethods = allAssertionMethodsResult.getElementsFromAllLevels();
            boolean areJUnit5ClassesAvailable = areJUnit5ClassesAvailable(testMethod);
            boolean isHamcrestAvailable = isHamcrestAvailable(testMethod);
            if (allAssertionMethods.size() > 0 && isJUnit4ExpectedTest(testMethod)) {
                bestPracticeViolations.add(new BestPracticeViolation(
                        Arrays.stream(testMethod.getAnnotations()).filter(psiAnnotation -> psiAnnotation.hasQualifiedName(JUnitConstants.JUNIT4_TEST_QUALIFIED_NAME)).findFirst().orElse(null),
                        "This test is with attribute \"expected\" which is assertion on it own but on the top of that there are some assertion methods in the test",
                        BestPractice.ONLY_ONE_ASSERTION,
                        new ArrayList<>(),
                        new ArrayList<>()
                ));
                for (PsiMethodCallExpression assertionMethod : allAssertionMethods) {
                    bestPracticeViolations.add(createDefaultOnlyOneBestPracticeViolation(testMethod, assertionMethod, allAssertionMethods.size() - 1, areJUnit5ClassesAvailable, isHamcrestAvailable, allAssertionMethods));
                }
                bestPracticeViolations.addAll(createBestPracticeViolation(allAssertionMethodsResult));
            } else if (allAssertionMethods.size() > 1) {

                for (PsiMethodCallExpression assertionMethod : allAssertionMethods) {
                    bestPracticeViolations.add(createDefaultOnlyOneBestPracticeViolation(testMethod, assertionMethod, allAssertionMethods.size() - 1, areJUnit5ClassesAvailable, isHamcrestAvailable, allAssertionMethods));
                }
                bestPracticeViolations.addAll(createBestPracticeViolation(allAssertionMethodsResult));
            }
        }
        return bestPracticeViolations;
    }


    private BestPracticeViolation createDefaultOnlyOneBestPracticeViolation(PsiMethod testMethod,
                                                                            PsiMethodCallExpression assertionMethod, int otherAssertionsCount, boolean areJUnit5ClassesAvailable, boolean isHamcrestAvailable, List<PsiMethodCallExpression> allAssertions) {
        List<String> hints = new ArrayList<>();
        List<Action<BestPracticeViolation>> actions = new ArrayList<>();
        String message = DEFAULT_PROBLEM_DESCRIPTION_MESSAGE + "There are " + otherAssertionsCount + " other assertion method calls in this test.";
        if (areJUnit5ClassesAvailable) {
            hints.add(String.format(
                    "You are using JUnit5 so it can be solved " +
                            "by wrapping multiple assertions into %s.assertAll() method",
                    JUnitConstants.JUNIT5_ASSERTIONS_CLASS_PATH));
            actions.add(new WrapAllAssertionsIntoAssertAll(testMethod, allAssertions));
        }
        if (isHamcrestAvailable) {
            hints.add("You can use hamcrest org.hamcrest.core.Every or org.hamcrest.core.AllOf matchers");
        }
        return new BestPracticeViolation(
                assertionMethod.getMethodExpression(),
                message,
                BestPractice.ONLY_ONE_ASSERTION,
                actions,
                hints);
    }

    private List<BestPracticeViolation> createBestPracticeViolation(ElementSearchResult<PsiMethodCallExpression> elementSearchResult) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        elementSearchResult.getReferencedResults()
                .forEach(result -> {
                    List<PsiMethodCallExpression> assertionMethods = result.getRight().getElementsFromAllLevels();
                    if (!assertionMethods.isEmpty()) {
                        bestPracticeViolations.add(createBestPracticeViolation(result.getLeft(), assertionMethods));
                    }
                    bestPracticeViolations.addAll(createBestPracticeViolation(result.getRight()));
                });
        return bestPracticeViolations;
    }

    private BestPracticeViolation createBestPracticeViolation(PsiReference reference, List<PsiMethodCallExpression> assertionMethods) {
        return new BestPracticeViolation(
                reference.getElement(),
                "Following method breaks best practice. " + DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                getCheckedBestPractice().get(0),
                assertionMethods.stream()
                        .map(assertionMethod -> new NavigateElementAction("assertion method", assertionMethod))
                        .collect(Collectors.toList())
        );

    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.ONLY_ONE_ASSERTION);
    }

}
