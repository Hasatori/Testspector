package com.testspector.model.checking.java;

import com.intellij.psi.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JavaClassHelper {


    public List<PsiMethod> getMethodsByAnnotation(PsiClass psiClass, String annotationQualifiedName) {
        return Arrays.stream(psiClass.getMethods())
                .filter(psiMethod -> psiMethod.hasAnnotation(annotationQualifiedName))
                .collect(Collectors.toList());
    }
    public List<PsiMethod> getMethodsByAnnotations(PsiClass psiClass, List<String> annotationQualifiedNames) {
        return Arrays.stream(psiClass.getMethods())
                .filter(psiMethod -> annotationQualifiedNames.stream().anyMatch(psiMethod::hasAnnotation))
                .collect(Collectors.toList());
    }

}
