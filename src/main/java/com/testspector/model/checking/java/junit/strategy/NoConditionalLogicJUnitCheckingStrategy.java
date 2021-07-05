package com.testspector.model.checking.java.junit.strategy;

import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.*;
import com.testspector.model.enums.BestPractice;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.testspector.model.checking.java.junit.JUnitConstants.JUNIT5_PARAMETERIZED_TEST_ABSOLUTE_PATH;
import static com.testspector.model.checking.java.junit.JUnitConstants.JUNIT5_TEST_QUALIFIED_NAMES;

public class NoConditionalLogicJUnitCheckingStrategy implements BestPracticeCheckingStrategy<PsiMethod> {

    private static final List<Class<? extends PsiStatement>> SUPPORTED_STATEMENT_CLASSES = Collections.unmodifiableList(Arrays.asList(
            PsiIfStatement.class,
            PsiWhileStatement.class,
            PsiSwitchStatement.class,
            PsiForStatement.class,
            PsiForeachStatement.class
    ));
    private static final String IF_STATEMENT_STRING = "if";
    private static final String FOR_STATEMENT_STRING = "for";
    private static final String FOR_EACH_STATEMENT_STRING = "forEach";
    private static final String WHILE_STATEMENT_STRING = "while";
    private static final String SWITCH_STATEMENT_STRING = "switch";

    private final JavaElementResolver elementResolver;
    private final JavaContextIndicator contextResolver;
    private final JavaMethodResolver methodResolver;
    private final ElementSearchQuery<PsiStatement> findAllConditionalStatements;
    private static final String DEFAULT_PROBLEM_DESCRIPTION_MESSAGE = "Conditional logic should not be part of the test " +
            "method, it makes test hard to understand, read and maintain.";

    public NoConditionalLogicJUnitCheckingStrategy(JavaElementResolver elementResolver, JavaContextIndicator contextResolver, JavaMethodResolver methodResolver) {
        this.elementResolver = elementResolver;
        this.contextResolver = contextResolver;
        this.methodResolver = methodResolver;
        findAllConditionalStatements = new ElementSearchQueryBuilder<PsiStatement>()
                .elementOfType(PsiStatement.class)
                .whereElement(isConditionalStatement())
                .whereReferences(el -> el instanceof PsiMethod && contextResolver.isInTestContext().test(el))
                .build();
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiMethod method) {
        return checkBestPractices(Collections.singletonList(method));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiMethod> methods) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();

