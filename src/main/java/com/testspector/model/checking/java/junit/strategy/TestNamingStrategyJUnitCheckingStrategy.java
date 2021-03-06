package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.*;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaElementResolver;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.junit.JUnitConstants;
import com.testspector.model.enums.BestPractice;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.util.*;
import java.util.stream.Collectors;

public class TestNamingStrategyJUnitCheckingStrategy implements BestPracticeCheckingStrategy {

    private final JavaElementResolver javaElementResolver;
    private final JavaMethodResolver methodResolver;
    private final JavaContextIndicator contextIndicator;

    public TestNamingStrategyJUnitCheckingStrategy(JavaElementResolver javaElementResolver, JavaMethodResolver methodResolver, JavaContextIndicator contextIndicator) {
        this.javaElementResolver = javaElementResolver;
        this.methodResolver = methodResolver;
        this.contextIndicator = contextIndicator;
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiElement psiElement) {
        return checkBestPractices(Collections.singletonList(psiElement));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiElement> psiElements) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        List<PsiMethod> methods = methodResolver.immediateMethodsWithAnnotations(psiElements, JUnitConstants.JUNIT_ALL_TEST_QUALIFIED_NAMES);

        for (PsiMethod testMethod : methods) {
            PsiIdentifier nameIdentifier = testMethod.getNameIdentifier();
            if (nameIdentifier != null) {
                String testMethodName = nameIdentifier.getText();
                List<PsiMethod> methodsWithAlmostSameName = javaElementResolver
                        .allChildrenOfTypeWithReferencesThatMeetCondition(testMethod, PsiMethodCallExpression.class, contextIndicator::isInTestContext)
                        .stream()
                        .map(PsiCall::resolveMethod)
                        .filter(Objects::nonNull)
                        .filter(method -> FuzzySearch.ratio(testMethodName.toLowerCase(), method.getName().toLowerCase()) > 75)
                        .collect(Collectors.toList());
                if (methodsWithAlmostSameName.size() > 1) {
                    bestPracticeViolations.add(new BestPracticeViolation(
                            testMethod,
                            nameIdentifier.getTextRange(),
                            "The test name is more or less that same as a tested method. This says nothing about tests scenarion. You should setup a clear strategy for naming your tests so that the person reading then knows what is tests",
                            Arrays.asList(
                                    "Possible strategy: 'doingSomeOperationGeneratesSomeResult'",
                                    "Possible strategy: 'someResultOccursUnderSomeCondition'",
                                    "Possible strategy: 'given-when-then'",
                                    "Possible strategy: 'givenSomeContextWhenDoingSomeBehaviorThenSomeResultOccurs'",
                                    "Chosen naming strategy is subjective. The key thing to remember is that name of the test should say: What is tests, What are the conditions, What is expected result"
                            ),
                            getCheckedBestPractice().get(0),
                            methodsWithAlmostSameName.stream().map(method -> (PsiElement) method).collect(Collectors.toList())
                    ));
                }
            }


        }

        return bestPracticeViolations;
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.SETUP_A_TEST_NAMING_STRATEGY);
    }
}
