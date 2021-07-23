package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiKeyword;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
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
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


public class TestOnlyPublicBehaviourJUnitCheckingJUnitStrategyTest extends JUnitStrategyTest {

    private TestOnlyPublicBehaviourJUnitCheckingStrategy strategy;

    @BeforeEach
    public void beforeEach() {
        this.strategy = new TestOnlyPublicBehaviourJUnitCheckingStrategy(elementSearchEngine, contextIndicator,methodResolver);
    }

    @ParameterizedTest
    @ValueSource(strings = {"private", "protected", ""})
    public void checkBestPractices_TestTestsPrivateBehaviourViaMethodCall_OneViolationReportingAboutTestingPrivateBehaviourShouldBeReturned(String testedMethodAccessQualifier) {
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
                        "It is recommended to always test only the public behaviour of the system under test, which is expressed through public methods. " +
                                "Private methods are often updated, deleted or added regardless of if public behaviour of a system under test has changed. " +
                                "Private methods are only a helper tool for the public behaviour of the tested system. " +
                                "Testing them leads to dependencies between the code and the tests, and in the long run, it makes it hard to maintain the tests and even the slightest change will require an update to the tests.",
                        Arrays.asList(
                                "Remove tests testing private behaviour",
                                "If you really feel that private behaviour is complex enough that there should " +
                                        "be a separate test for it, then it is very probable that the system under" +
                                        " test is breaking 'Single Responsibility Principle' and this private " +
                                        "behaviour should probably be extracted into a separate class"
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
