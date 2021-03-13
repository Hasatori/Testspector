package com.testspector.model.checking.java.junit;

import com.intellij.psi.*;
import com.testspector.model.checking.factory.UnitTestFrameworkFactory;
import com.testspector.model.enums.UnitTestFramework;

import java.util.Arrays;
import java.util.Optional;

import static com.testspector.model.checking.java.junit.JUnitConstants.JUNIT_ALL_PACKAGES_QUALIFIED_NAMES;
import static com.testspector.model.checking.java.junit.JUnitConstants.JUNIT_ALL_TEST_QUALIFIED_NAMES;

public class JUnitUnitTestFrameworkFactory implements UnitTestFrameworkFactory {


    @Override
    public Optional<UnitTestFramework> getUnitTestFramework(PsiElement psiElement) {
        boolean resolved = false;
        if (psiElement instanceof PsiFile) {
            resolved = isFromFile((PsiFile) psiElement);
        } else if (psiElement instanceof PsiClass) {
            resolved = isFromFile(psiElement.getContainingFile());
        } else if (psiElement instanceof PsiMethod) {
            resolved = isFromMethod((PsiMethod) psiElement);
        }
        if (resolved){
            return Optional.of(UnitTestFramework.JUNIT);
        }
        return Optional.empty();
    }

    private boolean isFromFile(PsiFile psiFile) {
        Optional<PsiElement> optionalPsiElement = Arrays.stream(psiFile.getChildren())
                .filter(element -> element instanceof PsiImportList)
                .findFirst();
        if (optionalPsiElement.isPresent()) {
            PsiImportList psiImportList = (PsiImportList) optionalPsiElement.get();
            return Arrays.stream(psiImportList.getImportStatements())
                    .anyMatch(
                            psiImportStatement -> JUNIT_ALL_PACKAGES_QUALIFIED_NAMES
                                    .stream()
                                    .anyMatch(junitPackageName -> psiImportStatement.getQualifiedName() != null && psiImportStatement.getQualifiedName().startsWith(junitPackageName))
                    );


        }
        return false;
    }

    private boolean isFromMethod(PsiMethod psiMethod) {
        return Arrays
                .stream(psiMethod.getModifierList().getAnnotations())
                .anyMatch(psiAnnotation -> JUNIT_ALL_TEST_QUALIFIED_NAMES.contains(psiAnnotation.getQualifiedName()));

    }

}
