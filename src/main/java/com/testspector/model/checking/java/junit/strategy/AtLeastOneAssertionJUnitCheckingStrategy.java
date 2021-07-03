package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.ElementSearchResult;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaElementResolver;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.enums.BestPractice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AtLeastOneAssertionJUnitCheckingStrategy extends AssertionCountJUnitCheckingStrategy{


    public AtLeastOneAssertionJUnitCheckingStrategy(JavaElementResolver elementResolver, JavaContextIndicator contextIndicator, JavaMethodResolver methodResolver) {
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
            ElementSearchResult<PsiMethodCallExpression> allAssertionMethodsResult = elementResolver
                    .allChildrenOfTypeMeetingConditionWithReferences(
                            testMethod,
                            PsiMethodCallExpression.class,
                            (psiMethodCallExpression -> methodResolver
                                    .assertionMethod(psiMethodCallExpression)
                                    .isPresent())
                            , methodInTestContext());
            removeGroupedAssertions(allAssertionMethodsResult);
            PsiIdentifier methodIdentifier = testMethod.getNameIdentifier();
            if (allAssertionMethodsResult.getElementsFromAllLevels().isEmpty()) {
                bestPracticeViolations.add(createAtLeastOneAssertionBestPracticeViolation(
                        testMethod,
                        methodIdentifier));
            }
        }
        return bestPracticeViolations;
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.AT_LEAST_ONE_ASSERTION);
    }


    private BestPracticeViolation createAtLeastOneAssertionBestPracticeViolation(PsiMethod testMethod,
                                                                                   PsiIdentifier methodIdentifier) {
        return new BestPracticeViolation(
                methodIdentifier,
                "Test should contain at least one assertion method!",
                BestPractice.AT_LEAST_ONE_ASSERTION,
                null);
    }
}
