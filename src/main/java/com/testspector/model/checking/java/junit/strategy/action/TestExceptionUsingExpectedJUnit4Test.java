package com.testspector.model.checking.java.junit.strategy.action;

import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiTryStatement;
import com.intellij.psi.PsiType;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;

import java.util.Arrays;

import static com.testspector.model.checking.java.junit.JUnitConstants.JUNIT4_TEST_QUALIFIED_NAME;

public class TestExceptionUsingExpectedJUnit4Test implements Action<BestPracticeViolation> {

    private final PsiMethod testMethod;
    private final PsiTryStatement psiTryStatement;
    private final PsiType exceptionType;

    public TestExceptionUsingExpectedJUnit4Test(PsiMethod testMethod, PsiTryStatement psiTryStatement, PsiType exceptionType) {
        this.testMethod = testMethod;
        this.psiTryStatement = psiTryStatement;
        this.exceptionType = exceptionType;
    }

    @Override
    public String getName() {
        return "Test exception using expected attribute in the the Test annotation";
    }

    @Override
    public void execute(BestPracticeViolation bestPracticeViolation) {
        Arrays.stream(testMethod.getAnnotations()).filter(psiAnnotation -> psiAnnotation.hasQualifiedName(JUNIT4_TEST_QUALIFIED_NAME)).findFirst().ifPresent(psiAnnotation -> {
            psiAnnotation.replace(PsiElementFactory.getInstance(psiTryStatement.getProject())
                    .createAnnotationFromText(String.format("@Test(expected = %s.class)", exceptionType.getPresentableText()), null));

        });
        new RemoveTryCatchStatementAction(psiTryStatement, true).execute(bestPracticeViolation);
    }
}
