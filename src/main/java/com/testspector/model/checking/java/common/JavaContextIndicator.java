package com.testspector.model.checking.java.common;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class JavaContextIndicator {


    public Predicate<PsiElement> isInTestContext() {
        return element -> {
            PsiJavaFile psiJavaFile = (PsiJavaFile) element.getContainingFile();
            if (psiJavaFile != null) {
                VirtualFile virtualFile = psiJavaFile.getVirtualFile();
                if (virtualFile != null) {
                    String absolutePath = virtualFile.getPath();
                    String packagePath = psiJavaFile.getPackageName();
                    return Pattern.compile(String.format("src/test/java/%s/%s$", packagePath.replaceAll("\\.", "/"), psiJavaFile.getName())).matcher((absolutePath)).find();
                }

            }
            return false;
        };
    }

    public Predicate<PsiElement> isInProductionCodeContext() {
        return element -> {
            PsiJavaFile psiJavaFile = (PsiJavaFile) element.getContainingFile();
            if (psiJavaFile != null) {
                VirtualFile virtualFile = psiJavaFile.getVirtualFile();
                if (virtualFile != null) {
                    String absolutePath = virtualFile.getPath();
                    String packagePath = psiJavaFile.getPackageName();
                    return Pattern.compile(String.format("src/main/java/%s/%s$", packagePath.replaceAll("\\.", "/"), psiJavaFile.getName())).matcher((absolutePath)).find();
                }
            }
            return false;
        };
    }
}
