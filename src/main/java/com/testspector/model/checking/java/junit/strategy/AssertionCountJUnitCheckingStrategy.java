package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.*;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.RelatedElementWrapper;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaElementResolver;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.junit.JUnitConstants;
import com.testspector.model.enums.BestPractice;

import java.util.*;
import java.util.function.Predicate;

public class AssertionCountJUnitCheckingStrategy implements BestPracticeCheckingStrategy<PsiMethod> {

    private final JavaElementResolver elementResolver;
    private final JavaContextIndicator contextResolver;
    private final JavaMethodResolver methodResolver;

    public static final List<String> GROUP_ASSERTION_NAMES = Collections.unmodifiableList(Arrays.asList(
            "assertAll"
    ));

    public AssertionCountJUnitCheckingStrategy(JavaElementResolver elementResolver, JavaContextIndicator contextResolver, JavaMethodResolver methodResolver) {
        this.elementResolver = elementResolver;
        this.contextResolver = contextResolver;
        this.methodResolver = methodResolver;
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiMethod method) {
        return checkBestPractices(Collections.singletonList(method));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiMethod> methods) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        for (PsiMethod method : methods) {
            List<PsiMethodCallExpression> allAssertionMethods = elementResolver
                    .allChildrenOfType(method, PsiMethodCallExpression.class, (psiMethodCallExpression -> methodResolver.assertionMethod(psiMethodCallExpression).isPresent()), contextResolver.isInTestContext());
            allAssertionMethods = removeGroupedAssertions(allAssertionMethods);
            PsiIdentifier methodIdentifier = method.getNameIdentifier();
            if (allAssertionMethods.isEmpty()) {
                bestPracticeViolations.add(new BestPracticeViolation(
                        String.format("%s#%s", method.getContainingClass().getQualifiedName(), method.getName()),
                        method,
                        methodIdentifier != null ? methodIdentifier.getTextRange() : method.getTextRange(),
                        "Test should contain at least one assertion method!",
                        BestPractice.AT_LEAST_ONE_ASSERTION
                ));
            }
            if (allAssertionMethods.size() > 1) {
                List<String> hints = new ArrayList<>();
                String message = "Test should contain only one assertion method!";
                if (Arrays.stream(method.getAnnotations()).anyMatch(psiAnnotation -> JUnitConstants.JUNIT5_TEST_QUALIFIED_NAMES.contains(psiAnnotation.getQualifiedName()))) {
                    hints.add(String.format("You are using JUnit5 so it can be solved by wrapping multiple assertions into %s.assertAll() method", JUnitConstants.JUNIT5_ASSERTIONS_CLASS_PATH));
                }
                if (allAssertionMethods.stream().anyMatch(isAssertionMethodFrom(JUnitConstants.HAMCREST_ASSERTIONS_CLASS_PATH))) {
                    hints.add("You can use hamcrest org.hamcrest.core.Every or org.hamcrest.core.AllOf matchers");
                }

                bestPracticeViolations.add(new BestPracticeViolation(
                        String.format("%s#%s", method.getContainingClass().getQualifiedName(), method.getName()),
                        method,
                        methodIdentifier != null ? methodIdentifier.getTextRange() : method.getTextRange(),
                        message,
                        hints,
                        BestPractice.ONLY_ONE_ASSERTION,
                        createRelatedElements(method, allAssertionMethods)));
            }
        }
        return bestPracticeViolations;
    }

    private List<PsiMethodCallExpression> removeGroupedAssertions(List<PsiMethodCallExpression> allAssertions) {
        List<PsiMethodCallExpression> toRemove = new ArrayList<>();
        for (PsiMethodCallExpression assertion : allAssertions) {
            toRemove.addAll(elementResolver.allChildrenOfType(assertion, PsiMethodCallExpression.class, psiMethodCallExpression -> methodResolver.assertionMethod(psiMethodCallExpression).isPresent(), contextResolver.isInTestContext()));
        }
        allAssertions.removeAll(toRemove);
        return allAssertions;
    }

    private Predicate<PsiMethodCallExpression> isAssertionMethodFrom(String qualifiedName) {
        return psiMethodCallExpression -> Optional.of(psiMethodCallExpression.getMethodExpression())
                .map(psiClass -> psiClass.getQualifiedName().contains(qualifiedName))
                .orElse(false);
    }

    private List<RelatedElementWrapper> createRelatedElements(PsiMethod method, List<PsiMethodCallExpression> assertionMethods) {
        List<RelatedElementWrapper> result = new ArrayList<>();
        for (PsiMethodCallExpression assertionMethod : assertionMethods) {
            HashMap<PsiElement, String> elementNameHashMap = new HashMap<PsiElement, String>();
            Optional<PsiReferenceExpression> optionalPsiReferenceExpression = firstReferenceToAssertionMethod(method, assertionMethod);
            if (optionalPsiReferenceExpression.isPresent()) {
                elementNameHashMap.put(optionalPsiReferenceExpression.get(), "reference from test method");
                elementNameHashMap.put(assertionMethod, "final assertion call");
            } else {
                elementNameHashMap.put(assertionMethod, "reference in the test method");
            }
            result.add(new RelatedElementWrapper(assertionMethod.getText(), elementNameHashMap));
        }

        return result;
    }


    private Optional<PsiReferenceExpression> firstReferenceToAssertionMethod(PsiElement element, PsiMethodCallExpression psiMethodCallExpression) {
        List<PsiReferenceExpression> references = elementResolver.allChildrenOfType(element, PsiReferenceExpression.class);
        for (PsiReferenceExpression reference : references) {
            if (!elementResolver.allChildrenOfType(reference.getParent(), PsiMethodCallExpression.class, psiMethodCallExpression1 -> psiMethodCallExpression == psiMethodCallExpression1, contextResolver.isInTestContext()).isEmpty()) {
                return Optional.of(reference);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.AT_LEAST_ONE_ASSERTION);
    }
}
