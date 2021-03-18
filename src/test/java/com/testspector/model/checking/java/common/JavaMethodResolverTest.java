package com.testspector.model.checking.java.common;

import com.intellij.psi.*;
import com.testspector.model.checking.java.JavaTest;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;


class JavaMethodResolverTest extends JavaTest {

    private JavaElementResolver elementResolver;
    private JavaContextIndicator contextIndicator;

    @BeforeEach
    public void beforeEach() {
        this.elementResolver = EasyMock.mock(JavaElementResolver.class);
        this.contextIndicator = EasyMock.mock(JavaContextIndicator.class);
    }


    @ParameterizedTest
    @CsvSource(value = {
            "'org.junit.jupiter.api' | 'Assertions'",
            "'org.junit'             | 'Assert'",
            "'org.hamcrest'          | 'MatcherAssert'",
            "'junit.framework'       | 'TestCase'",
            "'org.assertj.core.api'  | 'AssertionsForClassTypes'"
    }, delimiter = '|')
    public void resolveAssertMethod_assertMethodProvidedByLibrary_ShouldResolve(String packagePath, String className) {
        PsiJavaFile psiJavaFile = this.javaTestElementUtil.createFile(className, packagePath, Collections.emptyList(), Collections.emptyList());
        PsiClass psiClass = (PsiClass) psiJavaFile.add(this.psiElementFactory.createClass(className));
        PsiThrowStatement throwStatement = (PsiThrowStatement) this.psiElementFactory.createStatementFromText("throw new AssertionError()",null);
        String testedMethodName = "assertionMethod";
        PsiMethod assertMethod = this.javaTestElementUtil.createMethod(testedMethodName, "String", Collections.singletonList("public"));
        PsiMethodCallExpression testedMethodCall = (PsiMethodCallExpression) psiElementFactory.createExpressionFromText(String.format("%s()", testedMethodName), psiClass);
        assertMethod = (PsiMethod) psiClass.add(assertMethod);
        JavaMethodResolver javaMethodResolver = new JavaMethodResolver(elementResolver, contextIndicator);
        PsiMethod foundMethod = javaMethodResolver.assertionMethod(testedMethodCall).get();

        Assert.assertSame(assertMethod, foundMethod);
        customAssertion("","");
    }

    private  void customAssertion(String expected,String actual){
        if (!expected.equals(actual)){
            throw new AssertionError();
        }
    }
}
