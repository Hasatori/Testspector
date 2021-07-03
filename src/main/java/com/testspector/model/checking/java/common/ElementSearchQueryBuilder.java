package com.testspector.model.checking.java.common;

import com.intellij.psi.PsiElement;

import java.util.function.Predicate;

public class ElementSearchQueryBuilder<T> {

    private Class<T> searchedElementType;
    private Predicate<T> typeCondition;
    private Predicate<PsiElement> referencesCondition;


    public ElementSearchQueryBuilder<T> elementOfType(Class<T> searchedElementType) {
        this.searchedElementType = searchedElementType;
        this.typeCondition = (el) -> true;
        return this;
    }

    public ElementSearchQueryBuilder<T> whereElement(Predicate<T> typeCondition) {
        this.typeCondition = typeCondition;
        return this;
    }


    public ElementSearchQueryBuilder<T> withoutReferences() {
        this.referencesCondition = (element) -> false;
        return this;
    }

    public ElementSearchQueryBuilder<T> withReferences() {
        this.referencesCondition = (element) -> true;
        return this;
    }

    public ElementSearchQueryBuilder<T> whereReferences(Predicate<PsiElement> referencesCondition) {
        this.referencesCondition = referencesCondition;
        return this;
    }


    public ElementSearchQuery<T> build() {

        if (searchedElementType == null) {
            throw new IllegalStateException("Searched element type was not set!");
        }

        if (referencesCondition == null){
            throw new IllegalStateException("Whether search should be with or without references was not stated!");
        }

        return new ElementSearchQuery<>(
                searchedElementType,
                typeCondition,
                referencesCondition
        );
    }
}
