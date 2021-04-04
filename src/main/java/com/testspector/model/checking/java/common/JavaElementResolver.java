package com.testspector.model.checking.java.common;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.impl.file.PsiPackageBase;

import java.util.*;
import java.util.function.Predicate;

public class JavaElementResolver {


    public <T extends PsiElement> List<T> allChildrenOfTypeMeetingConditionWithReferences(PsiElement psiElement, Class<T> elementType) {
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


    public <T extends PsiElement> List<T> allChildrenOfTypeWithReferences(PsiElement psiElement, Class<T> elementType, Predicate<PsiElement> fromReferencesMeetingCondition) {
        return allChildrenOfTypeMeetingConditionWithReferences(psiElement, elementType, t -> true, fromReferencesMeetingCondition);
    }

    public <T extends PsiElement> List<T> allChildrenOfTypeMeetingConditionWithReferences(PsiElement psiElement, Class<T> elementType, Predicate<T> typeCondition, Predicate<PsiElement> fromReferencesMeetingCondition) {
        return allChildrenOfTypeMeetingConditionWithReferences(new HashSet<>(), new HashMap<>(), psiElement, elementType, typeCondition, fromReferencesMeetingCondition);
    }

    private <T extends PsiElement> List<T> allChildrenOfTypeMeetingConditionWithReferences(HashSet<PsiElement> visitedReferences,
                                                                                           HashMap<PsiElement, List<T>> referenceFetchedElementsMap,
                                                                                           PsiElement psiElement,
                                                                                           Class<T> elementType,
                                                                                           Predicate<T> typeCondition, Predicate<PsiElement> fromReferencesMeetingCondition) {
        List<T> result = new ArrayList<>();
        if (!(psiElement instanceof PsiPackageBase)) {
            for (PsiElement child : psiElement.getChildren()) {
                if (elementType.isInstance(child) && typeCondition.test(elementType.cast(child))) {
                    result.add(elementType.cast(child));
                }
                if (child instanceof PsiReferenceExpression) {
                    PsiElement referencedElement = ((PsiReferenceExpression) child).resolve();
                    result.addAll(
                            fromReference(
                                    referencedElement,
                                    visitedReferences,
                                    referenceFetchedElementsMap,
                                    elementType,
                                    typeCondition,
                                    fromReferencesMeetingCondition)
                    );
                }
                result.addAll(allChildrenOfTypeMeetingConditionWithReferences(
                        visitedReferences,
                        referenceFetchedElementsMap,
                        child,
                        elementType,
                        typeCondition,
                        fromReferencesMeetingCondition)
                );

            }
        }
        return result;
    }

    private <T extends PsiElement> List<T> fromReference(PsiElement referencedElement,
                                                         HashSet<PsiElement> visitedReferences,
                                                         HashMap<PsiElement, List<T>> referenceFetchedElementsMap,
                                                         Class<T> elementType,
                                                         Predicate<T> typeCondition, Predicate<PsiElement> fromReferencesMeetingCondition) {
        List<T> result = new ArrayList<>();
        if (referencedElement != null && !visitedReferences.contains(referencedElement)) {
            if (fromReferencesMeetingCondition.test(referencedElement)) {
                if (elementType.isInstance(referencedElement) && typeCondition.test(elementType.cast(referencedElement))) {
                    result.add(elementType.cast(referencedElement));
                }
                visitedReferences.add(referencedElement);
                List<T> referenceFetchedElements = allChildrenOfTypeMeetingConditionWithReferences(
                        visitedReferences,
                        referenceFetchedElementsMap,
                        referencedElement,
                        elementType,
                        typeCondition,
                        fromReferencesMeetingCondition);
                referenceFetchedElementsMap.put(referencedElement, referenceFetchedElements);
                result.addAll(
                        referenceFetchedElements
                );
            }
        } else if (referencedElement != null) {
            List<T> referenceFetchedElements = referenceFetchedElementsMap.get(referencedElement);
            if (referenceFetchedElements != null) {
                result.addAll(referenceFetchedElements);
            }
        }
        return result;
    }

}
