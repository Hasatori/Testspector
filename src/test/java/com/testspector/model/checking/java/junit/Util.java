package com.testspector.model.checking.java.junit;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Util {

    private Util() {
    }

    public static Optional<PsiMethod> getMethodFromFileByName(PsiJavaFile psiJavaFile, String methodName) {
        PsiMethod[] methods = Arrays.stream(psiJavaFile.getClasses())
                .findFirst()
                .map(PsiClass::getMethods)
                .get();
        return Arrays.stream(methods).filter(psiMethod -> methodName.equals(psiMethod.getName())).findFirst();

    }

    public static Optional<PsiMethod> getFirstMethodWithAnnotationQualifiedName(PsiJavaFile psiJavaFile, String annotationQualifiedName) {
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

    public static <T extends PsiElement> List<T> getAllChildrenOfType(PsiElement psiElement, Class<T> elementType) {
        List<T> result = new ArrayList<>();
        for (PsiElement child : psiElement.getChildren()) {
            if (elementType.isInstance(child)) {
                result.add(elementType.cast(child));
            }
            result.addAll(getAllChildrenOfType(child, elementType));
        }
        return result;
    }
    public static void assertbla(){
       tet();
    }

    public static void tet(){
        try{

        }catch (Exception e){

        }
        Assert.assertTrue(true);
    }
}
