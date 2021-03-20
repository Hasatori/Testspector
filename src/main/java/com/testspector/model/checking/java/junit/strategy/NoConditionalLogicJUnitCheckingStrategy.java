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

public class NoConditionalLogicJUnitCheckingStrategy implements BestPracticeCheckingStrategy<PsiMethod> {

    private static final List<Class<? extends PsiStatement>> SUPPORTED_STATEMENT_CLASSES = Collections.unmodifiableList(Arrays.asList(
            PsiIfStatement.class,
            PsiWhileStatement.class,
            PsiSwitchStatement.class,
            PsiForStatement.class,
            PsiForeachStatement.class
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
    public List<BestPracticeViolation> checkBestPractices(PsiMethod method) {
        return checkBestPractices(Collections.singletonList(method));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiMethod> methods) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();

        for (PsiMethod method : methods) {
            List<PsiStatement> statements = elementResolver.allChildrenOfType(method, PsiStatement.class, isConditionalStatement().and(partOfAssertionMethod().negate()), contextResolver.isInTestContext());

            statements = statements.stream().distinct().collect(Collectors.toList());
            if (statements.size() > 0) {
                List<String> hints = new ArrayList<>();
                hints.add(String.format("Remove statements [ %s ] and create separate test scenario for each branch",
                        SUPPORTED_STATEMENT_CLASSES.stream()
                                .map(this::statementString).collect(Collectors.joining(", "))));
                hints.add("Acceptable place where conditional logic can be are custom assertions, where base on inputs we decide if we throw exception or not");
                if (Arrays.stream(method.getAnnotations()).anyMatch(psiAnnotation -> JUnitConstants.JUNIT5_TEST_QUALIFIED_NAMES.contains(psiAnnotation.getQualifiedName()))) {
                    hints.add(String.format("You are using JUnit5 so the problem can be solved by using data driven approach and generating each scenario using %s", JUNIT5_PARAMETERIZED_TEST_ABSOLUTE_PATH));
                }
                PsiIdentifier methodIdentifier = method.getNameIdentifier();
                bestPracticeViolations.add(new BestPracticeViolation(
                                String.format("%s#%s", method.getContainingClass().getQualifiedName(), method.getName()),
                                method,
                                methodIdentifier != null ? methodIdentifier.getTextRange() : method.getTextRange(),
                                "Conditional logic should not be part of the test method, it makes test hard to understand, read and maintain.",
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

    Predicate<PsiStatement> partOfAssertionMethod() {
        return psiStatement -> {
            PsiElement element = psiStatement.getContext();
            while (element instanceof PsiCodeBlock) {
                element = element.getContext();
            }
            if (element instanceof PsiMethod) {
                return methodResolver.assertionMethod((PsiMethod) element).isPresent();
            }
            return false;
        };
    }


    private List<RelatedElementWrapper> createRelatedElements(PsiMethod method, List<PsiStatement> conditionalStatements) {
        List<RelatedElementWrapper> result = new ArrayList<>();
        for (PsiStatement conditionalStatement : conditionalStatements) {
            HashMap<PsiElement, String> elementNameHashMap = new HashMap<>();
            Optional<PsiReferenceExpression> optionalPsiReferenceExpression = firstReferenceToConditionalStatement(method, conditionalStatement);
            optionalPsiReferenceExpression.ifPresent(psiReferenceExpression -> elementNameHashMap.put(psiReferenceExpression, "reference from test method"));
            elementNameHashMap.put(conditionalStatement, "statement");
            result.add(new RelatedElementWrapper(String.format("%s ...%d - %d...", statementString(conditionalStatement), conditionalStatement.getTextRange().getStartOffset(), conditionalStatement.getTextRange().getEndOffset()), elementNameHashMap));
        }

        return result;
    }

    private Optional<PsiReferenceExpression> firstReferenceToConditionalStatement(PsiMethod method, PsiStatement statement) {
        List<PsiReferenceExpression> references = elementResolver.allChildrenOfType(method, PsiReferenceExpression.class);
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
        } else if (statement instanceof PsiForeachStatement) {
            return "forEach";
        } else if (statement instanceof PsiWhileStatement) {
            return "while";
        } else if (statement instanceof PsiSwitchStatement) {
            return "switch";
        }
        throw new InvalidParameterException(String.format("Invalid statement instance %s. Supported instances are: %s", statement.getClass(), SUPPORTED_STATEMENT_CLASSES));
    }

    private String statementString(Class<? extends PsiStatement> statementClass) {
        if (PsiIfStatement.class == statementClass) {
            return "if";
        } else if (PsiForStatement.class == statementClass) {
            return "for";
        } else if (PsiForeachStatement.class == statementClass) {
            return "forEach";
        } else if (PsiWhileStatement.class == statementClass) {
            return "while";
        } else if (PsiSwitchStatement.class == statementClass) {
            return "switch";
        }
        throw new InvalidParameterException(String.format("Invalid statement instance %s. Supported instances are: %s", statementClass, SUPPORTED_STATEMENT_CLASSES));
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.NO_CONDITIONAL_LOGIC);
    }
}
