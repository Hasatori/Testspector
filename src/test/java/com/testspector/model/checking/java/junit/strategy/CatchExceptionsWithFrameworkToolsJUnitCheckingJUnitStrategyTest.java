package com.testspector.model.checking.java.junit.strategy;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.RelatedElementWrapper;
import com.testspector.model.enums.BestPractice;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class CatchExceptionsWithFrameworkToolsJUnitCheckingJUnitStrategyTest extends JUnitStrategyTest {

    private CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy strategy;

    @BeforeEach
    public void beforeEach() {
        this.strategy = new CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy(elementResolver, contextIndicator, methodResolver);
    }

    @ParameterizedTest
    @ValueSource(strings = {"org.junit.jupiter.api.Test", "org.junit.jupiter.params.ParameterizedTest", "org.junit.jupiter.api.RepeatedTest"})
    public void checkBestPractices_JUnit5TestMethodContainsTryCatchStatement_OneViolationReportingAboutCatchingTheExceptionUsingJUnit5AssertionShouldBeReturned(String jUnit5TestAnnotationQualifiedName) {
        // Given
        PsiMethod testMethod = (PsiMethod) testClass.add(this.javaTestElementUtil.createTestMethod("testMethod", Collections.singletonList("@" + jUnit5TestAnnotationQualifiedName)));
        PsiTryStatement tryStatement = (PsiTryStatement) testMethod.getBody().add(this.psiElementFactory.createStatementFromText("try {} catch (Exception e){}", null));
        EasyMock.expect(contextIndicator.isInTestContext()).andReturn((element) -> true).anyTimes();
        EasyMock.replay(contextIndicator);
        EasyMock.expect(elementResolver.allChildrenOfType(EasyMock.eq(testMethod), EasyMock.eq(PsiTryStatement.class), EasyMock.eq(contextIndicator.isInTestContext())))
                .andReturn(Arrays.asList(tryStatement)).times(1);
        EasyMock.expect(elementResolver.allChildrenOfType(testMethod, PsiReferenceExpression.class)).andReturn(Collections.emptyList()).times(1);
        EasyMock.replay(elementResolver);
        List<BestPracticeViolation> expectedViolations = Collections.singletonList(
                createBestPracticeViolation(
                        String.format("%s#%s", testMethod.getContainingClass().getQualifiedName(), testMethod.getName()),
                        testMethod,
                        testMethod.getNameIdentifier().getTextRange(),
                        "Tests should not contain try catch block. These blocks are redundant and make test harder to read and understand. In some cases it might even lead to never failing tests if we are not handling the exception properly.",
                        Collections.singletonList("You are using JUnit5 so it can be solved by using org.junit.jupiter.api.Assertions.assertThrows() method"),
                        Arrays.asList(
                                new RelatedElementWrapper(String.format("Try catch statement ...%d - %d...", tryStatement.getTextRange().getStartOffset(), tryStatement.getTextRange().getEndOffset()), new HashMap<PsiElement, String>() {{
                                    put(tryStatement, "statement");
                                }}))));
        // When
        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testMethod);

        //Then
        assertAll(
                () -> Assert.assertSame("Incorrect number of found violations", 1, foundViolations.size()),
                () -> assertThat(foundViolations.get(0)).as("Checking first found violation").isEqualToIgnoringGivenFields(expectedViolations.get(0), "relatedElementsWrapper"),
                () -> Assert.assertSame("Incorrect number of related elements for first violation", 1, foundViolations.get(0).getRelatedElements().size()),
                () -> assertThat(foundViolations.get(0).getRelatedElements().get(0)).as("Checking first related element in the first violation").isEqualToComparingFieldByField(expectedViolations.get(0).getRelatedElements().get(0))
        );
    }

    @Test
    public void checkBestPractices_JUnit4TestMethodContainsTryCatchStatement_OneViolationReportingAboutCatchingTheExceptionUsingJUnit4AnnotationShouldBeReturned() {
        // Given
        PsiMethod testMethod = (PsiMethod) testClass.add(this.javaTestElementUtil.createTestMethod("testMethod", Collections.singletonList("@org.junit.Test")));
        PsiTryStatement tryStatement = (PsiTryStatement) testMethod.getBody().add(this.psiElementFactory.createStatementFromText("try {} catch (Exception e){}", null));
        EasyMock.expect(contextIndicator.isInTestContext()).andReturn((element) -> true).anyTimes();
        EasyMock.replay(contextIndicator);
        EasyMock.expect(elementResolver.allChildrenOfType(EasyMock.eq(testMethod), EasyMock.eq(PsiTryStatement.class), EasyMock.eq(contextIndicator.isInTestContext())))
                .andReturn(Arrays.asList(tryStatement)).times(1);
        EasyMock.expect(elementResolver.allChildrenOfType(testMethod, PsiReferenceExpression.class)).andReturn(Collections.emptyList()).times(1);
        EasyMock.replay(elementResolver);
        List<BestPracticeViolation> expectedViolations = Collections.singletonList(
                createBestPracticeViolation(
                        String.format("%s#%s", testMethod.getContainingClass().getQualifiedName(), testMethod.getName()),
                        testMethod,
                        testMethod.getNameIdentifier().getTextRange(),
                        "Tests should not contain try catch block. These blocks are redundant and make test harder to read and understand. In some cases it might even lead to never failing tests if we are not handling the exception properly.",
                        Collections.singletonList("You are using JUnit4 so it can be solved by using @org.junit.Assert.Test(expected = Exception.class) for the test method"),
                        Arrays.asList(
                                new RelatedElementWrapper(String.format("Try catch statement ...%d - %d...", tryStatement.getTextRange().getStartOffset(), tryStatement.getTextRange().getEndOffset()), new HashMap<PsiElement, String>() {{
                                    put(tryStatement, "statement");
                                }}))));
        // When
        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testMethod);

        //Then
        assertAll(
                () -> Assert.assertSame("Incorrect number of found violations", 1, foundViolations.size()),
                () -> assertThat(foundViolations.get(0)).as("Checking first found violation").isEqualToIgnoringGivenFields(expectedViolations.get(0), "relatedElementsWrapper"),
                () -> Assert.assertSame("Incorrect number of related elements for first violation", 1, foundViolations.get(0).getRelatedElements().size()),
                () -> assertThat(foundViolations.get(0).getRelatedElements().get(0)).as("Checking first related element in the first violation").isEqualToComparingFieldByField(expectedViolations.get(0).getRelatedElements().get(0))
        );
    }

    @Test
    public void checkBestPractices_JUnit4TestMethodCallsMethodWithTryCatchStatement_OneViolationReportingContainingRelatedElementWithRefenceFromTestMethodAndReferenceToTheStatement() {
        // Given
        PsiMethod testMethod = (PsiMethod) testClass.add(this.javaTestElementUtil.createTestMethod("testMethod", Collections.singletonList("@org.junit.Test")));
        String helperMethodName = "helperMethod";
        PsiMethod helperMethod = (PsiMethod) testClass.add(this.javaTestElementUtil.createMethod(helperMethodName, "void", Arrays.asList("public")));
        PsiMethodCallExpression helperMethodCall = (PsiMethodCallExpression) testMethod.getBody().add(this.psiElementFactory.createExpressionFromText(String.format("%s()", helperMethodName), null));
        PsiTryStatement tryStatement = (PsiTryStatement) helperMethod.getBody().add(this.psiElementFactory.createStatementFromText("try {} catch (Exception e){}", null));
        EasyMock.expect(contextIndicator.isInTestContext()).andReturn((element) -> true).anyTimes();
        EasyMock.replay(contextIndicator);
        EasyMock.expect(elementResolver.allChildrenOfType(EasyMock.eq(testMethod), EasyMock.eq(PsiTryStatement.class), EasyMock.eq(contextIndicator.isInTestContext())))
                .andReturn(Arrays.asList(tryStatement)).times(1);
        EasyMock.expect(elementResolver.allChildrenOfType(testMethod, PsiReferenceExpression.class)).andReturn(Collections.singletonList(helperMethodCall.getMethodExpression())).times(1);
        EasyMock.expect(elementResolver.allChildrenOfType(EasyMock.eq(helperMethodCall), EasyMock.eq(PsiStatement.class), EasyMock.anyObject(), EasyMock.eq(contextIndicator.isInTestContext()))).andReturn(Collections.singletonList(tryStatement)).times(1);
        EasyMock.replay(elementResolver);
        List<BestPracticeViolation> expectedViolations = Collections.singletonList(
                createBestPracticeViolation(
                        String.format("%s#%s", testMethod.getContainingClass().getQualifiedName(), testMethod.getName()),
                        testMethod,
                        testMethod.getNameIdentifier().getTextRange(),
                        "Tests should not contain try catch block. These blocks are redundant and make test harder to read and understand. In some cases it might even lead to never failing tests if we are not handling the exception properly.",
                        Collections.singletonList("You are using JUnit4 so it can be solved by using @org.junit.Assert.Test(expected = Exception.class) for the test method"),
                        Arrays.asList(
                                new RelatedElementWrapper(String.format("Try catch statement ...%d - %d...", tryStatement.getTextRange().getStartOffset(), tryStatement.getTextRange().getEndOffset()), new HashMap<PsiElement, String>() {{
                                    put(helperMethodCall.getMethodExpression(), "reference from test method");
                                    put(tryStatement, "statement position");
                                }}))));
        // When
        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testMethod);

        //Then
        assertAll(
                () -> Assert.assertSame("Incorrect number of found violations", 1, foundViolations.size()),
                () -> assertThat(foundViolations.get(0)).as("Checking first found violation").isEqualToIgnoringGivenFields(expectedViolations.get(0), "relatedElementsWrapper"),
                () -> Assert.assertSame("Incorrect number of related elements for first violation", 1, foundViolations.get(0).getRelatedElements().size()),
                () -> assertThat(foundViolations.get(0).getRelatedElements().get(0)).as("Checking first related element in the first violation").isEqualToComparingFieldByField(expectedViolations.get(0).getRelatedElements().get(0))
        );
    }

    @Test
    public void checkBestPractices_TestMethodDoesNotContainTryCatchStatement_NoViolationShouldBeFound() {
        // Given
        PsiMethod testMethod = (PsiMethod) testClass.add(this.javaTestElementUtil.createTestMethod("testMethod", Collections.singletonList("@org.junit.Test")));
        EasyMock.expect(contextIndicator.isInTestContext()).andReturn((element) -> true).anyTimes();
        EasyMock.replay(contextIndicator);
        EasyMock.expect(elementResolver.allChildrenOfType(EasyMock.eq(testMethod), EasyMock.eq(PsiTryStatement.class), EasyMock.eq(contextIndicator.isInTestContext())))
                .andReturn(Collections.emptyList()).times(1);
        EasyMock.replay(elementResolver);
        // When
        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testMethod);
        //Then
        Assert.assertSame("Incorrect number of found violations", 0, foundViolations.size());
    }

    private BestPracticeViolation createBestPracticeViolation(String name, PsiElement testMethodElement, TextRange testMethodTextRange, String problemDescription, List<String> hints, List<RelatedElementWrapper> relatedElementsWrapper) {
        return new BestPracticeViolation(
                name,
                testMethodElement,
                testMethodTextRange,
                problemDescription,
                hints,
                BestPractice.CATCH_TESTED_EXCEPTIONS_USING_FRAMEWORK_TOOLS,
                relatedElementsWrapper

        );
    }

}
