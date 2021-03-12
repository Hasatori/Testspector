package com.testspector.model.checking.java.junit;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.testspector.model.checking.java.JavaTest;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.util.Collections;


public class JUnitUnitTestFrameworkResolveIndicationStrategyTest extends JavaTest {


    @DisplayName(value = "File name:{0}")
    @ParameterizedTest
    @ValueSource(strings = {"JavaWithJunit4.java", "JavaWithJunit5.java"})
    public void canResolveFromPsiElement_PsiFilesWithDifferentJUnitVersions_ShoudIndicateThatCanResolve(String fileName) throws Exception {
        String fileText = FileUtils.readFileToString(new File(getClass().getClassLoader().getResource("jUnitUnitTestFrameworkResolveIndicationStrategyTest/" + fileName).getFile()), "UTF-8");
        PsiFile psiFile = psiFileFactory.createFileFromText(JavaLanguage.INSTANCE, fileText);

        boolean canResolveJUnitFramework = ApplicationManager
                .getApplication()
                .runReadAction(((Computable<Boolean>) () ->
                        new JUnitUnitTestFrameworkResolveIndicationStrategy().canResolveFromPsiElement(psiFile)));

        assertTrue(canResolveJUnitFramework);

    }

    @Test
    public void canResolveFromPsiElement_JUnit4MethodTest_ShoudIndicateThatCanResolve() {
        PsiMethod psiMethod = this.javaTestElementUtil.createTestMethod("someTest", Collections.singletonList("@org.junit.Test"));
        ;

        boolean canResolveJUnitFramework = ApplicationManager
                .getApplication()
                .runReadAction(((Computable<Boolean>) () ->
                        new JUnitUnitTestFrameworkResolveIndicationStrategy().canResolveFromPsiElement(psiMethod)));

        assertTrue(canResolveJUnitFramework);
    }

    @DisplayName("Test method qualified name:{0}")
    @ParameterizedTest
    @ValueSource(strings = {"org.junit.jupiter.api.Test", "org.junit.jupiter.params.ParameterizedTest", "org.junit.jupiter.api.RepeatedTest"})
    public void canResolveFromPsiElement_AllJUnit5Methods_ShoudIndicateThatCanResolve(String testMethodQualifiedName) {
        PsiMethod psiMethod = this.javaTestElementUtil.createTestMethod("someTest", Collections.singletonList(String.format("@%s", testMethodQualifiedName)));

        boolean canResolveJUnitFramework = ApplicationManager
                .getApplication()
                .runReadAction(((Computable<Boolean>) () ->
                        new JUnitUnitTestFrameworkResolveIndicationStrategy().canResolveFromPsiElement(psiMethod)));

        assertTrue(canResolveJUnitFramework);
    }

    @Test
    public void canResolveFromPsiElement_TypescriptFileWithJestTests_ShoudIndicateThatCanNotResolve() throws Exception {
        String typescriptFileText = FileUtils.readFileToString(new File(getClass().getClassLoader().getResource("jUnitUnitTestFrameworkResolveIndicationStrategyTest/TypeScriptWithJest.tsx").getFile()), "UTF-8");
        PsiFile psiFile = psiFileFactory.createFileFromText(JavaLanguage.INSTANCE, typescriptFileText);

        boolean canResolveJUnitFramework = ApplicationManager
                .getApplication()
                .runReadAction(((Computable<Boolean>) () ->
                        new JUnitUnitTestFrameworkResolveIndicationStrategy().canResolveFromPsiElement(psiFile)));

        assertFalse(canResolveJUnitFramework);

    }


}
