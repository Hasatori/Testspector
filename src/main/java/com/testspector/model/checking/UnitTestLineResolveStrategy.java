package com.testspector.model.checking;

import com.intellij.psi.PsiElement;

import java.util.Optional;

public abstract class UnitTestLineResolveStrategy {

    public abstract Optional<PsiElement> resolveTestLine(PsiElement element);

}