        for (PsiMethod testMethod : methods) {

            ElementSearchResult<PsiStatement> statementsElementSearchResult = elementResolver
                    .allChildrenByQuery(testMethod, findAllConditionalStatements);
            List<PsiStatement> statements = statementsElementSearchResult
                    .getElementsFromAllLevels()
                    .stream()
                    .filter(partOfAssertionMethod().negate())
                    .collect(Collectors.toList());
            statements = statements.stream().distinct().collect(Collectors.toList());
            for (PsiStatement statement : statements) {
                bestPracticeViolations.add(createBestPracticeViolation(testMethod, statement));
            }

            bestPracticeViolations.addAll(createBestPracticeViolation(statementsElementSearchResult));

        }
        return bestPracticeViolations;
    }

    Predicate<PsiElement> methodInTestContext() {
        return (element) -> element instanceof PsiMethod && contextResolver.isInTestContext().test(element);
    }

    Predicate<PsiStatement> isConditionalStatement() {
        return psiStatement -> SUPPORTED_STATEMENT_CLASSES
                .stream()
                .anyMatch(supportedStatement ->
                        supportedStatement.isInstance(psiStatement));
    }

    Predicate<PsiStatement> partOfAssertionMethod() {
        return psiStatement -> {
            PsiElement element = psiStatement.getParent();
            while (element != null) {
                if (element instanceof PsiMethod) {
                    return methodResolver.assertionMethod((PsiMethod) element).isPresent();
                }
                element = element.getContext();
            }
            return false;
        };
    }

    private String statementString(PsiStatement statement) {
        if (statement instanceof PsiIfStatement) {
            return IF_STATEMENT_STRING;
        } else if (statement instanceof PsiForStatement) {
            return FOR_STATEMENT_STRING;
        } else if (statement instanceof PsiForeachStatement) {
            return FOR_EACH_STATEMENT_STRING;
        } else if (statement instanceof PsiWhileStatement) {
            return WHILE_STATEMENT_STRING;
        } else if (statement instanceof PsiSwitchStatement) {
            return SWITCH_STATEMENT_STRING;
        }
        throw new InvalidParameterException(
                String.format("Invalid statement instance %s. Supported instances are: %s",
                        statement.getClass(),
                        SUPPORTED_STATEMENT_CLASSES));
    }

    private String statementString(Class<? extends PsiStatement> statementClass) {
        if (PsiIfStatement.class == statementClass) {
            return IF_STATEMENT_STRING;
        } else if (PsiForStatement.class == statementClass) {
            return FOR_STATEMENT_STRING;
        } else if (PsiForeachStatement.class == statementClass) {
            return FOR_EACH_STATEMENT_STRING;
        } else if (PsiWhileStatement.class == statementClass) {
            return WHILE_STATEMENT_STRING;
        } else if (PsiSwitchStatement.class == statementClass) {
            return SWITCH_STATEMENT_STRING;
        }
        throw new InvalidParameterException(
                String.format("Invalid statement instance %s. Supported instances are: %s",
                        statementClass,
                        SUPPORTED_STATEMENT_CLASSES));
    }

    private List<BestPracticeViolation> createBestPracticeViolation(ElementSearchResult<PsiStatement> elementSearchResult) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        elementSearchResult.getReferencedResults()
                .forEach(result -> {
                    List<PsiStatement> conditionalStatements = result.getRight().getElementsFromAllLevels();
                    if (!conditionalStatements.isEmpty()) {
                        bestPracticeViolations.add(createBestPracticeViolation(result.getLeft(), conditionalStatements));
                    }
                    bestPracticeViolations.addAll(createBestPracticeViolation(result.getRight()));
                });
        return bestPracticeViolations;
    }

    private BestPracticeViolation createBestPracticeViolation(PsiReference reference, List<PsiStatement> tryStatements) {
        return new BestPracticeViolation(
                reference.getElement(),
                "Following method contains conditional logic. " + DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                getCheckedBestPractice().get(0),
                tryStatements.stream().map(tryStatement -> new Action<BestPracticeViolation>() {
                    @Override
                    public String getName() {
                        return String.format("Go to %s statement in file %s (line %s)"
                                , statementString(tryStatement),
                                tryStatement.getContainingFile().getName(),
                                (PsiDocumentManager.getInstance(tryStatement.getProject()).getDocument(tryStatement.getContainingFile()).getLineNumber(tryStatement.getTextOffset()) + 1));
                    }

                    @Override
                    public void execute(BestPracticeViolation bestPracticeViolation) {
                        ((Navigatable) tryStatement.getNavigationElement()).navigate(true);
                    }
                }).collect(Collectors.toList())
        );

    }

    private BestPracticeViolation createBestPracticeViolation(PsiMethod testMethod, PsiStatement conditionalStatement) {
        List<String> hints = new ArrayList<>();
        hints.add(String.format("Remove statements [ %s ] and create separate test scenario for each branch",
                SUPPORTED_STATEMENT_CLASSES
                        .stream()
                        .map(this::statementString).collect(Collectors.joining(", "))));
        hints.add("Acceptable place where conditional logic can be are custom assertions," +
                " where base on inputs we decide if we throw exception or not");
        if (methodResolver.methodHasAnyOfAnnotations(testMethod, JUNIT5_TEST_QUALIFIED_NAMES)) {
            hints.add(String.format("You are using JUnit5 so the problem can be solved by " +
                            "using data driven approach and generating each scenario using %s",
                    JUNIT5_PARAMETERIZED_TEST_ABSOLUTE_PATH));
        }
        return new BestPracticeViolation(
                conditionalStatement,
                DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                getCheckedBestPractice().get(0),
                Collections.singletonList(new Action<>() {
                                              @Override
                                              public String getName() {
                                                  return "Navigate " + conditionalStatement.toString() + " in " + conditionalStatement.getContainingFile().getName();
                                              }

                                              @Override
                                              public void execute(BestPracticeViolation bestPracticeViolation) {
                                                  Optional<PsiElement> optionalNavigationElement = Optional.ofNullable(bestPracticeViolation.getElement().getNavigationElement());
                                                  if (optionalNavigationElement.isPresent() && optionalNavigationElement.get() instanceof Navigatable
                                                          && ((Navigatable) optionalNavigationElement.get()).canNavigate()) {
                                                      ((Navigatable) optionalNavigationElement.get()).navigate(true);
                                                  }
                                              }
                                          }
                ),
                hints
        );
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.NO_CONDITIONAL_LOGIC);
    }

}
