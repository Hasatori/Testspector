package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.*;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.RelatedElementWrapper;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaElementResolver;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.junit.JUnitConstants;
import com.testspector.model.enums.BestPractice;

import java.util.*;

import static com.testspector.model.checking.java.junit.JUnitConstants.JUNIT4_ASSERTIONS_CLASS_PATH;
import static com.testspector.model.checking.java.junit.JUnitConstants.JUNIT5_ASSERTIONS_CLASS_PATH;

public class CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy implements BestPracticeCheckingStrategy<PsiMethod> {

    private final JavaElementResolver elementResolver;
    private final JavaContextIndicator contextResolver;
    private final JavaMethodResolver methodResolver;

    public CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy(JavaElementResolver elementResolver, JavaContextIndicator contextResolver, JavaMethodResolver methodResolver) {
        this.elementResolver = elementResolver;
        this.contextResolver = contextResolver;
        this.methodResolver = methodResolver;
    }


    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiMethod method) {
        return this.checkBestPractices(Collections.singletonList(method));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiMethod> methods) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        for (PsiMethod method : methods) {
            List<PsiTryStatement> psiTryStatements = elementResolver.allChildrenOfTypeWithReferences(method, PsiTryStatement.class,contextResolver.isInTestContext());
            if (psiTryStatements.size() > 0) {
                List<String> hints = new ArrayList<>();
                String message = "Tests should not contain try catch block. These blocks are redundant and make test harder to read and understand. In some cases it might even lead to never failing tests if we are not handling the exception properly.";
                if (Arrays.stream(method.getAnnotations()).anyMatch(psiAnnotation -> JUnitConstants.JUNIT5_TEST_QUALIFIED_NAMES.contains(psiAnnotation.getQualifiedName()))) {
                    hints.add(String.format("You are using JUnit5 so it can be solved by using %s.assertThrows() method", JUNIT5_ASSERTIONS_CLASS_PATH));
                }
                if (Arrays.stream(method.getAnnotations()).anyMatch(psiAnnotation -> JUnitConstants.JUNIT4_TEST_QUALIFIED_NAMES.contains(psiAnnotation.getQualifiedName()))) {
                    hints.add(String.format("You are using JUnit4 so it can be solved by using @%s.Test(expected = Exception.class) for the test method", JUNIT4_ASSERTIONS_CLASS_PATH));
                }
                PsiIdentifier methodIdentifier = method.getNameIdentifier();
                bestPracticeViolations.add(new BestPracticeViolation(
                        String.format("%s#%s",method.getContainingClass().getQualifiedName(),method.getName()),
                        method,
                        methodIdentifier != null ? methodIdentifier.getTextRange() : method.getTextRange(),
                        message,
                        hints,
                        this.getCheckedBestPractice().get(0),
                        createRelatedElements(method,psiTryStatements)
                ));
            }
        }
        return bestPracticeViolations;
    }

    private List<RelatedElementWrapper> createRelatedElements(PsiMethod method, List<PsiTryStatement> tryStatements) {
        List<RelatedElementWrapper> result = new ArrayList<>();
        for (PsiTryStatement conditionalStatement : tryStatements) {
            HashMap<PsiElement, String> elementNameHashMap = new HashMap<>();
            Optional<PsiReferenceExpression> optionalPsiReferenceExpression = firstReferenceToTryStatement(method, conditionalStatement);
            if (optionalPsiReferenceExpression.isPresent()) {
                elementNameHashMap.put(optionalPsiReferenceExpression.get(), "reference from test method");
                elementNameHashMap.put(conditionalStatement, "statement position");
            } else {
                elementNameHashMap.put(conditionalStatement, "statement");
            }
            result.add(new RelatedElementWrapper(String.format("Try catch statement ...%d - %d...",conditionalStatement.getTextRange().getStartOffset(),conditionalStatement.getTextRange().getEndOffset()), elementNameHashMap));
        }

        return result;
    }

    private Optional<PsiReferenceExpression> firstReferenceToTryStatement(PsiElement element, PsiTryStatement statement) {
        List<PsiReferenceExpression> references = elementResolver.allChildrenOfTypeMeetingConditionWithReferences(element, PsiReferenceExpression.class);
        for (PsiReferenceExpression reference : references) {
            if (!elementResolver.allChildrenOfTypeMeetingConditionWithReferences(reference.getParent(), PsiStatement.class, psiStatement -> statement == psiStatement, contextResolver.isInTestContext()).isEmpty()) {
                return Optional.of(reference);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.CATCH_TESTED_EXCEPTIONS_USING_FRAMEWORK_TOOLS);
    }
}
