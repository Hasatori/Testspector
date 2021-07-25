package com.testspector.model.checking.java.junit;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.testspector.model.checking.java.JavaTest;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.enums.UnitTestFramework;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.Optional;

public class JUnitUnitTestFrameworkFactoryTest extends JavaTest {

    private JUnitUnitTestFrameworkFactory jUnitUnitTestFrameworkFactory;
    private JavaContextIndicator contextIndicator;
    @BeforeEach
    public void beforeEach() {
        contextIndicator = EasyMock.mock(JavaContextIndicator.class);
        jUnitUnitTestFrameworkFactory = new JUnitUnitTestFrameworkFactory(contextIndicator);
        ;
    }

    @DisplayName(value = "File name:{0}")
    @ParameterizedTest
    @ValueSource(strings = {"JavaWithJunit4.java", "JavaWithJunit5.java"})
    public void getUnitTestFramework_ElementIsJUnitPsiFile_ShouldReturnJUnit(String fileName) throws Exception {
        String fileText = loadFileContentFromResources("jUnitUnitTestFrameworkResolveIndicationStrategyTest/" + fileName);
        PsiFile psiFile = psiFileFactory.createFileFromText(JavaLanguage.INSTANCE, fileText);
        EasyMock.expect(this.contextIndicator.isInTestContext()).andReturn(element -> true).times(1);
        EasyMock.replay(contextIndicator);

        UnitTestFramework unitTestFramework = jUnitUnitTestFrameworkFactory.getUnitTestFramework(psiFile).get();

        assertSame(UnitTestFramework.JUNIT, unitTestFramework);
    }

    @Test
    public void getUnitTestFramework_JUnit4MethodTest_ShouldReturnJUnit() {
        PsiMethod psiMethod = this.javaTestElementUtil.createTestMethod("someTest", Collections.singletonList("@org.junit.Test"));
        EasyMock.expect(this.contextIndicator.isInTestContext()).andReturn(element -> true).times(1);
        EasyMock.replay(contextIndicator);

        UnitTestFramework unitTestFramework = jUnitUnitTestFrameworkFactory.getUnitTestFramework(psiMethod).get();

        assertSame(UnitTestFramework.JUNIT, unitTestFramework);
    }

    @DisplayName("Test method qualified name:{0}")
    @ParameterizedTest
    @ValueSource(strings = {"org.junit.jupiter.api.Test", "org.junit.jupiter.params.ParameterizedTest", "org.junit.jupiter.api.RepeatedTest"})
    public void getUnitTestFramework_AllJUnit5Methods_ShouldReturnJUnit(String testMethodQualifiedName) {
        PsiMethod psiMethod = this.javaTestElementUtil
                .createTestMethod("someTest", Collections.singletonList(String.format("@%s", testMethodQualifiedName)));
        EasyMock.expect(this.contextIndicator.isInTestContext()).andReturn(element -> true).times(2);
        EasyMock.replay(this.contextIndicator);

        UnitTestFramework unitTestFramework = jUnitUnitTestFrameworkFactory.getUnitTestFramework(psiMethod).get();

        assertSame(UnitTestFramework.JUNIT, unitTestFramework);
    }

    @Test
    public void canResolveFromPsiElement_NullElement_ShouldBeEmpty() {
        PsiElement element = null;
        EasyMock.expect(this.contextIndicator.isInTestContext()).andReturn(el -> true).times(1);
        EasyMock.replay(contextIndicator);
        
        Optional<UnitTestFramework> optionalUnitTestFramework = jUnitUnitTestFrameworkFactory.getUnitTestFramework(element);

        assertFalse(optionalUnitTestFramework.isPresent());
    }
}
