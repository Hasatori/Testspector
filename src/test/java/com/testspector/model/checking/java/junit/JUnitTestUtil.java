package com.testspector.model.checking.java.junit;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;

import java.util.Arrays;
import java.util.Optional;

public class JUnitTestUtil {

    private JUnitTestUtil() {
    }

    public static Optional<PsiMethod> getMethodFromFileByName(PsiJavaFile psiJavaFile, String methodName) {
        return ApplicationManager
                .getApplication()
                .runReadAction(((Computable<Optional<PsiMethod>>) () -> {
                    PsiMethod[] methods = Arrays.stream(psiJavaFile.getClasses())
                            .findFirst()
                            .map(PsiClass::getMethods)
                            .get();
                    return Arrays.stream(methods).filter(psiMethod -> methodName.equals(psiMethod.getName())).findFirst();
                }
                ));
    }

    public static Optional<PsiMethod> getFirstMethodWithAnnotationQualifiedName(PsiJavaFile psiJavaFile, String annotationQualifiedName) {
        return ApplicationManager
                .getApplication()
                .runReadAction(((Computable<Optional<PsiMethod>>) () -> {
                    PsiMethod[] methods = Arrays.stream(psiJavaFile.getClasses())
                            .findFirst()
                            .map(PsiClass::getMethods)
                            .get();
                    return Arrays
                            .stream(methods)
                            .filter(psiMethod -> Arrays
                                    .stream(psiMethod.getAnnotations())
                                    .anyMatch(psiAnnotation -> annotationQualifiedName.equals(psiAnnotation.getQualifiedName())))
                            .findFirst();
                }
                ));
    }
}
