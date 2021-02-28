package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.JavaElementHelper;
import com.testspector.model.checking.java.junit.JUnitConstants;
import com.testspector.model.enums.BestPractice;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.testspector.model.checking.java.junit.JUnitConstants.ASSERTION_CLASS_PATHS;

public class NoSimpleTestsJUnitCheckingStrategy implements BestPracticeCheckingStrategy {


    private final JavaElementHelper javaElementHelper;

    public NoSimpleTestsJUnitCheckingStrategy(JavaElementHelper javaElementHelper) {
        this.javaElementHelper = javaElementHelper;
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiElement psiElement) {
        return checkBestPractices(Collections.singletonList(psiElement));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiElement> psiElements) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        List<PsiMethod> methods = javaElementHelper.getMethodsFromElementByAnnotations(psiElements, JUnitConstants.JUNIT_ALL_TEST_QUALIFIED_NAMES);
        for (PsiMethod method : methods) {
            List<PsiMethodCallExpression> assertions = getAssertionsMethods(method);
            List<PsiMethod> simpleMethods = assertions.stream()
                    .map(element -> getAllChildrenOfTypeWithReferences(element, PsiMethodCallExpression.class))
                    .flatMap(Collection::stream)
                    .map(PsiCall::resolveMethod)
                    .filter(Objects::nonNull)
                    .filter(javaElementHelper::isInProductionCodeContext)
                    .filter(isMethodSimple(method))
                    .collect(Collectors.toList());
            PsiIdentifier methodIdentifier = method.getNameIdentifier();
            if (simpleMethods.size() > 0) {
                bestPracticeViolations.add(new BestPracticeViolation(
                        method,
                        methodIdentifier != null ? methodIdentifier.getTextRange() : method.getTextRange(),
                        "No simple tests",
                        Collections.singletonList("Do not tests this"),
                        this.getCheckedBestPractice().get(0),
                        simpleMethods.stream().map(assertion -> (PsiElement) assertion).collect(Collectors.toList())));
            }
        }
        return bestPracticeViolations;
    }

    private Predicate<PsiMethod> isMethodSimple(PsiMethod testingMethod) {
        return method -> {
            if (isSimpleGetter().test(method)) {
                PsiClass getterReferenceClass = PsiTypesUtil.getPsiClass(javaElementHelper.getAllChildrenOfType(testingMethod, PsiReferenceExpression.class)
                        .stream()
                        .filter(psiReferenceExpression -> psiReferenceExpression.resolve() == method).findFirst()
                        .map(psiReferenceExpression -> javaElementHelper.getImmediateChildrenOfType(psiReferenceExpression, PsiReferenceExpression.class))
                        .map(psiReferenceExpressions -> psiReferenceExpressions.size() >= 1 ? psiReferenceExpressions.get(0) : null)
                        .map(PsiReference::resolve)
                        .filter(element -> element instanceof PsiLocalVariable)
                        .map(element -> (PsiLocalVariable) element)
                        .map(PsiVariable::getType).orElse(null));
                return getterReferenceClass == method.getContainingClass();
            }
            return false;
        };
    }

    private Predicate<PsiMethod> isSimpleGetter() {
        return method -> {
            Optional<PsiElement> returnCandidate = javaElementHelper.getFirstChildIgnoring(Objects.requireNonNull(method.getBody()), Arrays.asList(PsiJavaToken.class, PsiWhiteSpace.class));
            if (returnCandidate.isPresent() && returnCandidate.get() instanceof PsiReturnStatement) {
                return javaElementHelper.getImmediateChildrenOfType(returnCandidate.get(), PsiReferenceExpression.class)
                        .stream()
                        .map(PsiReference::resolve)
                        .anyMatch(element -> element instanceof PsiField);
            }
            return false;
        };
    }


    private List<PsiMethodCallExpression> getAssertionsMethods(PsiElement psiElement) {
        List<PsiMethodCallExpression> assertionMethods = new ArrayList<>();
        for (PsiElement child : psiElement.getChildren()) {
            if (child instanceof PsiMethodCallExpression) {
                PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) child;
                if (isAssertionMethod().test(methodCallExpression)) {
                    assertionMethods.add(methodCallExpression);
                } else {
                    PsiMethod referencedMethod = methodCallExpression.resolveMethod();
                    if (referencedMethod != null && javaElementHelper.isInTestContext(referencedMethod)) {
                        assertionMethods.addAll(getAssertionsMethods(referencedMethod));
                    }
                }

            }
            assertionMethods.addAll(getAssertionsMethods(child));
        }
        return assertionMethods;
    }


    private Predicate<PsiMethodCallExpression> isAssertionMethod() {
        return psiMethodCallExpression -> Optional.ofNullable(psiMethodCallExpression.resolveMethod())
                .map(PsiJvmMember::getContainingClass)
                .map(psiClass -> ASSERTION_CLASS_PATHS.contains(psiClass.getQualifiedName()))
                .orElse(false);
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.NO_SIMPLE_TESTS);
    }

    private <T extends PsiElement> List<T> getAllChildrenOfTypeWithReferences(PsiElement psiElement, Class<T> elementType) {
        List<T> result = new ArrayList<>();
        for (PsiElement child : psiElement.getChildren()) {
            if (elementType.isInstance(child)) {
                result.add(elementType.cast(child));
            }
            if (child instanceof PsiReferenceExpression) {
                PsiElement referencedElement = ((PsiReferenceExpression) child).resolve();
                if (referencedElement != null) {
                    if (javaElementHelper.isInProductionCodeContext(referencedElement)) {

                    } else if (javaElementHelper.isInTestContext(referencedElement)) {
                        result.addAll(getAllChildrenOfTypeWithReferences(referencedElement, elementType));
                    }

                }
            }
            result.addAll(getAllChildrenOfTypeWithReferences(child, elementType));
        }
        return result;
    }
}
