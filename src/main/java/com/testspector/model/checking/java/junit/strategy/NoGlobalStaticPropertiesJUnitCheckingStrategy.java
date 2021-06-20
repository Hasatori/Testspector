package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.*;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
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
            List<PsiField> staticProperties = elementResolver
                    .allChildrenOfTypeMeetingConditionWithReferences(
                            testMethod,
                            PsiField.class,
                            (psiField ->
                                    !(psiField instanceof PsiEnumConstant)
                            ),
                            contextIndicator.isInTestContext())
                    .stream()
                    .filter(isStaticAndNotFinal())
                    .collect(Collectors.toList());
            PsiIdentifier methodIdentifier = testMethod.getNameIdentifier();
            for (PsiField staticProperty : staticProperties) {
                bestPracticeViolations.add(createBestPracticeViolation(staticProperty));
            }

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
                "Global static properties should not be part of a test. " +
                        "Tests are sharing the reference and if some of them would update" +
                        " it it might influence behaviour of other tests.",
                getCheckedBestPractice().get(0),
                null,
                Arrays.asList(
                        "If the property is immutable e.g.,String, Integer, Byte, Character" +
                                " etc. then you can add 'final' identifier so that tests can " +
                                "not change reference",
                        "If the property is mutable then delete static modifier " +
                                "and make property reference unique for each test."));
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.NO_GLOBAL_STATIC_PROPERTIES);
    }
}
