package com.testspector.model.checking.java.junit;

import com.intellij.openapi.application.ApplicationManager;import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.testspector.model.checking.java.JavaTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.Optional;

@RunWith(JUnitPlatform.class)
public class JUnitTestLineResolveStrategyInspectionInvocation extends JavaTest {

    @DisplayName(value = "Test method qualified name:{0}")
    @ParameterizedTest
    @ValueSource(strings = {"org.junit.jupiter.api.Test", "org.junit.jupiter.params.ParameterizedTest", "org.junit.jupiter.api.RepeatedTest"})
    public void resolveInspectionInvocationLine_AllJunit5Annotations_shouldResolve(String testMethodQualifiedName) {
        WriteCommandAction.runWriteCommandAction(getProject(),() -> {
            PsiMethod someJUnit5Method = this.javaTestElementUtil.createTestMethod("someTest", Collections.singletonList(String.format("@%s", testMethodQualifiedName)));
            PsiElement expectedTestLine = someJUnit5Method.getNameIdentifier();
            JUnitInspectionInvocationLineResolveStrategy jUnitTestLineResolveStrategy = new JUnitInspectionInvocationLineResolveStrategy();

            PsiElement resolvedTestLine = jUnitTestLineResolveStrategy.resolveInspectionInvocationLine(someJUnit5Method).get();

            Assertions.assertSame(expectedTestLine, resolvedTestLine, "Methods identifier should have been returned!");
        });
    }

    @Test
    public void resolveInspectionInvocationLine_JUnit4Test_ShouldReturnMethodsIdentifier() {
        WriteCommandAction.runWriteCommandAction(getProject(),() -> {
            PsiMethod someJUnit4Method = this.javaTestElementUtil.createTestMethod("someTest", Collections.singletonList("@org.junit.Test"));
            PsiElement expectedTestLine = someJUnit4Method.getNameIdentifier();
            JUnitInspectionInvocationLineResolveStrategy jUnitTestLineResolveStrategy = new JUnitInspectionInvocationLineResolveStrategy();

            PsiElement resolvedTestLine = jUnitTestLineResolveStrategy.resolveInspectionInvocationLine(someJUnit4Method).get();

            Assertions.assertSame(expectedTestLine, resolvedTestLine, "Methods identifier should have been returned!");
        });
    }

    @Test
    public void resolveInspectionInvocationLine_NullElement_ShouldReturnEmpty() {
        WriteCommandAction.runWriteCommandAction(getProject(),() -> {
            JUnitInspectionInvocationLineResolveStrategy jUnitTestLineResolveStrategy = new JUnitInspectionInvocationLineResolveStrategy();

            Optional<PsiElement> optionalPsiElement = jUnitTestLineResolveStrategy.resolveInspectionInvocationLine(null);

            Assertions.assertFalse(optionalPsiElement.isPresent());
        });
    }

    @Test
    public void resolveInspectionInvocationLine_TestNGTestMethod_ShouldReturnEmpty() {
        WriteCommandAction.runWriteCommandAction(getProject(),() -> {
            PsiMethod someTestNgMethod = this.javaTestElementUtil.createTestMethod("someTest", Collections.singletonList("@org.testng.annotations.Test(groups = { \"fast\" })"));

            JUnitInspectionInvocationLineResolveStrategy jUnitTestLineResolveStrategy = new JUnitInspectionInvocationLineResolveStrategy();

            Optional<PsiElement> optionalPsiElement = ApplicationManager
                    .getApplication()
                    .runReadAction(((Computable<Optional<PsiElement>>) () -> jUnitTestLineResolveStrategy.resolveInspectionInvocationLine(someTestNgMethod)));

            Assertions.assertFalse(optionalPsiElement.isPresent());
        });
    }

}
