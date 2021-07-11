package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.*;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.search.ElementSearchResult;
import com.testspector.model.checking.java.common.search.QueriesRepository;
import com.testspector.model.enums.BestPractice;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


public class NoConditionalLogicJUnitCheckingJUnitStrategyTest extends JUnitStrategyTest {

    private NoConditionalLogicJUnitCheckingStrategy strategy;

    private static Stream<Arguments> provideAllSupportedStatementStringDefinitions() {
        return Stream.of(
                Arguments.of("if", "if(true){}else if(false){}"),
                Arguments.of("for", "for (int i = 0; i < 50; i++) {\n}"),
                Arguments.of("forEach", "for (Object object: Object){}"),
                Arguments.of("while", "while(true){}"),
                Arguments.of("switch", "switch (integer) {case 1:\"Test1\"; default:\"Test2\";}"));
    }
    @BeforeEach
    public void beforeEach() {
        this.strategy = new NoConditionalLogicJUnitCheckingStrategy(elementSearchEngine, contextIndicator, methodResolver);
    }

    @ParameterizedTest
    @MethodSource(value = "provideAllSupportedStatementStringDefinitions")
    public void checkBestPractices_CheckingAllConditionalStatementsOneIsInTheTestMethodAndSecondInTheHelperMethod_OneViolationReportingAboutConditionalLogicShouldBeReturned(String statementName, String statementStringDefinition) {
        // Given
        String helperMethodName = "helperMethod";
        PsiMethod helperMethodWithStatement = this.javaTestElementUtil
                .createMethod(helperMethodName, "Object", Collections.singletonList(PsiKeyword.PUBLIC));
        PsiStatement helperMethodStatement = (PsiStatement) helperMethodWithStatement
                .getBody()
                .add(this.psiElementFactory.createStatementFromText(statementStringDefinition, null));
        String testMethodName = "testWithStatement";
        PsiMethod testMethodWithStatement = this.javaTestElementUtil
                .createTestMethod(testMethodName, Collections.singletonList("@Test"));
        PsiStatement testMethodStatement = (PsiStatement) testMethodWithStatement
                .getBody()
                .add(this.psiElementFactory.createStatementFromText(statementStringDefinition, null));
        PsiMethodCallExpression helperMethodCall = (PsiMethodCallExpression) testMethodWithStatement
                .getBody()
                .add(this.psiElementFactory.createExpressionFromText(String.format("%s()", helperMethodName), null));
        helperMethodWithStatement = (PsiMethod) testClass.add(helperMethodWithStatement);
        testMethodWithStatement = (PsiMethod) testClass.add(testMethodWithStatement);
        EasyMock.expect(elementSearchEngine
                .findByQuery(EasyMock.eq(testMethodWithStatement), EasyMock.eq(QueriesRepository.FIND_ALL_CONDITIONAL_STATEMENTS)))
                .andReturn(new ElementSearchResult<>(new ArrayList<>(), Arrays.asList(testMethodStatement, helperMethodStatement))).times(1);
        EasyMock.expect(methodResolver.methodHasAnyOfAnnotations(testMethodWithStatement, JUNIT5_TEST_QUALIFIED_NAMES)).andReturn(true).times(2);
        EasyMock.expect(methodResolver.assertionMethod(EasyMock.anyObject(PsiMethod.class))).andReturn(Optional.empty()).times(2);
        EasyMock.replay(elementSearchEngine, methodResolver);
        List<BestPracticeViolation> expectedViolations = Arrays.asList(
                createBestPracticeViolation(
                        testMethodStatement,
                        "Conditional logic should not be part of the test method, it " +
                                "makes test hard to understand, read and maintain.",
                        Arrays.asList("Remove statements [ if, while, switch, for, forEach ] and create " +
                                        "separate test scenario for each branch",
                                "Acceptable place where conditional logic can be are custom assertions, " +
                                        "where base on inputs we decide if we throw exception or not",
                                "You are using JUnit5 so the problem can be solved by using data driven " +
                                        "approach and generating each scenario using org.junit.jupiter.params.ParameterizedTest"),
                        new ArrayList<>()
                ),
                createBestPracticeViolation(
                        testMethodStatement,
                        "Conditional logic should not be part of the test method, it " +
                                "makes test hard to understand, read and maintain.",
                        Arrays.asList("Remove statements [ if, while, switch, for, forEach ] and create " +
                                        "separate test scenario for each branch",
                                "Acceptable place where conditional logic can be are custom assertions, " +
                                        "where base on inputs we decide if we throw exception or not",
                                "You are using JUnit5 so the problem can be solved by using data driven " +
                                        "approach and generating each scenario using org.junit.jupiter.params.ParameterizedTest"),
                        new ArrayList<>()
                ));

        // When
        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testMethodWithStatement);

        //Then
        assertAll(
                () -> Assert.assertSame("Incorrect number of found violations", 2, foundViolations.size()),
                () -> assertThat(foundViolations.get(0)).as("Checking first found violation").usingRecursiveComparison().isEqualTo(expectedViolations.get(0))
        );
    }

    @Test
    public void checkBestPractices_TestWithNoConditionalLogic_NoViolationsShouldBeFound() {
        PsiMethod testWithNoConditionalLogic = this.javaTestElementUtil
                .createTestMethod("testWithNoConditionalLogic", Collections.singletonList("@Test"));
        testWithNoConditionalLogic = (PsiMethod) testClass.add(testWithNoConditionalLogic);
        EasyMock.expect(elementSearchEngine
                .findByQuery(EasyMock.eq(testWithNoConditionalLogic), EasyMock.eq(QueriesRepository.FIND_ALL_CONDITIONAL_STATEMENTS)))
                .andReturn(new ElementSearchResult<>(Collections.emptyList(), Collections.emptyList())).times(1);
        EasyMock.replay(elementSearchEngine);

        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testWithNoConditionalLogic);

        Assert.assertSame("Incorrect number of found violations", 0, foundViolations.size());
    }


    private BestPracticeViolation createBestPracticeViolation(PsiElement testMethodElement, String problemDescription, List<String> hints, List<Action<BestPracticeViolation>> actions) {
        return new BestPracticeViolation(
                testMethodElement,
                problemDescription,
                BestPractice.NO_CONDITIONAL_LOGIC,
                actions,
                hints
        );
    }

}
