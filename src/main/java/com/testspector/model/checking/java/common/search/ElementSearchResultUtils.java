package com.testspector.model.checking.java.common.search;

import com.intellij.psi.PsiReferenceExpression;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ElementSearchResultUtils {

    private ElementSearchResultUtils() {
    }


    public static <T> ElementSearchResult<T> concatResults(List<ElementSearchResult<T>> results) {
        List<T> currentLevelElements = new ArrayList<>();
        List<Pair<PsiReferenceExpression, ElementSearchResult<T>>> currentLevelReferences = new ArrayList<>();
        for (ElementSearchResult<T> result : results) {
            currentLevelElements.addAll(result.getElementsOfCurrentLevel());
            currentLevelReferences.addAll(result.getReferencedResults());
        }
        return new ElementSearchResult<T>(currentLevelReferences, currentLevelElements);
    }

    public static <S, R> ElementSearchResult<R> mapResult(ElementSearchResult<S> assertionMethodsSearchResult, Function<S, ElementSearchResult<R>> mappingFunction) {
        List<ElementSearchResult<R>> result = new ArrayList<>();
        for (S methodCall : assertionMethodsSearchResult.getElementsOfCurrentLevel()) {
            result.add(mappingFunction.apply(methodCall));
        }
        for (Pair<PsiReferenceExpression, ElementSearchResult<S>> referencedResult : assertionMethodsSearchResult.getReferencedResults()) {
            ElementSearchResult<R> testedMethodsSearch = mapResult(referencedResult.getRight(), mappingFunction);
            result.add(new ElementSearchResult<>(Collections.singletonList(Pair.of(referencedResult.getLeft(), testedMethodsSearch)), new ArrayList<>()));
        }
        return concatResults(result);
    }

    public static <T> ElementSearchResult<T> filterResult(Predicate<T> condition, ElementSearchResult<T> elementSearchResult) {
        List<T> newElements = elementSearchResult
                .getElementsOfCurrentLevel()
                .stream()
                .filter(condition)
                .collect(Collectors.toList());
        List<Pair<PsiReferenceExpression, ElementSearchResult<T>>> newReferences = new ArrayList<>();
        for (Pair<PsiReferenceExpression, ElementSearchResult<T>> referencedResult : elementSearchResult.getReferencedResults()) {
            newReferences.add(Pair.of(referencedResult.getLeft(), filterResult(condition, referencedResult.getRight())));
        }
        return new ElementSearchResult<T>(newReferences, newElements);
    }
}
