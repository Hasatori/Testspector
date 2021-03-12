package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.RelatedElementWrapper;
import com.testspector.model.enums.BestPractice;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class AssertionCountJUnitCheckingStrategyTest extends StrategyTest {


    private AssertionCountJUnitCheckingStrategy strategy;


    @BeforeEach
    public void beforeEach() {
        this.strategy = new AssertionCountJUnitCheckingStrategy(elementResolver, contextIndicator, methodResolver);
    }


    @Test
    public void checkBestPractices_TestMethodWithoutAnyAssertions_OneViolationReportingAboutThatAtLeastOneAssertionShouldBeInTheTestShouldBeReturned() {
        // Given
        String testMethodName = "testWithNoAssertions";
        PsiMethod testMethodWithoutAssertions = this.javaTestElementUtil.createTestMethod(testMethodName, Collections.singletonList("@Test"));
        testMethodWithoutAssertions = (PsiMethod) testClass.add(testMethodWithoutAssertions);
        EasyMock.expect(contextIndicator.isInTestContext()).andReturn((element) -> true).anyTimes();
        EasyMock.replay(contextIndicator);
        EasyMock.expect(elementResolver.allChildrenOfType(EasyMock.eq(testMethodWithoutAssertions), EasyMock.eq(PsiMethodCallExpression.class), EasyMock.anyObject(), EasyMock.eq(contextIndicator.isInTestContext())))
                .andReturn(new ArrayList<>()).times(1);
        EasyMock.replay(elementResolver);
        List<BestPracticeViolation> expectedViolations = Collections.singletonList(
                new BestPracticeViolation(
                        String.format("%s#%s", testMethodWithoutAssertions.getContainingClass().getQualifiedName(), testMethodName),
                        testMethodWithoutAssertions,
                        testMethodWithoutAssertions.getNameIdentifier().getTextRange(),
                        "Test should contain at least one assertion method!",
                        BestPractice.AT_LEAST_ONE_ASSERTION));
        // When
        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testMethodWithoutAssertions);

        //Then
        assertAll(
                () -> Assert.assertSame("Incorrect number of found violations", 1, foundViolations.size()),
                () -> assertThat(foundViolations.get(0)).as("Checking first found violation").isEqualToComparingFieldByField(expectedViolations.get(0))
        );
    }

    @Test
    public void checkBestPractices_TestMethodWithOneAssertion_NoViolationShouldBeReturned() {
        // Given
        this.testJavaFile.getImportList().add(this.psiElementFactory.createImportStatementOnDemand("org.junit.Assert"));
        String testMethodName = "testWithOneAssertion";
        PsiMethod testWithOneAssertion = this.javaTestElementUtil.createTestMethod(testMethodName, Collections.singletonList("@Test"));
        PsiMethodCallExpression assertionMethodCall = (PsiMethodCallExpression) testWithOneAssertion.getBody().add(this.psiElementFactory.createExpressionFromText("Assert.assertTrue(true)", null));
        testWithOneAssertion = (PsiMethod) testClass.add(testWithOneAssertion);
        EasyMock.expect(contextIndicator.isInTestContext()).andReturn((element) -> true).anyTimes();
        EasyMock.replay(contextIndicator);
        EasyMock.expect(elementResolver.allChildrenOfType(EasyMock.eq(testWithOneAssertion), EasyMock.eq(PsiMethodCallExpression.class), EasyMock.anyObject(), EasyMock.eq(contextIndicator.isInTestContext())))
                .andReturn(Collections.singletonList(assertionMethodCall)).times(1);
        EasyMock.expect(elementResolver.allChildrenOfType(EasyMock.eq(assertionMethodCall), EasyMock.eq(PsiMethodCallExpression.class), EasyMock.anyObject(), EasyMock.eq(contextIndicator.isInTestContext())))
                .andReturn(Collections.emptyList()).times(1);
        EasyMock.replay(elementResolver);
        // When
        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testWithOneAssertion);

        //Then
        Assert.assertSame("Incorrect number of found violations", 0, foundViolations.size());

    }

    @Test
    public void checkBestPractices_Junit5TestMethodWithTwoHamcrestAssertionsWhichAreNotGrouped_OneViolationReportingAboutThatOnlyOneAssertionShouldBeInTheTestShouldBeReturnedAndShouldContainHintRecommendingUsingJUnit5GroupAssertionOrHamcrest() {
        // Given
        String testMethodName = "testWithOneAssertion";
        String assertMethodText = "org.hamcrest.MatcherAssert.assertThat(null,null )";
        PsiMethod testWithTwoNonGroupedAssertions = this.javaTestElementUtil.createTestMethod(testMethodName, Collections.singletonList("@org.junit.jupiter.api.Test"));

        PsiMethodCallExpression firstAssertionMethodCall = (PsiMethodCallExpression) testWithTwoNonGroupedAssertions.getBody().add(this.psiElementFactory.createExpressionFromText(assertMethodText, null));
        PsiMethodCallExpression secondAssertionMethodCall = (PsiMethodCallExpression) testWithTwoNonGroupedAssertions.getBody().add(this.psiElementFactory.createExpressionFromText(assertMethodText, null));
        testWithTwoNonGroupedAssertions = (PsiMethod) testClass.add(testWithTwoNonGroupedAssertions);
        EasyMock.expect(contextIndicator.isInTestContext()).andReturn((element) -> true).anyTimes();
        EasyMock.replay(contextIndicator);
        EasyMock.expect(elementResolver.allChildrenOfType(EasyMock.eq(testWithTwoNonGroupedAssertions), EasyMock.eq(PsiMethodCallExpression.class), EasyMock.anyObject(), EasyMock.eq(contextIndicator.isInTestContext())))
                .andReturn(Arrays.asList(firstAssertionMethodCall, secondAssertionMethodCall)).times(1);
        EasyMock.expect(elementResolver.allChildrenOfType(EasyMock.eq(firstAssertionMethodCall), EasyMock.eq(PsiMethodCallExpression.class), EasyMock.anyObject(), EasyMock.eq(contextIndicator.isInTestContext())))
                .andReturn(Collections.emptyList()).times(1);
        EasyMock.expect(elementResolver.allChildrenOfType(EasyMock.eq(secondAssertionMethodCall), EasyMock.eq(PsiMethodCallExpression.class), EasyMock.anyObject(), EasyMock.eq(contextIndicator.isInTestContext())))
                .andReturn(Collections.emptyList()).times(1);
        EasyMock.expect(elementResolver.allChildrenOfType(testWithTwoNonGroupedAssertions, PsiReferenceExpression.class))
                .andReturn(Collections.emptyList()).times(2);
        EasyMock.replay(elementResolver);
        List<BestPracticeViolation> expectedViolations = Collections.singletonList(
                new BestPracticeViolation(
                        String.format("%s#%s", testWithTwoNonGroupedAssertions.getContainingClass().getQualifiedName(), testMethodName),
                        testWithTwoNonGroupedAssertions,
                        testWithTwoNonGroupedAssertions.getNameIdentifier().getTextRange(),
                        "Test should contain only one assertion method!",
                        Arrays.asList("You are using JUnit5 so it can be solved by wrapping multiple assertions into org.junit.jupiter.api.Assertions.assertAll() method",
                                "You can use hamcrest org.hamcrest.core.Every or org.hamcrest.core.AllOf matchers"),
                        BestPractice.ONLY_ONE_ASSERTION,
                        Arrays.asList(
                                new RelatedElementWrapper(
                                        firstAssertionMethodCall.getText(), new HashMap<PsiElement, String>() {{
                                    put(firstAssertionMethodCall, "reference in the test method");
                                }}),
                                new RelatedElementWrapper(
                                        secondAssertionMethodCall.getText(), new HashMap<PsiElement, String>() {{
                                    put(secondAssertionMethodCall, "reference in the test method");
                                }})
                        )
                ));
        // When
        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testWithTwoNonGroupedAssertions);

        //Then
        assertAll(
                () -> Assert.assertSame("Incorrect number of found violations", 1, foundViolations.size()),
                () -> assertThat(foundViolations.get(0)).as("Checking first found violation").isEqualToIgnoringGivenFields(expectedViolations.get(0), "relatedElementsWrapper"),
                () -> Assert.assertSame("Incorrect number of related elements for first violation", 2, foundViolations.get(0).getRelatedElements().size()),
                () -> assertThat(foundViolations.get(0).getRelatedElements().get(0)).as("Checking first related element in the first violation").isEqualToComparingFieldByField(expectedViolations.get(0).getRelatedElements().get(0)),
                () -> assertThat(foundViolations.get(0).getRelatedElements().get(1)).as("Checking second related element in the first violation").isEqualToComparingFieldByField(expectedViolations.get(0).getRelatedElements().get(1))
        );


    }
}
