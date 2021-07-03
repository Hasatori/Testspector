package com.testspector.model.checking.java.common;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.impl.file.PsiPackageBase;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class JavaElementResolver {


    public <T extends PsiElement> ElementSearchResult<T> allChildrenOfTypeMeetingConditionWithReferences(PsiElement psiElement, Class<T> elementType) {
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

    public <T> ElementSearchResult<T> allChildrenOfTypeMeetingCondition(PsiElement psiElement, Class<T> elementType, Predicate<T> typeCondition) {
        return allChildrenOfTypeMeetingConditionWithReferences(true, new HashSet<>(), null,null, psiElement, elementType, typeCondition, t -> false);
    }

    public <T> ElementSearchResult<T> allChildrenOfTypeWithReferences(PsiElement psiElement, Class<T> elementType, Predicate<PsiElement> fromReferencesMeetingCondition) {
        return allChildrenOfTypeMeetingConditionWithReferences(psiElement, elementType, t -> true, fromReferencesMeetingCondition);
    }

    public <T> ElementSearchResult<T> allChildrenOfTypeMeetingConditionWithReferences(PsiElement psiElement, Class<T> elementType, Predicate<T> typeCondition, Predicate<PsiElement> fromReferencesMeetingCondition) {
        return allChildrenOfTypeMeetingConditionWithReferences(true,new HashSet<>(), null,null, psiElement, elementType, typeCondition, fromReferencesMeetingCondition);
    }

    private <T> ElementSearchResult<T> allChildrenOfTypeMeetingConditionWithReferences(boolean recursionStart, HashSet<PsiElement> visitedReferences, List<T> elements,List<Pair<PsiReferenceExpression, ElementSearchResult<T>>> references, PsiElement psiElement, Class<T> elementType, Predicate<T> typeCondition, Predicate<PsiElement> fromReferencesMeetingCondition) {
        List<T> elementsOfTheCurrentLevel = Optional.ofNullable(elements).orElse(new ArrayList<>());
        List<Pair<PsiReferenceExpression, ElementSearchResult<T>>> referencedResults = Optional.ofNullable(references).orElse(new ArrayList<>());
        if (!(psiElement instanceof PsiPackageBase)) {
            if (!recursionStart && elementType.isInstance(psiElement) && typeCondition.test(elementType.cast(psiElement))){
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
                            ElementSearchResult<T> next = allChildrenOfTypeMeetingConditionWithReferences(
                                    false,
                                    visitedReferences,
                                    null,
                                    null,
                                    referencedElement,
                                    elementType,
                                    typeCondition,
                                    fromReferencesMeetingCondition);
                            referencedResults.add(Pair.of((PsiReferenceExpression) child, next));
                        }
                    }
                }
                allChildrenOfTypeMeetingConditionWithReferences(
                        false,
                        visitedReferences,
                        elementsOfTheCurrentLevel,
                        referencedResults,
                        child,
                        elementType,
                        typeCondition,
                        fromReferencesMeetingCondition);
            }
        }
        return new ElementSearchResult<>(referencedResults, elementsOfTheCurrentLevel);
    }
}
