package com.testspector.model.checking.java.junit.strategy;

import com.intellij.lang.java.JavaFindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.searches.ReferencesSearch;
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

import static com.testspector.model.checking.java.junit.JUnitConstants.JUNIT_ALL_TEST_QUALIFIED_NAMES;

public class NoDeadCodeJUnitCheckingStrategy implements BestPracticeCheckingStrategy<PsiElement> {
    protected final JavaElementResolver elementResolver;
    protected final JavaContextIndicator contextIndicator;
    protected final JavaMethodResolver methodResolver;


    public NoDeadCodeJUnitCheckingStrategy(JavaElementResolver elementResolver, JavaContextIndicator contextIndicator, JavaMethodResolver methodResolver) {
        this.elementResolver = elementResolver;
        this.contextIndicator = contextIndicator;
        this.methodResolver = methodResolver;
    }


    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiElement checkedElement) {
        return checkBestPractices(Collections.singletonList(checkedElement));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiElement> checkedElements) {
        List<BestPracticeViolation> result = new ArrayList<>();
        JavaFindUsagesProvider javaFindUsagesProvider = new JavaFindUsagesProvider();
        for (PsiElement checkedElement : checkedElements) {
            elementResolver.allChildrenOfTypeMeetingCondition(
                    checkedElement,
                    PsiField.class,
                    element -> ReferencesSearch.search(element).findFirst() == null)
                    .forEach(element -> {
                        result.add(new BestPracticeViolation(
                                checkedElement,
                                "Dead code should not be part of any tests",
                                BestPractice.NO_DEAD_CODE,
                                Collections.singletonList(element),
                                null
                        ));

                    });
        }
        return result;
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.NO_DEAD_CODE);
    }


    private boolean isJunitTestMethod(PsiMethod method) {
        return Arrays
                .stream((method).getAnnotations())
                .anyMatch(psiAnnotation -> JUNIT_ALL_TEST_QUALIFIED_NAMES.contains(psiAnnotation.getQualifiedName()));
    }
}
