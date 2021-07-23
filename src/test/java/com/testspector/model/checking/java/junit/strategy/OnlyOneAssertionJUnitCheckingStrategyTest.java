package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.search.ElementSearchResult;
import com.testspector.model.checking.java.common.search.QueriesRepository;
import com.testspector.model.enums.BestPractice;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class OnlyOneAssertionJUnitCheckingStrategyTest extends JUnitStrategyTest {


    private AssertionCountJUnitCheckingStrategy strategy;

    @BeforeEach
    public void beforeEach() {
        this.strategy = new OnlyOneAssertionJUnitCheckingStrategy(elementSearchEngine, contextIndicator, methodResolver);
    }

    @Test
    public void checkBestPractices_Junit5TestMethodWithTwoHamcrestAssertionsWhichAreNotGrouped_OneViolationReportingAboutThatOnlyOneAssertionShouldBeInTheTestShouldBeReturned() {
        // Given
        String testMethodName = "testWithOneAssertion";
        String assertMethodText = "org.hamcrest.MatcherAssert.assertThat(null,null )";
        PsiMethod testWithTwoNonGroupedAssertions = this.javaTestElementUtil
                .createTestMethod(testMethodName, Collections.singletonList("@org.junit.jupiter.api.Test"));
        PsiMethodCallExpression firstAssertionMethodCall = (PsiMethodCallExpression) testWithTwoNonGroupedAssertions
                .getBody()
                .add(this.psiElementFactory.createExpressionFromText(assertMethodText, null));
        PsiMethodCallExpression secondAssertionMethodCall = (PsiMethodCallExpression) testWithTwoNonGroupedAssertions
                .getBody()
                .add(this.psiElementFactory.createExpressionFromText(assertMethodText, null));
        testWithTwoNonGroupedAssertions = (PsiMethod) testClass.add(testWithTwoNonGroupedAssertions);
        EasyMock.expect(elementSearchEngine
                .findByQuery(EasyMock.eq(testWithTwoNonGroupedAssertions), EasyMock.eq(QueriesRepository.FIND_ALL_ASSERTION_METHOD_CALL_EXPRESSIONS)))
                .andReturn(new ElementSearchResult<>(new ArrayList<>(), Arrays.asList(firstAssertionMethodCall, secondAssertionMethodCall))).times(1);
        EasyMock.expect(elementSearchEngine
                .findByQuery(EasyMock.eq(firstAssertionMethodCall), EasyMock.eq(QueriesRepository.FIND_ALL_ASSERTION_METHOD_CALL_EXPRESSIONS)))
                .andReturn(new ElementSearchResult<>(new ArrayList<>(), new ArrayList<>())).times(1);
        EasyMock.expect(elementSearchEngine
                .findByQuery(EasyMock.eq(secondAssertionMethodCall), EasyMock.eq(QueriesRepository.FIND_ALL_ASSERTION_METHOD_CALL_EXPRESSIONS)))
                .andReturn(new ElementSearchResult<>(new ArrayList<>(), new ArrayList<>())).times(1);
        EasyMock.replay(elementSearchEngine);
        List<BestPracticeViolation> expectedViolations = Arrays.asList(
                createBasicViolation(firstAssertionMethodCall.getMethodExpression(), new ArrayList<>()),
                createBasicViolation(secondAssertionMethodCall.getMethodExpression(), new ArrayList<>())
        );
        // When
        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testWithTwoNonGroupedAssertions);

        //Then
        assertAll(
                () -> Assert.assertSame("Incorrect number of found violations", 2, foundViolations.size()),
                () -> assertThat(foundViolations.get(0)).as("Checking first found violation").usingRecursiveComparison().isEqualTo(expectedViolations.get(0)),
                () -> assertThat(foundViolations.get(1)).as("Checking second found violation").usingRecursiveComparison().isEqualTo(expectedViolations.get(1))
        );
    }


    @Test
    public void checkBestPractices_TestMethodWithOneAssertion_NoViolationShouldBeReturned() {
        // Given
        this.testJavaFile.getImportList().add(this.psiElementFactory.createImportStatementOnDemand("org.junit.Assert"));
        String testMethodName = "testWithOneAssertion";
        PsiMethod testWithOneAssertion = this.javaTestElementUtil
                .createTestMethod(testMethodName, Collections.singletonList("@Test"));
        PsiMethodCallExpression assertionMethodCall = (PsiMethodCallExpression) testWithOneAssertion.getBody()
                .add(this.psiElementFactory.createExpressionFromText("Assert.assertTrue(true)", null));
        testWithOneAssertion = (PsiMethod) testClass.add(testWithOneAssertion);
        EasyMock.expect(elementSearchEngine
                .findByQuery(EasyMock.eq(testWithOneAssertion), EasyMock.eq(QueriesRepository.FIND_ALL_ASSERTION_METHOD_CALL_EXPRESSIONS)))
                .andReturn(new ElementSearchResult<>(new ArrayList<>(), Collections.singletonList(assertionMethodCall))).times(1);
        EasyMock.expect(elementSearchEngine
                .findByQuery(EasyMock.eq(assertionMethodCall), EasyMock.eq(QueriesRepository.FIND_ALL_ASSERTION_METHOD_CALL_EXPRESSIONS)))
                .andReturn(new ElementSearchResult<>(new ArrayList<>(), new ArrayList<>())).times(1);
        EasyMock.replay(elementSearchEngine);
        // When
        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testWithOneAssertion);
        //Then
        Assert.assertSame("Incorrect number of found violations", 0, foundViolations.size());
    }


    public BestPracticeViolation createBasicViolation(PsiElement element, List<Action<BestPracticeViolation>> actions) {
        return new BestPracticeViolation(
                element,
                "Test should fail for only one reason. " +
                        "Using multiple assertions in JUnit leads to that if " +
                        "one assertion fails other will not be executed and " +
                        "therefore you will not get overview of all problems.",
                BestPractice.ONLY_ONE_ASSERTION,
                actions,
                Collections.emptyList()
        );
    }

}
