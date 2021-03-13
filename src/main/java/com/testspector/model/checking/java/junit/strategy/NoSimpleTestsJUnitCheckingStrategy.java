package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.crate.BestPracticeViolation;
import com.testspector.model.checking.crate.RelatedElementWrapper;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaElementResolver;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.enums.BestPractice;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NoSimpleTestsJUnitCheckingStrategy implements BestPracticeCheckingStrategy<PsiMethod> {


    private final JavaElementResolver elementResolver;
    private final JavaMethodResolver methodResolver;
    private final JavaContextIndicator contextIndicator;

    public NoSimpleTestsJUnitCheckingStrategy(JavaElementResolver elementResolver, JavaMethodResolver methodResolver, JavaContextIndicator contextIndicator) {
        this.elementResolver = elementResolver;
        this.methodResolver = methodResolver;
        this.contextIndicator = contextIndicator;
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiMethod method) {
        return checkBestPractices(Collections.singletonList(method));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiMethod> methods) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();

        for (PsiMethod method : methods) {
            List<PsiMethod> simpleMethods = methodResolver.allTestedMethods(method)
                    .stream()
                    .filter(isTestedMethodSimple(method))
                    .collect(Collectors.toList());
            PsiIdentifier methodIdentifier = method.getNameIdentifier();
            if (simpleMethods.size() > 0) {
                bestPracticeViolations.add(new BestPracticeViolation(
                        String.format("%s#%s", method.getContainingClass().getQualifiedName(), method.getName()),
                        method,
                        methodIdentifier != null ? methodIdentifier.getTextRange() : method.getTextRange(),
                        "Simple tests for getter and setters are redundant and can be deleted. These methods do not represent complex logic and therefore does not need to be tested.",
                        Collections.singletonList("Delete simple tests"),
                        this.getCheckedBestPractice().get(0),
                        createRelatedElements(method, simpleMethods)));
            }
        }
        return bestPracticeViolations;
    }

    private Predicate<PsiMethod> isTestedMethodSimple(PsiMethod testMethod) {
        return testedMethod -> {
            if (methodResolver.isGetter(testedMethod)) {
                return elementResolver.allChildrenOfType(testMethod, PsiReferenceExpression.class)
                        .stream()
                        .filter(psiReferenceExpression -> psiReferenceExpression.resolve() == testedMethod).findFirst()
                        .map(psiReferenceExpression ->
                                elementResolver.allChildrenOfType(psiReferenceExpression, PsiReferenceExpression.class)
                                        .stream().map(PsiReferenceExpression::resolve)
                                        .filter(element -> element instanceof PsiLocalVariable)
                                        .map(element -> (PsiLocalVariable) element)
                                        .map(PsiVariable::getType)
                                        .filter(psiType -> PsiTypesUtil.getPsiClass(psiType) == testedMethod.getContainingClass())
                                        .findFirst()
                                        .orElse(null))
                        .isPresent();
            }
            return false;
        };
    }


    private List<RelatedElementWrapper> createRelatedElements(PsiMethod method, List<PsiMethod> simpleMethods) {
        List<RelatedElementWrapper> result = new ArrayList<>();
        for (PsiMethod simpleMethod : simpleMethods) {
            HashMap<PsiElement, String> elementNameHashMap = new HashMap<>();
            Optional<PsiReferenceExpression> optionalPsiReferenceExpression = firstReferenceToSimpleMethod(method, simpleMethod);
            if (optionalPsiReferenceExpression.isPresent()) {
                elementNameHashMap.put(optionalPsiReferenceExpression.get(), "method reference in test");
            }
            elementNameHashMap.put(simpleMethod, "method definition in production code");

            result.add(new RelatedElementWrapper(simpleMethod.getName(), elementNameHashMap));
        }

        return result;
    }


    private Optional<PsiReferenceExpression> firstReferenceToSimpleMethod(PsiElement element, PsiMethod simpleMethod) {
        List<PsiReferenceExpression> references = elementResolver.allChildrenOfType(element, PsiReferenceExpression.class);
        for (PsiReferenceExpression reference : references) {
            if (!elementResolver.allChildrenOfType(
                    reference.getParent(),
                    PsiMethodCallExpression.class,
                    psiMethodCallExpression -> psiMethodCallExpression.resolveMethod() != null && psiMethodCallExpression.resolveMethod() == simpleMethod,
                    contextIndicator.isInTestContext()).isEmpty()) {
                return Optional.of(reference);
            }
        }
        return Optional.empty();
    }


    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.NO_SIMPLE_TESTS);
    }


}
