package com.testspector.model.checking.java.junit.strategy;

import com.intellij.lang.jvm.annotation.JvmAnnotationClassValue;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTypesUtil;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.common.search.ElementSearchEngine;
import com.testspector.model.checking.java.common.search.ElementSearchResult;
import com.testspector.model.checking.java.common.search.QueriesRepository;
import com.testspector.model.checking.java.junit.JUnitConstants;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.testspector.model.checking.java.junit.JUnitConstants.JUNIT5_ASSERTIONS_CLASS_PATH;

public abstract class AssertionCountJUnitCheckingStrategy extends JUnitBestPracticeCheckingStrategy {


    public AssertionCountJUnitCheckingStrategy(ElementSearchEngine elementSearchEngine, JavaContextIndicator contextIndicator, JavaMethodResolver methodResolver) {
        super(elementSearchEngine, contextIndicator, methodResolver);
    }

    protected ElementSearchResult<PsiMethodCallExpression> removeGroupedAssertions(ElementSearchResult<PsiMethodCallExpression> allAssertionsSearch) {
        List<PsiMethodCallExpression> allElementsOfTheCurrentLevel = new ArrayList<>(allAssertionsSearch.getElementsOfCurrentLevel());
        List<PsiMethodCallExpression> toRemove = new ArrayList<>();

        for (PsiMethodCallExpression assertion : allAssertionsSearch.getElementsOfCurrentLevel()) {
            if (Optional.ofNullable(assertion.resolveMethod())
                    .filter(method ->
                            "assertAll".equals(method.getName()) &&
                                    Optional.ofNullable(method.getContainingClass())
                                            .map(PsiClass::getQualifiedName).stream()
                                            .anyMatch(name -> name.equals(JUNIT5_ASSERTIONS_CLASS_PATH)))
                    .isPresent()) {
                toRemove.addAll(elementSearchEngine.findByQuery(assertion, QueriesRepository.FIND_ALL_ASSERTION_METHOD_CALL_EXPRESSIONS)
                        .getElementsOfCurrentLevel());
            }
        }
        allElementsOfTheCurrentLevel.removeAll(toRemove);
        List<Pair<PsiReferenceExpression, ElementSearchResult<PsiMethodCallExpression>>> referencedElements = new ArrayList<>();
        for (Pair<PsiReferenceExpression, ElementSearchResult<PsiMethodCallExpression>> referencedResult : allAssertionsSearch.getReferencedResults()) {
            ElementSearchResult<PsiMethodCallExpression> newReferencedResult = removeGroupedAssertions(referencedResult.getRight());
            referencedElements.add(Pair.of(referencedResult.getLeft(), newReferencedResult));
        }
        return new ElementSearchResult<>(referencedElements, allElementsOfTheCurrentLevel);
    }

}
