package com.testspector.model.checking.java.common;

import com.intellij.psi.*;
import com.testspector.model.checking.java.JavaTest;
import com.testspector.model.checking.java.common.search.ElementSearchEngine;
import com.testspector.model.checking.java.common.search.ElementSearchResult;
import com.testspector.model.checking.java.common.search.QueriesRepository;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


class JavaMethodResolverTest extends JavaTest {

    private ElementSearchEngine elementSearchEngine;
    private JavaContextIndicator contextIndicator;

    @BeforeEach
    public void beforeEach() {
        this.elementSearchEngine = EasyMock.mock(ElementSearchEngine.class);
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
        PsiJavaFile psiJavaFile = this.javaTestElementUtil
                .createFile(className, packagePath, Collections.emptyList(), Collections.emptyList());
        PsiClass psiClass = (PsiClass) psiJavaFile.add(this.psiElementFactory.createClass(className));
        String libraryAssertionMethodName = "assertionMethod";
        PsiMethod assertMethod = this.javaTestElementUtil
                .createMethod(libraryAssertionMethodName, "String", Collections.singletonList("public"));
        PsiMethodCallExpression assertionMethodCall = (PsiMethodCallExpression) psiElementFactory
                .createExpressionFromText(String.format("%s()", libraryAssertionMethodName), psiClass);
        assertMethod = (PsiMethod) psiClass.add(assertMethod);

        JavaMethodResolver javaMethodResolver = new JavaMethodResolver(elementSearchEngine, contextIndicator);
        PsiMethod foundMethod = javaMethodResolver.assertionMethod(assertionMethodCall).get();

        Assert.assertSame(assertMethod, foundMethod);
    }

     @Test
     public void resolveAssertMethod_methodCallReferencingCustomAssertionMethodInTestSource_ShouldResolve() {
         String className = "Test";
         PsiJavaFile psiJavaFile = this.javaTestElementUtil
                 .createFile(className, "com.testspector", Collections.emptyList(), Collections.emptyList());
         PsiClass psiClass = (PsiClass) psiJavaFile.add(this.psiElementFactory.createClass(className));
         String customAssertionMethodName = "customAssertionMethod";
         PsiThrowStatement throwStatement = (PsiThrowStatement) this.psiElementFactory
                 .createStatementFromText("throw new AssertionError()", null);
         PsiMethod assertMethod = this.psiElementFactory
                 .createMethodFromText(createCustomAssertionMethod(customAssertionMethodName, throwStatement), null);
         PsiMethodCallExpression testedMethodCall = (PsiMethodCallExpression) psiElementFactory
                 .createExpressionFromText(String.format("%s()", customAssertionMethodName), psiClass);
         assertMethod = (PsiMethod) psiClass.add(assertMethod);
         EasyMock.expect(contextIndicator.isInTestContext()).andReturn((element) -> true).once();
         EasyMock.expect(contextIndicator.isInProductionCodeContext()).andReturn((element -> false)).once();
         EasyMock.replay(contextIndicator, elementSearchEngine);

         JavaMethodResolver javaMethodResolver = new JavaMethodResolver(elementSearchEngine, contextIndicator);
         PsiMethod foundMethod = javaMethodResolver.assertionMethod(testedMethodCall).get();

         Assert.assertSame(assertMethod, foundMethod);
     }

    @Test
    public void resolveAssertMethod_assertionMethodCallThatReferencesNullMethod_ShouldNotResolve() {
        String className = "Test";
        PsiJavaFile psiJavaFile = this.javaTestElementUtil
                .createFile(className, "com.testspector", Collections.emptyList(), Collections.emptyList());
        PsiClass psiClass = (PsiClass) psiJavaFile.add(this.psiElementFactory.createClass(className));
        PsiMethodCallExpression assertionMethodCall = (PsiMethodCallExpression) psiElementFactory
                .createExpressionFromText(String.format("%s()", "assertionMethod"), psiClass);

        JavaMethodResolver javaMethodResolver = new JavaMethodResolver(elementSearchEngine, contextIndicator);
        Optional<PsiMethod> optionalFoundMethod = javaMethodResolver.assertionMethod(assertionMethodCall);

        Assert.assertFalse(optionalFoundMethod.isPresent());
    }

