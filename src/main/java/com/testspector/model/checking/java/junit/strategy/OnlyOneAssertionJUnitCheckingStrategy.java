package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
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

public class OnlyOneAssertionJUnitCheckingStrategy extends AssertionCountJUnitCheckingStrategy {

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
            List<PsiMethodCallExpression> allAssertionMethods = elementResolver
                    .allChildrenOfTypeMeetingConditionWithReferences(
                            testMethod,
                            PsiMethodCallExpression.class,
                            (psiMethodCallExpression -> methodResolver
                                    .assertionMethod(psiMethodCallExpression)
                                    .isPresent())
                            , methodInTestContext());
            removeGroupedAssertions(allAssertionMethods);
            PsiIdentifier methodIdentifier = testMethod.getNameIdentifier();
            if (allAssertionMethods.size() > 1) {
                allAssertionMethods.forEach(assertionMethod -> {
                    bestPracticeViolations.add(createOnlyOneBestPracticeViolation(
                            testMethod, assertionMethod, methodIdentifier
                    ));
                });
            }
        }
        return bestPracticeViolations;
    }


    private BestPracticeViolation createOnlyOneBestPracticeViolation(PsiMethod testMethod,
                                                                     PsiMethodCallExpression assertionMethod,
                                                                     PsiIdentifier methodIdentifier) {
        List<String> hints = new ArrayList<>();
        String message = "Test should fail for only one reason. " +
                "Using multiple assertions in JUnit leads to that if " +
                "one assertion fails other will not be executed and " +
                "therefore you will not get overview of all problems.";
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
                testMethod,
                message,
                BestPractice.ONLY_ONE_ASSERTION,
                Collections.singletonList(assertionMethod.getMethodExpression()),
                null,
                hints);
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.ONLY_ONE_ASSERTION);
    }

}
