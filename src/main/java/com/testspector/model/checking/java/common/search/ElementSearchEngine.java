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
            result = findByQuery(new HashMap<>(), new HashSet<>(), null, null, element, query);
            resultHashMap.put(query, result);
        }
        return result;
    }

    private <T> ElementSearchResult<T> findByQuery(
            HashMap<PsiElement, ElementSearchResult<T>> visitedElementsMap,
            HashSet<PsiElement> visitedElements,
            List<T> elementsOfTheCurrentLevel,
            List<Pair<PsiReferenceExpression, ElementSearchResult<T>>> referencedResults,
            PsiElement psiElement,
            ElementSearchQuery<T> elementSearchQuery) {
        Class<T> elementType = elementSearchQuery.getElementType();
        Predicate<T> typeCondition = elementSearchQuery.getWhereTypeCondition();
        elementsOfTheCurrentLevel = Optional.ofNullable(elementsOfTheCurrentLevel).orElse(new ArrayList<>());
        referencedResults = Optional.ofNullable(referencedResults).orElse(new ArrayList<>());
        if (!(psiElement instanceof PsiPackageBase)) {
            for (PsiElement child : psiElement.getChildren()) {
                if (elementType.isInstance(child) && typeCondition.test(elementType.cast(child))) {
                    elementsOfTheCurrentLevel.add(elementType.cast(child));
                    if (elementSearchQuery.isOnlyFirstMatch()) {
                        return new ElementSearchResult<>(referencedResults, elementsOfTheCurrentLevel);
                    }
                }
                if (child instanceof PsiReferenceExpression) {
                    addReferences(
                            child,
                            referencedResults,
                            visitedElementsMap,
                            visitedElements,
                            elementsOfTheCurrentLevel,
                            elementSearchQuery
                    );
                }
                findByQuery(
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

    private <T> void addReferences(PsiElement child, List<Pair<PsiReferenceExpression, ElementSearchResult<T>>> referencedResults,
                                   HashMap<PsiElement, ElementSearchResult<T>> visitedElementsMap,
                                   HashSet<PsiElement> visitedElements, List<T> elementsOfTheCurrentLevel,
                                   ElementSearchQuery<T> elementSearchQuery) {
        Class<T> elementType = elementSearchQuery.getElementType();
        Predicate<T> typeCondition = elementSearchQuery.getWhereTypeCondition();
        PsiElement referencedElement = ((PsiReferenceExpression) child).resolve();
        Optional.ofNullable(visitedElementsMap.get(referencedElement)).ifPresent(result -> referencedResults.add(Pair.of((PsiReferenceExpression) child, result)));
        if (referencedElement != null && !visitedElements.contains(referencedElement)) {
            visitedElements.add(referencedElement);
            if (elementSearchQuery.getReferencesCondition().test(referencedElement)) {
                if (elementType.isInstance(referencedElement) && typeCondition.test(elementType.cast(referencedElement))) {
                    elementsOfTheCurrentLevel.add(elementType.cast(referencedElement));
                }
                ElementSearchResult<T> next = findByQuery(
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
}