    @Test
    public void resolveMethodsWithAnnotations_elementsContainOneFileWithTwoMethodsWhereBothHaveRequiredAnnotation_ShouldResolveTwoMethods() {
        String className = "Test";
        List<String> annotations = Arrays.asList("org.junit.Test", "java.lang.Override");
        PsiJavaFile psiJavaFile = this.javaTestElementUtil
                .createFile(className, "com.testspector", Collections.emptyList(), Collections.emptyList());
        PsiClass psiClass = (PsiClass) psiJavaFile.add(this.psiElementFactory.createClass(className));
        PsiMethod someMethod1 = (PsiMethod) psiClass.add(this.javaTestElementUtil
                .createTestMethod("method1", Collections.singletonList("@" + annotations.get(0))));
        PsiMethod someMethod2 = (PsiMethod) psiClass.add(this.javaTestElementUtil
                .createTestMethod("method2", Collections.singletonList("@" + annotations.get(0))));

        JavaMethodResolver javaMethodResolver = new JavaMethodResolver(elementSearchEngine, contextIndicator);
        List<PsiMethod> resolvedMethods = javaMethodResolver.methodsWithAnnotations(Arrays.asList(psiJavaFile), annotations);

        assertAll(
                () -> assertSame("First method was not resolved!", someMethod1, resolvedMethods.get(0)),
                () -> assertSame("Second method was not resolved!", someMethod2, resolvedMethods.get(1))
        );
    }


    @Test
    public void resolveMethodsWithAnnotations_methodsWithoutAnnotationsRequiredAndElementsContainOneFileWithMethodWithoutAnnotation_ShouldResolveOneMethod() {
        String className = "Test";
        PsiJavaFile psiJavaFile = this.javaTestElementUtil
                .createFile(className, "com.testspector", Collections.emptyList(), Collections.emptyList());
        PsiClass psiClass = (PsiClass) psiJavaFile.add(this.psiElementFactory.createClass(className));
        PsiMethod someMethod = (PsiMethod) psiClass.add(this.javaTestElementUtil
                .createMethod("method1", "void", Collections.singletonList(PsiKeyword.PUBLIC)));

        JavaMethodResolver javaMethodResolver = new JavaMethodResolver(elementSearchEngine, contextIndicator);
        List<PsiMethod> resolvedMethods = javaMethodResolver.methodsWithAnnotations(Arrays.asList(psiJavaFile), Collections.emptyList());

        assertSame(someMethod, resolvedMethods.get(0));
    }

    @Test
    public void resolveMethodsWithAnnotations_elementsContainOneFileWithWithOneMethodWithoutRequiredAnnotation_ShouldNotResolveAnyMethods() {
        String className = "Test";
        List<String> annotations = Arrays.asList("org.junit.Test", "java.lang.Override");
        PsiJavaFile psiJavaFile = this.javaTestElementUtil
                .createFile(className, "com.testspector", Collections.emptyList(), Collections.emptyList());
        PsiClass psiClass = (PsiClass) psiJavaFile.add(this.psiElementFactory.createClass(className));
        psiClass.add(this.javaTestElementUtil
                .createMethod("method1", "void", Collections.singletonList(PsiKeyword.PUBLIC)));

        JavaMethodResolver javaMethodResolver = new JavaMethodResolver(elementSearchEngine, contextIndicator);
        List<PsiMethod> resolvedMethods = javaMethodResolver.methodsWithAnnotations(Arrays.asList(psiJavaFile), annotations);

        assertTrue(resolvedMethods.isEmpty());
    }


