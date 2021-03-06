package com.testspector.model.checking.java.junit;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.testspector.model.checking.InspectionInvocationLineResolveStrategy;
import com.testspector.model.enums.UnitTestFramework;

import java.util.Arrays;
import java.util.Optional;

import static com.testspector.model.checking.java.junit.JUnitConstants.JUNIT_ALL_TEST_QUALIFIED_NAMES;

public class JUnitInspectionInvocationLineResolveStrategy extends InspectionInvocationLineResolveStrategy {

    @Override
    public Optional<PsiElement> resolveInspectionInvocationLine(PsiElement element) {
        if (element instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) element;
            Optional<PsiIdentifier> identifier = getIdentifier(method);
            if (identifier.isPresent() && isJunitTestMethod((method))) {
                return Optional.of(identifier.get());
            }
        } else if (element instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) element;
            Optional<PsiIdentifier> identifier = getIdentifier(element);
            if (identifier.isPresent() && Arrays.stream(psiClass.getMethods()).anyMatch(this::isJunitTestMethod)) {
                return Optional.of(identifier.get());
            }
        }
        return Optional.empty();
    }

    @Override
    public UnitTestFramework getUnitTestFramework() {
        return UnitTestFramework.JUNIT;
    }


    private Optional<PsiIdentifier> getIdentifier(PsiElement element) {
        return Arrays
                .stream(element.getChildren())
                .filter(psiElement -> psiElement instanceof PsiIdentifier).findFirst().map(ident -> (PsiIdentifier) ident);
    }

    private boolean isJunitTestMethod(PsiMethod method) {
        return Arrays
                .stream((method).getAnnotations())
                .anyMatch(psiAnnotation -> JUNIT_ALL_TEST_QUALIFIED_NAMES.contains(psiAnnotation.getQualifiedName()));
    }
}
