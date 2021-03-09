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

import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

public class TestOnlyPublicBehaviourJUnitCheckingStrategy implements BestPracticeCheckingStrategy<PsiMethod> {


    private final JavaElementResolver elementResolver;
    private final JavaMethodResolver methodResolver;
    private final JavaContextIndicator contextIndicator;

    public TestOnlyPublicBehaviourJUnitCheckingStrategy(JavaElementResolver elementResolver, JavaMethodResolver methodResolver, JavaContextIndicator contextIndicator) {
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
            List<PsiMethod> notPublicMethods = methodResolver.allTestedMethods(method)
                    .stream()
                    .filter(testedMethod -> methodHasModifier(testedMethod, "protected") || isMethodPackagePrivate(testedMethod) || methodHasModifier(testedMethod, "private"))
                    .collect(Collectors.toList());
            PsiIdentifier methodIdentifier = method.getNameIdentifier();
            if (notPublicMethods.size() > 0) {
                bestPracticeViolations.add(new BestPracticeViolation(
                        String.format("%s#%s", method.getContainingClass().getQualifiedName(), method.getName()),
                        method,
                        methodIdentifier != null ? methodIdentifier.getTextRange() : method.getTextRange(),
                        "Only public behaviour should be tested. Testing 'private','protected' or 'package private' methods leads to problems with maintenance of tests because this private behaviour is likely to be changed very often. " +
                                "In many cases we are refactoring private behaviour without influencing public behaviour of the class, yet this changes will change behaviour of the private method and cause tests to fail.",
                        Arrays.asList(
                                "There is an exception to this rule and that is in case when private 'method' is part of the observed behaviour of the system under test. For example we can have private constructor for class which is part of ORM and its initialization should not be permitted.",
                                "Remove tests testing private behaviour",
                                "If you really feel that private behaviour is complex enough that there should be separate test for it, then it is very probable that the system under test is breaking 'Single Responsibility Principle' and this private behaviour should be extracted to a separate system"
                                ),
                        getCheckedBestPractice().get(0),
                        createRelatedElements(method, notPublicMethods)
                ));
            }
        }

        return bestPracticeViolations;
    }

    private boolean methodHasModifier(PsiMethod method, String modifier) {
        return method.getModifierList().hasModifierProperty(modifier);
    }

    private boolean isMethodPackagePrivate(PsiMethod method) {
        PsiModifierList modifierList = method.getModifierList();
        return !modifierList.hasModifierProperty("public") &&
                !modifierList.hasModifierProperty("private") &&
                !modifierList.hasModifierProperty("protected");
    }


    private List<RelatedElementWrapper> createRelatedElements(PsiMethod method, List<PsiMethod> notPublicMethods) {
        List<RelatedElementWrapper> result = new ArrayList<>();
        for (PsiMethod notPublicMethod : notPublicMethods) {
            HashMap<PsiElement, String> elementNameHashMap = new HashMap<>();
            Optional<PsiReferenceExpression> optionalPsiReferenceExpression = firstReferenceToMethod(method, notPublicMethod);
            String methodAccessQualifier = getMethodAccessQualifier(notPublicMethod);
            if (optionalPsiReferenceExpression.isPresent()) {
                elementNameHashMap.put(optionalPsiReferenceExpression.get(), methodAccessQualifier + " method call from test");
            }
            elementNameHashMap.put(notPublicMethod, methodAccessQualifier + " method call in production code");

            result.add(new RelatedElementWrapper(notPublicMethod.getName(), elementNameHashMap));
        }

        return result;
    }


    private Optional<PsiReferenceExpression> firstReferenceToMethod(PsiElement element, PsiMethod notPublicMethod) {
        List<PsiReferenceExpression> references = elementResolver.allChildrenOfType(element, PsiReferenceExpression.class);
        for (PsiReferenceExpression reference : references) {
            if (!elementResolver.allChildrenOfType(
                    reference.getParent(),
                    PsiMethodCallExpression.class,
                    psiMethodCallExpression -> psiMethodCallExpression.resolveMethod() != null && psiMethodCallExpression.resolveMethod() == notPublicMethod,
                    contextIndicator.isInTestContext()).isEmpty()) {
                return Optional.of(reference);
            }
        }
        return Optional.empty();
    }

    private String getMethodAccessQualifier(PsiMethod method) {
        if (method.hasModifierProperty("private")) {
            return "private";
        } else if (method.hasModifierProperty("protected")) {
            return "protected";
        } else if (isMethodPackagePrivate(method)) {
            return "package private";
        }
        throw new InvalidParameterException(String.format("Invalid not public method instance %s. Supported not public methods are: ['private','protected','package private']", method));
    }


    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.TEST_ONLY_PUBLIC_BEHAVIOUR);
    }
}
