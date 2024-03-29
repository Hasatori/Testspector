package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.common.search.ElementSearchEngine;
import com.testspector.model.checking.java.common.search.ElementSearchResult;
import com.testspector.model.checking.java.common.search.QueriesRepository;
import com.testspector.model.checking.java.junit.strategy.action.MakeFieldFinal;
import com.testspector.model.checking.java.junit.strategy.action.RemoveStaticModifierFromField;
import com.testspector.model.checking.java.junit.strategy.action.NavigateElementAction;
import com.testspector.model.enums.BestPractice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.testspector.model.checking.java.common.search.ElementSearchResultUtils.filterResult;

public class NoGlobalStaticPropertiesJUnitCheckingStrategy extends JUnitBestPracticeCheckingStrategy {

    private static final String DEFAULT_PROBLEM_DESCRIPTION_MESSAGE = "Global static properties should not be part of a test. " +
            "Tests are sharing the reference and if some of them would update" +
            " it it might influence behaviour of other tests.";

    private static final List<String> hints = Arrays.asList(
            "If the property is immutable e.g.,String, Integer, Byte, Character" +
                    " etc. then you can add 'final' identifier so that tests can " +
                    "not change reference",
            "If the property is mutable then delete static modifier " +
                    "and make property reference unique for each test.");

    public NoGlobalStaticPropertiesJUnitCheckingStrategy(ElementSearchEngine elementSearchEngine, JavaContextIndicator contextIndicator, JavaMethodResolver methodResolver) {
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

            ElementSearchResult<PsiField> staticPropertiesResult = elementSearchEngine.findByQuery(
                    testMethod, QueriesRepository.FIND_ALL_STATIC_PROPS);
            staticPropertiesResult = filterResult(isStaticAndNotFinal(), staticPropertiesResult);
            for (PsiField staticProperty : staticPropertiesResult.getElementsFromAllLevels()) {
                bestPracticeViolations.add(createBestPracticeViolation(staticProperty));
            }

            bestPracticeViolations.addAll(createBestPracticeViolation(staticPropertiesResult));

        }

        return bestPracticeViolations;
    }

    private static Predicate<PsiField> isStaticAndNotFinal() {
        return psiField -> {
            PsiModifierList modifierList = psiField.getModifierList();
            if (modifierList != null) {
                return modifierList.hasModifierProperty(PsiModifier.STATIC) && !modifierList.hasExplicitModifier(PsiModifier.FINAL);
            }
            return false;
        };
    }

    private BestPracticeViolation createBestPracticeViolation(PsiField staticProperty) {
        return new BestPracticeViolation(
                staticProperty,
                DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                getCheckedBestPractice().get(0),
                Arrays.asList(new RemoveStaticModifierFromField(staticProperty),new MakeFieldFinal(staticProperty)),
                hints);
    }


    private List<BestPracticeViolation> createBestPracticeViolation(ElementSearchResult<PsiField> elementSearchResult) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        elementSearchResult.getReferencedResults()
                .forEach(result -> {
                    List<PsiField> globalStaticProps = result.getRight().getElementsFromAllLevels();
                    if (result.getLeft().getParent() instanceof PsiMethodCallExpression && !globalStaticProps.isEmpty()) {
                        bestPracticeViolations.add(createBestPracticeViolation(getMethodCallExpressionIdentifier((PsiMethodCallExpression) result.getLeft().getParent()), globalStaticProps));
                    } else if (!globalStaticProps.isEmpty()) {
                        bestPracticeViolations.add(createBestPracticeViolation(result.getLeft()));
                    }
                    bestPracticeViolations.addAll(createBestPracticeViolation(result.getRight()));
                });
        return bestPracticeViolations;
    }

    private BestPracticeViolation createBestPracticeViolation(PsiReference reference) {
        return new BestPracticeViolation(
                reference.getElement(),
                DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                getCheckedBestPractice().get(0),
                Arrays.asList(new RemoveStaticModifierFromField((PsiField) reference.resolve()), new MakeFieldFinal((PsiField) reference.resolve())),
                hints);
    }

    private BestPracticeViolation createBestPracticeViolation(PsiElement element, List<PsiField> staticProperties) {
        return new BestPracticeViolation(
                element,
                "Following method contains global static properties. " + DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                getCheckedBestPractice().get(0),
                staticProperties.stream()
                        .map(staticProperty -> new NavigateElementAction("global static property", staticProperty))
                        .collect(Collectors.toList())
        );

    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.NO_GLOBAL_STATIC_PROPERTIES);
    }
}
