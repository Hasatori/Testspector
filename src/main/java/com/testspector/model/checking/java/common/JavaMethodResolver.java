package com.testspector.model.checking.java.common;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.testspector.model.checking.java.common.search.ElementSearchEngine;
import com.testspector.model.checking.java.common.search.ElementSearchResult;
import com.testspector.model.checking.java.common.search.QueriesRepository;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static com.testspector.model.checking.java.common.JavaConstants.ASSERTION_CLASSES_CLASS_PATHS;

public class JavaMethodResolver {

    private final ElementSearchEngine elementSearchEngine;
    private final JavaContextIndicator contextResolver;


    public JavaMethodResolver(ElementSearchEngine elementSearchEngine, JavaContextIndicator contextResolver) {
        this.elementSearchEngine = elementSearchEngine;
        this.contextResolver = contextResolver;
    }

    public ElementSearchResult<PsiMethodCallExpression> allTestedMethodsMethodCalls(PsiMethod testMethod) {
        ElementSearchResult<PsiMethodCallExpression> assertionMethods = elementSearchEngine
                .findByQuery(testMethod, QueriesRepository.FIND_ALL_ASSERTION_METHOD_CALL_EXPRESSIONS);
        return elementSearchEngine.mapResultUsingQuery(assertionMethods, QueriesRepository.FIND_ALL_TESTED_METHOD_CALL_EXPRESSIONS);
    }

    public ElementSearchResult<PsiReference> allTestedMethodsReferences(PsiMethod testMethod) {
        ElementSearchResult<PsiMethodCallExpression> assertionMethods = elementSearchEngine
                .findByQuery(testMethod, QueriesRepository.FIND_ALL_ASSERTION_METHOD_CALL_EXPRESSIONS);
        ElementSearchResult<PsiLiteralExpression> literalExpressionElementSearchResult = elementSearchEngine.mapResultUsingQuery(assertionMethods, QueriesRepository.FIND_ALL_LITERAL_EXPRESSIONS);
        return fillReferencesFromLiteralExpressions(literalExpressionElementSearchResult);
    }

    private ElementSearchResult<PsiReference> fillReferencesFromLiteralExpressions(ElementSearchResult<PsiLiteralExpression> literalExpressionElementSearchResult) {
        List<PsiReference> elementsOfTheCurrentLevel = literalExpressionElementSearchResult
                .getElementsOfCurrentLevel()
                .stream()
                .map(literalExpression -> Arrays.stream(ReferenceProvidersRegistry.getReferencesFromProviders(literalExpression))
                        .filter(reference -> {
                            PsiElement resolvedElement = reference.resolve();
                            return resolvedElement instanceof PsiMethod && contextResolver.isInProductionCodeContext().test(resolvedElement);
                        })
                        .collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        List<Pair<PsiReferenceExpression, ElementSearchResult<PsiReference>>> referencedElements = new ArrayList<>();
        for (Pair<PsiReferenceExpression, ElementSearchResult<PsiLiteralExpression>> referencedResult : literalExpressionElementSearchResult.getReferencedResults()) {
            ElementSearchResult<PsiReference> newReferencedResult = fillReferencesFromLiteralExpressions(referencedResult.getRight());
            referencedElements.add(Pair.of(referencedResult.getLeft(), newReferencedResult));
        }
        return new ElementSearchResult<>(referencedElements, elementsOfTheCurrentLevel);
    }

    public List<PsiMethod> methodsWithAnnotations(List<PsiElement> fromElements, List<String> annotationQualifiedNames) {
        return methodsWithAnnotations(new HashSet<>(), fromElements, annotationQualifiedNames);
    }

    public List<PsiMethod> methodsWithAnnotations(HashSet<PsiElement> visited, List<PsiElement> fromElements, List<String> annotationQualifiedNames) {
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
                } else if (!visited.contains(method)) {
                    psiMethods.addAll(ReferencesSearch.search(method)
                            .findAll()
                            .stream()
                            .map(reference -> PsiTreeUtil.getParentOfType(reference.getElement(), PsiMethod.class))
                            .filter(met -> met != null && met != method && contextResolver.isInTestContext().test(met))
                            .map(met -> methodsWithAnnotations(visited, Collections.singletonList(met), annotationQualifiedNames))
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList()));
                }
                visited.add(method);
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
            Optional<PsiElement> returnCandidate = Arrays.stream(body.getChildren()).filter(el -> !(el instanceof PsiJavaToken) && !(el instanceof PsiWhiteSpace)).findFirst();
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
