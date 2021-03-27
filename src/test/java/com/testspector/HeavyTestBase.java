package com.testspector;

import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFileFactory;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;


@ExtendWith(WriteCommandActionTestInterceptor.class)
public abstract class HeavyTestBase extends BasePlatformTestCase {

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

    protected File loadFileFromResources(String path) {
        return new File(getClass().getClassLoader().getResource(path).getFile());
    }

    protected String loadFileContentFromResources(String path) throws IOException {
        return FileUtils.readFileToString(loadFileFromResources(path), "UTF-8");
    }
}
