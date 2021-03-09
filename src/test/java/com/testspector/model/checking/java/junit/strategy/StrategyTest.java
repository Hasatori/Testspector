package com.testspector.model.checking.java.junit.strategy;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.testspector.model.checking.TestBase;
import com.testspector.model.checking.java.JavaTest;
import com.testspector.model.checking.java.JavaTestElementUtil;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaElementResolver;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collections;

public abstract class StrategyTest extends JavaTest {

    protected JavaElementResolver elementResolver;
    protected JavaContextIndicator contextIndicator;
    protected JavaMethodResolver methodResolver;
    protected PsiJavaFile testJavaFile;
    protected PsiClass testClass;

    @BeforeEach
    public void beforeEach(){
        super.beforeEach();
        this.elementResolver = EasyMock.mock(JavaElementResolver.class);
        this.contextIndicator = EasyMock.mock(JavaContextIndicator.class);
        this.methodResolver = EasyMock.mock(JavaMethodResolver.class);
        String fileName = "Test";
        WriteCommandAction.runWriteCommandAction(null, () -> {
            this.testJavaFile = this.javaTestElementUtil.createFile(fileName, "com.testspector", Collections.singletonList("import org.junit.jupiter.api.Test;"), Collections.emptyList());
            this.testClass = this.psiElementFactory.createClass(fileName);
            this.testClass = (PsiClass) testJavaFile.add(testClass);
        });
    }
}
