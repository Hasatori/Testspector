package com.testspector.model.checking.java.junit.strategy;

import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.ElementSearchResult;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaElementResolver;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.junit.JUnitConstants;
import com.testspector.model.enums.BestPractice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OnlyOneAssertionJUnitCheckingStrategy extends AssertionCountJUnitCheckingStrategy {

    private static final String DEFAULT_PROBLEM_DESCRIPTION_MESSAGE =  "Test should fail for only one reason. " +
            "Using multiple assertions in JUnit leads to that if " +
            "one assertion fails other will not be executed and " +
            "therefore you will not get overview of all problems.";

    public OnlyOneAssertionJUnitCheckingStrategy(JavaElementResolver elementResolver, JavaContextIndicator contextIndicator, JavaMethodResolver methodResolver) {
        super(elementResolver, contextIndicator, methodResolver);
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
                    elementResolver
                            .allChildrenOfTypeMeetingConditionWithReferences(
                                    testMethod,
                                    PsiMethodCallExpression.class,
                                    (psiMethodCallExpression -> methodResolver
                                            .assertionMethod(psiMethodCallExpression)
                                            .isPresent())
                                    ,el-> el instanceof  PsiMethod && methodInTestContext().test(el));
            removeGroupedAssertions(allAssertionMethodsResult);
            List<PsiMethodCallExpression> allAssertionMethods = allAssertionMethodsResult.getAllElements();
/**/            if (allAssertionMethods.size() > 1) {
                for (PsiMethodCallExpression assertionMethod : allAssertionMethods) {
                    bestPracticeViolations.add(createOnlyOneBestPracticeViolation(testMethod, assertionMethod));
                }
                bestPracticeViolations.addAll(createBestPracticeViolation(allAssertionMethodsResult));
            }


        }
        return bestPracticeViolations;
    }


    private BestPracticeViolation createOnlyOneBestPracticeViolation(PsiMethod testMethod,
                                                                     PsiMethodCallExpression assertionMethod) {
        List<String> hints = new ArrayList<>();
        String message = DEFAULT_PROBLEM_DESCRIPTION_MESSAGE;
        if (isJUnit5TestMethod(testMethod)) {
            hints.add(String.format(
                    "You are using JUnit5 so it can be solved " +
                            "by wrapping multiple assertions into %s.assertAll() method",
                    JUnitConstants.JUNIT5_ASSERTIONS_CLASS_PATH));
        }
        if (containsHamcrestAssertion(Arrays.asList(assertionMethod))) {
            hints.add("You can use hamcrest org.hamcrest.core.Every or org.hamcrest.core.AllOf matchers");
        }
        return new BestPracticeViolation(
                assertionMethod.getMethodExpression(),
                message,
                BestPractice.ONLY_ONE_ASSERTION,
                null,
                hints);
    }

    private List<BestPracticeViolation> createBestPracticeViolation(ElementSearchResult<PsiMethodCallExpression> elementSearchResult){
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        elementSearchResult.getReferencedResults()
                .forEach(result -> {
                    List<PsiMethodCallExpression> assertionMethods = result.getRight().getAllElements();
                    if (!assertionMethods.isEmpty()){
                        bestPracticeViolations.add(createBestPracticeViolation(result.getLeft(), assertionMethods));
                    }
                    bestPracticeViolations.addAll(createBestPracticeViolation(result.getRight()));
                });
        return bestPracticeViolations;
    }

    private BestPracticeViolation createBestPracticeViolation(PsiReference reference, List<PsiMethodCallExpression> assertionMethods) {
        return new BestPracticeViolation(
                reference.getElement(),
                "Following method breaks best practice. "+ DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                getCheckedBestPractice().get(0),
                assertionMethods.stream().map(assertionMethod -> new Action<BestPracticeViolation>() {
                    @Override
                    public String getName() {
                        return "Go to assertion method in " + assertionMethod.getContainingFile().getName() + "(line " + (PsiDocumentManager.getInstance(assertionMethod.getProject()).getDocument(assertionMethod.getContainingFile()).getLineNumber(assertionMethod.getTextOffset())+1) + ")";
                    }

                    @Override
                    public void execute(BestPracticeViolation bestPracticeViolation) {
                        ((Navigatable) assertionMethod.getNavigationElement()).navigate(true);
                    }
                }).collect(Collectors.toList())
        );

    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.ONLY_ONE_ASSERTION);
    }

}
