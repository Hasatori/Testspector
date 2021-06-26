package com.testspector.model.checking.java.junit.strategy;

import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.ElementSearchResult;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaElementResolver;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.enums.BestPractice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NoGlobalStaticPropertiesJUnitCheckingStrategy implements BestPracticeCheckingStrategy<PsiMethod> {

    private final JavaElementResolver elementResolver;
    private final JavaContextIndicator contextIndicator;

    private static final String DEFAULT_PROBLEM_DESCRIPTION_MESSAGE = "Global static properties should not be part of a test. " +
            "Tests are sharing the reference and if some of them would update" +
            " it it might influence behaviour of other tests.";

    private static final List<String> hints =  Arrays.asList(
            "If the property is immutable e.g.,String, Integer, Byte, Character" +
                    " etc. then you can add 'final' identifier so that tests can " +
                    "not change reference",
            "If the property is mutable then delete static modifier " +
                    "and make property reference unique for each test.");

    public NoGlobalStaticPropertiesJUnitCheckingStrategy(JavaElementResolver elementResolver, JavaMethodResolver methodResolver, JavaContextIndicator contextIndicator) {
        this.elementResolver = elementResolver;
        this.contextIndicator = contextIndicator;
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiMethod method) {
        return checkBestPractices(Collections.singletonList(method));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiMethod> methods) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();


        for (PsiMethod testMethod : methods) {
            ElementSearchResult<PsiField> staticPropertiesResult =
                    elementResolver
                            .allChildrenOfTypeMeetingConditionWithReferences(
                                    testMethod,
                                    PsiField.class,
                                    (psiField ->
                                            !(psiField instanceof PsiEnumConstant) && isStaticAndNotFinal().test(psiField)
                                    ),
                                   el-> (el instanceof PsiMethod || el instanceof PsiField) && contextIndicator.isInTestContext().test(el));
            for (PsiField staticProperty : staticPropertiesResult.getAllElements()) {
                bestPracticeViolations.add(createBestPracticeViolation(staticProperty));
            }

            bestPracticeViolations.addAll(createBestPracticeViolation(staticPropertiesResult));

        }

        return bestPracticeViolations;
    }

    private Predicate<PsiField> isStaticAndNotFinal() {
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
                null,
                hints);
    }



    private List<BestPracticeViolation> createBestPracticeViolation(ElementSearchResult<PsiField> elementSearchResult) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        elementSearchResult.getReferencedResults()
                .forEach(result -> {
                    List<PsiField> globalStaticProps = result.getRight().getAllElements();
                    if (result.getLeft().getParent() instanceof PsiMethodCallExpression && !globalStaticProps.isEmpty()){
                        bestPracticeViolations.add(createBestPracticeViolation(result.getLeft(),globalStaticProps));
                    } else if (!globalStaticProps.isEmpty()){
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
                null,
               hints);
    }

    private BestPracticeViolation createBestPracticeViolation(PsiReference reference, List<PsiField> staticProperties) {
        return new BestPracticeViolation(
                reference.getElement(),
                "Following method contains global static properties. "+ DEFAULT_PROBLEM_DESCRIPTION_MESSAGE,
                getCheckedBestPractice().get(0),
                staticProperties.stream().map(staticProperty -> new Action<BestPracticeViolation>() {
                    @Override
                    public String getName() {
                        return "Go to global static property in " + staticProperty.getContainingFile().getName() + "(line " + (PsiDocumentManager.getInstance(staticProperty.getProject()).getDocument(staticProperty.getContainingFile()).getLineNumber(staticProperty.getTextOffset())+1) + ")";
                    }

                    @Override
                    public void execute(BestPracticeViolation bestPracticeViolation) {
                        ((Navigatable) staticProperty.getNavigationElement()).navigate(true);
                    }
                }).collect(Collectors.toList())
        );

    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.NO_GLOBAL_STATIC_PROPERTIES);
    }
}
