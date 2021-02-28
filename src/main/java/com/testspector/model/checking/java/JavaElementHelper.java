package com.testspector.model.checking.java;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;

import java.util.*;
import java.util.function.Predicate;
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


    public <T extends PsiElement> List<T> getImmediateChildrenOfType(PsiElement psiElement, Class<T> elementType) {
        return Arrays.stream(psiElement.getChildren())
                .filter(elementType::isInstance)
                .map(elementType::cast)
                .collect(Collectors.toList());
    }

    public boolean isInTestContext(PsiElement element) {
        PsiJavaFile psiJavaFile = (PsiJavaFile) element.getContainingFile();
        String absolutePath = psiJavaFile.getVirtualFile().getPath();
        String packagePath = psiJavaFile.getPackageName();
        return Pattern.compile(String.format("src/test/java/%s/%s$", packagePath.replaceAll("\\.", "/"), psiJavaFile.getName())).matcher((absolutePath)).find();

    }

    public boolean isInProductionCodeContext(PsiElement element) {
        PsiJavaFile psiJavaFile = (PsiJavaFile) element.getContainingFile();
        String absolutePath = psiJavaFile.getVirtualFile().getPath();
        String packagePath = psiJavaFile.getPackageName();
        return Pattern.compile(String.format("src/main/java/%s/%s$", packagePath.replaceAll("\\.", "/"), psiJavaFile.getName())).matcher((absolutePath)).find();

    }

    public <T extends PsiElement> List<T> getAllChildrenOfType(PsiElement psiElement, Class<T> elementType) {
        List<T> result = new ArrayList<>();
        for (PsiElement child : psiElement.getChildren()) {
            if (elementType.isInstance(child)) {
                result.add(elementType.cast(child));
            }
            result.addAll(getAllChildrenOfType(child, elementType));
        }
        return result;
    }

    public <T extends PsiElement> Optional<T> getLastChildOfType(PsiElement psiElement, Class<T> elementType) {
        Optional<T> result = Optional.empty();

        for (PsiElement child : psiElement.getChildren()) {
            if (elementType.isInstance(child)) {
                result = Optional.of(elementType.cast(child));
            }
            Optional<T> candidate = getLastChildOfType(child, elementType);
            if (candidate.isPresent()) {
                result = candidate;
            }
        }
        return result;
    }

    public <T extends PsiElement> Optional<T> getLastLeftChildOfType(PsiElement psiElement, Class<T> elementType) {
        Optional<T> result = Optional.empty();

        for (PsiElement child : psiElement.getChildren()) {
            if (elementType.isInstance(child)) {
                result = Optional.of(elementType.cast(child));
            }
            Optional<T> candidate = getLastLeftChildOfType(child, elementType);
            if (candidate.isPresent()) {
                result = candidate;
                break;
            }
        }
        return result;
    }

    public <T extends PsiElement> List<T> getAllChildrenOfType(PsiElement psiElement, Class<T> elementType, Predicate<T> conditionToMeet) {
        List<T> result = new ArrayList<>();
        for (PsiElement child : psiElement.getChildren()) {
            if (elementType.isInstance(child) && conditionToMeet.test(elementType.cast(child))) {
                result.add(elementType.cast(child));
            }
            result.addAll(getAllChildrenOfType(child, elementType));
        }
        return result;
    }

    public Optional<PsiElement> getFirstChildIgnoring(PsiElement psiElement, List<Class<? extends PsiElement>> ignoredList) {
        for (PsiElement child : psiElement.getChildren()) {
            if (ignoredList.stream().noneMatch(ignored -> ignored.isInstance(child))) {
                return Optional.of(child);
            }
        }
        return Optional.empty();
    }

}
