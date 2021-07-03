package com.testspector.model.checking.java.common;

import com.intellij.psi.PsiReferenceExpression;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ElementSearchResult<T> {
    private final List<Pair<PsiReferenceExpression, ElementSearchResult<T>>> referencedResults;
    private final List<T> elements;

    public ElementSearchResult(List<Pair<PsiReferenceExpression, ElementSearchResult<T>>> referencedResults, List<T> elements) {
        this.referencedResults = Collections.unmodifiableList(referencedResults);
        this.elements = Collections.unmodifiableList(elements);
    }

    public List<Pair<PsiReferenceExpression, ElementSearchResult<T>>> getReferencedResults() {
        return referencedResults;
    }

    public List<T> getElementsOfCurrentLevel() {
        return elements;
    }


    public List<T> getElementsFromAllLevels() {
        List<T> result = new ArrayList<>();
        result.addAll(this.getElementsOfCurrentLevel());
        for (Pair<PsiReferenceExpression, ElementSearchResult<T>> referencedResult : this.getReferencedResults()) {
            result.addAll(referencedResult.getRight().getElementsFromAllLevels());
        }

        return Collections.unmodifiableList(result);
    }

}
