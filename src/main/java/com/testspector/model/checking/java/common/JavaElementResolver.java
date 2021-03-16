package com.testspector.model.checking.java.common;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
        int level = 0;
        int maxLevel = 2;
        return allChildrenOfType(maxLevel, level, psiElement, elementType, typeCondition, fromReferencesMeetingCondition);
    }

    private <T extends PsiElement> List<T> allChildrenOfType(int maxLevel, int level, PsiElement psiElement, Class<T> elementType, Predicate<T> typeCondition, Predicate<PsiElement> fromReferencesMeetingCondition) {
        List<T> result = new ArrayList<>();
        for (PsiElement child : psiElement.getChildren()) {
            if (elementType.isInstance(child) && typeCondition.test(elementType.cast(child))) {
                result.add(elementType.cast(child));
            }
            if (child instanceof PsiReferenceExpression) {
                level = ++level;
              // if (level < maxLevel) {
                    PsiElement referencedElement = ((PsiReferenceExpression) child).resolve();
                    if (referencedElement != null) {
                        if (fromReferencesMeetingCondition.test(referencedElement)) {
                            if (elementType.isInstance(referencedElement)) {
                                result.add(elementType.cast(referencedElement));
                            }
                            result.addAll(allChildrenOfType(maxLevel, level, referencedElement, elementType, typeCondition, fromReferencesMeetingCondition));

                        }
                    }
              //  }
            }
            result.addAll(allChildrenOfType(maxLevel,level,child, elementType, typeCondition, fromReferencesMeetingCondition));
        }
        return result;
    }


}
