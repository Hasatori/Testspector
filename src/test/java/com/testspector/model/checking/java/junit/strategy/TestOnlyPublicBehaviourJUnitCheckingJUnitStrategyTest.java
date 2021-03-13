package com.testspector.model.checking.java.junit.strategy;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.testspector.model.checking.crate.BestPracticeViolation;
import com.testspector.model.checking.crate.RelatedElementWrapper;
import com.testspector.model.enums.BestPractice;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


public class TestOnlyPublicBehaviourJUnitCheckingJUnitStrategyTest extends JUnitStrategyTest {

    private TestOnlyPublicBehaviourJUnitCheckingStrategy strategy;


    @BeforeEach
    public void beforeEach() {
        this.strategy = new TestOnlyPublicBehaviourJUnitCheckingStrategy(elementResolver, methodResolver, contextIndicator);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "'private'         | 'private'",
            "'protected'       | 'protected'",
            "'package private' | ''"
    }, delimiter = '|')
    public void checkBestPractices_TestTestsPrivateBehaviourViaMethodCall_OneViolationReportingAboutTestingPrivateBehaviourShouldBeReturned(String expectedRelatedElementQualifierName, String testedMethodAccessQualifier) {
        // Given
        PsiMethod testMethod = this.javaTestElementUtil.createTestMethod("testMethod", Collections.singletonList("@Test"));
        String testedMethodName = "testedMethod";
        PsiMethod testedMethod = this.javaTestElementUtil.createMethod(testedMethodName, "String", Collections.singletonList(testedMethodAccessQualifier));
        PsiMethodCallExpression testedMethodCall = (PsiMethodCallExpression) psiElementFactory.createExpressionFromText(String.format("%s()", testedMethodName), null);
        testMethod = (PsiMethod) testClass.add(testMethod);
        EasyMock.expect(contextIndicator.isInTestContext()).andReturn((element) -> true).times(2);
        EasyMock.replay(contextIndicator);
        EasyMock.expect(methodResolver.allTestedMethods(testMethod)).andReturn(Collections.singletonList(testedMethod)).times(1);
        EasyMock.expect(elementResolver.allChildrenOfType(testMethod, PsiReferenceExpression.class)).andReturn(Collections.singletonList(testedMethodCall.getMethodExpression())).times(1);
        EasyMock.expect(elementResolver.allChildrenOfType(EasyMock.eq(testedMethodCall), EasyMock.eq(PsiMethodCallExpression.class), EasyMock.anyObject(), EasyMock.eq(contextIndicator.isInTestContext())))
                .andReturn(Collections.singletonList(testedMethodCall)).times(1);
        EasyMock.replay(methodResolver, elementResolver);

        List<BestPracticeViolation> expectedViolations = Collections.singletonList(
                createBestPracticeViolation(
                        String.format("%s#%s", testMethod.getContainingClass().getQualifiedName(), testMethod.getName()),
                        testMethod,
                        testMethod.getNameIdentifier().getTextRange(),
                        "Only public behaviour should be tested. Testing 'private','protected' or 'package private' methods leads to problems with maintenance of tests because this private behaviour is likely to be changed very often. " +
                                "In many cases we are refactoring private behaviour without influencing public behaviour of the class, yet this changes will change behaviour of the private method and cause tests to fail.",
                        Arrays.asList(
                                "There is an exception to this rule and that is in case when private 'method' is part of the observed behaviour of the system under test. For example we can have private constructor for class which is part of ORM and its initialization should not be permitted.",
                                "Remove tests testing private behaviour",
                                "If you really feel that private behaviour is complex enough that there should be separate test for it, then it is very probable that the system under test is breaking 'Single Responsibility Principle' and this private behaviour should be extracted to a separate system"
                        ),
                        Arrays.asList(
                                new RelatedElementWrapper(testedMethod.getName(), new HashMap<PsiElement, String>() {{
                                    put(testedMethodCall.getMethodExpression(), String.format("%s method call from test", expectedRelatedElementQualifierName));
                                    put(testedMethod, String.format("%s method call in production code", expectedRelatedElementQualifierName));
                                }}))));

        // When
        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testMethod);

        //Then
        assertAll(
                () -> Assertions.assertSame(1, foundViolations.size(), "Incorrect number of found violations"),
                () -> assertThat(foundViolations.get(0)).as("Checking first found violation").isEqualToIgnoringGivenFields(expectedViolations.get(0), "relatedElementsWrapper"),
                () -> Assertions.assertSame(1, foundViolations.get(0).getRelatedElements().size(), "Incorrect number of related elements for first violation"),
                () -> assertThat(foundViolations.get(0).getRelatedElements().get(0)).as("Checking first related element in the first violation").isEqualToComparingFieldByField(expectedViolations.get(0).getRelatedElements().get(0))
        );
    }

    @Test
    public void checkBestPractices_TestTestsJustPublicMethods_NoViolationsShouldBeFound() {
        PsiMethod testMethod = this.javaTestElementUtil.createTestMethod("testMethod", Collections.singletonList("@Test"));
        PsiMethod testedMethod = this.javaTestElementUtil.createMethod("testedMethod", "String", Collections.singletonList(PsiKeyword.PUBLIC));
        testMethod = (PsiMethod) testClass.add(testMethod);
        EasyMock.expect(methodResolver.allTestedMethods(testMethod)).andReturn(Collections.singletonList(testedMethod)).times(1);
        EasyMock.replay(methodResolver);

        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testMethod);

        Assertions.assertSame(0, foundViolations.size(), "Incorrect number of found violations");
    }


    private BestPracticeViolation createBestPracticeViolation(String name, PsiElement testMethodElement, TextRange testMethodTextRange, String problemDescription, List<String> hints, List<RelatedElementWrapper> relatedElementsWrapper) {
        return new BestPracticeViolation(
                name,
                testMethodElement,
                testMethodTextRange,
                problemDescription,
                hints,
                BestPractice.TEST_ONLY_PUBLIC_BEHAVIOUR,
                relatedElementsWrapper

        );
    }

}
