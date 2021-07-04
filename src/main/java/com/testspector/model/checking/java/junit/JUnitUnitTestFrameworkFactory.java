package com.testspector.model.checking.java.junit;

import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.testspector.model.checking.factory.UnitTestFrameworkFactory;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.enums.UnitTestFramework;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static com.testspector.model.checking.java.junit.JUnitConstants.JUNIT_ALL_PACKAGES_QUALIFIED_NAMES;
import static com.testspector.model.checking.java.junit.JUnitConstants.JUNIT_ALL_TEST_QUALIFIED_NAMES;

public class JUnitUnitTestFrameworkFactory implements UnitTestFrameworkFactory {


    private final JavaContextIndicator contextIndicator;

    public JUnitUnitTestFrameworkFactory() {
        this.contextIndicator = new JavaContextIndicator();
    }

    @Override
    public Optional<UnitTestFramework> getUnitTestFramework(PsiElement psiElement) {
        boolean resolved = false;
        if (contextIndicator.isInTestContext().test(psiElement)) {
            if (psiElement instanceof PsiFile) {
                resolved = isFromFile((PsiFile) psiElement);
            } else if (psiElement instanceof PsiClass) {
                resolved = isFromFile(psiElement.getContainingFile());
            } else if (psiElement instanceof PsiMethod) {
                resolved = isFromMethod(new HashSet<>(), (PsiMethod) psiElement);
            }
            if (resolved) {
                return Optional.of(UnitTestFramework.JUNIT);
            }
        }
        return Optional.empty();
    }

    private boolean isFromFile(PsiFile psiFile) {
        if (psiFile instanceof PsiJavaFile) {
            PsiJavaFile javaFile = (PsiJavaFile) psiFile;
            Optional<PsiElement> optionalPsiElement = Optional.ofNullable(javaFile.getImportList());
            if (optionalPsiElement.isPresent()) {
                PsiImportList psiImportList = (PsiImportList) optionalPsiElement.get();
                return Arrays.stream(psiImportList.getImportStatements())
                        .anyMatch(
                                psiImportStatement -> JUNIT_ALL_PACKAGES_QUALIFIED_NAMES
                                        .stream()
                                        .anyMatch(junitPackageName -> psiImportStatement.getQualifiedName() != null &&
                                                psiImportStatement.getQualifiedName().startsWith(junitPackageName))
                        ) ||
                        Arrays.stream(javaFile.getClasses())
                                .map(PsiClass::getMethods)
                                .flatMap(Arrays::stream)
                                .filter(contextIndicator.isInTestContext())
                                .anyMatch(method -> isFromMethod(new HashSet<>(), method));


            }
        }
        return false;
    }

    private boolean isFromMethod(HashSet<PsiMethod> visitedMethods, PsiMethod psiMethod) {
        visitedMethods.add(psiMethod);
        return methodHasAnyOfAnnotations(psiMethod, JUNIT_ALL_TEST_QUALIFIED_NAMES) ||
                ReferencesSearch.search(psiMethod).findAll().stream().map(reference -> PsiTreeUtil.getParentOfType(reference.getElement(), PsiMethod.class))
                        .filter(method -> method != null && !visitedMethods.contains(method) && method != psiMethod)
                        .anyMatch(method -> isFromMethod(visitedMethods, method));

    }

    private boolean methodHasAnyOfAnnotations(PsiMethod method, List<String> annotationQualifiedNames) {
        return annotationQualifiedNames.stream().anyMatch(method::hasAnnotation)
                || (annotationQualifiedNames.isEmpty() && method.getAnnotations().length == 0);
    }

}
