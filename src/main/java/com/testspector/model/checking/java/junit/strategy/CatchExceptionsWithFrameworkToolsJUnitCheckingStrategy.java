package com.testspector.model.checking.java.junit.strategy;

import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.*;
import com.testspector.model.enums.BestPractice;

import java.util.*;
import java.util.stream.Collectors;

import static com.testspector.model.checking.java.junit.JUnitConstants.*;

public class CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy implements BestPracticeCheckingStrategy<PsiMethod> {

    private final JavaElementResolver elementResolver;
    private final JavaContextIndicator contextResolver;
    private final JavaMethodResolver methodResolver;
    private final ElementSearchQuery<PsiTryStatement> findAllTryStatements;
    private static final String DEFAULT_PROBLEM_DESCRIPTION_MESSAGE = "Tests should not contain try catch block. " +
            "These blocks are redundant and make test harder to read and understand. " +
            "In some cases it might even lead to never failing tests " +
            "if we are not handling the exception properly.";

    public CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy(JavaElementResolver elementResolver, JavaContextIndicator contextResolver, JavaMethodResolver methodResolver) {
        this.elementResolver = elementResolver;
        this.contextResolver = contextResolver;
        this.methodResolver = methodResolver;
        findAllTryStatements = new ElementSearchQueryBuilder<PsiTryStatement>()
                .elementOfType(PsiTryStatement.class)
                .whereReferences(el -> el instanceof PsiMethod && contextResolver.isInTestContext().test(el))
                .build();
    }


    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiMethod method) {
        return this.checkBestPractices(Collections.singletonList(method));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiMethod> methods) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        for (PsiMethod testMethod : methods) {

            ElementSearchResult<PsiTryStatement> psiTryStatementsElementSearchResult = elementResolver
                    .allChildrenByQuery(testMethod, findAllTryStatements);
            for (PsiTryStatement psiTryStatement : psiTryStatementsElementSearchResult.getElementsFromAllLevels()) {
                bestPracticeViolations.add(createBestPracticeViolation(testMethod, psiTryStatement));
            }
            bestPracticeViolations.addAll(createBestPracticeViolation(psiTryStatementsElementSearchResult));


        }
        return bestPracticeViolations;
    }


    private BestPracticeViolation createBestPracticeViolation(PsiMethod testMethod, PsiTryStatement psiTryStatement) {
        List<String> hints = new ArrayList<>();
        hints.add("If catching an exception is not part of a test then just delete it.");
        if (methodResolver.methodHasAnyOfAnnotations(testMethod, JUNIT5_TEST_QUALIFIED_NAMES)) {
            hints.add(String.format("If catching an exception is part of a test " +
                            "then since you are using JUnit5 it can be solved by using %s.assertThrows() method"
                    , JUNIT5_ASSERTIONS_CLASS_PATH));
        }
        if (methodResolver.methodHasAnyOfAnnotations(testMethod, JUNIT4_TEST_QUALIFIED_NAMES)) {
            hints.add(String.format(
                    "If catching an exception is part of a test then since you are using JUnit4 " +
                            "it can be solved by using @%s.Test(expected = Exception.class) for the test method"
                    , JUNIT4_ASSERTIONS_CLASS_PATH));
        }
        return new BestPracticeViolation(
                psiTryStatement,
                DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                this.getCheckedBestPractice().get(0),
                Collections.singletonList(new Action<>() {
                    @Override
                    public String getName() {
                        if (Optional.ofNullable(psiTryStatement.getTryBlock()).map(tryBlock -> tryBlock.getStatements().length > 0).isPresent()) {
                            return "Remove try catch block and catch exception at method level";
                        }
                        return "Remove try catch block";
                    }

                    @Override
                    public void execute(BestPracticeViolation bestPracticeViolation) {
                        if (Optional.ofNullable(psiTryStatement.getTryBlock()).map(tryBlock -> tryBlock.getStatements().length > 0).isPresent()) {
                            Optional.ofNullable(PsiTreeUtil.getParentOfType(psiTryStatement, PsiMethod.class)).ifPresent(method -> addThrowsListToAllReferencedMethods(PsiElementFactory.getInstance(psiTryStatement.getProject()), method));
                            Optional.ofNullable(psiTryStatement.getTryBlock()).map(PsiCodeBlock::getLBrace).ifPresent(PsiElement::delete);
                            Optional.ofNullable(psiTryStatement.getTryBlock()).map(PsiCodeBlock::getRBrace).ifPresent(PsiElement::delete);
                            psiTryStatement.replace(psiTryStatement.getTryBlock());
                        } else {
                            psiTryStatement.delete();
                        }


                    }
                }));
    }

    private void addThrowsListToAllReferencedMethods(PsiElementFactory psiElementFactory, PsiMethod method) {
        method.getThrowsList().replace(psiElementFactory.createReferenceList(new PsiJavaCodeReferenceElement[]{psiElementFactory.createReferenceFromText("Exception", null)}));
        ReferencesSearch.search(method)
                .findAll()
                .stream().map(reference -> PsiTreeUtil.getParentOfType(reference.getElement(), PsiMethod.class))
                .filter(Objects::nonNull)
                .forEach(met -> addThrowsListToAllReferencedMethods(psiElementFactory, met));
    }

    private List<BestPracticeViolation> createBestPracticeViolation(ElementSearchResult<PsiTryStatement> elementSearchResult) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        elementSearchResult.getReferencedResults()
                .forEach(result -> {
                    List<PsiTryStatement> tryStatements = result.getRight().getElementsFromAllLevels();
                    if (!tryStatements.isEmpty()) {
                        bestPracticeViolations.add(createBestPracticeViolation(result.getLeft(), tryStatements));
                    }
                    bestPracticeViolations.addAll(createBestPracticeViolation(result.getRight()));
                });
        return bestPracticeViolations;
    }

    private BestPracticeViolation createBestPracticeViolation(PsiReference reference, List<PsiTryStatement> tryStatements) {
        return new BestPracticeViolation(
                reference.getElement(),
                "Following method breaks best practice. " + DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                getCheckedBestPractice().get(0),
                tryStatements.stream().map(tryStatement -> new Action<BestPracticeViolation>() {
                    @Override
                    public String getName() {
                        return "Go to try catch statement in "
                                + tryStatement.getContainingFile().getName() + "(line " +
                                (Optional.ofNullable(PsiDocumentManager.getInstance(tryStatement.getProject()).getDocument(tryStatement.getContainingFile()))
                                        .map(file -> file.getLineNumber(tryStatement.getTextOffset()))
                                        .orElse(0) + 1) + ")";
                    }

                    @Override
                    public void execute(BestPracticeViolation bestPracticeViolation) {
                        ((Navigatable) tryStatement.getNavigationElement()).navigate(true);
                    }
                }).collect(Collectors.toList())
        );

    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.CATCH_TESTED_EXCEPTIONS_USING_FRAMEWORK_TOOLS);
    }
}
