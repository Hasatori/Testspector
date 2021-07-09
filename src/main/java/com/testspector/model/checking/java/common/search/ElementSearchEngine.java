package com.testspector.model.checking.java.common.search;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.impl.file.PsiPackageBase;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Predicate;

public class ElementSearchEngine {

    private HashMap<PsiElement, HashMap<ElementSearchQuery, ElementSearchResult>> cache = new HashMap<>();

    public <T> ElementSearchResult<T> findByQuery(PsiElement element, ElementSearchQuery<T> query) {
        HashMap<ElementSearchQuery, ElementSearchResult> resultHashMap = cache.get(element);
        ElementSearchResult<T> result = null;
        if (resultHashMap == null) {
            resultHashMap = new HashMap<>();
            cache.put(element, resultHashMap);
        } else {
            result = resultHashMap.get(query);
        }
        if (result == null) {
            result = findByQuery(false, new HashMap<>(), new HashSet<>(), null, null, element, query);
            resultHashMap.put(query, result);
        }
        return result;
    }

    private <T> ElementSearchResult<T> findByQuery(
            boolean addRootIfPossible,
            HashMap<PsiElement,
                    ElementSearchResult<T>> visitedElementsMap,
            HashSet<PsiElement> visitedElements,
            List<T> elements,
            List<Pair<PsiReferenceExpression, ElementSearchResult<T>>> references,
            PsiElement psiElement,
            ElementSearchQuery<T> elementSearchQuery) {
        Class<T> elementType = elementSearchQuery.getElementType();
        Predicate<T> typeCondition = elementSearchQuery.getWhereTypeCondition();
        Predicate<PsiElement> fromReferencesMeetingCondition = elementSearchQuery.getReferencesCondition();
        List<T> elementsOfTheCurrentLevel = Optional.ofNullable(elements).orElse(new ArrayList<>());
        List<Pair<PsiReferenceExpression, ElementSearchResult<T>>> referencedResults = Optional.ofNullable(references).orElse(new ArrayList<>());
        if (!(psiElement instanceof PsiPackageBase)) {
            if (addRootIfPossible && elementType.isInstance(psiElement) && typeCondition.test(elementType.cast(psiElement))) {
                elementsOfTheCurrentLevel.add(elementType.cast(psiElement));
            }
            for (PsiElement child : psiElement.getChildren()) {
                if (elementType.isInstance(child) && typeCondition.test(elementType.cast(child))) {
                    elementsOfTheCurrentLevel.add(elementType.cast(child));
                }
                if (child instanceof PsiReferenceExpression) {
                    PsiElement referencedElement = ((PsiReferenceExpression) child).resolve();
                    Optional.ofNullable(visitedElementsMap.get(referencedElement)).ifPresent(result -> referencedResults.add(Pair.of((PsiReferenceExpression) child, result)));
                    if (referencedElement != null && !visitedElements.contains(referencedElement)) {
                        visitedElements.add(referencedElement);
                        if (fromReferencesMeetingCondition.test(referencedElement)) {
                            ElementSearchResult<T> next = findByQuery(
                                    true,
                                    visitedElementsMap,
                                    visitedElements,
                                    null,
                                    null,
                                    referencedElement,
                                    elementSearchQuery);
                            visitedElementsMap.put(referencedElement, next);
                            referencedResults.add(Pair.of((PsiReferenceExpression) child, next));
                        }
                    }
                }
                findByQuery(
                        false,
                        visitedElementsMap,
                        visitedElements,
                        elementsOfTheCurrentLevel,
                        referencedResults,
                        child,
                        elementSearchQuery);
            }
        }
        return new ElementSearchResult<>(referencedResults, elementsOfTheCurrentLevel);
    }

    public <T> ElementSearchResult<T> concat(List<ElementSearchResult<T>> results) {
        List<T> currentLevelElements = new ArrayList<>();
        List<Pair<PsiReferenceExpression, ElementSearchResult<T>>> currentLevelReferences = new ArrayList<>();
        for (ElementSearchResult<T> result : results) {
            currentLevelElements.addAll(result.getElementsOfCurrentLevel());
            currentLevelReferences.addAll(result.getReferencedResults());
        }
        return new ElementSearchResult<T>(currentLevelReferences, currentLevelElements);
    }


    public <T, R extends PsiElement> ElementSearchResult<T> mapResultUsingQuery(ElementSearchResult<R> assertionMethodsSearchResult, ElementSearchQuery<T> mappingQuery) {
        List<ElementSearchResult<T>> result = new ArrayList<>();
        for (R methodCall : assertionMethodsSearchResult.getElementsOfCurrentLevel()) {
            result.add(findByQuery(methodCall, mappingQuery));
        }
        for (Pair<PsiReferenceExpression, ElementSearchResult<R>> referencedResult : assertionMethodsSearchResult.getReferencedResults()) {
            ElementSearchResult<T> testedMethodsSearch = mapResultUsingQuery(referencedResult.getRight(), mappingQuery);
            result.add(new ElementSearchResult<>(Collections.singletonList(Pair.of(referencedResult.getLeft(), testedMethodsSearch)), new ArrayList<>()));
        }
        return concat(result);
    }
}
