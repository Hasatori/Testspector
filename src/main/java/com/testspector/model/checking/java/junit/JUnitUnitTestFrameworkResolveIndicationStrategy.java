package com.testspector.model.checking.java.junit;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiMethod;
import com.testspector.model.checking.UnitTestFrameworkResolveIndicationStrategy;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JUnitUnitTestFrameworkResolveIndicationStrategy extends UnitTestFrameworkResolveIndicationStrategy {

    private static final List<String> JUNIT_PACKAGES_QUALIFIED_NAMES = Arrays.asList(
            "org.junit.jupiter.api",
            "org.junit.jupiter.params",
            "org.junit"
    );

    private static final List<String> TEST_QUALIFIED_NAMES = Arrays.asList(
            "org.junit.Test",
            "org.junit.jupiter.api.Test",
            "org.junit.jupiter.params.ParameterizedTest",
            "org.junit.jupiter.api.RepeatedTest"
    );


    @Override
    public boolean canResolveFromPsiElement(PsiElement psiElement) {
        boolean resolved = false;
        if (psiElement instanceof PsiFile) {
            resolved = isFromFile((PsiFile) psiElement);
        } else if (psiElement instanceof PsiMethod) {
            resolved = isFromMethod((PsiMethod) psiElement);
        }
        return resolved;
    }

    private boolean isFromFile(PsiFile psiFile) {
        Optional<PsiElement> optionalPsiElement = Arrays.stream(psiFile.getChildren())
                .filter(element -> element instanceof PsiImportList)
                .findFirst();
        if (optionalPsiElement.isPresent()) {
            PsiImportList psiImportList = (PsiImportList) optionalPsiElement.get();
            return Arrays.stream(psiImportList.getImportStatements())
                    .anyMatch(
                            psiImportStatement -> JUNIT_PACKAGES_QUALIFIED_NAMES
                                    .stream()
                                    .anyMatch(junitPackageName -> psiImportStatement.getQualifiedName() != null && psiImportStatement.getQualifiedName().startsWith(junitPackageName))
                    );


        }
        return false;
    }

    private boolean isFromMethod(PsiMethod psiMethod) {
        return Arrays
                .stream(psiMethod.getModifierList().getAnnotations())
                .anyMatch(psiAnnotation -> TEST_QUALIFIED_NAMES.contains(psiAnnotation.getQualifiedName()));

    }
}
