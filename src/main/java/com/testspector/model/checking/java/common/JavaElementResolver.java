package com.testspector.model.checking.java.common;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.impl.file.PsiPackageBase;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class JavaElementResolver {


    public <T extends PsiElement> SearchResult<T> allChildrenOfTypeMeetingConditionWithReferences(PsiElement psiElement, Class<T> elementType) {
        return allChildrenOfTypeMeetingConditionWithReferences(psiElement, elementType, t -> true, t -> false);
    }

    public Optional<PsiElement> firstImmediateChildIgnoring(PsiElement psiElement, List<Class<? extends PsiElement>> ignoredList) {
        for (PsiElement child : psiElement.getChildren()) {
            if (ignoredList.stream().noneMatch(ignored -> ignored.isInstance(child))) {
                return Optional.of(child);
            }
        }
        return Optional.empty();
    }

    public <T extends PsiElement> SearchResult<T> allChildrenOfTypeMeetingCondition(PsiElement psiElement, Class<T> elementType, Predicate<T> typeCondition) {
        return allChildrenOfTypeMeetingConditionWithReferences(new HashSet<>(), null, psiElement, elementType, typeCondition, t -> false);
    }

    public <T extends PsiElement> SearchResult<T> allChildrenOfTypeWithReferences(PsiElement psiElement, Class<T> elementType, Predicate<PsiElement> fromReferencesMeetingCondition) {
        return allChildrenOfTypeMeetingConditionWithReferences(psiElement, elementType, t -> true, fromReferencesMeetingCondition);
    }

    public <T extends PsiElement> SearchResult<T> allChildrenOfTypeMeetingConditionWithReferences(PsiElement psiElement, Class<T> elementType, Predicate<T> typeCondition, Predicate<PsiElement> fromReferencesMeetingCondition) {
        return allChildrenOfTypeMeetingConditionWithReferences(new HashSet<>(), null,  psiElement, elementType, typeCondition, fromReferencesMeetingCondition);
    }

    private <T extends PsiElement> SearchResult<T> allChildrenOfTypeMeetingConditionWithReferences(HashSet<PsiElement> visitedReferences, SearchResult currentSearchResult, PsiElement psiElement, Class<T> elementType, Predicate<T> typeCondition, Predicate<PsiElement> fromReferencesMeetingCondition) {
        SearchResult<T> result = currentSearchResult;
        if (result == null) {
            result = new SearchResult<>();
        }
        if (!(psiElement instanceof PsiPackageBase)) {
            for (PsiElement child : psiElement.getChildren()) {
                if (elementType.isInstance(child) && typeCondition.test(elementType.cast(child))) {
                    result.getElements().add(elementType.cast(child));
                }
                if (child instanceof PsiReferenceExpression) {
                    PsiElement referencedElement = ((PsiReferenceExpression) child).resolve();
                    if (!(referencedElement instanceof PsiClass) && referencedElement != null && !visitedReferences.contains(referencedElement)) {
                        if (fromReferencesMeetingCondition.test(referencedElement)) {
                            if (elementType.isInstance(referencedElement) && typeCondition.test(elementType.cast(referencedElement))) {
                                result.getElements().add(elementType.cast(referencedElement));
                            }
                            visitedReferences.add(referencedElement);
                            SearchResult<T> next = allChildrenOfTypeMeetingConditionWithReferences(
                                    visitedReferences,
                                    null,
                                    referencedElement,
                                    elementType,
                                    typeCondition,
                                    fromReferencesMeetingCondition)
                                    ;
                            next.setPrevious(result);
                            result.addReferencedResults(Pair.of((PsiReferenceExpression)child,next));
                        }
                    }
                }
                allChildrenOfTypeMeetingConditionWithReferences(
                        visitedReferences,
                        result,
                        child,
                        elementType,
                        typeCondition,
                        fromReferencesMeetingCondition);
            }
        }
        return result;
    }

    public class SearchResult<T> {
        private SearchResult previous;
        private List<Pair<PsiReferenceExpression,SearchResult>> referencedResults = new ArrayList<>();
        private List<T> elements = new ArrayList<>();

        public SearchResult getPrevious() {
            return previous;
        }

        public void setPrevious(SearchResult previous) {
            this.previous = previous;
        }

        public List<Pair<PsiReferenceExpression,SearchResult>> getReferencedResults() {
            return referencedResults;
        }

        public void addReferencedResults(Pair<PsiReferenceExpression,SearchResult> referencedResult) {
           this.referencedResults.add(referencedResult);
        }

        public List<T> getElements() {
            return elements;
        }

        public void setElements(List<T> elements) {
            this.elements = elements;
        }

    }

    public <T extends PsiElement> List<T> getElementsFromSearchResult(SearchResult<T> searchResult) {
        List<T> result = new ArrayList<>();
        result.addAll(searchResult.getElements());
        for (Pair<PsiReferenceExpression,SearchResult> referencedResult : searchResult.getReferencedResults()) {
            result.addAll(getElementsFromSearchResult(referencedResult.getRight()));
        }

        return result;
    }
}
