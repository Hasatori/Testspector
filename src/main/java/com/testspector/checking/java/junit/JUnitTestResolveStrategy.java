package com.testspector.checking.java.junit;

import com.intellij.psi.PsiElement;
import com.testspector.checking.TestResolveStrategy;

public class JUnitTestResolveStrategy extends TestResolveStrategy {

    @Override
    public PsiElement resolveTest(PsiElement element) {
        return null;
    }
}
