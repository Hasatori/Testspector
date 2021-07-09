package com.testspector.model.checking.java.common.search;

import com.intellij.psi.PsiElement;

import java.util.function.Predicate;

 class ElementSearchQueryBuilder<T> {

    private Class<T> searchedElementType;
    private Predicate<T> typeCondition;
    private Predicate<PsiElement> referencesCondition;


     ElementSearchQueryBuilder<T> elementOfType(Class<T> searchedElementType) {
        this.searchedElementType = searchedElementType;
        this.typeCondition = (el) -> true;
        return this;
    }

     ElementSearchQueryBuilder<T> whereElement(Predicate<T> typeCondition) {
        this.typeCondition = typeCondition;
        return this;
    }


     ElementSearchQueryBuilder<T> withoutReferences() {
        this.referencesCondition = (element) -> false;
        return this;
    }

     ElementSearchQueryBuilder<T> withReferences() {
        this.referencesCondition = (element) -> true;
        return this;
    }

     ElementSearchQueryBuilder<T> whereReferences(Predicate<PsiElement> referencesCondition) {
        this.referencesCondition = referencesCondition;
        return this;
    }


     ElementSearchQuery<T> build() {

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
