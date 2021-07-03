package com.testspector.model.checking.java.junit.strategy;

import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.ElementSearchResult;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaElementResolver;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.enums.BestPractice;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TestOnlyPublicBehaviourJUnitCheckingStrategy implements BestPracticeCheckingStrategy<PsiMethod> {


    private final JavaElementResolver elementResolver;
    private final JavaMethodResolver methodResolver;
    private final JavaContextIndicator contextIndicator;
    private static final String DEFAULT_PROBLEM_DESCRIPTION = "Only public behaviour should be tested. Testing 'private','protected' " +
            "or 'package private' methods leads to problems with maintenance of tests because " +
            "this private behaviour is likely to be changed very often. " +
            "In many cases we are refactoring private behaviour without influencing public " +
            "behaviour of the class, yet this changes will change behaviour of the private method" +
            " and cause tests to fail.";
    private static final List<String> DEFAULT_HINTS = Arrays.asList(
            "There is an exception to this rule and that is in case when private 'method' " +
                    "is part of the observed behaviour of the system under test. For example " +
                    "we can have private constructor for class which is part of ORM and its " +
                    "initialization should not be permitted.",
            "Remove tests testing private behaviour",
            "If you really feel that private behaviour is complex enough that there should be " +
                    "separate test for it, then it is very probable that the system under test is " +
                    "breaking 'Single Responsibility Principle' and this private behaviour should be " +
                    "extracted to a separate system"
    );


    public TestOnlyPublicBehaviourJUnitCheckingStrategy(JavaElementResolver elementResolver, JavaMethodResolver methodResolver, JavaContextIndicator contextIndicator) {
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

            ElementSearchResult<PsiMethodCallExpression> nonPublicTestedMethodsFromMethodCallExpressions = methodResolver.allTestedMethodsMethodCalls(testMethod);
            removePublicTestedMethods(nonPublicTestedMethodsFromMethodCallExpressions);
            nonPublicTestedMethodsFromMethodCallExpressions
                    .getAllElements()
                    .forEach(nonPublicFromMethodCallExpression -> {
                        bestPracticeViolations.add(new BestPracticeViolation(
                                nonPublicFromMethodCallExpression,
                                DEFAULT_PROBLEM_DESCRIPTION,
                                this.getCheckedBestPractice().get(0),
                                Collections.singletonList(makePublicQuickFix(nonPublicFromMethodCallExpression.resolveMethod(), nonPublicFromMethodCallExpression.getMethodExpression()))
                                , DEFAULT_HINTS)
                        );
                    });
            bestPracticeViolations.addAll(createBestPracticeViolationFromMethodExpression(nonPublicTestedMethodsFromMethodCallExpressions));

            ElementSearchResult<PsiReference> nonPublicTestedMethodsFromReferences = methodResolver.allTestedMethodsReferences(testMethod);
            removePublicTestedMethodsFromReference(nonPublicTestedMethodsFromReferences);
            nonPublicTestedMethodsFromReferences
                    .getAllElements()
                    .forEach(reference -> {
                        bestPracticeViolations.add(new BestPracticeViolation(
                                reference.getElement(),
                                DEFAULT_PROBLEM_DESCRIPTION,
                                this.getCheckedBestPractice().get(0),
                                Collections.singletonList(makePublicQuickFix((PsiMethod) reference.resolve(), reference))
                                , DEFAULT_HINTS)
                        );
                    });
            bestPracticeViolations.addAll(createBestPracticeViolationFromReferences(nonPublicTestedMethodsFromReferences));

        }

        return bestPracticeViolations;
    }

    private void removePublicTestedMethods(ElementSearchResult<PsiMethodCallExpression> nonPublicTestedMethodsFromMethodCallExpressions) {
        List<PsiMethodCallExpression> toRemove = new ArrayList<>();
        toRemove.addAll(nonPublicTestedMethodsFromMethodCallExpressions
                .getElements()
                .stream()
                .filter(methodCall -> {
                    PsiMethod testedMethod = methodCall.resolveMethod();
                    return !(methodHasModifier(testedMethod, PsiModifier.PROTECTED) ||
                            isMethodPackagePrivate(testedMethod) ||
                            methodHasModifier(testedMethod, PsiModifier.PRIVATE));

                }).collect(Collectors.toList()));
        nonPublicTestedMethodsFromMethodCallExpressions.getElements().removeAll(toRemove);
        for (Pair<PsiReferenceExpression, ElementSearchResult<PsiMethodCallExpression>> referencedResult : nonPublicTestedMethodsFromMethodCallExpressions.getReferencedResults()) {
            removePublicTestedMethods(referencedResult.getRight());
        }
    }

    private void removePublicTestedMethodsFromReference(ElementSearchResult<PsiReference> nonPublicTestedMethodsFromReferences) {
        List<PsiReference> toRemove = new ArrayList<>();
        toRemove.addAll(nonPublicTestedMethodsFromReferences
                .getElements()
                .stream()
                .filter(reference -> {
                    PsiMethod testedMethod = (PsiMethod) reference.resolve();
                    return !(methodHasModifier(testedMethod, PsiModifier.PROTECTED) ||
                            isMethodPackagePrivate(testedMethod) ||
                            methodHasModifier(testedMethod, PsiModifier.PRIVATE));
                }).collect(Collectors.toList()));
        nonPublicTestedMethodsFromReferences.getElements().removeAll(toRemove);
        for (Pair<PsiReferenceExpression, ElementSearchResult<PsiReference>> referencedResult : nonPublicTestedMethodsFromReferences.getReferencedResults()) {
            removePublicTestedMethodsFromReference(referencedResult.getRight());
        }
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
                    List<PsiMethodCallExpression> globalStaticProps = result.getRight().getAllElements();
                    if (result.getLeft().getParent() instanceof PsiMethodCallExpression && !globalStaticProps.isEmpty()){
                        bestPracticeViolations.add(createBestPracticeViolation(result.getLeft(),globalStaticProps));
                    }
                    bestPracticeViolations.addAll(createBestPracticeViolationFromMethodExpression(result.getRight()));
                });
        return bestPracticeViolations;
    }

    private List<BestPracticeViolation> createBestPracticeViolationFromReferences(ElementSearchResult<PsiReference> elementSearchResult) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        elementSearchResult.getReferencedResults()
                .forEach(result -> {
                    List<PsiElement> globalStaticProps = result.getRight().getAllElements().stream().map(PsiReference::resolve).collect(Collectors.toList());
                    if (result.getLeft().getParent() instanceof PsiMethodCallExpression && !globalStaticProps.isEmpty()){
                        bestPracticeViolations.add(createBestPracticeViolation(result.getLeft(),globalStaticProps));
                    }
                    bestPracticeViolations.addAll(createBestPracticeViolationFromReferences(result.getRight()));
                });
        return bestPracticeViolations;
    }

    private BestPracticeViolation createBestPracticeViolation(PsiReference reference, List<? extends PsiElement> elements) {
        return new BestPracticeViolation(
                reference.getElement(),
                "Following method breaks best practice. " + DEFAULT_PROBLEM_DESCRIPTION,
                getCheckedBestPractice().get(0),
                elements.stream().map(element -> new Action<BestPracticeViolation>() {
                    @Override
                    public String getName() {
                        return String.format("Go to %s method call in file %s (line %s)"
                                , element,
                                element.getContainingFile().getName(),
                                (PsiDocumentManager.getInstance(element.getProject()).getDocument(element.getContainingFile()).getLineNumber(element.getTextOffset()) + 1));
                    }

                    @Override
                    public void execute(BestPracticeViolation bestPracticeViolation) {
                        ((Navigatable) element.getNavigationElement()).navigate(true);
                    }
                }).collect(Collectors.toList())
                ,DEFAULT_HINTS
        );

    }


    private Action<BestPracticeViolation> makePublicQuickFix(PsiMethod method, PsiReference psiReference) {
        return new Action<>() {
            @Override
            public
            @NotNull
            String getName() {
                return String.format("Make %s public", method.getName());
            }

            @Override
            public void execute(BestPracticeViolation bestPracticeViolation) {
                method.replace(PsiElementFactory.getInstance(method.getProject()).createMethodFromText("public " + method.getText(), null));
                ((Navigatable) psiReference.resolve().getNavigationElement()).navigate(true);
            }
        };
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.TEST_ONLY_PUBLIC_BEHAVIOUR);
    }
}
