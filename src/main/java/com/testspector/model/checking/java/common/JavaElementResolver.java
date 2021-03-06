package com.testspector.model.checking.java.common;

import com.intellij.psi.*;

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
        List<T> result = new ArrayList<>();
        for (PsiElement child : psiElement.getChildren()) {
            if (elementType.isInstance(child)) {
                result.add(elementType.cast(child));
            }
            result.addAll(allChildrenOfType(child, elementType));
        }
        return result;
    }

    public Optional<PsiElement> firstChildIgnoring(PsiElement psiElement, List<Class<? extends PsiElement>> ignoredList) {
        for (PsiElement child : psiElement.getChildren()) {
            if (ignoredList.stream().noneMatch(ignored -> ignored.isInstance(child))) {
                return Optional.of(child);
            }
        }
        return Optional.empty();
    }

    public <T extends PsiElement> List<T> allChildrenOfTypeWithReferencesThatMeetCondition(PsiElement psiElement, Class<T> elementType, Predicate<PsiElement> referencedElementCondition) {
        List<T> result = new ArrayList<>();
        for (PsiElement child : psiElement.getChildren()) {
            if (elementType.isInstance(child)) {
                result.add(elementType.cast(child));
            }
            if (child instanceof PsiReferenceExpression) {
                PsiElement referencedElement = ((PsiReferenceExpression) child).resolve();
                if (referencedElement != null) {
                    if (referencedElementCondition.test(referencedElement)) {
                        if (elementType.isInstance(referencedElement)) {
                            result.add(elementType.cast(referencedElement));
                        }
                        result.addAll(allChildrenOfTypeWithReferencesThatMeetCondition(referencedElement, elementType, referencedElementCondition));
                    }
                }
            }
            result.addAll(allChildrenOfTypeWithReferencesThatMeetCondition(child, elementType, referencedElementCondition));
        }
        return result;
    }


}
