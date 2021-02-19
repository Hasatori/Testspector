package com.testspector.model.checking;

import com.intellij.psi.PsiElement;

public abstract class UnitTestFrameworkResolveIndicationStrategy {


    public abstract boolean canResolveFromPsiElement(PsiElement psiElement);

}
