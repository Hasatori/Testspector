package com.testspector.model.checking.java.junit;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static com.testspector.model.checking.java.junit.JUnitTestUtil.getFirstMethodWithAnnotationQualifiedName;
import static com.testspector.model.checking.java.junit.JUnitTestUtil.getMethodFromFileByName;

@RunWith(JUnitPlatform.class)
public class JUnitUnitTestFrameworkResolveIndicationStrategyTest extends BasePlatformTestCase {


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
        return "src/test/resources/jUnitUnitTestFrameworkResolveIndicationStrategyTest";
    }


    @DisplayName(value = "File name:{0}")
    @ParameterizedTest
    @ValueSource(strings = {"JavaWithJunit4.java", "JavaWithJunit5.java"})
    public void canResolveFromPsiElement_PsiFilesWithDifferentJUnitVersions_ShoudIndicateThatCanResolve(String fileName) {
        PsiFile psiFile = myFixture.configureByFile(fileName);

        boolean canResolveJUnitFramework = ApplicationManager
                .getApplication()
                .runReadAction(((Computable<Boolean>) () ->
                        new JUnitUnitTestFrameworkResolveIndicationStrategy().canResolveFromPsiElement(psiFile)));

        assertTrue(canResolveJUnitFramework);
    }

    @Test
    public void canResolveFromPsiElement_JUnit4MethodTest_ShoudIndicateThatCanResolve() {
        PsiMethod psiMethod = this.getSomeJUnit4PsiMethod();

        boolean canResolveJUnitFramework = ApplicationManager
                .getApplication()
                .runReadAction(((Computable<Boolean>) () ->
                        new JUnitUnitTestFrameworkResolveIndicationStrategy().canResolveFromPsiElement(psiMethod)));

        assertTrue(canResolveJUnitFramework);
    }

    @DisplayName("Test method qualified name:{0}")
    @ParameterizedTest
    @ValueSource(strings = {"org.junit.jupiter.api.Test","org.junit.jupiter.params.ParameterizedTest","org.junit.jupiter.api.RepeatedTest"})
    public void canResolveFromPsiElement_AllJUnit5Methods_ShoudIndicateThatCanResolve(String testMethodQualifiedName) {
        PsiMethod psiMethod = this.getSomeJUnit5PsiMethodByAnnotationQualifiedName(testMethodQualifiedName);

        boolean canResolveJUnitFramework = ApplicationManager
                .getApplication()
                .runReadAction(((Computable<Boolean>) () ->
                        new JUnitUnitTestFrameworkResolveIndicationStrategy().canResolveFromPsiElement(psiMethod)));

        assertTrue(canResolveJUnitFramework);
    }

    @Test
    public void canResolveFromPsiElement_TypescriptFileWithJestTests_ShoudIndicateThatCanNotResolve() {
        PsiFile psiFile = myFixture.configureByFile("TypeScriptWithJest.tsx");

        boolean canResolveJUnitFramework = ApplicationManager
                .getApplication()
                .runReadAction(((Computable<Boolean>) () ->
                        new JUnitUnitTestFrameworkResolveIndicationStrategy().canResolveFromPsiElement(psiFile)));

        assertFalse(canResolveJUnitFramework);
    }

    private PsiMethod getSomeJUnit4PsiMethod() {
        PsiJavaFile file = (PsiJavaFile) myFixture.configureByFile("JavaWithJunit4.java");
        return getMethodFromFileByName(file, "substraction").get();
    }

    private PsiMethod getSomeJUnit5PsiMethodByAnnotationQualifiedName(String qualifiedName) {
        PsiJavaFile file = (PsiJavaFile) myFixture.configureByFile("JavaWithJunit5.java");
        return getFirstMethodWithAnnotationQualifiedName(file, qualifiedName).get();
    }


}
