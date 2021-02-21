package com.testspector.model.checking;

import com.intellij.psi.PsiElement;
import com.testspector.model.enums.UnitTestFramework;

import java.util.Optional;

public abstract class UnitTestFrameworkResolveIndicationStrategy {

    protected final UnitTestFramework unitTestFramework;

    protected UnitTestFrameworkResolveIndicationStrategy(UnitTestFramework unitTestFramework) {
        this.unitTestFramework = unitTestFramework;
    }

    public abstract boolean canResolveFromPsiElement(PsiElement psiElement);

    public UnitTestFramework getUnitTestFramework() {
        return unitTestFramework;
    }
}
