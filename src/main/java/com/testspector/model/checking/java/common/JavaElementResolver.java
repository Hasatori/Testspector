package com.testspector.model.checking.java.common;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.impl.file.PsiPackageBase;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JavaElementResolver {

    public <T extends PsiElement> List<T> immediateChildrenOfType(PsiElement psiElement, Class<T> elementType) {
        return Arrays.stream(psiElement.getChildren())
                .filter(elementType::isInstance)
                .map(elementType::cast)
                .collect(Collectors.toList());
    }


    public <T extends PsiElement> List<T> allChildrenOfType(PsiElement psiElement, Class<T> elementType) {
        return allChildrenOfType(psiElement, elementType, t -> true, t -> false);
    }

    public Optional<PsiElement> firstChildIgnoring(PsiElement psiElement, List<Class<? extends PsiElement>> ignoredList) {
        for (PsiElement child : psiElement.getChildren()) {
            if (ignoredList.stream().noneMatch(ignored -> ignored.isInstance(child))) {
                return Optional.of(child);
            }
        }
        return Optional.empty();
    }


    public <T extends PsiElement> List<T> allChildrenOfType(PsiElement psiElement, Class<T> elementType, Predicate<PsiElement> fromReferencesMeetingCondition) {
        return allChildrenOfType(psiElement, elementType, t -> true, fromReferencesMeetingCondition);
    }

    public <T extends PsiElement> List<T> allChildrenOfType(PsiElement psiElement, Class<T> elementType, Predicate<T> typeCondition, Predicate<PsiElement> fromReferencesMeetingCondition) {
        return allChildrenOfType(new HashSet<>(), psiElement, elementType, typeCondition, fromReferencesMeetingCondition);
    }

    private <T extends PsiElement> List<T> allChildrenOfType(HashSet<PsiElement> visited, PsiElement psiElement, Class<T> elementType, Predicate<T> typeCondition, Predicate<PsiElement> fromReferencesMeetingCondition) {
        List<T> result = new ArrayList<>();
        if (!(psiElement instanceof PsiPackageBase)) {
            for (PsiElement child : psiElement.getChildren()) {
                if (elementType.isInstance(child) && typeCondition.test(elementType.cast(child))) {
                    result.add(elementType.cast(child));
                }
                if (child instanceof PsiReferenceExpression) {
                    PsiElement referencedElement = ((PsiReferenceExpression) child).resolve();
                    if (referencedElement != null && !visited.contains(referencedElement)) {
                        if (fromReferencesMeetingCondition.test(referencedElement)) {
                            if (elementType.isInstance(referencedElement)) {
                                result.add(elementType.cast(referencedElement));
                            }
                            visited.add(referencedElement);
                            result.addAll(allChildrenOfType(visited, referencedElement, elementType, typeCondition, fromReferencesMeetingCondition));
                        }
                    }
                }
                result.addAll(allChildrenOfType(visited, child, elementType, typeCondition, fromReferencesMeetingCondition));

            }
        }
        return result;
    }

}
