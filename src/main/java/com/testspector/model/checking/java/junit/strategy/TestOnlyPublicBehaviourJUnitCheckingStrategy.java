package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.*;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.common.search.ElementSearchEngine;
import com.testspector.model.checking.java.common.search.ElementSearchResult;
import com.testspector.model.checking.java.junit.strategy.action.MakeMethodPublicAction;
import com.testspector.model.checking.java.junit.strategy.action.NavigateElementAction;
import com.testspector.model.enums.BestPractice;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TestOnlyPublicBehaviourJUnitCheckingStrategy extends JUnitBestPracticeCheckingStrategy {

    private static final String DEFAULT_PROBLEM_DESCRIPTION = "It is recommended to always test only the public behaviour of the system under test, which is expressed through public methods. " +
            "Private methods are often updated, deleted or added regardless of if public behaviour of a system under test has changed. " +
            "Private methods are only a helper tool for the public behaviour of the tested system. " +
            "Testing them leads to dependencies between the code and the tests, and in the long run, it makes it hard to maintain the tests and even the slightest change will require an update to the tests.";
    private static final List<String> DEFAULT_HINTS = Arrays.asList(
            "Remove tests testing private behaviour",
            "If you really feel that private behaviour is complex enough that there should be " +
                    "separate test for it, then it is very probable that the system under test is " +
                    "breaking 'Single Responsibility Principle' and this private behaviour should be " +
                    "extracted into to a separate class"
    );

    public TestOnlyPublicBehaviourJUnitCheckingStrategy(ElementSearchEngine elementSearchEngine, JavaContextIndicator contextIndicator, JavaMethodResolver methodResolver) {
        super(elementSearchEngine, contextIndicator, methodResolver);
    }


    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiMethod method) {
        return checkBestPractices(Collections.singletonList(method));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiMethod> methods) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        for (PsiMethod testMethod : methods) {

            ElementSearchResult<PsiMethodCallExpression> nonPublicTestedMethodsFromMethodCallExpressions = methodResolver.allTestedMethodsMethodCalls(testMethod);
            nonPublicTestedMethodsFromMethodCallExpressions = removePublicTestedMethods(nonPublicTestedMethodsFromMethodCallExpressions);
            nonPublicTestedMethodsFromMethodCallExpressions
                    .getElementsFromAllLevels()
                    .forEach(nonPublicFromMethodCallExpression -> {
                        bestPracticeViolations.add(new BestPracticeViolation(
                                nonPublicFromMethodCallExpression.getMethodExpression(),
                                DEFAULT_PROBLEM_DESCRIPTION,
                                this.getCheckedBestPractice().get(0),
                                Collections.singletonList(nonPublicFromMethodCallExpression.resolveMethod() != null ?
                                        new MakeMethodPublicAction(nonPublicFromMethodCallExpression.resolveMethod()) :
                                        null)
                                , DEFAULT_HINTS)
                        );
                    });
            bestPracticeViolations.addAll(createBestPracticeViolationFromMethodExpression(nonPublicTestedMethodsFromMethodCallExpressions));

            ElementSearchResult<PsiReference> nonPublicTestedMethodsFromReferences = methodResolver.allTestedMethodsReferences(testMethod);
            nonPublicTestedMethodsFromReferences = removePublicTestedMethodsFromReference(nonPublicTestedMethodsFromReferences);
            nonPublicTestedMethodsFromReferences
                    .getElementsFromAllLevels()
                    .forEach(reference -> {
                        bestPracticeViolations.add(new BestPracticeViolation(
                                reference.getElement(),
                                DEFAULT_PROBLEM_DESCRIPTION,
                                this.getCheckedBestPractice().get(0),
                                Collections.singletonList(reference.resolve() != null ?
                                        new MakeMethodPublicAction((PsiMethod) reference.resolve()) :
                                        null)
                                , DEFAULT_HINTS)
                        );
                    });
            bestPracticeViolations.addAll(createBestPracticeViolationFromReferences(nonPublicTestedMethodsFromReferences));

        }

        return bestPracticeViolations;
    }

    private ElementSearchResult<PsiMethodCallExpression> removePublicTestedMethods(ElementSearchResult<PsiMethodCallExpression> nonPublicTestedMethodsFromMethodCallExpressions) {
        List<PsiMethodCallExpression> notToRemove = nonPublicTestedMethodsFromMethodCallExpressions
                .getElementsOfCurrentLevel()
                .stream()
                .filter(methodCall -> {
                    PsiMethod testedMethod = methodCall.resolveMethod();
                    return testedMethod != null && (methodHasModifier(testedMethod, PsiModifier.PROTECTED) ||
                            isMethodPackagePrivate(testedMethod) ||
                            methodHasModifier(testedMethod, PsiModifier.PRIVATE));

                }).collect(Collectors.toList());
        List<Pair<PsiReferenceExpression, ElementSearchResult<PsiMethodCallExpression>>> referencedElements = new ArrayList<>();
        for (Pair<PsiReferenceExpression, ElementSearchResult<PsiMethodCallExpression>> referencedResult : nonPublicTestedMethodsFromMethodCallExpressions.getReferencedResults()) {
            ElementSearchResult<PsiMethodCallExpression> newReferencedResult = removePublicTestedMethods(referencedResult.getRight());
            referencedElements.add(Pair.of(referencedResult.getLeft(), newReferencedResult));
        }
        return new ElementSearchResult<>(referencedElements, notToRemove);
    }

    private ElementSearchResult<PsiReference> removePublicTestedMethodsFromReference(ElementSearchResult<PsiReference> nonPublicTestedMethodsFromReferences) {
        List<PsiReference> notToRemove = nonPublicTestedMethodsFromReferences
                .getElementsOfCurrentLevel()
                .stream()
                .filter(reference -> {
                    PsiMethod testedMethod = (PsiMethod) reference.resolve();
                    return testedMethod != null && (methodHasModifier(testedMethod, PsiModifier.PROTECTED) ||
                            isMethodPackagePrivate(testedMethod) ||
                            methodHasModifier(testedMethod, PsiModifier.PRIVATE));
                }).collect(Collectors.toList());
        List<Pair<PsiReferenceExpression, ElementSearchResult<PsiReference>>> referencedElements = new ArrayList<>();
        for (Pair<PsiReferenceExpression, ElementSearchResult<PsiReference>> referencedResult : nonPublicTestedMethodsFromReferences.getReferencedResults()) {
            ElementSearchResult<PsiReference> newReferencedResult = removePublicTestedMethodsFromReference(referencedResult.getRight());
            referencedElements.add(Pair.of(referencedResult.getLeft(), newReferencedResult));
        }
        return new ElementSearchResult<>(referencedElements, notToRemove);
    }

    private boolean methodHasModifier(PsiMethod method, String modifier) {
        return method.getModifierList().hasModifierProperty(modifier);
    }

    private boolean isMethodPackagePrivate(PsiMethod method) {
        PsiModifierList modifierList = method.getModifierList();
        return !modifierList.hasModifierProperty(PsiModifier.PUBLIC) &&
                !modifierList.hasModifierProperty(PsiModifier.PRIVATE) &&
                !modifierList.hasModifierProperty(PsiModifier.PROTECTED);
    }

    private List<BestPracticeViolation> createBestPracticeViolationFromMethodExpression(ElementSearchResult<PsiMethodCallExpression> elementSearchResult) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        elementSearchResult.getReferencedResults()
                .forEach(result -> {
                    List<PsiMethodCallExpression> globalStaticProps = result.getRight().getElementsFromAllLevels();
                    if (!globalStaticProps.isEmpty()) {
                        if (result.getLeft().getParent() instanceof PsiMethodCallExpression) {
                            bestPracticeViolations.add(createBestPracticeViolation("Following method contains code that breaks best practice. ", result.getLeft(), globalStaticProps));
                        } else {
                            bestPracticeViolations.add(createBestPracticeViolation("", result.getLeft(), globalStaticProps));
                        }
                    }
                    bestPracticeViolations.addAll(createBestPracticeViolationFromMethodExpression(result.getRight()));
                });
        return bestPracticeViolations;
    }

    private List<BestPracticeViolation> createBestPracticeViolationFromReferences(ElementSearchResult<PsiReference> elementSearchResult) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        elementSearchResult.getReferencedResults()
                .forEach(result -> {
                    List<PsiElement> globalStaticProps = result.getRight().getElementsFromAllLevels().stream().map(PsiReference::resolve).collect(Collectors.toList());
                    if (!globalStaticProps.isEmpty()) {
                        if (result.getLeft().getParent() instanceof PsiMethodCallExpression) {
                            bestPracticeViolations.add(createBestPracticeViolation("Following method contains code that breaks best practice. ", result.getLeft(), globalStaticProps));
                        } else {
                            bestPracticeViolations.add(createBestPracticeViolation(result.getLeft(), globalStaticProps));
                        }
                    }
                    bestPracticeViolations.addAll(createBestPracticeViolationFromReferences(result.getRight()));
                });
        return bestPracticeViolations;
    }

    private BestPracticeViolation createBestPracticeViolation(PsiReference reference, List<? extends PsiElement> elements) {
        return createBestPracticeViolation("", reference, elements);
    }

    private BestPracticeViolation createBestPracticeViolation(String problemDescriptionPrefix, PsiReference reference, List<? extends PsiElement> elements) {
        return new BestPracticeViolation(
                reference.getElement(),
                problemDescriptionPrefix + DEFAULT_PROBLEM_DESCRIPTION,
                getCheckedBestPractice().get(0),
                elements.stream()
                        .map(testedMethod -> new NavigateElementAction("method call", testedMethod))
                        .collect(Collectors.toList())
                , DEFAULT_HINTS
        );

    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.TEST_ONLY_PUBLIC_BEHAVIOUR);
    }
}
