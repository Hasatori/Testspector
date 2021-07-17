package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.search.ElementSearchResult;
import com.testspector.model.checking.java.common.search.QueriesRepository;
import com.testspector.model.enums.BestPractice;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


public class NoGlobalStaticPropertiesJUnitCheckingJUnitStrategyTest extends JUnitStrategyTest {

    private NoGlobalStaticPropertiesJUnitCheckingStrategy strategy;


    @BeforeEach
    public void beforeEach() {
        this.strategy = new NoGlobalStaticPropertiesJUnitCheckingStrategy(elementSearchEngine, contextIndicator, methodResolver);
    }

    @Test
    public void checkBestPractices_TestReferenceToStaticNotFinalConstant_OneViolationReportingAboutUsingStaticGlobalConstantShouldBeReturned() {
        // Given
        String constantName = "EXAMPLE_UNSECURE_CONSTANT";
        PsiField staticNotFinalStringConstant = (PsiField) testClass.add(psiElementFactory
                .createFieldFromText(String.format("private static String %s = \"VALUE\"", constantName), testClass));
        PsiMethod testMethod = this.javaTestElementUtil.createTestMethod("testMethod", Collections.singletonList("@Test"));
        testMethod = (PsiMethod) testClass.add(testMethod);
        PsiMethodCallExpression methodCallingTheConstant = (PsiMethodCallExpression) psiElementFactory
                .createExpressionFromText(String.format("Assert.assertEquals(%s,\"Test\")", constantName), null);
        EasyMock.expect(contextIndicator.isInTestContext()).andReturn((element) -> true).times(2);
        EasyMock.replay(contextIndicator);
        EasyMock.expect(elementSearchEngine
                .findByQuery(EasyMock.eq(testMethod), EasyMock.eq(QueriesRepository.FIND_ALL_STATIC_PROPS)))
                .andReturn(new ElementSearchResult<>(new ArrayList<>(), Collections.singletonList(staticNotFinalStringConstant))).times(1);
        EasyMock.replay(elementSearchEngine);
        List<BestPracticeViolation> expectedViolations = Collections.singletonList(
                createBestPracticeViolation(
                        staticNotFinalStringConstant,
                        "Global static properties should not be part of a test. " +
                                "Tests are sharing the reference and if some of them would update it " +
                                "it might influence behaviour of other tests.",
                        Arrays.asList(
                                "If the property is immutable e.g.,String, Integer, Byte, Character etc. then " +
                                        "you can add 'final' identifier so that tests can not change reference",
                                "If the property is mutable then delete static modifier and make property reference" +
                                        " unique for each test."),
                        new ArrayList<>()));

        // When
        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testMethod);

        //Then
        assertAll(
                () -> Assertions.assertSame(1, foundViolations.size(), "Incorrect number of found violations"),
                () -> assertThat(foundViolations.get(0)).as("Checking first found violation").usingRecursiveComparison().isEqualTo(expectedViolations.get(0))
        );
    }

    @Test
    public void checkBestPractices_TestDoestNotReferenceToAnyStaticNotFinalConstants_NoViolationsShouldBeFound() {
        PsiField staticFinalStringConstant = psiElementFactory
                .createFieldFromText("private static final String EXAMPLE_SECURE_CONSTANT = \"VALUE\"", null);
        PsiMethod testMethod = this.javaTestElementUtil.createTestMethod("testMethod", Collections.singletonList("@Test"));
        testMethod = (PsiMethod) testClass.add(testMethod);
        EasyMock.expect(contextIndicator.isInTestContext()).andReturn((element) -> true).times(1);
        EasyMock.replay(contextIndicator);
        EasyMock.expect(elementSearchEngine.findByQuery(EasyMock.eq(testMethod), EasyMock.eq(QueriesRepository.FIND_ALL_STATIC_PROPS)))
                .andReturn(new ElementSearchResult<>(new ArrayList<>(), Collections.singletonList(staticFinalStringConstant)))
                .times(1);
        EasyMock.replay(elementSearchEngine);

        List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testMethod);

        Assertions.assertSame(0, foundViolations.size(), "Incorrect number of found violations");
    }


    private BestPracticeViolation createBestPracticeViolation(PsiElement element, String problemDescription, List<String> hints, List<Action<BestPracticeViolation>> actions) {
        return new BestPracticeViolation(
                element,
                problemDescription,
                BestPractice.NO_GLOBAL_STATIC_PROPERTIES,
                actions,
                hints);
    }

}
