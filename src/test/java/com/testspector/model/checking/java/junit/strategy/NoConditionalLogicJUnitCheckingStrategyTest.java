package com.testspector.model.checking.java.junit.strategy;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.*;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.JavaElementHelper;
import com.testspector.model.enums.BestPractice;
import org.easymock.EasyMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.testspector.model.checking.java.junit.JUnitTestUtil.getMethodFromFileByName;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


@RunWith(JUnitPlatform.class)
public class NoConditionalLogicJUnitCheckingStrategyTest extends BasePlatformTestCase {

    private static final List<String> JUNIT_TEST_QUALIFIED_NAMES = Collections.unmodifiableList(Arrays.asList(
            "org.junit.Test",
            "org.junit.jupiter.api.Test",
            "org.junit.jupiter.params.ParameterizedTest",
            "org.junit.jupiter.api.RepeatedTest"
    ));

    private static final String VIOLATION_DESCRIPTION_BASE = "statement logic should not be part of the test method, it makes test hard to understand and read. Remove if statements and create separate test scenario for each branch.";

    @BeforeEach
    public void beforeEach() throws Exception {
        this.setUp();
    }

    @AfterEach
    public void afterEach() throws Exception {
        this.tearDown();
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/java/junit/NoConditionalLogicJUnitCheckingStrategyTest";
    }

    @Test
    public void checkBestPractices_CheckingInMethodWhichContainsForStatementAndUsesHelperMethodThatContainsIfStatement_TwoViolationsReportingAboutConditionalLogicShouldBeReturned() {
        PsiJavaFile javaFile = (PsiJavaFile) myFixture.configureByFile("NoConditionalLogicJUnitCheckingStrategyTest.java");
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiMethod testMethod = getMethodFromFileByName(javaFile, "testWithForStatement").get();
            PsiForStatement testMethodForStatement = getConditionalStatement(testMethod, PsiForStatement.class);
            PsiMethod helperMethod = getMethodFromFileByName(javaFile, "helperMethodWithIf").get();
            PsiIfStatement helperMethodIfStatement = getConditionalStatement(helperMethod, PsiIfStatement.class);

            List<BestPracticeViolation> expectedViolations = Arrays.asList(
                    createBestPracticeViolation(helperMethodIfStatement),
                    createBestPracticeViolation(testMethodForStatement)
            );
            sortViolationsByTextRangeStartOffset(expectedViolations);
            JavaElementHelper javaElementHelper = EasyMock.mock(JavaElementHelper.class);
            EasyMock.expect(javaElementHelper.getMethodsFromElementByAnnotations(Collections.singletonList(testMethod), JUNIT_TEST_QUALIFIED_NAMES)).andReturn(Collections.singletonList(testMethod));
            EasyMock.replay(javaElementHelper);

            NoConditionalLogicJUnitCheckingStrategy noConditionalLogicJUnitCheckingStrategy = new NoConditionalLogicJUnitCheckingStrategy(javaElementHelper);
            List<BestPracticeViolation> foundViolations = noConditionalLogicJUnitCheckingStrategy.checkBestPractices(testMethod);
            sortViolationsByTextRangeStartOffset(foundViolations);


            assertAll(
                    () -> assertThat(foundViolations.get(0)).as("Test method for statement violation").isEqualToComparingFieldByField(expectedViolations.get(0)),
                    () -> assertThat(foundViolations.get(1)).as("Helper method if statement violation").isEqualToComparingFieldByField(expectedViolations.get(1))
            );
        });
    }

    @Test
    public void checkBestPractices_CheckingInMethodWhichContainsWhileStatementAndUsesHelperMethodThatContainsSwitchStatement_TwoViolationsReportingAboutConditionalLogicShouldBeReturned() {
        PsiJavaFile javaFile = (PsiJavaFile) myFixture.configureByFile("NoConditionalLogicJUnitCheckingStrategyTest.java");
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiMethod testMethod = getMethodFromFileByName(javaFile, "testWithWhileStatement").get();
            PsiWhileStatement testMethodWhileStatement = getConditionalStatement(testMethod, PsiWhileStatement.class);
            PsiMethod helperMethod = getMethodFromFileByName(javaFile, "helperMethodWithSwitch").get();
            PsiSwitchStatement helperMethodSwitchStatement = getConditionalStatement(helperMethod, PsiSwitchStatement.class);
            List<BestPracticeViolation> expectedViolations = Arrays.asList(
                    createBestPracticeViolation(helperMethodSwitchStatement),
                    createBestPracticeViolation(testMethodWhileStatement)
            );
            sortViolationsByTextRangeStartOffset(expectedViolations);
            JavaElementHelper javaElementHelper = EasyMock.mock(JavaElementHelper.class);
            EasyMock.expect(javaElementHelper.getMethodsFromElementByAnnotations(Collections.singletonList(testMethod), JUNIT_TEST_QUALIFIED_NAMES)).andReturn(Collections.singletonList(testMethod));
            EasyMock.replay(javaElementHelper);

            NoConditionalLogicJUnitCheckingStrategy noConditionalLogicJUnitCheckingStrategy = new NoConditionalLogicJUnitCheckingStrategy(javaElementHelper);
            List<BestPracticeViolation> foundViolations = noConditionalLogicJUnitCheckingStrategy.checkBestPractices(testMethod);

            sortViolationsByTextRangeStartOffset(foundViolations);


            assertAll(
                    () -> assertThat(foundViolations.get(0)).as("Test method while statement violation").isEqualToComparingFieldByField(expectedViolations.get(0)),
                    () -> assertThat(foundViolations.get(1)).as("Helper method switch statement violation").isEqualToComparingFieldByField(expectedViolations.get(1))
            );
        });
    }

    private <T extends PsiStatement> T getConditionalStatement(PsiMethod method, Class<T> statementClass) {
        return Arrays.stream(method.getBody().getStatements())
                .filter(psiStatement -> statementClass.isInstance(psiStatement))
                .map(statementClass::cast)
                .findFirst()
                .get();
    }

    private void sortViolationsByTextRangeStartOffset(List<BestPracticeViolation> bestPracticeViolations) {
        bestPracticeViolations.sort(Comparator.comparingInt(o -> o.getTextRange().getStartOffset()));
    }

    private BestPracticeViolation createBestPracticeViolation(PsiStatement psiStatement) {
        return new BestPracticeViolation(
                psiStatement,
                psiStatement.getTextRange(),
                String.format("%s statement logic should not be part of the test method, it makes test hard to understand and read. Remove if statements and create separate test scenario for each branch.", psiStatement.toString()),
                BestPractice.NO_CONDITIONAL_LOGIC);

    }
}