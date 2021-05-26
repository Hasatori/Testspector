package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaElementResolver;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.enums.BestPractice;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SetupTestNamingStrategyJUnitCheckingStrategy implements BestPracticeCheckingStrategy<PsiMethod> {

    private final JavaElementResolver elementResolver;
    private final JavaMethodResolver methodResolver;
    private final JavaContextIndicator contextIndicator;

    public SetupTestNamingStrategyJUnitCheckingStrategy(JavaElementResolver elementResolver, JavaMethodResolver methodResolver, JavaContextIndicator contextIndicator) {
        this.elementResolver = elementResolver;
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
                List<PsiMethodCallExpression> allTestedMethod = methodResolver.allTestedMethodsExpressions(testMethod);
                List<PsiMethodCallExpression> methodsWithAlmostSameName = allTestedMethod
                        .stream()
                        .filter(testedMethodCall -> {
                            PsiMethod testedMethod = testedMethodCall.resolveMethod();
                            if (testedMethod != null) {
                                int minRatio = selectMinRatio(testedMethod.getName());
                                return FuzzySearch.ratio(
                                        testMethodName.toLowerCase(),
                                        testedMethod.getName().toLowerCase()) > minRatio;
                            }
                            return false;
                        })
                        .collect(Collectors.toList());
                if (methodsWithAlmostSameName.size() >= 1) {
                    bestPracticeViolations.add(createBestPracticeViolation(
                            testMethod,
                            nameIdentifier,
                            methodsWithAlmostSameName));
                }
            }
        }

        return bestPracticeViolations;
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


    private BestPracticeViolation createBestPracticeViolation(PsiMethod testMethod, PsiIdentifier nameIdentifier, List<PsiMethodCallExpression> methodsWithAlmostSameName) {
        return new BestPracticeViolation(
                testMethod.getNameIdentifier(),
                "The test name is more or less the same as the tested method. " +
                        "This says nothing about tests scenario. You should setup a clear strategy " +
                        "for naming your tests so that the person reading then knows what is tested",
                getCheckedBestPractice().get(0),
                methodsWithAlmostSameName.stream().map(methodWithAlmostSameNameCall -> (PsiElement) methodWithAlmostSameNameCall.getMethodExpression()).collect(Collectors.toList()),
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
