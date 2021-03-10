package com.testspector.model.checking.java.junit.strategy;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.RelatedElementWrapper;
import com.testspector.model.enums.BestPractice;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


@RunWith(JUnitPlatform.class)
public class NoSimpleTestsJUnitCheckingStrategyTest extends StrategyTest {

    private NoSimpleTestsJUnitCheckingStrategy strategy;


    @BeforeEach
    public void beforeEach() {
        this.strategy = new NoSimpleTestsJUnitCheckingStrategy(elementResolver, methodResolver, contextIndicator);
    }

    @Test
    public void checkBestPractices_TestMethodTestsSimpleGetter_OneViolationReportingAboutNoSimpleTestsShouldBeReturned() {
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            // Given
            PsiJavaFile psiJavaFile = (PsiJavaFile) this.javaTestElementUtil.createFile("Test2", "com.testspector", Collections.emptyList(), Collections.emptyList());
            psiJavaFile = (PsiJavaFile) myFixture.addFileToProject("Test2.java", psiJavaFile.getText().replaceAll("\n", ""));
            testJavaFile = (PsiJavaFile) myFixture.addFileToProject("Test.java", testJavaFile.getText().replaceAll("\n", ""));
            PsiClass class2 = (PsiClass) psiJavaFile.add(psiElementFactory.createClass("Test2"));
            PsiField getterField = (PsiField) class2.add(psiElementFactory.createField("test", PsiType.getJavaLangString(PsiManager.getInstance(getProject()), GlobalSearchScope.EMPTY_SCOPE)));
            String simpleGetterMethodName = "getName";
            PsiMethod simpleGetterMethod = (PsiMethod) class2.add(this.javaTestElementUtil.createMethod(simpleGetterMethodName, "String", "return test;", Collections.singletonList(PsiKeyword.PUBLIC)));
            PsiReturnStatement simpleGetterReturnStatement = (PsiReturnStatement) simpleGetterMethod.getBody().getChildren()[1];
            PsiReferenceExpression fieldReference = (PsiReferenceExpression) simpleGetterReturnStatement.getChildren()[2];
            PsiMethod testMethod = (PsiMethod) testClass.add(this.javaTestElementUtil.createTestMethod("testMethod", Collections.singletonList("@org.junit.Test")));
            PsiDeclarationStatement declarationStatement = (PsiDeclarationStatement) testMethod.getBody().add(psiElementFactory.createVariableDeclarationStatement("testedClass", PsiTypeVariable.getTypeByName(class2.getQualifiedName(), getProject(), GlobalSearchScope.allScope(getProject()))
                    , psiElementFactory.createExpressionFromText("new com.testspector.Test2()", class2)));
            PsiExpressionStatement testMethodExpression = (PsiExpressionStatement) testMethod.getBody().add(this.psiElementFactory.createStatementFromText(String.format("testedClass.%s();", simpleGetterMethodName), testMethod));
            PsiMethodCallExpression testMethodMethodCallExpress = (PsiMethodCallExpression) testMethodExpression.getExpression();
            PsiReferenceExpression localVariableExpression = ((PsiReferenceExpression) testMethodExpression.getChildren()[0].getChildren()[0].getChildren()[0]);
            EasyMock.expect(methodResolver.allTestedMethods(testMethod)).andReturn(Collections.singletonList(simpleGetterMethod)).times(1);
            EasyMock.expect(methodResolver.isGetter(simpleGetterMethod)).andReturn(true).times(1);
            EasyMock.expect(elementResolver.allChildrenOfType(testMethod, PsiReferenceExpression.class))
                    .andReturn(Collections.singletonList(testMethodMethodCallExpress.getMethodExpression())).times(1);
            EasyMock.expect(elementResolver.allChildrenOfType(testMethodMethodCallExpress.getMethodExpression(), PsiReferenceExpression.class))
                    .andReturn(Collections.singletonList(localVariableExpression)).times(1);
            EasyMock.expect(elementResolver.allChildrenOfType(testMethod, PsiReferenceExpression.class))
                    .andReturn(Collections.singletonList(testMethodMethodCallExpress.getMethodExpression())).times(1);
            EasyMock.expect(contextIndicator.isInTestContext()).andReturn((element) -> true).times(1);
            EasyMock.expect(elementResolver.allChildrenOfType(EasyMock.eq(testMethodMethodCallExpress), EasyMock.eq(PsiMethodCallExpression.class), EasyMock.anyObject(), EasyMock.anyObject()))
                    .andReturn(Collections.singletonList(testMethodMethodCallExpress)).times(1);
            EasyMock.replay(contextIndicator, methodResolver, elementResolver);
            List<BestPracticeViolation> expectedViolations = Collections.singletonList(
                    createBestPracticeViolation(
                            String.format("%s#%s", testMethod.getContainingClass().getQualifiedName(), testMethod.getName()),
                            testMethod,
                            testMethod.getNameIdentifier().getTextRange(),
                            "Simple tests for getter and setters are redundant and can be deleted. These methods do not represent complex logic and therefore does not need to be tested.",
                            Collections.singletonList("Delete simple tests"),
                            Arrays.asList(
                                    new RelatedElementWrapper(simpleGetterMethod.getName(), new HashMap<PsiElement, String>() {{
                                        put(testMethodMethodCallExpress.getMethodExpression(), "method reference in test");
                                        put(simpleGetterMethod, "method definition in production code");
                                    }}))));
            // When
            List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testMethod);
            EasyMock.verify(contextIndicator, methodResolver, elementResolver);
            //Then
            assertAll(
                    () -> Assert.assertSame("Incorrect number of found violations", 1, foundViolations.size()),
                    () -> assertThat(foundViolations.get(0)).as("Checking first found violation").isEqualToIgnoringGivenFields(expectedViolations.get(0), "relatedElementsWrapper"),
                    () -> Assert.assertSame("Incorrect number of related elements for first violation", 1, foundViolations.get(0).getRelatedElements().size()),
                    () -> assertThat(foundViolations.get(0).getRelatedElements().get(0)).as("Checking first related element in the first violation").isEqualToComparingFieldByField(expectedViolations.get(0).getRelatedElements().get(0))
            );
        });
    }

    @Test
    public void checkBestPractices_TestedMethodNotSimple_NoViolationShouldBeReturned() {
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            // Given
            String simpleGetterMethodName = "getName";
            PsiMethod simpleGetterMethod = (PsiMethod) this.javaTestElementUtil.createMethod(simpleGetterMethodName, "String", "return test;", Collections.singletonList(PsiKeyword.PUBLIC));
            PsiMethod testMethod = (PsiMethod) testClass.add(this.javaTestElementUtil.createTestMethod("testMethod", Collections.singletonList("@org.junit.Test")));
            EasyMock.expect(methodResolver.allTestedMethods(testMethod)).andReturn(Collections.singletonList(simpleGetterMethod)).times(1);
            EasyMock.expect(methodResolver.isGetter(simpleGetterMethod)).andReturn(false).times(1);
            EasyMock.replay(methodResolver);
            // When
            List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testMethod);
            EasyMock.verify(methodResolver);
            //Then
            Assert.assertSame("Incorrect number of found violations", 0, foundViolations.size());
        });
    }

    private BestPracticeViolation createBestPracticeViolation(String name, PsiElement testMethodElement, TextRange testMethodTextRange, String problemDescription, List<String> hints, List<RelatedElementWrapper> relatedElementsWrapper) {
        return new BestPracticeViolation(
                name,
                testMethodElement,
                testMethodTextRange,
                problemDescription,
                hints,
                BestPractice.NO_SIMPLE_TESTS,
                relatedElementsWrapper

        );
    }

}
