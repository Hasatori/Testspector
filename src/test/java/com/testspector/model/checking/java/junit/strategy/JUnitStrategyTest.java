package com.testspector.model.checking.java.junit.strategy;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.testspector.model.checking.java.JavaTest;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.search.ElementSearchEngine;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class JUnitStrategyTest extends JavaTest {

    protected ElementSearchEngine elementSearchEngine;
    protected JavaContextIndicator contextIndicator;
    protected JavaMethodResolver methodResolver;
    protected PsiJavaFile testJavaFile;
    protected PsiClass testClass;
    protected static final List<String> JUNIT5_TEST_QUALIFIED_NAMES = Collections.unmodifiableList(Arrays.asList(
            "org.junit.jupiter.api.Test",
            "org.junit.jupiter.params.ParameterizedTest",
            "org.junit.jupiter.api.RepeatedTest"
    ));

    protected static final List<String> JUNIT4_TEST_QUALIFIED_NAMES = Collections.unmodifiableList(Arrays.asList(
            "org.junit.Test"
    ));
    @BeforeEach
    public final void strategyTestSetup() {
        this.elementSearchEngine = EasyMock.mock(ElementSearchEngine.class);
        this.contextIndicator = EasyMock.mock(JavaContextIndicator.class);
        this.methodResolver = EasyMock.mock(JavaMethodResolver.class);
        String fileName = "Test";
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            this.testJavaFile = this.javaTestElementUtil.createFile(fileName, "com.testspector", Collections.singletonList("import org.junit.jupiter.api.Test;import com.intellij.openapi.application.ApplicationManager;import com.intellij.openapi.command.WriteCommandAction;"), Collections.emptyList());
            this.testClass = this.psiElementFactory.createClass(fileName);
            this.testClass = (PsiClass) testJavaFile.add(testClass);
        });
    }
}
