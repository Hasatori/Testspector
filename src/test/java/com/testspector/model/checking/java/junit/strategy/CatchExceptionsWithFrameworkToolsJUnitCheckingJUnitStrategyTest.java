package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiTryStatement;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.search.ElementSearchResult;
import com.testspector.model.checking.java.common.search.QueriesRepository;
import com.testspector.model.checking.java.junit.strategy.action.RemoveTryCatchStatementAction;
import com.testspector.model.enums.BestPractice;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class CatchExceptionsWithFrameworkToolsJUnitCheckingJUnitStrategyTest extends JUnitStrategyTest {

    private CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy strategy;

  private static final String DEFAULT_PROBLEM_DESCRIPTION_MESSAGE = "It is not recommended to test exceptions by using try and catch block. " +
            "Using the blocks only is redundant and it make test method bigger and makes it harder to read and understand it.";
  private static final List<String> DEFAULT_HINTS = List.of(
          "If throwing an exception is not part of the test delete the try catch and catch exception at method level",
          "Instead it is recommended to use methods or tools provided by testing frameworks and testing libraries. " +
          "For example annotation @expectException for testing framework JUnit version 4 or assertThrows method in JUnit version 5");

    @BeforeEach
    public void beforeEach() {
        this.strategy = new CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy(elementSearchEngine, contextIndicator, methodResolver);
    }
    @ParameterizedTest
    @ValueSource(strings = {"org.junit.jupiter.api.Test", "org.junit.jupiter.params.ParameterizedTest", "org.junit.jupiter.api.RepeatedTest"})
    public void checkBestPractices_JUnit5TestMethodContainsTryCatchStatement_OneViolationReportingAboutCatchingTheExceptionUsingJUnit5AssertionShouldBeReturned(String jUnit5TestAnnotationQualifiedName) {
        // Given
        PsiMethod testMethod = (PsiMethod) testClass.add(this.javaTestElementUtil
                .createTestMethod("testMethod", Collections.singletonList("@" + jUnit5TestAnnotationQualifiedName)));
        PsiTryStatement tryStatement = (PsiTryStatement) testMethod.getBody().add(this.psiElementFactory
                .createStatementFromText("try {} catch (Exception e){}", null));
        EasyMock.expect(elementSearchEngine
                .findByQuery(EasyMock.eq(testMethod), EasyMock.eq(QueriesRepository.FIND_ALL_TRY_STATEMENTS)))
                .andReturn(new ElementSearchResult<>(new ArrayList<>(), Collections.singletonList(tryStatement))).times(1);
        EasyMock.expect(methodResolver.methodHasAnyOfAnnotations(testMethod, JUNIT5_TEST_QUALIFIED_NAMES)).andReturn(true).once();
        EasyMock.expect(methodResolver.methodHasAnyOfAnnotations(testMethod, JUNIT4_TEST_QUALIFIED_NAMES)).andReturn(false).once();
        EasyMock.expect(elementSearchEngine.findByQuery(tryStatement.getTryBlock(), QueriesRepository.FIND_ALL_METHOD_CALL_EXPRESSIONS_THROWING_ANY_EXCEPTION_WITHOUT_REFERENCES))
                .andReturn(new ElementSearchResult<>(new ArrayList<>(),new ArrayList<>()));
        EasyMock.replay(elementSearchEngine, methodResolver);
        List<BestPracticeViolation> expectedViolations = Collections.singletonList(
                createBestPracticeViolation(
                        tryStatement,
                        DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                        Collections.singletonList(new RemoveTryCatchStatementAction(tryStatement,false)),
                       DEFAULT_HINTS));
        // When
        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testMethod);

        //Then
        assertAll(
                () -> Assert.assertSame("Incorrect number of found violations", 1, foundViolations.size()),
                () -> assertThat(foundViolations.get(0)).as("Checking first found violation").usingRecursiveComparison().isEqualTo(expectedViolations.get(0))
        );
    }

    @Test
    public void checkBestPractices_JUnit4TestMethodContainsTryCatchStatement_OneViolationReportingAboutCatchingTheExceptionUsingJUnit4AnnotationShouldBeReturned() {
        // Given
        PsiMethod testMethod = (PsiMethod) testClass.add(this.javaTestElementUtil
                .createTestMethod("testMethod", Collections.singletonList("@org.junit.Test")));
        PsiTryStatement tryStatement = (PsiTryStatement) testMethod.getBody().add(this.psiElementFactory.createStatementFromText("try {} catch (Exception e){}", null));
        EasyMock.expect(contextIndicator.isInTestContext()).andReturn((element) -> true).anyTimes();
        EasyMock.replay(contextIndicator);
        EasyMock.expect(elementSearchEngine
                .findByQuery(EasyMock.eq(testMethod), EasyMock.eq(QueriesRepository.FIND_ALL_TRY_STATEMENTS)))
                .andReturn(new ElementSearchResult<>(new ArrayList<>(), Arrays.asList(tryStatement))).times(1);
        EasyMock.expect(methodResolver.methodHasAnyOfAnnotations(testMethod, JUNIT5_TEST_QUALIFIED_NAMES)).andReturn(false).once();
        EasyMock.expect(methodResolver.methodHasAnyOfAnnotations(testMethod, JUNIT4_TEST_QUALIFIED_NAMES)).andReturn(true).once();
        EasyMock.expect(elementSearchEngine.findByQuery(tryStatement.getTryBlock(), QueriesRepository.FIND_ALL_METHOD_CALL_EXPRESSIONS_THROWING_ANY_EXCEPTION_WITHOUT_REFERENCES))
                .andReturn(new ElementSearchResult<>(new ArrayList<>(),new ArrayList<>()));
        EasyMock.replay(elementSearchEngine, methodResolver);
        List<BestPracticeViolation> expectedViolations = Collections.singletonList(
                createBestPracticeViolation(
                        tryStatement,
                        DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                        Collections.singletonList(new RemoveTryCatchStatementAction(tryStatement,false)),
                       DEFAULT_HINTS
                ));
        // When
        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testMethod);

        //Then
        assertAll(
                () -> Assert.assertSame("Incorrect number of found violations", 1, foundViolations.size()),
                () -> assertThat(foundViolations.get(0)).as("Checking first found violation").usingRecursiveComparison().isEqualTo(expectedViolations.get(0))
        );
    }

    @Test
    public void checkBestPractices_TestMethodDoesNotContainTryCatchStatement_NoViolationShouldBeFound() {
        // Given
        PsiMethod testMethod = (PsiMethod) testClass.add(this.javaTestElementUtil
                .createTestMethod("testMethod", Collections.singletonList("@org.junit.Test")));
        EasyMock.expect(contextIndicator.isInTestContext()).andReturn((element) -> true).anyTimes();
        EasyMock.replay(contextIndicator);
        EasyMock.expect(elementSearchEngine
                .findByQuery(EasyMock.eq(testMethod), EasyMock.eq(QueriesRepository.FIND_ALL_TRY_STATEMENTS)))
                .andReturn(new ElementSearchResult<>(Collections.emptyList(),Collections.emptyList())).times(1);
        EasyMock.replay(elementSearchEngine);
        // When
        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testMethod);
        //Then
        Assert.assertSame("Incorrect number of found violations", 0, foundViolations.size());
    }

    private BestPracticeViolation createBestPracticeViolation(PsiElement testMethodElement, String problemDescription, List<Action<BestPracticeViolation>> actions, List<String> hints) {
        return new BestPracticeViolation(
                testMethodElement,
                problemDescription,
                BestPractice.CATCH_TESTED_EXCEPTIONS_USING_FRAMEWORK_TOOLS,
                actions,
                hints);
    }

}
