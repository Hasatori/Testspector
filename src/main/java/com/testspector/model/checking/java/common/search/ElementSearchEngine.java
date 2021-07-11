package com.testspector.model.checking.java.common.search;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.impl.file.PsiPackageBase;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
}
