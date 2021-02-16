package com.testspector.model.checking;

import com.intellij.psi.PsiElement;

public abstract class UnitTestFrameworkResolveStrategy {


    public abstract boolean canResolveFromPsiElement(PsiElement psiElement);

}
