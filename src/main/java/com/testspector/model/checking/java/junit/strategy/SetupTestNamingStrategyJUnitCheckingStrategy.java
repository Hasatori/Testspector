package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.crate.BestPracticeViolation;
import com.testspector.model.checking.crate.RelatedElementWrapper;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaElementResolver;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.enums.BestPractice;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.util.*;
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
                List<PsiMethod> allTestedMethod = methodResolver.allTestedMethods(testMethod);
                List<PsiMethod> methodsWithAlmostSameName = allTestedMethod
                        .stream()
                        .filter(method -> FuzzySearch.ratio(testMethodName.toLowerCase(), method.getName().toLowerCase()) > 70)
                        .collect(Collectors.toList());
                List<PsiMethod> methodsWithTooDifferentName = allTestedMethod
                        .stream()
                        .filter(method -> FuzzySearch.weightedRatio(testMethodName, method.getName()) < 55)
                        .collect(Collectors.toList());
                if (methodsWithAlmostSameName.size() >= 1) {
                    bestPracticeViolations.add(new BestPracticeViolation(
                            String.format("%s#%s", testMethod.getContainingClass().getQualifiedName(), testMethod.getName()),
                            testMethod,
                            nameIdentifier.getTextRange(),
                            "The test name is more or less that same as a tested method. This says nothing about tests scenario. You should setup a clear strategy for naming your tests so that the person reading then knows what is tested",
                            Arrays.asList(
                                    "Possible strategy: 'doingSomeOperationGeneratesSomeResult'",
                                    "Possible strategy: 'someResultOccursUnderSomeCondition'",
                                    "Possible strategy: 'given-when-then'",
                                    "Possible strategy: 'givenSomeContextWhenDoingSomeBehaviorThenSomeResultOccurs'",
                                    "Chosen naming strategy is subjective. The key thing to remember is that name of the test should say: What is tests, What are the conditions, What is expected result"
                            ),
                            getCheckedBestPractice().get(0),
                            createRelatedElements(testMethod, methodsWithAlmostSameName)
                    ));
                }
                if (methodsWithTooDifferentName.size() >= 1) {
                    bestPracticeViolations.add(new BestPracticeViolation(
                            String.format("%s#%s", testMethod.getContainingClass().getQualifiedName(), testMethod.getName()),
                            testMethod,
                            nameIdentifier.getTextRange(),
                            "The test name has nothing to do with tested method. This says nothing about tests scenario. You should set up a clear strategy for naming your tests so that the person reading then knows what is tested",
                            Arrays.asList(
                                    "Possible strategy: 'doingSomeOperationGeneratesSomeResult'",
                                    "Possible strategy: 'someResultOccursUnderSomeCondition'",
                                    "Possible strategy: 'given-when-then'",
                                    "Possible strategy: 'givenSomeContextWhenDoingSomeBehaviorThenSomeResultOccurs'",
                                    "Chosen naming strategy is subjective. The key thing to remember is that name of the test should say: What is tests, What are the conditions, What is expected result"
                            ),
                            getCheckedBestPractice().get(0),
                            createRelatedElements(testMethod, methodsWithTooDifferentName)
                    ));
                }
            }


        }

        return bestPracticeViolations;
    }
    private List<RelatedElementWrapper> createRelatedElements(PsiMethod method, List<PsiMethod> methodsWithAlmostSameName) {
        List<RelatedElementWrapper> result = new ArrayList<>();
        for (PsiMethod methodWithAlmostSameName : methodsWithAlmostSameName) {
            HashMap<PsiElement, String> elementNameHashMap = new HashMap<>();
            Optional<PsiReferenceExpression> optionalPsiReferenceExpression = firstReferenceToMethodWithAlmostSameName(method, methodWithAlmostSameName);
            if (optionalPsiReferenceExpression.isPresent()) {
                elementNameHashMap.put(optionalPsiReferenceExpression.get(), "simple method call from test");
                elementNameHashMap.put(methodWithAlmostSameName, "method call in production code");
            } else {
                elementNameHashMap.put(methodWithAlmostSameName, "method call in production code");
            }
            result.add(new RelatedElementWrapper(methodWithAlmostSameName.getName(), elementNameHashMap));
        }

        return result;
    }


    private Optional<PsiReferenceExpression> firstReferenceToMethodWithAlmostSameName(PsiElement element, PsiMethod methodWithAlmostSameName) {
        List<PsiReferenceExpression> references = elementResolver.allChildrenOfType(element, PsiReferenceExpression.class);
        for (PsiReferenceExpression reference : references) {
            if (!elementResolver.allChildrenOfType(
                    reference.getParent(),
                    PsiMethodCallExpression.class,
                    psiMethodCallExpression -> psiMethodCallExpression.resolveMethod() != null && psiMethodCallExpression.resolveMethod() == methodWithAlmostSameName,
                    contextIndicator.isInTestContext()).isEmpty()
                    || !elementResolver.allChildrenOfType(
                    reference.getParent(),
                    PsiLiteralExpression.class,
                    psiLiteralExpression -> Arrays.stream(ReferenceProvidersRegistry.getReferencesFromProviders(psiLiteralExpression))
                            .map(PsiReference::resolve)
                            .filter(Objects::nonNull)
                            .filter(referencedElement -> referencedElement instanceof PsiMethod)
                            .filter(contextIndicator.isInProductionCodeContext())
                            .map(resolvedElement->(PsiMethod)resolvedElement)
                            .anyMatch(method -> method==methodWithAlmostSameName),
                    contextIndicator.isInTestContext()).isEmpty()
            ) {
                return Optional.of(reference);
            }
        }
        return Optional.empty();
    }


    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.SETUP_A_TEST_NAMING_STRATEGY);
    }
}