    @Test
    public void resolveMethodsWithAnnotations_elementsContainOneClassWithTwoMethodsWhereBothHaveRequiredAnnotation_ShouldResolveTwoMethods() {
        String className = "Test";
        List<String> annotations = Arrays.asList("org.junit.Test", "java.lang.Override");
        PsiJavaFile psiJavaFile = this.javaTestElementUtil
                .createFile(className, "com.testspector", Collections.emptyList(), Collections.emptyList());
        PsiClass psiClass = (PsiClass) psiJavaFile.add(this.psiElementFactory.createClass(className));
        PsiMethod someMethod1 = (PsiMethod) psiClass.add(this.javaTestElementUtil
                .createTestMethod("method1", Collections.singletonList("@" + annotations.get(0))));
        PsiMethod someMethod2 = (PsiMethod) psiClass.add(this.javaTestElementUtil
                .createTestMethod("method2", Collections.singletonList("@" + annotations.get(0))));

        JavaMethodResolver javaMethodResolver = new JavaMethodResolver(elementSearchEngine, contextIndicator);
        List<PsiMethod> resolvedMethods = javaMethodResolver.methodsWithAnnotations(Arrays.asList(psiClass), annotations);

        assertAll(
                () -> assertSame("First method was not resolved!", someMethod1, resolvedMethods.get(0)),
                () -> assertSame("Second method was not resolved!", someMethod2, resolvedMethods.get(1))
        );
    }

    @Test
    public void resolveMethodsWithAnnotations_elementsContainOneClassWithOneMethodWithoutRequiredAnnotation_ShouldNotResolveAnyMethods() {
        String className = "Test";
        List<String> annotations = Arrays.asList("org.junit.Test", "java.lang.Override");
        PsiJavaFile psiJavaFile = this.javaTestElementUtil
                .createFile(className, "com.testspector", Collections.emptyList(), Collections.emptyList());
        PsiClass psiClass = (PsiClass) psiJavaFile.add(this.psiElementFactory.createClass(className));
        psiClass.add(this.javaTestElementUtil.createMethod("method1", "void", Collections.singletonList(PsiKeyword.PUBLIC)));

        JavaMethodResolver javaMethodResolver = new JavaMethodResolver(elementSearchEngine, contextIndicator);
        List<PsiMethod> resolvedMethods = javaMethodResolver.methodsWithAnnotations(Arrays.asList(psiClass), annotations);

        assertTrue(resolvedMethods.isEmpty());
    }

    @Test
    public void resolveMethodsWithAnnotations_elementsContainOneMethodWithGivenAnnotation_ShouldResolveOneMethod() {
        List<String> annotations = Arrays.asList("org.junit.Test");
        PsiMethod someMethod = this.javaTestElementUtil.createTestMethod("method1", Collections.singletonList("@" + annotations.get(0)));

        JavaMethodResolver javaMethodResolver = new JavaMethodResolver(elementSearchEngine, contextIndicator);
        List<PsiMethod> resolvedMethods = javaMethodResolver.methodsWithAnnotations(Arrays.asList(someMethod), annotations);

        assertSame(someMethod, resolvedMethods.get(0));
    }

    @Test
    public void resolveMethodsWithAnnotations_methodsWithoutAnnotationsRequiredAndElementsContainOneMethodWithoutAnnotation_ShouldResolveOneMethod() {
        PsiMethod someMethod = this.javaTestElementUtil.createMethod("method1", "void", Collections.singletonList(PsiKeyword.PUBLIC));

        JavaMethodResolver javaMethodResolver = new JavaMethodResolver(elementSearchEngine, contextIndicator);
        List<PsiMethod> resolvedMethods = javaMethodResolver.methodsWithAnnotations(Arrays.asList(someMethod), Collections.emptyList());

        assertSame(someMethod, resolvedMethods.get(0));
    }

