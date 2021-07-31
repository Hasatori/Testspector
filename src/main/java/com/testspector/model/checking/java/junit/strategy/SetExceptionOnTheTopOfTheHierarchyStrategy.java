package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.common.search.ElementSearchEngine;
import com.testspector.model.checking.java.common.search.ElementSearchResult;
import com.testspector.model.checking.java.common.search.ElementSearchResultUtils;
import com.testspector.model.checking.java.common.search.QueriesRepository;
import com.testspector.model.checking.java.junit.strategy.action.MakeMethodAndReferencesCatchGeneralLevelException;
import com.testspector.model.enums.BestPractice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class SetExceptionOnTheTopOfTheHierarchyStrategy extends JUnitBestPracticeCheckingStrategy {

    private static final String DEFAULT_PROBLEM_DESCRIPTION_MESSAGE = "Test methods should throw exception on the top of the hierarchy. " +
            "This approach ensures easier maintainability of the tests, because if the production code is modified and a certain method starts throwing a " +
            "different type of exception, the tests will not need to be changed as the top level exception will catch this case.";

    public SetExceptionOnTheTopOfTheHierarchyStrategy(ElementSearchEngine elementSearchEngine, JavaContextIndicator contextIndicator, JavaMethodResolver methodResolver) {
        super(elementSearchEngine, contextIndicator, methodResolver);
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiMethod checkedElement) {
        return checkBestPractices(Collections.singletonList(checkedElement));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiMethod> checkedMethods) {
        List<BestPracticeViolation> violations = new ArrayList<>();

        for (PsiMethod testMethod : checkedMethods) {

            if (doestNotThrowGeneralLevelException().test(testMethod)){
                violations.add(createBestPracticeViolation(testMethod));
            }

            ElementSearchResult<PsiMethod> allMethodsResult = elementSearchEngine.findByQuery(testMethod, QueriesRepository.FIND_ALL_METHODS_THAT_THROWS_EXCEPTIONS_IN_TESTS);
            allMethodsResult = ElementSearchResultUtils.filterResult(doestNotThrowGeneralLevelException(),allMethodsResult);
            for (PsiMethod method : allMethodsResult.getElementsFromAllLevels()) {
                violations.add(createBestPracticeViolation(method));
            }
           violations.addAll(createBestPracticeViolation(allMethodsResult));

        }

        return violations;
    }

    private Predicate<PsiMethod> doestNotThrowGeneralLevelException() {
        return method -> Arrays.stream(method.getThrowsList().getReferenceElements())
                .anyMatch(psiJavaCodeReferenceElement -> !"java.lang.Exception".equals(psiJavaCodeReferenceElement.getQualifiedName()));
    }

    private BestPracticeViolation createBestPracticeViolation(PsiMethod psiMethod){
        return new BestPracticeViolation(
                psiMethod.getThrowsList(),
                DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                getCheckedBestPractice().get(0),
                Collections.singletonList(new MakeMethodAndReferencesCatchGeneralLevelException(psiMethod))
        );
    }

    private List<BestPracticeViolation> createBestPracticeViolation(ElementSearchResult<PsiMethod> elementSearchResult) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        elementSearchResult.getReferencedResults()
                .forEach(result -> {
                    List<PsiMethod> methods = result.getRight().getElementsFromAllLevels();
                    if (result.getLeft().getParent() instanceof PsiMethodCallExpression && !methods.isEmpty()) {
                        bestPracticeViolations.add(createBestPracticeViolation(PsiTreeUtil.getChildOfType(PsiTreeUtil.getChildOfType(result.getLeft().getParent(), PsiReferenceExpression.class), PsiIdentifier.class)));
                    }
                    bestPracticeViolations.addAll(createBestPracticeViolation(result.getRight()));
                });
        return bestPracticeViolations;
    }

    private BestPracticeViolation createBestPracticeViolation(PsiElement element) {
        return new BestPracticeViolation(
                element,
                "Following method contains code that breaks best practice. " + DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                getCheckedBestPractice().get(0),
                new ArrayList<>()
        );

    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.SET_EXCEPTION_CLASS_ON_THE_TOP_OF_THE_HIERARCHY);
    }
}
