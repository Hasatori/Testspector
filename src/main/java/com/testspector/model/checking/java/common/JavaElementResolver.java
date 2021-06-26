package com.testspector.model.checking.java.common;
import com.intellij.psi.PsiClass;
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

    public <T extends PsiElement> ElementSearchResult<T> allChildrenOfTypeMeetingCondition(PsiElement psiElement, Class<T> elementType, Predicate<T> typeCondition) {
        return allChildrenOfTypeMeetingConditionWithReferences(new HashSet<>(), null, psiElement, elementType, typeCondition, t -> false);
    }

    public <T extends PsiElement> ElementSearchResult<T> allChildrenOfTypeWithReferences(PsiElement psiElement, Class<T> elementType, Predicate<PsiElement> fromReferencesMeetingCondition) {
        return allChildrenOfTypeMeetingConditionWithReferences(psiElement, elementType, t -> true, fromReferencesMeetingCondition);
    }

    public <T extends PsiElement> ElementSearchResult<T> allChildrenOfTypeMeetingConditionWithReferences(PsiElement psiElement, Class<T> elementType, Predicate<T> typeCondition, Predicate<PsiElement> fromReferencesMeetingCondition) {
        return allChildrenOfTypeMeetingConditionWithReferences(new HashSet<>(), null,  psiElement, elementType, typeCondition, fromReferencesMeetingCondition);
    }

    private <T extends PsiElement> ElementSearchResult<T> allChildrenOfTypeMeetingConditionWithReferences(HashSet<PsiElement> visitedReferences, ElementSearchResult currentElementSearchResult, PsiElement psiElement, Class<T> elementType, Predicate<T> typeCondition, Predicate<PsiElement> fromReferencesMeetingCondition) {
        ElementSearchResult<T> result = currentElementSearchResult;
        if (result == null) {
            result = new ElementSearchResult<>();
        }
        if (!(psiElement instanceof PsiPackageBase)) {
            for (PsiElement child : psiElement.getChildren()) {
                if (elementType.isInstance(child) && typeCondition.test(elementType.cast(child))) {
                    result.getElements().add(elementType.cast(child));
                }
                if (child instanceof PsiReferenceExpression) {
                    PsiElement referencedElement = ((PsiReferenceExpression) child).resolve();
                    if (referencedElement != null && !visitedReferences.contains(referencedElement)) {
                        if (fromReferencesMeetingCondition.test(referencedElement)) {
                            visitedReferences.add(referencedElement);
                            ElementSearchResult<T> next = allChildrenOfTypeMeetingConditionWithReferences(
                                    visitedReferences,
                                    null,
                                    referencedElement,
                                    elementType,
                                    typeCondition,
                                    fromReferencesMeetingCondition)
                                    ;
                            next.setPrevious(result);
                            if (elementType.isInstance(referencedElement) && typeCondition.test(elementType.cast(referencedElement))) {
                                next.getElements().add(elementType.cast(referencedElement));
                            }
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
}
