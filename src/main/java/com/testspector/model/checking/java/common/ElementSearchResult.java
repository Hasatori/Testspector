package com.testspector.model.checking.java.common;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceExpression;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class ElementSearchResult<T> {
    private ElementSearchResult<T> previous;
    private List<Pair<PsiReferenceExpression, ElementSearchResult<T>>> referencedResults = new ArrayList<>();
    private List<T> elements = new ArrayList<>();

    public ElementSearchResult<T> getPrevious() {
        return previous;
    }

    public void setPrevious(ElementSearchResult<T> previous) {
        this.previous = previous;
    }

    public List<Pair<PsiReferenceExpression, ElementSearchResult<T>>> getReferencedResults() {
        return referencedResults;
    }

    public void addReferencedResults(Pair<PsiReferenceExpression, ElementSearchResult<T>> referencedResult) {
        this.referencedResults.add(referencedResult);
    }

    public List<T> getElements() {
        return elements;
    }

    public void setElements(List<T> elements) {
        this.elements = elements;
    }

    public List<T> getAllElements() {
        List<T> result = new ArrayList<>();
        result.addAll(this.getElements());
        for (Pair<PsiReferenceExpression, ElementSearchResult<T>> referencedResult : this.getReferencedResults()) {
            result.addAll(referencedResult.getRight().getAllElements());
        }

        return result;
    }

}
