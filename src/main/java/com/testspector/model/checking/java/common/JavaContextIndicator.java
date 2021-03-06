package com.testspector.model.checking.java.common;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;

import java.util.regex.Pattern;

public class JavaContextIndicator {



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
}
