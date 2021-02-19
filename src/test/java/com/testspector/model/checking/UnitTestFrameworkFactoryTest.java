package com.testspector.model.checking;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Optional;

@RunWith(JUnitPlatform.class)
public class UnitTestFrameworkFactoryTest extends BasePlatformTestCase {


    @BeforeEach
    public void beforeEach() throws Exception {
        this.setUp();
    }

    @AfterEach
    public void afterEach() throws Exception {
        this.tearDown();
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/unitTestFrameworkFactoryTest";
    }


    @Test
    public void getUnitTestFramework_JavaAndFileWithJUnit_ShouldReturnJunit() {
        PsiFile psiFile = myFixture.configureByFile("JavaWithJUnit5.java");
        UnitTestFrameworkFactory unitTestFrameworkFactory = new UnitTestFrameworkFactory();
        UnitTestFramework returnedUnitTestFramework = ApplicationManager
                .getApplication()
                .runReadAction(((Computable<UnitTestFramework>) () -> unitTestFrameworkFactory.getUnitTestFramework(ProgrammingLanguage.JAVA, psiFile).get()));
        Assert.assertSame(UnitTestFramework.JUNIT, returnedUnitTestFramework);

    }


    @Test
    public void getUnitTestFramework_JavaButGroovyFile_ShouldNotReturnFramework() {
        PsiFile psiFile = myFixture.configureByFile("Groovy.groovy");

        UnitTestFrameworkFactory unitTestFrameworkFactory = new UnitTestFrameworkFactory();
        Optional<UnitTestFramework> optionalUnitTestFramework = ApplicationManager
                .getApplication()
                .runReadAction(((Computable<Optional<UnitTestFramework>>) () -> unitTestFrameworkFactory.getUnitTestFramework(ProgrammingLanguage.JAVA, psiFile)));

        Assert.assertFalse(optionalUnitTestFramework.isPresent());

    }
}