    @Test
    public void resolveMethodsWithAnnotations_elementsContainOneMethodWithoutRequiredAnnotation_ShouldNotResolveAnyMethods() {
        List<String> annotations = Arrays.asList("org.junit.Test");
        PsiMethod someMethod = this.javaTestElementUtil.createMethod("method1", "void", Collections.singletonList(PsiKeyword.PUBLIC));

        JavaMethodResolver javaMethodResolver = new JavaMethodResolver(elementSearchEngine, contextIndicator);
        List<PsiMethod> resolvedMethods = javaMethodResolver.methodsWithAnnotations(Arrays.asList(someMethod), annotations);

        assertTrue(resolvedMethods.isEmpty());
    }

    @Test
    public void resolveMethodsWithAnnotations_elementsContainOneIfStatement_ShouldNotResolveAnyMethods() {
        List<String> annotations = Arrays.asList("org.junit.Test");
        PsiIfStatement psiIfStatement = this.javaTestElementUtil.createIfStatement();

        JavaMethodResolver javaMethodResolver = new JavaMethodResolver(elementSearchEngine, contextIndicator);
        List<PsiMethod> resolvedMethods = javaMethodResolver.methodsWithAnnotations(Arrays.asList(psiIfStatement), annotations);

        assertTrue(resolvedMethods.isEmpty());
    }

    @Test
    public void isGetter_methodIsSimpleGetterWhichReturnsFieldOfTheClass_ShouldReturnTrue() {
        PsiClass psiClass = this.psiElementFactory.createClass("Test");
        String fieldName = "name";
        PsiField field = (PsiField) psiClass.add(this.psiElementFactory
                .createFieldFromText(String.format("private String %s;", fieldName), psiClass));
        PsiMethod getterMethod = (PsiMethod) psiClass.add(this.psiElementFactory.createMethodFromText(
                "public String getName(){return name;}", psiClass));
        JavaMethodResolver javaMethodResolver = new JavaMethodResolver(elementSearchEngine, contextIndicator);

        boolean result = javaMethodResolver.isGetter(getterMethod);

        assertTrue(result);
    }

    @Test
    public void isGetter_isComplexMethod_ShouldReturnFalse() {
        PsiMethod complexMethod = this.javaTestElementUtil
                .createMethod("getName", "String", Collections.singletonList("public"));
        PsiStatement ifStatement = (PsiStatement) complexMethod.getBody().add(this.javaTestElementUtil.createIfStatement());
        ifStatement.add(this.psiElementFactory.createStatementFromText("String fieldName = \"name\";", complexMethod));

        JavaMethodResolver javaMethodResolver = new JavaMethodResolver(elementSearchEngine, contextIndicator);
        boolean result = javaMethodResolver.isGetter(complexMethod);

        assertFalse(result);
    }

