package com.testspector.model.checking.java.junit;

import com.intellij.psi.*;
import com.testspector.model.checking.UnitTestFrameworkFactory;
import com.testspector.model.checking.UnitTestFrameworkResolveIndicationStrategy;
import com.testspector.model.enums.UnitTestFramework;

import java.util.Arrays;
import java.util.Optional;

import static com.testspector.model.checking.java.junit.JUnitConstants.JUNIT_PACKAGES_QUALIFIED_NAMES;
import static com.testspector.model.checking.java.junit.JUnitConstants.TEST_QUALIFIED_NAMES;

public class JUnitUnitTestFrameworkResolveIndicationStrategy extends UnitTestFrameworkResolveIndicationStrategy {

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

    @Override
    public UnitTestFramework getUnitTestFramework() {
        return UnitTestFramework.JUNIT;
    }
}
