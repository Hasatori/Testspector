package com.testspector.model.checking.java.junit.strategy;

import com.intellij.openapi.application.ApplicationManager;import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.RelatedElementWrapper;
import com.testspector.model.enums.BestPractice;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


@RunWith(JUnitPlatform.class)
public class NoGlobalStaticPropertiesJUnitCheckingStrategyTest extends StrategyTest {

    private NoGlobalStaticPropertiesJUnitCheckingStrategy strategy;


    @BeforeEach
    public void beforeEach() {
        this.strategy = new NoGlobalStaticPropertiesJUnitCheckingStrategy(elementResolver, methodResolver, contextIndicator);
    }

    @Test
    public void checkBestPractices_TestReferenceToStaticNotFinalConstant_OneViolationReportingAboutUsingStaticGlobalConstantShouldBeReturned() {
        WriteCommandAction.runWriteCommandAction(getProject(),() -> {
            // Given
            String constantName = "EXAMPLE_UNSECURE_CONSTANT";
            PsiField staticNotFinalStringConstant = (PsiField) testClass.add(psiElementFactory.createFieldFromText(String.format("private static String %s = \"VALUE\"", constantName), testClass));
            PsiMethod testMethod = this.javaTestElementUtil.createTestMethod("testMethod", Collections.singletonList("@Test"));
            testMethod = (PsiMethod) testClass.add(testMethod);
            PsiMethodCallExpression methodCallingTheConstant = (PsiMethodCallExpression) psiElementFactory.createExpressionFromText(String.format("Assert.assertEquals(%s,\"Test\")", constantName), null);

            EasyMock.expect(contextIndicator.isInTestContext()).andReturn((element) -> true).times(2);
            EasyMock.replay(contextIndicator);
            EasyMock.expect(elementResolver.allChildrenOfType(EasyMock.eq(testMethod), EasyMock.eq(PsiField.class), EasyMock.anyObject())).andReturn(Collections.singletonList(staticNotFinalStringConstant)).times(1);
            EasyMock.expect(elementResolver.allChildrenOfType(testMethod, PsiReferenceExpression.class)).andReturn(Collections.singletonList(methodCallingTheConstant.getMethodExpression())).times(1);
            EasyMock.expect(elementResolver.allChildrenOfType(EasyMock.eq(methodCallingTheConstant), EasyMock.eq(PsiField.class), EasyMock.anyObject(), EasyMock.anyObject()))
                    .andReturn(Collections.singletonList(staticNotFinalStringConstant)).times(1);
            EasyMock.replay(elementResolver);

            List<BestPracticeViolation> expectedViolations = Collections.singletonList(
                    createBestPracticeViolation(
                            String.format("%s#%s", testMethod.getContainingClass().getQualifiedName(), testMethod.getName()),
                            testMethod,
                            testMethod.getNameIdentifier().getTextRange(),
                            "Global static properties should not be part of a test. " +
                                    "Tests are sharing the reference and if some of them would update it it might influence behaviour of other tests.",
                            Arrays.asList(
                                    "If the property is immutable e.g.,String, Integer, Byte, Character etc. then you can add 'final' identifier so that tests can not change reference",
                                    "If the property is mutable then delete static modifier and make property reference unique for each test."),
                            Arrays.asList(
                                    new RelatedElementWrapper(staticNotFinalStringConstant.getName(), new HashMap<PsiElement, String>() {{
                                        put(methodCallingTheConstant.getMethodExpression(), "property reference from test method");
                                        put(staticNotFinalStringConstant, "property");
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
        });
    }

    @Test
    public void checkBestPractices_TestDoestNotReferenceToAnyStaticNotFinalConstants_NoViolationsShouldBeFound() {
        WriteCommandAction.runWriteCommandAction(getProject(),() -> {
            PsiField staticFinalStringConstant = psiElementFactory.createFieldFromText("private static final String EXAMPLE_SECURE_CONSTANT = \"VALUE\"", null);
            PsiMethod testMethod = this.javaTestElementUtil.createTestMethod("testMethod", Collections.singletonList("@Test"));
            testMethod = (PsiMethod) testClass.add(testMethod);
            EasyMock.expect(contextIndicator.isInTestContext()).andReturn((element) -> true).times(1);
            EasyMock.replay(contextIndicator);
            EasyMock.expect(elementResolver.allChildrenOfType(EasyMock.eq(testMethod), EasyMock.eq(PsiField.class), EasyMock.anyObject())).andReturn(Collections.singletonList(staticFinalStringConstant)).times(1);
            EasyMock.replay(elementResolver);

            List<BestPracticeViolation> foundViolations = strategy.checkBestPractices(testMethod);

            Assertions.assertSame(0, foundViolations.size(), "Incorrect number of found violations");
        });
    }


    private BestPracticeViolation createBestPracticeViolation(String name, PsiElement testMethodElement, TextRange testMethodTextRange, String problemDescription, List<String> hints, List<RelatedElementWrapper> relatedElementsWrapper) {
        return new BestPracticeViolation(
                name,
                testMethodElement,
                testMethodTextRange,
                problemDescription,
                hints,
                BestPractice.NO_GLOBAL_STATIC_PROPERTIES,
                relatedElementsWrapper

        );
    }

}
