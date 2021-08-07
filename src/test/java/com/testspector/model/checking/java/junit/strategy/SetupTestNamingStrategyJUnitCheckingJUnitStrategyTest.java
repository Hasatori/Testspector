package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiKeyword;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.search.ElementSearchResult;
import com.testspector.model.enums.BestPractice;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


public class SetupTestNamingStrategyJUnitCheckingJUnitStrategyTest extends JUnitStrategyTest {

    private static final String ALMOST_SAME_NAME_PROBLEM_DESCRIPTION = "The test name is more or less the same as" +
            " the tested method. This says nothing about tests scenario. You should setup a clear " +
            "strategy for naming your tests so that the person reading then knows what is tested";
    private SetupTestNamingStrategyJUnitCheckingStrategy strategy;

    @BeforeEach
    public void beforeEach() {
        this.strategy = new SetupTestNamingStrategyJUnitCheckingStrategy(elementSearchEngine, contextIndicator, methodResolver);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "'getName' | 'testGetName'        | '" + ALMOST_SAME_NAME_PROBLEM_DESCRIPTION + "'",
            "'getName' | 'getNameTest'        | '" + ALMOST_SAME_NAME_PROBLEM_DESCRIPTION + "'",
            "'getName' | 'test_GetName'       | '" + ALMOST_SAME_NAME_PROBLEM_DESCRIPTION + "'",
            "'getName' | 'GetName_test'       | '" + ALMOST_SAME_NAME_PROBLEM_DESCRIPTION + "'",
            "'g' | 'g_test'               | '" + ALMOST_SAME_NAME_PROBLEM_DESCRIPTION + "'",
            "'run' | 'test_Run'                | '" + ALMOST_SAME_NAME_PROBLEM_DESCRIPTION + "'",
    }, delimiter = '|')
    public void checkBestPractices_TestNameTooSimilarOrTooDifferent_OneViolationReportingAboutTestNamingStrategyShouldBeReturned(String testedMethodName, String testName, String problemDescription) {
        // Given
        PsiMethod testedMethod = this.javaTestElementUtil
                .createMethod(testedMethodName, "String", Collections.singletonList(PsiKeyword.PUBLIC));
        PsiMethod testMethod = this.javaTestElementUtil.createTestMethod(testName, Collections.singletonList("@org.junit.Test"));
        testMethod = (PsiMethod) testClass.add(testMethod);
        testedMethod = (PsiMethod) testClass.add(testedMethod);
        PsiMethodCallExpression testedMethodCall = (PsiMethodCallExpression) this.psiElementFactory
                .createExpressionFromText(String.format("%s()", testedMethodName), testedMethod);
        testedMethodCall = (PsiMethodCallExpression) testMethod.getBody().add(testedMethodCall);
        EasyMock.expect(methodResolver.allTestedMethodsMethodCalls(testMethod))
                .andReturn(new ElementSearchResult<>(new ArrayList<>(), Collections.singletonList(testedMethodCall)))
                .times(1);
        EasyMock.replay(methodResolver);
        List<BestPracticeViolation> expectedViolations = Collections.singletonList(
                createBestPracticeViolation(
                        testedMethodCall.getMethodExpression().getChildren()[1],
                        problemDescription,
                        Arrays.asList(
                                "Possible strategy: 'doingSomeOperationGeneratesSomeResult'",
                                "Possible strategy: 'someResultOccursUnderSomeCondition'",
                                "Possible strategy: 'given-when-then'",
                                "Possible strategy: 'givenSomeContextWhenDoingSomeBehaviorThenSomeResultOccurs'",
                                "Possible strategy: 'whatIsTested_conditions_expectedResult'",
                                "Chosen naming strategy is subjective. The key thing to remember is that name " +
                                        "of the test should say: What is tests, What are the conditions, What is expected result"
                        ),
                        new ArrayList<>()));
        // When
        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testMethod);
        //Then
        assertAll(
                () -> Assert.assertSame("Incorrect number of found violations", 1, foundViolations.size()),
                () -> assertThat(foundViolations.get(0)).as("Checking first found violation").usingRecursiveComparison().isEqualTo(expectedViolations.get(0)));
    }


    @ParameterizedTest
    @CsvSource(value = {
            "'checkBestPractices'       | 'checkBestPractices_TestNameIsUsingStrategy_NoViolationShouldBeReturned'",
            "'checkBestPractices'       | 'checkBestPractices_JUnit5TestMethodContainsTryCatchStatement_OneViolationReportingAboutCatchingTheExceptionUsingJUnit5AssertionShouldBeReturned'",
            "'canResolveFromPsiElement' | 'canResolveFromPsiElement_PsiFilesWithDifferentJUnitVersions_ShoudIndicateThatCanResolve'",
            "'resolveTest'              | 'resolveTest_NullElement_ShouldReturnEmpty'",
    }, delimiter = '|')
    public void checkBestPractices_TestNameIsUsingStrategy_NoViolationShouldBeReturned(String testName, String testedMethodName) {
        // Given
        PsiMethod testedMethod = this.javaTestElementUtil
                .createMethod(testedMethodName, "String", Collections.singletonList(PsiKeyword.PUBLIC));
        PsiMethodCallExpression testedMethodCall = (PsiMethodCallExpression) this.psiElementFactory
                .createExpressionFromText(String.format("%s()", testedMethodName), null);
        PsiMethod testMethod = this.javaTestElementUtil.createTestMethod(testName, Collections.singletonList("@org.junit.Test"));
        testMethod = (PsiMethod) testClass.add(testMethod);
        EasyMock.expect(methodResolver.allTestedMethodsMethodCalls(testMethod))
                .andReturn(new ElementSearchResult<>(new ArrayList<>(), Collections.singletonList(testedMethodCall)))
                .times(1);
        EasyMock.replay(methodResolver);
        // When
        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testMethod);
        //Then
        Assert.assertSame("Incorrect number of found violations", 0, foundViolations.size());
    }

    private BestPracticeViolation createBestPracticeViolation(PsiElement element, String problemDescription, List<String> hints, List<Action<BestPracticeViolation>> actions) {
        return new BestPracticeViolation(
                element,
                problemDescription,
                BestPractice.SETUP_A_TEST_NAMING_STRATEGY,
                actions,
                hints);
    }

}
