package com.testspector.checking;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiImportList;
import com.testspector.enums.ProgrammingLanguage;
import com.testspector.enums.UnitTestFramework;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class UnitTestFrameworkResolveStrategy {


    public abstract boolean canResolveFromPsiElement(PsiElement psiElement);

}
