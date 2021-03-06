package com.testspector.model.checking;

import com.intellij.psi.PsiElement;

import java.util.List;

public abstract class UnitTestFrameworkResolveIndicationStrategy implements UnitTestFrameworkComponent {

    public abstract boolean canResolveFromPsiElement(PsiElement psiElement);

}
