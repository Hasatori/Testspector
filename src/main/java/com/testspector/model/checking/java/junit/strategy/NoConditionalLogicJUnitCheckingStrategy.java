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

import java.security.InvalidParameterException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.testspector.model.checking.java.junit.JUnitConstants.JUNIT5_PARAMETERIZED_TEST_ABSOLUTE_PATH;

public class NoConditionalLogicJUnitCheckingStrategy implements BestPracticeCheckingStrategy {

    private static final List<Class<? extends PsiStatement>> SUPPORTED_STATEMENT_CLASSES = Collections.unmodifiableList(Arrays.asList(
            PsiIfStatement.class,
            PsiWhileStatement.class,
            PsiSwitchStatement.class,
            PsiForStatement.class
    ));
    private final JavaElementResolver elementResolver;
    private final JavaContextIndicator contextResolver;
    private final JavaMethodResolver methodResolver;

    public NoConditionalLogicJUnitCheckingStrategy(JavaElementResolver elementResolver, JavaContextIndicator contextResolver, JavaMethodResolver methodResolver) {
        this.elementResolver = elementResolver;
        this.contextResolver = contextResolver;
        this.methodResolver = methodResolver;
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiElement psiElement) {
        return checkBestPractices(Collections.singletonList(psiElement));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiElement> psiElements) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        List<PsiMethod> methods = methodResolver.testMethodsWithAnnotations(psiElements, JUnitConstants.JUNIT_ALL_TEST_QUALIFIED_NAMES);
        for (PsiMethod method : methods) {
            List<PsiStatement> statements = elementResolver.allChildrenOfType(method, PsiStatement.class, isConditionalStatement(), contextResolver.isInTestContext());

            statements = statements.stream().distinct().collect(Collectors.toList());
            if (statements.size() > 0) {
                List<String> hints = new ArrayList<>();
                hints.add("Remove statements and create separate test scenario for each branch");
                if (Arrays.stream(method.getAnnotations()).anyMatch(psiAnnotation -> JUnitConstants.JUNIT5_TEST_QUALIFIED_NAMES.contains(psiAnnotation.getQualifiedName()))) {
                    hints.add(String.format("You are using JUnit5 so it can be solved by using %s", JUNIT5_PARAMETERIZED_TEST_ABSOLUTE_PATH));
                }
                PsiIdentifier methodIdentifier = method.getNameIdentifier();
                bestPracticeViolations.add(new BestPracticeViolation(
                                String.format("%s#%s", method.getContainingClass().getQualifiedName(), method.getName()),
                                method,
                                methodIdentifier != null ? methodIdentifier.getTextRange() : method.getTextRange(),
                                "Conditional logic should not be part of the test method, it makes test hard to understand and read.",
                                hints,
                                getCheckedBestPractice().get(0),
                                createRelatedElements(method, statements)
                        )

                );
            }

        }
        return bestPracticeViolations;
    }

    Predicate<PsiStatement> isConditionalStatement() {
        return psiStatement -> SUPPORTED_STATEMENT_CLASSES.stream().anyMatch(supportedStatement -> supportedStatement.isInstance(psiStatement));
    }

    private List<RelatedElementWrapper> createRelatedElements(PsiMethod method, List<PsiStatement> conditionalStatements) {
        List<RelatedElementWrapper> result = new ArrayList<>();
        for (PsiStatement conditionalStatement : conditionalStatements) {
            HashMap<PsiElement, String> elementNameHashMap = new HashMap<>();
            Optional<PsiReferenceExpression> optionalPsiReferenceExpression = firstReferenceToConditionalStatement(method, conditionalStatement);
            if (optionalPsiReferenceExpression.isPresent()) {
                elementNameHashMap.put(optionalPsiReferenceExpression.get(), "reference from test method");
                elementNameHashMap.put(conditionalStatement, "statement position");
            } else {
                elementNameHashMap.put(conditionalStatement, "statement");
            }
            result.add(new RelatedElementWrapper(String.format("%s ...%d - %d...",statementString(conditionalStatement), conditionalStatement.getTextRange().getStartOffset(), conditionalStatement.getTextRange().getEndOffset()), elementNameHashMap));
        }

        return result;
    }

    private Optional<PsiReferenceExpression> firstReferenceToConditionalStatement(PsiElement element, PsiStatement statement) {
        List<PsiReferenceExpression> references = elementResolver.allChildrenOfType(element, PsiReferenceExpression.class);
        for (PsiReferenceExpression reference : references) {
            if (!elementResolver.allChildrenOfType(reference.getParent(), PsiStatement.class, psiStatement -> statement == psiStatement, contextResolver.isInTestContext()).isEmpty()) {
                return Optional.of(reference);
            }
        }
        return Optional.empty();
    }

    private String statementString(PsiStatement statement) {
        if (statement instanceof PsiIfStatement) {
            return "if";
        } else if (statement instanceof PsiForStatement) {
            return "for";
        } else if (statement instanceof PsiWhileStatement) {
            return "while";
        } else if (statement instanceof PsiSwitchStatement) {
            return "switch";
        }
        throw new InvalidParameterException(String.format("Invalid statement instance %s. Supported instances are: %s", statement.getClass(), SUPPORTED_STATEMENT_CLASSES));
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.NO_CONDITIONAL_LOGIC);
    }
}
