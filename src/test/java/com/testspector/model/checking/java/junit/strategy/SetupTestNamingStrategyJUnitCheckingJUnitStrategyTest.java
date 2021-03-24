package com.testspector.model.checking.java.junit.strategy;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.RelatedElementWrapper;
import com.testspector.model.enums.BestPractice;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


public class SetupTestNamingStrategyJUnitCheckingJUnitStrategyTest extends JUnitStrategyTest {

    private static final String ALMOST_SAME_NAME_PROBLEM_DESCRIPTION = "The test name is more or less that same as a tested method. This says nothing about tests scenario. You should setup a clear strategy for naming your tests so that the person reading then knows what is tested";
    private SetupTestNamingStrategyJUnitCheckingStrategy strategy;

    @BeforeEach
    public void beforeEach() {
        this.strategy = new SetupTestNamingStrategyJUnitCheckingStrategy(elementResolver, methodResolver, contextIndicator);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "'getName' | 'getName'            | '" + ALMOST_SAME_NAME_PROBLEM_DESCRIPTION + "'",
            "'getName' | 'testGetName'        | '" + ALMOST_SAME_NAME_PROBLEM_DESCRIPTION + "'",
            "'getName' | 'getNameTest'        | '" + ALMOST_SAME_NAME_PROBLEM_DESCRIPTION + "'",
            "'getName' | 'test_GetName'       | '" + ALMOST_SAME_NAME_PROBLEM_DESCRIPTION + "'",
            "'getName' | 'GetName_test'       | '" + ALMOST_SAME_NAME_PROBLEM_DESCRIPTION + "'",
    }, delimiter = '|')
    public void checkBestPractices_TestNameTooSimilarOrTooDifferent_OneViolationReportingAboutTestNamingStrategyShouldBeReturned(String testName, String testedMethodName, String problemDescription) {
        // Given
        PsiMethod testedMethod = this.javaTestElementUtil.createMethod(testedMethodName, "String", Collections.singletonList(PsiKeyword.PUBLIC));
        PsiMethodCallExpression testedMethodCall = (PsiMethodCallExpression) this.psiElementFactory.createExpressionFromText(String.format("%s()", testedMethodName), null);
        PsiMethod testMethod = this.javaTestElementUtil.createTestMethod(testName, Collections.singletonList("@org.junit.Test"));
        testMethod = (PsiMethod) testClass.add(testMethod);
        EasyMock.expect(contextIndicator.isInTestContext()).andReturn((element) -> true).anyTimes();
        EasyMock.replay(contextIndicator);
        EasyMock.expect(methodResolver.allTestedMethods(testMethod)).andReturn(Collections.singletonList(testedMethod)).times(1);
        EasyMock.expect(elementResolver.allChildrenOfTypeMeetingConditionWithReferences(testMethod, PsiReferenceExpression.class)).andReturn(Collections.singletonList(testedMethodCall.getMethodExpression())).times(1);
        EasyMock.expect(elementResolver.allChildrenOfTypeMeetingConditionWithReferences(EasyMock.eq(testedMethodCall), EasyMock.eq(PsiMethodCallExpression.class), EasyMock.anyObject(), EasyMock.eq(contextIndicator.isInTestContext())))
                .andReturn(Collections.singletonList(testedMethodCall)).times(1);
        EasyMock.replay(elementResolver, methodResolver);
        List<BestPracticeViolation> expectedViolations = Collections.singletonList(
                createBestPracticeViolation(
                        String.format("%s#%s", testMethod.getContainingClass().getQualifiedName(), testMethod.getName()),
                        testMethod,
                        testMethod.getNameIdentifier().getTextRange(),
                        problemDescription,
                        Arrays.asList(
                                "Possible strategy: 'doingSomeOperationGeneratesSomeResult'",
                                "Possible strategy: 'someResultOccursUnderSomeCondition'",
                                "Possible strategy: 'given-when-then'",
                                "Possible strategy: 'givenSomeContextWhenDoingSomeBehaviorThenSomeResultOccurs'",
                                "Possible strategy: 'whatIsTested_conditions_expectedResult'",
                                "Chosen naming strategy is subjective. The key thing to remember is that name of the test should say: What is tests, What are the conditions, What is expected result"
                        ),
                        Arrays.asList(
                                new RelatedElementWrapper(testedMethodName, new HashMap<PsiElement, String>() {{
                                    put(testedMethodCall.getMethodExpression(), "simple method call from test");
                                    put(testedMethod, "method call in production code");
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


    @ParameterizedTest
    @CsvSource(value = {
            "'checkBestPractices'       | 'checkBestPractices_TestNameIsUsingStrategy_NoViolationShouldBeReturned'",
            "'checkBestPractices'       | 'checkBestPractices_JUnit5TestMethodContainsTryCatchStatement_OneViolationReportingAboutCatchingTheExceptionUsingJUnit5AssertionShouldBeReturned'",
            "'canResolveFromPsiElement' | 'canResolveFromPsiElement_PsiFilesWithDifferentJUnitVersions_ShoudIndicateThatCanResolve'",
            "'resolveTest'              | 'resolveTest_NullElement_ShouldReturnEmpty'",
    }, delimiter = '|')
    public void checkBestPractices_TestNameIsUsingStrategy_NoViolationShouldBeReturned(String testName, String testedMethodName) {
        // Given
        PsiMethod testedMethod = this.javaTestElementUtil.createMethod(testedMethodName, "String", Collections.singletonList(PsiKeyword.PUBLIC));
        PsiMethodCallExpression testedMethodCall = (PsiMethodCallExpression) this.psiElementFactory.createExpressionFromText(String.format("%s()", testedMethodName), null);
        PsiMethod testMethod = this.javaTestElementUtil.createTestMethod(testName, Collections.singletonList("@org.junit.Test"));
        testMethod = (PsiMethod) testClass.add(testMethod);
        EasyMock.expect(methodResolver.allTestedMethods(testMethod)).andReturn(Collections.singletonList(testedMethod)).times(1);
        EasyMock.replay(methodResolver);
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
                BestPractice.SETUP_A_TEST_NAMING_STRATEGY,
                relatedElementsWrapper

        );
    }

}
