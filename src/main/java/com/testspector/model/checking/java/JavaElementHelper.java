package com.testspector.model.checking.java;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JavaElementHelper {

    private final JavaClassHelper javaClassHelper;

    public JavaElementHelper(JavaClassHelper javaClassHelper) {
        this.javaClassHelper = javaClassHelper;
    }

    public Optional<PsiJavaFile> castToFile(PsiElement psiElement) {
        if (psiElement instanceof PsiJavaFile) {
            return Optional.of((PsiJavaFile) psiElement);
        }
        return Optional.empty();
    }

    public Optional<PsiMethod> castToMethod(PsiElement psiElement) {
        if (psiElement instanceof PsiMethod) {
            return Optional.of((PsiMethod) psiElement);
        }
        return Optional.empty();
    }

    public Optional<PsiClass> castToClass(PsiElement psiElement) {
        if (psiElement instanceof PsiClass) {
            return Optional.of((PsiClass) psiElement);
        }
        return Optional.empty();
    }

    public List<PsiMethod> getMethodsFromElementByAnnotations(List<PsiElement> psiElements, List<String> annotationQualifiedNames) {
        List<PsiMethod> psiMethods = new ArrayList<>();
        for (PsiElement psiElement : psiElements) {
            if (castToFile(psiElement).isPresent()) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiElement;
                psiMethods.addAll(Arrays.stream(psiJavaFile.getClasses())
                        .map(psiClass -> javaClassHelper.getMethodsByAnnotations(psiClass, annotationQualifiedNames))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
            } else if (castToClass(psiElement).isPresent()) {
                PsiClass psiClass = (PsiClass) psiElement;
                psiMethods.addAll(javaClassHelper.getMethodsByAnnotations(psiClass, annotationQualifiedNames));
            } else if (castToMethod(psiElement).isPresent()) {
                psiMethods.add((PsiMethod) psiElement);
            }
        }
        return psiMethods;
    }


    public <T extends PsiElement> List<T> getElementsByType(PsiElement psiElement, Class<T> elementType) {
        return Arrays.stream(psiElement.getChildren())
                .filter(elementType::isInstance)
                .map(elementType::cast)
                .collect(Collectors.toList());
    }

    public boolean isInTestContext(PsiMethod method) {
        PsiJavaFile psiJavaFile = (PsiJavaFile) method.getContainingFile();
        String absolutePath = psiJavaFile.getVirtualFile().getPath();
        String packagePath = psiJavaFile.getPackageName();
        String projectName = psiJavaFile.getProject().getName();
        return Pattern.compile(String.format("%s/src/test/java/%s/%s$", projectName, packagePath.replaceAll("\\.", "/"),psiJavaFile.getName())).matcher((absolutePath)).find();

    }
}
