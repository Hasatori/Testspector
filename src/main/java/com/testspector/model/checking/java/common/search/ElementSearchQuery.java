package com.testspector.model.checking.java.common.search;

import com.intellij.psi.PsiElement;

import java.util.function.Predicate;

public class ElementSearchQuery<T> {

    private final Class<T> elementType;

    private final Predicate<T> whereTypeCondition;

    private final Predicate<PsiElement> referencesCondition;

    private final boolean onlyFirstMatch;

     ElementSearchQuery(Class<T> elementType, Predicate<T> typeCondition, Predicate<PsiElement> referencesCondition, boolean onlyFirstMatch) {
        this.elementType = elementType;
        this.whereTypeCondition = typeCondition;
        this.referencesCondition = referencesCondition;
         this.onlyFirstMatch = onlyFirstMatch;
     }

    public Class<T> getElementType() {
        return elementType;
    }

    public Predicate<T> getWhereTypeCondition() {
        return whereTypeCondition;
    }

    public Predicate<PsiElement> getReferencesCondition() {
        return referencesCondition;
    }

    public boolean isOnlyFirstMatch() {
        return onlyFirstMatch;
    }
}
