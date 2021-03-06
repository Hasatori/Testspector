package com.testspector.model.checking.java.common;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;

import java.util.*;
import java.util.stream.Collectors;

public class JavaMethodResolver {

    private final JavaElementResolver elementResolver;
    private final JavaContextIndicator contextResolver;
    private final JavaMethodCallExpressionResolver assertionResolver;

    public JavaMethodResolver(JavaElementResolver elementResolver, JavaContextIndicator contextResolver, JavaMethodCallExpressionResolver assertionResolver) {
        this.elementResolver = elementResolver;
        this.contextResolver = contextResolver;
        this.assertionResolver = assertionResolver;
    }


    public List<PsiMethod> allTestedMethods(PsiMethod testMethod) {
        List<PsiMethod> result = new ArrayList<>();
        List<PsiMethodCallExpression> assertionMethods = assertionResolver.assertionCallExpressions(testMethod);
        result.addAll(assertionMethods.stream()
                .map(element -> elementResolver.allChildrenOfTypeWithReferencesThatMeetCondition(element, PsiMethodCallExpression.class, contextResolver::isInTestContext))
                .flatMap(Collection::stream)
                .map(PsiCall::resolveMethod)
                .filter(Objects::nonNull)
                .filter(contextResolver::isInProductionCodeContext)
                .collect(Collectors.toList()));

        result.addAll(assertionMethods
                .stream()
                .map(assertionMethod -> elementResolver.allChildrenOfTypeWithReferencesThatMeetCondition(assertionMethod, PsiLiteralExpression.class, contextResolver::isInTestContext))
                .flatMap(Collection::stream)
                .map(ReferenceProvidersRegistry::getReferencesFromProviders)
                .flatMap(Arrays::stream)
                .map(PsiReference::resolve)
                .filter(Objects::nonNull)
                .filter(referencedElement -> referencedElement instanceof PsiMethod)
                .filter(contextResolver::isInProductionCodeContext)
                .map(element -> (PsiMethod) element)
                .collect(Collectors.toList()));
        return result;
    }

    public List<PsiMethod> immediateMethodsWithAnnotations(PsiClass psiClass, List<String> annotationQualifiedNames) {
        return Arrays.stream(psiClass.getMethods())
                .filter(psiMethod -> annotationQualifiedNames.stream().anyMatch(psiMethod::hasAnnotation))
                .collect(Collectors.toList());
    }

    public List<PsiMethod> immediateMethodsWithAnnotations(List<PsiElement> fromElements, List<String> annotationQualifiedNames) {
        List<PsiMethod> psiMethods = new ArrayList<>();
        for (PsiElement psiElement : fromElements) {
            if (psiElement instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiElement;
                psiMethods.addAll(Arrays.stream(psiJavaFile.getClasses())
                        .map(psiClass -> immediateMethodsWithAnnotations(psiClass, annotationQualifiedNames))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
            } else if (psiElement instanceof PsiClass) {
                PsiClass psiClass = (PsiClass) psiElement;
                psiMethods.addAll(immediateMethodsWithAnnotations(psiClass, annotationQualifiedNames));
            } else if (psiElement instanceof PsiMethod) {
                psiMethods.add((PsiMethod) psiElement);
            }
        }
        return psiMethods;
    }

}
