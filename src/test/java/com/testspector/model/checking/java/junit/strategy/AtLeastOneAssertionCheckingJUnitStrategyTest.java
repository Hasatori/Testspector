package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.search.ElementSearchResult;
import com.testspector.model.checking.java.common.search.QueriesRepository;
import com.testspector.model.enums.BestPractice;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class AtLeastOneAssertionCheckingJUnitStrategyTest extends JUnitStrategyTest {


    private AssertionCountJUnitCheckingStrategy strategy;

    @BeforeEach
    public void beforeEach() {
        this.strategy = new AtLeastOneAssertionJUnitCheckingStrategy(elementSearchEngine, contextIndicator, methodResolver);
    }

    @Test
    public void checkBestPractices_TestMethodWithoutAnyAssertions_OneViolationReportingAboutThatAtLeastOneAssertionShouldBeInTheTestShouldBeReturned() {
        // Given
        String testMethodName = "testWithNoAssertions";
        PsiMethod testMethodWithoutAssertions = this.javaTestElementUtil.createTestMethod(testMethodName, Collections.singletonList("@Test"));
        testMethodWithoutAssertions = (PsiMethod) testClass.add(testMethodWithoutAssertions);
        EasyMock.expect(elementSearchEngine.findByQuery(EasyMock.eq(testMethodWithoutAssertions), EasyMock.eq(QueriesRepository.FIND_ALL_ASSERTION_METHOD_CALL_EXPRESSIONS)))
                .andReturn(new ElementSearchResult<>(new ArrayList<>(), new ArrayList<>())).times(1);
        EasyMock.replay(methodResolver, elementSearchEngine);
        List<BestPracticeViolation> expectedViolations = Collections.singletonList(
                new BestPracticeViolation(
                        testMethodWithoutAssertions.getNameIdentifier(),
                        "Test should contain at least one assertion method!",
                        BestPractice.AT_LEAST_ONE_ASSERTION,
                        null,
                        null));
        // When
        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testMethodWithoutAssertions);
        //Then
        assertAll(
                () -> Assert.assertSame("Incorrect number of found violations", 1, foundViolations.size()),
                () -> assertThat(foundViolations.get(0)).as("Checking first found violation").usingRecursiveComparison().isEqualTo(expectedViolations.get(0))
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

}