    @Test
    public void allTestedMethods_oneAssertionWhichCallsMethodFromProduction_ShouldResolveOneMethod() {
        PsiJavaFile psiJavaFile = this.javaTestElementUtil
                .createFile("Test", "com.testspector", Collections.emptyList(), Collections.emptyList());
        PsiClass testClass = (PsiClass) psiJavaFile.add(this.psiElementFactory.createClass("Test"));
        String testedMethodName = "test";
        PsiMethod testedMethod = this.javaTestElementUtil
                .createMethod(testedMethodName, "void", Collections.singletonList(PsiKeyword.PUBLIC));
        PsiMethod testMethod = (PsiMethod) testClass.add(this.javaTestElementUtil
                .createTestMethod("TestMethod", Collections.singletonList("@org.junit.Test")));
        PsiMethodCallExpression testedMethodCall = (PsiMethodCallExpression) testMethod
                .getBody()
                .add(this.psiElementFactory.createExpressionFromText(testedMethodName + "()", testClass));
        testedMethod = (PsiMethod) testClass.add(testedMethod);
        PsiMethodCallExpression assertionMethodCall = (PsiMethodCallExpression) psiElementFactory.createExpressionFromText("assertionMethod()", null);
        EasyMock.expect(contextIndicator.isInTestContext()).andReturn((element -> true)).times(3);
        EasyMock.expect(contextIndicator.isInProductionCodeContext()).andReturn((element -> true)).times(2);
        EasyMock.replay(contextIndicator);
        EasyMock.expect(elementSearchEngine
                .findByQuery(
                        EasyMock.eq(testMethod), EasyMock.eq(QueriesRepository.FIND_ALL_ASSERTION_METHOD_CALL_EXPRESSIONS)))
                .andReturn(new ElementSearchResult<>(new ArrayList<>(), Collections.singletonList(assertionMethodCall))).times(1);
        ElementSearchResult<PsiMethodCallExpression> expectedResult = new ElementSearchResult<>(new ArrayList<>(), Collections.singletonList(assertionMethodCall));
        EasyMock.expect(elementSearchEngine
                .findByQuery(
                        EasyMock.eq(assertionMethodCall), EasyMock.eq(QueriesRepository.FIND_ALL_PRODUCTION_CODE_METHOD_CALL_EXPRESSIONS)))
                .andReturn(expectedResult).times(1);
        EasyMock.replay(elementSearchEngine);

        JavaMethodResolver javaMethodResolver = new JavaMethodResolver(elementSearchEngine, contextIndicator);
        ElementSearchResult<PsiMethodCallExpression> result = javaMethodResolver.allTestedMethodsMethodCalls(testMethod);

        assertThat(expectedResult).usingRecursiveComparison().isEqualTo(result);
    }

    @Test
    public void allTestedMethods_noAssertionMethods_ShouldNotResolveAnyMethods() {
        PsiJavaFile psiJavaFile = this.javaTestElementUtil
                .createFile("Test", "com.testspector", Collections.emptyList(), Collections.emptyList());
        PsiClass testClass = (PsiClass) psiJavaFile.add(this.psiElementFactory.createClass("Test"));
        String methodName = "TestMethod";
        PsiMethod testMethod = (PsiMethod) testClass.add(this.javaTestElementUtil
                .createTestMethod(methodName, Collections.singletonList("@org.junit.Test")));
        PsiMethodCallExpression methodCall = (PsiMethodCallExpression) psiElementFactory
                .createExpressionFromText(String.format("%s()", methodName), testClass);
        ElementSearchResult<PsiMethodCallExpression> expectedResult = new ElementSearchResult<>(new ArrayList<>(), Collections.singletonList(methodCall));
        ElementSearchResult<PsiMethodCallExpression> query = new ElementSearchResult<>(new ArrayList<>(), new ArrayList<>());
        EasyMock.expect(elementSearchEngine.findByQuery(EasyMock.eq(testMethod), EasyMock.eq(QueriesRepository.FIND_ALL_ASSERTION_METHOD_CALL_EXPRESSIONS))).andReturn(expectedResult).times(1);
        EasyMock.expect(elementSearchEngine.findByQuery(EasyMock.eq(testMethod), EasyMock.eq(QueriesRepository.FIND_ALL_PRODUCTION_CODE_METHOD_CALL_EXPRESSIONS))).andReturn(expectedResult).times(1);
        EasyMock.expect(contextIndicator.isInTestContext()).andReturn((element -> true)).anyTimes();
        EasyMock.replay(elementSearchEngine, contextIndicator);

        JavaMethodResolver javaMethodResolver = new JavaMethodResolver(new ElementSearchEngine(), new JavaContextIndicator());
        ElementSearchResult<PsiMethodCallExpression> result = javaMethodResolver.allTestedMethodsMethodCalls(testMethod);

        assertTrue(result.getElementsFromAllLevels().isEmpty());
    }


    private String createCustomAssertionMethod(String methodName, PsiThrowStatement assertionError) {
        return String.format("public static void %s(String expected,String actual) {\n" +
                "        if (expected!=null && !expected.equals(actual)) {\n" +
                "            %s;\n" +
                "        }\n" +
                "    }", methodName, assertionError.getText());
    }

}
