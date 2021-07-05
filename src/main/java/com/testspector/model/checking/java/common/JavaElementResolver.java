package com.testspector.model.checking.java.common;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.impl.file.PsiPackageBase;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Predicate;

public class JavaElementResolver {

    private HashMap<PsiElement, HashMap<ElementSearchQuery, ElementSearchResult>> cache = new HashMap<>();

    public <T> ElementSearchResult<T> allChildrenByQuery(PsiElement element, ElementSearchQuery<T> query) {
        HashMap<ElementSearchQuery, ElementSearchResult> resultHashMap = cache.get(element);
        ElementSearchResult<T> result = null;
        if (resultHashMap == null) {
            resultHashMap = new HashMap<>();
            cache.put(element, resultHashMap);
        } else {
            result = resultHashMap.get(query);
        }
        if (result == null) {
            result = allChildrenByQuery(false, new HashSet<>(), null, null, element, query);
            resultHashMap.put(query, result);
        }
        return result;
    }

    private <T> ElementSearchResult<T> allChildrenByQuery(boolean addRootIfPossible, HashSet<PsiElement> visitedReferences, List<T> elements, List<Pair<PsiReferenceExpression, ElementSearchResult<T>>> references, PsiElement psiElement, ElementSearchQuery<T> elementSearchQuery) {
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
                    if (referencedElement != null && !visitedReferences.contains(referencedElement)) {
                        if (fromReferencesMeetingCondition.test(referencedElement)) {
                            visitedReferences.add(referencedElement);
                            ElementSearchResult<T> next = allChildrenByQuery(
                                    true,
                                    visitedReferences,
                                    null,
                                    null,
                                    referencedElement,
                                    elementSearchQuery);
                            referencedResults.add(Pair.of((PsiReferenceExpression) child, next));
                        }
                    }
                }
                allChildrenByQuery(
                        false,
                        visitedReferences,
                        elementsOfTheCurrentLevel,
                        referencedResults,
                        child,
                        elementSearchQuery);
            }
        }
        return new ElementSearchResult<>(referencedResults, elementsOfTheCurrentLevel);
    }
}
