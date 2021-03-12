package com.testspector;

import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFileFactory;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
@ExtendWith(WriteCommandActionTestInterceptor.class)
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
