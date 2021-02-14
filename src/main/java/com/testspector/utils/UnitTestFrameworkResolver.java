package com.testspector.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiImportList;
import com.testspector.enums.ProgrammingLanguage;
import com.testspector.enums.UnitTestFramework;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class UnitTestFrameworkResolver {

    private static final List<String> JUNIT_PACKAGES_QUALIFIED_NAMES = Arrays.asList(
            "org.junit.jupiter.api",
            "org.junit.jupiter.params"
    );

    private UnitTestFrameworkResolver() {
    }

    public static UnitTestFramework resolveUnitTestFramework(ProgrammingLanguage programmingLanguage, PsiFile psiFile) {

        switch (programmingLanguage) {
            case JAVA:
                Optional<PsiElement> optionalPsiElement = Arrays.stream(psiFile.getChildren())
                        .filter(psiElement -> psiElement instanceof PsiImportList)
                        .findFirst();
                if (optionalPsiElement.isPresent()) {
                    PsiImportList psiImportList = (PsiImportList) optionalPsiElement.get();
                    if (isJUnit(psiImportList)) {
                        return UnitTestFramework.JUNIT;
                    }
                }
        }
        return null;
    }

    private static boolean isJUnit(PsiImportList psiImportList) {
        return Arrays.stream(psiImportList.getImportStatements())
                .anyMatch(
                        psiImportStatement -> JUNIT_PACKAGES_QUALIFIED_NAMES
                                .stream()
                                .anyMatch(junitPackageName -> psiImportStatement.getQualifiedName() != null && psiImportStatement.getQualifiedName().startsWith(junitPackageName))
                );

    }
}
