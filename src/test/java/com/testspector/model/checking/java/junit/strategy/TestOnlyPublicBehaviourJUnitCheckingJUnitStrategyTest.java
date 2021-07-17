package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.*;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.search.ElementSearchResult;
import com.testspector.model.checking.java.junit.strategy.action.MakeMethodPublicAction;
import com.testspector.model.enums.BestPractice;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


public class TestOnlyPublicBehaviourJUnitCheckingJUnitStrategyTest extends JUnitStrategyTest {

    private TestOnlyPublicBehaviourJUnitCheckingStrategy strategy;

    @BeforeEach
    public void beforeEach() {
        this.strategy = new TestOnlyPublicBehaviourJUnitCheckingStrategy(elementSearchEngine, contextIndicator,methodResolver);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "'private'         | 'private'",
            "'protected'       | 'protected'",
            "'package private' | ''"
    }, delimiter = '|')
    public void checkBestPractices_TestTestsPrivateBehaviourViaMethodCall_OneViolationReportingAboutTestingPrivateBehaviourShouldBeReturned(String expectedRelatedElementQualifierName, String testedMethodAccessQualifier) {
        // Given
        PsiMethod testMethod = this.javaTestElementUtil
                .createTestMethod("testMethod", Collections.singletonList("@Test"));
        String testedMethodName = "testedMethod";
        PsiMethod testedMethod = this.javaTestElementUtil.createMethod(testedMethodName, "String", Collections.singletonList(testedMethodAccessQualifier));
        testMethod = (PsiMethod) testClass.add(testMethod);
        testedMethod = (PsiMethod) testClass.add(testedMethod);
        PsiMethodCallExpression testedMethodCall = (PsiMethodCallExpression) psiElementFactory
                .createExpressionFromText(String.format("%s()", testedMethodName),testMethod);
        EasyMock.expect(methodResolver.allTestedMethodsMethodCalls(testMethod))
                .andReturn(new ElementSearchResult<>(new ArrayList<>(),Collections.singletonList(testedMethodCall)))
                .times(1);
        EasyMock.expect(methodResolver.allTestedMethodsReferences(testMethod))
                .andReturn(new ElementSearchResult<>(new ArrayList<>(),new ArrayList<>()))
                .times(1);
        EasyMock.replay(methodResolver, elementSearchEngine);

        List<BestPracticeViolation> expectedViolations = Collections.singletonList(
                createBestPracticeViolation(
                        testedMethodCall.getMethodExpression(),
                        "Only public behaviour should be tested. Testing 'private','protected' " +
                                "or 'package private' methods leads to problems with maintenance of tests because " +
                                "this private behaviour is likely to be changed very often. " +
                                "In many cases we are refactoring private behaviour without influencing public " +
                                "behaviour of the class, yet this changes will change behaviour of the private method " +
                                "and cause tests to fail.",
                        Arrays.asList(
                                "There is an exception to this rule and that is in case when private 'method' is " +
                                        "part of the observed behaviour of the system under test. For example we " +
                                        "can have private constructor for class which is part of ORM and its " +
                                        "initialization should not be permitted.",
                                "Remove tests testing private behaviour",
                                "If you really feel that private behaviour is complex enough that there should " +
                                        "be separate test for it, then it is very probable that the system under" +
                                        " test is breaking 'Single Responsibility Principle' and this private " +
                                        "behaviour should be extracted to a separate system"
                        ),Collections.singletonList(new MakeMethodPublicAction(testedMethod))));

        // When
        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testMethod);

        //Then
        assertAll(
                () -> Assertions.assertSame(1, foundViolations.size(), "Incorrect number of found violations"),
                () -> assertThat(foundViolations.get(0)).as("Checking first found violation").usingRecursiveComparison().isEqualTo(expectedViolations.get(0)));
    }

    @Test
    public void checkBestPractices_TestTestsJustPublicMethods_NoViolationsShouldBeFound() {
        PsiMethod testMethod = this.javaTestElementUtil.createTestMethod("testMethod", Collections.singletonList("@Test"));
        String testedMethodName = "testedMethod";
        PsiMethod testedMethod = this.javaTestElementUtil
                .createMethod(testedMethodName, "String", Collections.singletonList(PsiKeyword.PUBLIC));
        PsiMethodCallExpression testedMethodCall = (PsiMethodCallExpression) this.psiElementFactory
                .createExpressionFromText(String.format("%s()", testedMethodName), null);
        testedMethodCall = (PsiMethodCallExpression) testMethod.getBody().add(testedMethodCall);
        testMethod = (PsiMethod) testClass.add(testMethod);
        EasyMock.expect(methodResolver.allTestedMethodsMethodCalls(testMethod))
                .andReturn(new ElementSearchResult<>(new ArrayList<>(), Collections.singletonList(testedMethodCall)))
                .times(1);
        EasyMock.expect(methodResolver.allTestedMethodsReferences(testMethod))
                .andReturn(new ElementSearchResult<>(new ArrayList<>(),new ArrayList<>()))
                .times(1);
        EasyMock.replay(methodResolver);

        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testMethod);

        Assertions.assertSame(0, foundViolations.size(), "Incorrect number of found violations");
    }


    private BestPracticeViolation createBestPracticeViolation(PsiElement element, String problemDescription, List<String> hints, List<Action<BestPracticeViolation>> actions) {
        return new BestPracticeViolation(
                element,
                problemDescription,
                BestPractice.TEST_ONLY_PUBLIC_BEHAVIOUR,
                actions,
                hints);
    }

}
