package com.testspector.model.checking.java.common;

import com.intellij.psi.PsiReferenceExpression;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class ElementSearchResult<T> {
    private List<Pair<PsiReferenceExpression, ElementSearchResult<T>>> referencedResults = new ArrayList<>();
    private List<T> elements = new ArrayList<>();

    public List<Pair<PsiReferenceExpression, ElementSearchResult<T>>> getReferencedResults() {
        return referencedResults;
    }

    public void addReferencedResult(Pair<PsiReferenceExpression, ElementSearchResult<T>> referencedResult) {
        this.referencedResults.add(referencedResult);
    }

    public void addReferencedResults(List<Pair<PsiReferenceExpression, ElementSearchResult<T>>> referencedResults) {
        this.referencedResults.addAll(referencedResults);
    }

    public List<T> getElementsOfCurrentLevel() {
        return elements;
    }

    public void setElementsOfCurrentLevel(List<T> elements) {
        this.elements = elements;
    }

    public List<T> getElementsFromAllLevels() {
        List<T> result = new ArrayList<>();
        result.addAll(this.getElementsOfCurrentLevel());
        for (Pair<PsiReferenceExpression, ElementSearchResult<T>> referencedResult : this.getReferencedResults()) {
            result.addAll(referencedResult.getRight().getElementsFromAllLevels());
        }

        return result;
    }

}
