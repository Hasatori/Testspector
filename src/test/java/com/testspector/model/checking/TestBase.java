package com.testspector.model.checking;

import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFileFactory;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class TestBase extends BasePlatformTestCase {

    protected PsiElementFactory psiElementFactory;
    protected PsiFileFactory psiFileFactory;


    @BeforeEach
    public final void platformSetup() throws Exception {
        setUp();
        psiFileFactory = PsiFileFactory.getInstance(myFixture.getProject());
        psiElementFactory = PsiElementFactory.getInstance(myFixture.getProject());
    }

    @AfterEach
    public final void platformTeardown() throws Exception {
        tearDown();
    }
}
