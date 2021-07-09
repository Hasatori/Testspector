package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.search.ElementSearchEngine;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.common.search.ElementSearchResult;
import com.testspector.model.checking.java.common.search.QueriesRepository;
import com.testspector.model.checking.java.junit.JUnitConstants;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class AssertionCountJUnitCheckingStrategy implements BestPracticeCheckingStrategy<PsiMethod> {

    protected final ElementSearchEngine elementSearchEngine;
    protected final JavaContextIndicator contextIndicator;
    protected final JavaMethodResolver methodResolver;

    public AssertionCountJUnitCheckingStrategy(ElementSearchEngine elementSearchEngine, JavaContextIndicator contextIndicator, JavaMethodResolver methodResolver) {
        this.elementSearchEngine = elementSearchEngine;
        this.contextIndicator = contextIndicator;
        this.methodResolver = methodResolver;
    }

    protected ElementSearchResult<PsiMethodCallExpression> removeGroupedAssertions(ElementSearchResult<PsiMethodCallExpression> allAssertionsSearch) {
        List<PsiMethodCallExpression> allElementsOfTheCurrentLevel = new ArrayList<>(allAssertionsSearch.getElementsOfCurrentLevel());
        List<PsiMethodCallExpression> toRemove = new ArrayList<>();

        for (PsiMethodCallExpression assertion : allAssertionsSearch.getElementsOfCurrentLevel()) {
            toRemove.addAll(elementSearchEngine.findByQuery(assertion, QueriesRepository.FIND_ALL_ASSERTION_METHOD_CALL_EXPRESSIONS)
                    .getElementsOfCurrentLevel());
        }
        allElementsOfTheCurrentLevel.removeAll(toRemove);
        List<Pair<PsiReferenceExpression, ElementSearchResult<PsiMethodCallExpression>>> referencedElements = new ArrayList<>();
        for (Pair<PsiReferenceExpression, ElementSearchResult<PsiMethodCallExpression>> referencedResult : allAssertionsSearch.getReferencedResults()) {
            ElementSearchResult<PsiMethodCallExpression> newReferencedResult = removeGroupedAssertions(referencedResult.getRight());
            referencedElements.add(Pair.of(referencedResult.getLeft(), newReferencedResult));
        }
        return new ElementSearchResult<>(referencedElements, allElementsOfTheCurrentLevel);
    }

    protected Predicate<PsiMethodCallExpression> isAssertionMethodFrom(String qualifiedName) {
        return psiMethodCallExpression -> Optional.of(psiMethodCallExpression.getMethodExpression())
                .map(psiClass -> psiClass.getQualifiedName().contains(qualifiedName))
                .orElse(false);
    }

    protected boolean containsHamcrestAssertion(List<PsiMethodCallExpression> allAssertionMethods) {
        return allAssertionMethods
                .stream()
                .anyMatch(isAssertionMethodFrom(JUnitConstants.HAMCREST_ASSERTIONS_CLASS_PATH));
    }

    protected boolean isJUnit5TestMethod(PsiMethod testMethod) {
        return Arrays
                .stream(testMethod.getAnnotations())
                .anyMatch(psiAnnotation ->
                        JUnitConstants.JUNIT5_TEST_QUALIFIED_NAMES.contains(psiAnnotation.getQualifiedName()));
    }

}
