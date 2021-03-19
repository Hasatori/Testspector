package com.testspector.model.checking.java.common;

import com.intellij.psi.*;
import com.testspector.model.checking.java.JavaTest;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;


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
    public void resolveAssertMethod_assertionMethodCallThatReferencesAssertionMethodProvidedByLibrary_ShouldResolve(String packagePath, String className) {
        PsiJavaFile psiJavaFile = this.javaTestElementUtil.createFile(className, packagePath, Collections.emptyList(), Collections.emptyList());
        PsiClass psiClass = (PsiClass) psiJavaFile.add(this.psiElementFactory.createClass(className));
        String libraryAssertionMethodName = "assertionMethod";
        PsiMethod assertMethod = this.javaTestElementUtil.createMethod(libraryAssertionMethodName, "String", Collections.singletonList("public"));
        PsiMethodCallExpression assertionMethodCall = (PsiMethodCallExpression) psiElementFactory.createExpressionFromText(String.format("%s()", libraryAssertionMethodName), psiClass);
        assertMethod = (PsiMethod) psiClass.add(assertMethod);

        JavaMethodResolver javaMethodResolver = new JavaMethodResolver(elementResolver, contextIndicator);
        PsiMethod foundMethod = javaMethodResolver.assertionMethod(assertionMethodCall).get();

        Assert.assertSame(assertMethod, foundMethod);
    }

    @Test
    public void resolveAssertMethod_methodCallReferencingCustomAssertionMethodInTestSource_ShouldResolve() {
        String className = "Test";
        PsiJavaFile psiJavaFile = this.javaTestElementUtil.createFile(className, "com.testspector", Collections.emptyList(), Collections.emptyList());
        PsiClass psiClass = (PsiClass) psiJavaFile.add(this.psiElementFactory.createClass(className));
        String customAssertionMethodName = "customAssertionMethod";
        PsiThrowStatement throwStatement = (PsiThrowStatement) this.psiElementFactory.createStatementFromText("throw new AssertionError()", null);
        PsiMethod assertMethod = this.psiElementFactory.createMethodFromText(createCustomAssertionMethod(customAssertionMethodName,throwStatement), null);
        PsiMethodCallExpression testedMethodCall = (PsiMethodCallExpression) psiElementFactory.createExpressionFromText(String.format("%s()", customAssertionMethodName), psiClass);
        assertMethod = (PsiMethod) psiClass.add(assertMethod);
        EasyMock.expect(contextIndicator.isInTestContext()).andReturn((element)->true).once();
        EasyMock.expect(elementResolver.allChildrenOfType(EasyMock.eq(assertMethod),EasyMock.eq(PsiThrowStatement.class),EasyMock.anyObject(),EasyMock.anyObject())).andReturn(Collections.singletonList(throwStatement));
        EasyMock.replay(contextIndicator,elementResolver);

        JavaMethodResolver javaMethodResolver = new JavaMethodResolver(elementResolver, contextIndicator);
        PsiMethod foundMethod = javaMethodResolver.assertionMethod(testedMethodCall).get();

        Assert.assertSame(assertMethod, foundMethod);
    }

    @Test
    public void resolveAssertMethod_assertionMethodCallThatReferencesNullMethod_ShouldNotResolve() {
        String className = "Test";
        PsiJavaFile psiJavaFile = this.javaTestElementUtil.createFile(className, "com.testspector", Collections.emptyList(), Collections.emptyList());
        PsiClass psiClass = (PsiClass) psiJavaFile.add(this.psiElementFactory.createClass(className));
        PsiMethodCallExpression assertionMethodCall = (PsiMethodCallExpression) psiElementFactory.createExpressionFromText(String.format("%s()","assertionMethod"), psiClass);

        JavaMethodResolver javaMethodResolver = new JavaMethodResolver(elementResolver, contextIndicator);
        Optional<PsiMethod> optionalFoundMethod = javaMethodResolver.assertionMethod(assertionMethodCall);

        Assert.assertFalse(optionalFoundMethod.isPresent());
    }


    private String createCustomAssertionMethod(String methodName,PsiThrowStatement assertionError) {
        return String.format("public static void %s(String expected,String actual) {\n" +
                "        if (expected!=null && !expected.equals(actual)) {\n" +
                "            %s;\n" +
                "        }\n" +
                "    }", methodName,assertionError.getText());
    }

}
