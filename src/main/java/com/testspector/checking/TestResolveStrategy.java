package com.testspector.checking;

import com.intellij.psi.PsiElement;

public abstract class TestResolveStrategy {

    public abstract PsiElement resolveTest(PsiElement element);

}
