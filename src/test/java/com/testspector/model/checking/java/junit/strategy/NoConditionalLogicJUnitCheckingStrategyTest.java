package com.testspector.model.checking.java.junit.strategy;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.RelatedElementWrapper;
import com.testspector.model.checking.java.JavaTestElementUtil;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaElementResolver;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.enums.BestPractice;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

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

    private JavaTestElementUtil javaTestElementUtil;
    private PsiElementFactory psiElementFactory;
    private PsiFileFactory psiFileFactory;

    @BeforeEach
    public void beforeEach() throws Exception {
        this.setUp();
        this.psiFileFactory = PsiFileFactory.getInstance(getProject());
        this.psiElementFactory = PsiElementFactory.getInstance(getProject());
        this.javaTestElementUtil = new JavaTestElementUtil(psiFileFactory, psiElementFactory);
    }

    @AfterEach
    public void afterEach() throws Exception {
        this.tearDown();
    }

    private static Stream<Arguments> provideAllSupportedStatementStringDefinitions() {
        return Stream.of(
                Arguments.of("if", "if(true){}else if(false){}"),
                Arguments.of("for", "for (int i = 0; i < 50; i++) {\n}"),
                Arguments.of("while", "while(true){}"),
                Arguments.of("switch", "switch (integer) {case 1:\"Test1\"; default:\"Test2\";}"));
    }

    @ParameterizedTest
    @MethodSource(value = "provideAllSupportedStatementStringDefinitions")
    public void checkBestPractices_CheckingAllConditionalStatementsOneIsInTheTestMethodAndSecondInTheHelperMethod_TwoViolationsReportingAboutConditionalLogicShouldBeReturned(String statementName, String statementStringDefinition) {
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            // Given
            String packageName = "com.testspector";
            String fileName = "Test";
            PsiJavaFile javaFile = this.javaTestElementUtil.createFile(fileName, packageName, Collections.singletonList("import org.junit.jupiter.api.Test;"), Collections.emptyList());
            PsiClass psiClass = this.psiElementFactory.createClass(fileName);
            psiClass = (PsiClass) javaFile.add(psiClass);
            String helperMethodName = "helperMethod";
            PsiMethod helperMethodWithStatement = this.javaTestElementUtil.createMethod(helperMethodName, "Object", Collections.singletonList(PsiKeyword.PUBLIC));
            PsiStatement helperMethodStatement = (PsiStatement) helperMethodWithStatement.getBody().add(this.psiElementFactory.createStatementFromText(statementStringDefinition, null));
            String testMethodName = "testWithStatement";
            PsiMethod testMethodWithStatement = this.javaTestElementUtil.createTestMethod(testMethodName, Collections.singletonList("@Test"));
            PsiStatement testMethodStatement = (PsiStatement) testMethodWithStatement.getBody().add(this.psiElementFactory.createStatementFromText(statementStringDefinition, null));
            testMethodWithStatement.getBody().add(this.psiElementFactory.createExpressionFromText(String.format("%s()", helperMethodName), null));
            helperMethodWithStatement = (PsiMethod) psiClass.add(helperMethodWithStatement);
            testMethodWithStatement = (PsiMethod) psiClass.add(testMethodWithStatement);
            JavaElementResolver javaElementHelper = EasyMock.mock(JavaElementResolver.class);
            JavaContextIndicator javaContextIndicator = EasyMock.mock(JavaContextIndicator.class);
            JavaMethodResolver methodResolver = EasyMock.mock(JavaMethodResolver.class);
            EasyMock.expect(javaContextIndicator.isInTestContext()).andReturn((element) -> true).anyTimes();
            EasyMock.expect(methodResolver.testMethodsWithAnnotations(Collections.singletonList(testMethodWithStatement), JUNIT_TEST_QUALIFIED_NAMES)).andReturn(Collections.singletonList(testMethodWithStatement)).times(1);
            EasyMock.replay(javaContextIndicator);
            EasyMock.expect(javaElementHelper.allChildrenOfType(EasyMock.eq(testMethodWithStatement), EasyMock.eq(PsiStatement.class), EasyMock.anyObject(), EasyMock.eq(javaContextIndicator.isInTestContext()))).andReturn(Arrays.asList(testMethodStatement, helperMethodStatement)).times(1);
            EasyMock.expect(javaElementHelper.allChildrenOfType(EasyMock.anyObject(), EasyMock.anyObject())).andReturn(Collections.emptyList()).times(2);
            EasyMock.replay(javaElementHelper, methodResolver);
            List<BestPracticeViolation> expectedViolations = Collections.singletonList(
                    createBestPracticeViolation(
                            String.format("%s.%s#%s", packageName, fileName, testMethodName),
                            testMethodWithStatement,
                            testMethodWithStatement.getNameIdentifier().getTextRange(),
                            "Conditional logic should not be part of the test method, it makes test hard to understand and read.",
                            Collections.singletonList("Remove statements and create separate test scenario for each branch"),
                            Arrays.asList(
                                    new RelatedElementWrapper(String.format("%s ...%d - %d...", statementName, testMethodStatement.getTextRange().getStartOffset(), testMethodStatement.getTextRange().getEndOffset()), new HashMap<PsiElement, String>() {{
                                        put(testMethodStatement, "statement");
                                    }}),
                                    new RelatedElementWrapper(String.format("%s ...%d - %d...", statementName, helperMethodStatement.getTextRange().getStartOffset(), helperMethodStatement.getTextRange().getEndOffset()), new HashMap<PsiElement, String>() {{
                                        put(helperMethodStatement, "statement");
                                    }}))));

            // When
            NoConditionalLogicJUnitCheckingStrategy noConditionalLogicJUnitCheckingStrategy = new NoConditionalLogicJUnitCheckingStrategy(javaElementHelper, javaContextIndicator, methodResolver);
            List<BestPracticeViolation> foundViolations = noConditionalLogicJUnitCheckingStrategy.checkBestPractices(testMethodWithStatement);

            //Then
            assertAll(
                    () -> Assert.assertSame("Incorrect number of found violations", 1, foundViolations.size()),
                    () -> assertThat(foundViolations.get(0)).as("Checking first found violation").isEqualToIgnoringGivenFields(expectedViolations.get(0), "relatedElementsWrapper"),
                    () -> Assert.assertSame("Incorrect number of related elements for first violation", 2, foundViolations.get(0).getRelatedElements().size()),
                    () -> assertThat(foundViolations.get(0).getRelatedElements().get(0)).as("Checking first related element in the first violation").isEqualToComparingFieldByField(expectedViolations.get(0).getRelatedElements().get(0)),
                    () -> assertThat(foundViolations.get(0).getRelatedElements().get(1)).as("Checking second related element in the first violation").isEqualToComparingFieldByField(expectedViolations.get(0).getRelatedElements().get(1))
            );
        });
    }

    private BestPracticeViolation createBestPracticeViolation(String name, PsiElement testMethodElement, TextRange testMethodTextRange, String problemDescription, List<String> hints, List<RelatedElementWrapper> relatedElementsWrapper) {
        return new BestPracticeViolation(
                name,
                testMethodElement,
                testMethodTextRange,
                problemDescription,
                hints,
                BestPractice.NO_CONDITIONAL_LOGIC,
                relatedElementsWrapper

        );
    }

}
