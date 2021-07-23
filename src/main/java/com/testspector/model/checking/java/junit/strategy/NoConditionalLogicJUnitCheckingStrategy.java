package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.*;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.common.search.ElementSearchEngine;
import com.testspector.model.checking.java.common.search.ElementSearchResult;
import com.testspector.model.checking.java.common.search.ElementSearchResultUtils;
import com.testspector.model.checking.java.common.search.QueriesRepository;
import com.testspector.model.checking.java.junit.strategy.action.NavigateElementAction;
import com.testspector.model.enums.BestPractice;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.testspector.model.checking.java.junit.JUnitConstants.JUNIT5_PARAMETERIZED_TEST_ABSOLUTE_PATH;
import static com.testspector.model.checking.java.junit.JUnitConstants.JUNIT5_TEST_QUALIFIED_NAMES;

public class NoConditionalLogicJUnitCheckingStrategy extends JUnitBestPracticeCheckingStrategy {

    private static final String IF_STATEMENT_STRING = "if";
    private static final String FOR_STATEMENT_STRING = "for";
    private static final String FOR_EACH_STATEMENT_STRING = "forEach";
    private static final String WHILE_STATEMENT_STRING = "while";
    private static final String SWITCH_STATEMENT_STRING = "switch";
    private static final List<Class<? extends PsiStatement>> SUPPORTED_STATEMENT_CLASSES = List.of(PsiIfStatement.class, PsiWhileStatement.class, PsiSwitchStatement.class, PsiForStatement.class, PsiForeachStatement.class);
    private static final String DEFAULT_PROBLEM_DESCRIPTION_MESSAGE = "Conditional logic in the form of if, else, for, or while should not be part of part of the test code. " +
            "It generally increases the complexity of the test method, making it difficult to read and makes it very difficult to determine what is actually being tested.";

    public NoConditionalLogicJUnitCheckingStrategy(ElementSearchEngine elementSearchEngine, JavaContextIndicator contextIndicator, JavaMethodResolver methodResolver) {
        super(elementSearchEngine, contextIndicator, methodResolver);
    }


    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiMethod method) {
        return checkBestPractices(Collections.singletonList(method));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiMethod> methods) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();

        for (PsiMethod testMethod : methods) {
            ElementSearchResult<PsiStatement> statementsElementSearchResult = elementSearchEngine
                    .findByQuery(testMethod, QueriesRepository.FIND_ALL_CONDITIONAL_STATEMENTS);
            statementsElementSearchResult = ElementSearchResultUtils.filterResult(partOfAssertionMethod().negate(), statementsElementSearchResult);
            for (PsiStatement statement : statementsElementSearchResult.getElementsFromAllLevels()) {
                bestPracticeViolations.add(createBestPracticeViolation(testMethod, statement));
            }
            bestPracticeViolations.addAll(createBestPracticeViolation(statementsElementSearchResult));

        }
        return bestPracticeViolations;
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

    private BestPracticeViolation createBestPracticeViolation(PsiReference reference, List<PsiStatement> statements) {
        return new BestPracticeViolation(
                reference.getElement(),
                "Following method contains conditional logic. " + DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                getCheckedBestPractice().get(0),
                statements.stream()
                        .map(statement -> new NavigateElementAction(String.format("%s statement", statementString(statement)), statement))
                        .collect(Collectors.toList())
        );

    }

    private BestPracticeViolation createBestPracticeViolation(PsiMethod testMethod, PsiStatement conditionalStatement) {
        List<String> hints = new ArrayList<>();
        hints.add(String.format("Remove statements [ %s ] and create separate test scenario for each branch",
                SUPPORTED_STATEMENT_CLASSES
                        .stream()
                        .map(this::statementString).collect(Collectors.joining(", "))));
        if (areJUnit5ClassesAvailable(testMethod)) {
            hints.add(String.format("You are using JUnit5 so the problem can be solved by " +
                            "using data driven approach and generating each scenario using %s",
                    JUNIT5_PARAMETERIZED_TEST_ABSOLUTE_PATH));
        }
        return new BestPracticeViolation(
                conditionalStatement,
                DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                getCheckedBestPractice().get(0),
                new ArrayList<>(),
                hints
        );
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.NO_CONDITIONAL_LOGIC);
    }

}
