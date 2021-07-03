package com.testspector.model.checking.java.common;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static com.testspector.model.checking.java.common.JavaConstants.ASSERTION_CLASSES_CLASS_PATHS;

public class JavaMethodResolver {

    private final JavaElementResolver elementResolver;
    private final JavaContextIndicator contextResolver;

    public JavaMethodResolver(JavaElementResolver elementResolver, JavaContextIndicator contextResolver) {
        this.elementResolver = elementResolver;
        this.contextResolver = contextResolver;
    }

    public ElementSearchResult<PsiMethodCallExpression> allTestedMethodsMethodCalls(PsiMethod testMethod) {
        return elementResolver
                .allChildrenOfTypeMeetingConditionWithReferences(
                        testMethod,
                        PsiMethodCallExpression.class,
                        (psiMethodCallExpression -> {
                            PsiMethod method = psiMethodCallExpression.resolveMethod();
                            return method != null && contextResolver.isInProductionCodeContext().test(method) &&
                                    Optional.ofNullable(PsiTreeUtil.getParentOfType(psiMethodCallExpression, PsiMethodCallExpression.class))
                                            .map(methodExp -> assertionMethod(methodExp).isPresent()).get();

                        }),
                        contextResolver.isInTestContext());
    }

    public ElementSearchResult<PsiReference> allTestedMethodsReferences(PsiMethod testMethod) {
        List<PsiMethodCallExpression> assertionMethods = elementResolver
                .allChildrenOfTypeMeetingConditionWithReferences(
                        testMethod,
                        PsiMethodCallExpression.class,
                        (psiMethodCallExpression ->
                                assertionMethod(psiMethodCallExpression).isPresent()),
                        contextResolver.isInTestContext())
                .getAllElements()
                .stream()
                .distinct()
                .collect(Collectors.toList());
        HashSet<PsiLiteralExpression> literalExpressionsSet = assertionMethods
                .stream()
                .map(assertionMethod -> elementResolver
                        .allChildrenOfTypeWithReferences(
                                assertionMethod,
                                PsiLiteralExpression.class,
                                contextResolver.isInTestContext()).getAllElements())
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(HashSet::new));
        ElementSearchResult<PsiLiteralExpression> literalExpressionElementSearchResult = elementResolver.allChildrenOfTypeMeetingConditionWithReferences(
                testMethod,
                PsiLiteralExpression.class,
                literalExpression -> literalExpressionsSet.contains(literalExpression),
                contextResolver.isInTestContext());

        return fillReferencesFromLiteralExpressions(literalExpressionElementSearchResult);
    }


    private ElementSearchResult<PsiReference> fillReferencesFromLiteralExpressions(ElementSearchResult<PsiLiteralExpression> literalExpressionElementSearchResult) {
        ElementSearchResult<PsiReference> result = new ElementSearchResult<PsiReference>();
        result.setElements(literalExpressionElementSearchResult
                .getElements()
                .stream()
                .map(literalExpression -> Arrays.stream(ReferenceProvidersRegistry.getReferencesFromProviders(literalExpression))
                        .filter(reference -> {
                            PsiElement resolvedElement = reference.resolve();
                            return resolvedElement instanceof PsiMethod && contextResolver.isInProductionCodeContext().test(resolvedElement);
                        })
                        .collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));
        for (Pair<PsiReferenceExpression, ElementSearchResult<PsiLiteralExpression>> referencedResult : literalExpressionElementSearchResult.getReferencedResults()) {
            ElementSearchResult<PsiReference> newReferencedResult = fillReferencesFromLiteralExpressions(referencedResult.getRight());
            newReferencedResult.setPrevious(result);
            result.addReferencedResults(Pair.of(referencedResult.getLeft(), newReferencedResult));
        }
        return result;
    }

    public List<PsiMethod> methodsWithAnnotations(List<PsiElement> fromElements, List<String> annotationQualifiedNames) {
        List<PsiMethod> psiMethods = new ArrayList<>();
        for (PsiElement psiElement : fromElements) {
            if (psiElement instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiElement;
                psiMethods.addAll(Arrays.stream(psiJavaFile.getClasses())
                        .map(psiClass -> methodsWithAnnotations(Collections.singletonList(psiClass), annotationQualifiedNames))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
            } else if (psiElement instanceof PsiClass) {
                PsiClass psiClass = (PsiClass) psiElement;
                psiMethods.addAll(methodsWithAnnotations(Arrays.stream(psiClass.getMethods()).collect(Collectors.toList()), annotationQualifiedNames));
            } else if (psiElement instanceof PsiMethod) {
                PsiMethod method = (PsiMethod) psiElement;
                if (methodHasAnyOfAnnotations(method, annotationQualifiedNames)) {
                    psiMethods.add((PsiMethod) psiElement);
                }
                psiMethods.addAll(ReferencesSearch.search(method)
                        .findAll()
                        .stream()
                        .map(reference -> PsiTreeUtil.getParentOfType(reference.getElement(), PsiMethod.class))
                        .map(met -> methodsWithAnnotations(Collections.singletonList(met), annotationQualifiedNames))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
            }
        }
        return psiMethods;
    }

    public Optional<PsiMethod> assertionMethod(PsiMethodCallExpression psiMethodCallExpression) {
        PsiMethod assertionMethodCandidate = psiMethodCallExpression.resolveMethod();
        if (assertionMethodCandidate != null) {
            return assertionMethod(assertionMethodCandidate);
        }
        return Optional.empty();
    }

    public Optional<PsiMethod> assertionMethod(PsiMethod method) {
        if ((method.getContainingClass() != null && ASSERTION_CLASSES_CLASS_PATHS.contains(method.getContainingClass().getQualifiedName()))
                || (method.getName().toLowerCase().contains("assert") && !contextResolver.isInProductionCodeContext().test(method))) {
            return Optional.of(method);
        }
        return Optional.empty();
    }

    public boolean isGetter(PsiMethod method) {
        PsiCodeBlock body = method.getBody();
        if (body != null) {
            Optional<PsiElement> returnCandidate = elementResolver.firstImmediateChildIgnoring(body, Arrays.asList(PsiJavaToken.class, PsiWhiteSpace.class));
            if (returnCandidate.isPresent() && returnCandidate.get() instanceof PsiReturnStatement) {
                PsiReturnStatement returnStatement = (PsiReturnStatement) returnCandidate.get();
                PsiExpression returnValueExpression = returnStatement.getReturnValue();
                if (returnValueExpression instanceof PsiReferenceExpression) {
                    return Optional.ofNullable(((PsiReferenceExpression) returnValueExpression).resolve())
                            .filter(element -> element instanceof PsiField)
                            .isPresent();
                }
            }
        }

        return false;
    }

    public boolean methodHasAnyOfAnnotations(PsiMethod method, List<String> annotationQualifiedNames) {
        return annotationQualifiedNames.stream().anyMatch(method::hasAnnotation)
                || (annotationQualifiedNames.isEmpty() && method.getAnnotations().length == 0);
    }


}
