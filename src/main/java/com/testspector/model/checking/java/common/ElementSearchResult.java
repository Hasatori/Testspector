package com.testspector.model.checking.java.common;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceExpression;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class ElementSearchResult<T> {
    private ElementSearchResult previous;
    private List<Pair<PsiReferenceExpression, ElementSearchResult>> referencedResults = new ArrayList<>();
    private List<T> elements = new ArrayList<>();

    public ElementSearchResult getPrevious() {
        return previous;
    }

    public void setPrevious(ElementSearchResult previous) {
        this.previous = previous;
    }

    public List<Pair<PsiReferenceExpression, ElementSearchResult>> getReferencedResults() {
        return referencedResults;
    }

    public void addReferencedResults(Pair<PsiReferenceExpression, ElementSearchResult> referencedResult) {
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
        for (Pair<PsiReferenceExpression, ElementSearchResult> referencedResult : this.getReferencedResults()) {
            result.addAll(referencedResult.getRight().getAllElements());
        }

        return result;
    }

}
