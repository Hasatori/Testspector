package com.testspector.model.checking.java.junit;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Optional;

import static com.testspector.model.checking.java.junit.Util.getFirstMethodWithAnnotationQualifiedName;
import static com.testspector.model.checking.java.junit.Util.getMethodFromFileByName;

@RunWith(JUnitPlatform.class)
public class JUnitTestLineResolveStrategyInspectionInvocation extends BasePlatformTestCase {


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
        return "src/test/resources/jUnitTestLineResolveStrategyTest";
    }


    @DisplayName(value = "Test method qualified name:{0}")
    @ParameterizedTest
    @ValueSource(strings = {"org.junit.jupiter.api.Test", "org.junit.jupiter.params.ParameterizedTest", "org.junit.jupiter.api.RepeatedTest"})
    public void resolveTest(String testMethodQualifiedName) {
        PsiMethod someJUnit5Method = getSomeJUnit5PsiMethodByAnnotationQualifiedName(testMethodQualifiedName);
        PsiElement expectedTestLine = ApplicationManager
                .getApplication()
                .runReadAction(((Computable<PsiElement>) someJUnit5Method::getNameIdentifier
                ));
        JUnitInspectionInvocationLineResolveStrategy jUnitTestLineResolveStrategy = new JUnitInspectionInvocationLineResolveStrategy();

        PsiElement resolvedTestLine = ApplicationManager
                .getApplication()
                .runReadAction(((Computable<PsiElement>) () -> jUnitTestLineResolveStrategy.resolveInspectionInvocationLine(someJUnit5Method).get()));

        Assert.assertSame("Methods identifier should have been returned!", expectedTestLine, resolvedTestLine);
    }

    @Test
    public void resolveTest_JUnit4Test_ShouldReturnMethodsIdentifier() {
        PsiMethod someJUnit5Method = getSomeJUnit4PsiMethod();
        PsiElement expectedTestLine = ApplicationManager
                .getApplication()
                .runReadAction(((Computable<PsiElement>) someJUnit5Method::getNameIdentifier
                ));
        JUnitInspectionInvocationLineResolveStrategy jUnitTestLineResolveStrategy = new JUnitInspectionInvocationLineResolveStrategy();

        PsiElement resolvedTestLine = ApplicationManager
                .getApplication()
                .runReadAction(((Computable<PsiElement>) () -> jUnitTestLineResolveStrategy.resolveInspectionInvocationLine(someJUnit5Method).get()));

        Assert.assertSame("Methods identifier should have been returned!", expectedTestLine, resolvedTestLine);
    }

    @Test
    public void resolveTest_NullElement_ShouldReturnEmpty() {
        JUnitInspectionInvocationLineResolveStrategy jUnitTestLineResolveStrategy = new JUnitInspectionInvocationLineResolveStrategy();

        Optional<PsiElement> optionalPsiElement = ApplicationManager
                .getApplication()
                .runReadAction(((Computable<Optional<PsiElement>>) () -> jUnitTestLineResolveStrategy.resolveInspectionInvocationLine(null)));

        Assert.assertFalse(optionalPsiElement.isPresent());
    }

    @Test
    public void resolveTest_TestNGTestMethod_ShouldReturnEmpty() {
        PsiMethod someTestNgMethod = getSomeTestNGPsiMethod();
        JUnitInspectionInvocationLineResolveStrategy jUnitTestLineResolveStrategy = new JUnitInspectionInvocationLineResolveStrategy();

        Optional<PsiElement> optionalPsiElement = ApplicationManager
                .getApplication()
                .runReadAction(((Computable<Optional<PsiElement>>) () -> jUnitTestLineResolveStrategy.resolveInspectionInvocationLine(someTestNgMethod)));

        Assert.assertFalse(optionalPsiElement.isPresent());
    }

    private PsiMethod getSomeTestNGPsiMethod() {
        PsiJavaFile file = (PsiJavaFile) myFixture.configureByFile("JavaWithTestNG.java");
        return getMethodFromFileByName(file, "aFastTest").get();
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
