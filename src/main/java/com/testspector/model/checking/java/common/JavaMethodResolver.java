package com.testspector.model.checking.java.common;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.*;
import java.util.stream.Collectors;

import static com.testspector.model.checking.java.common.JavaConstants.ASSERTION_CLASSES_CLASS_PATHS;

public class JavaMethodResolver {

    private final JavaElementResolver elementResolver;
    private final JavaContextIndicator contextResolver;

    public JavaMethodResolver(JavaElementResolver elementResolver, JavaContextIndicator contextResolver) {
        this.elementResolver = elementResolver;
        this.contextResolver = contextResolver;
    }


    public List<PsiMethod> allTestedMethods(PsiMethod testMethod) {
        List<PsiMethod> result = new ArrayList<>();
        List<PsiMethodCallExpression> assertionMethods = elementResolver
                .allChildrenOfTypeMeetingConditionWithReferences(
                        testMethod,
                        PsiMethodCallExpression.class,
                        (psiMethodCallExpression ->
                                assertionMethod(psiMethodCallExpression).isPresent()),
                        contextResolver.isInTestContext())
                .stream()
                .distinct()
                .collect(Collectors.toList());

        result.addAll(assertionMethods.stream()
                .map(element -> elementResolver
                        .allChildrenOfTypeWithReferences(
                                element,
                                PsiMethodCallExpression.class,
                                contextResolver.isInTestContext()))
                .flatMap(Collection::stream)
                .map(PsiCall::resolveMethod)
                .filter(Objects::nonNull)
                .filter(contextResolver.isInProductionCodeContext())
                .collect(Collectors.toList()));

        result.addAll(assertionMethods
                .stream()
                .map(assertionMethod -> elementResolver
                        .allChildrenOfTypeWithReferences(
                                assertionMethod,
                                PsiLiteralExpression.class,
                                contextResolver.isInTestContext()))
                .flatMap(Collection::stream)
                .map(ReferenceProvidersRegistry::getReferencesFromProviders)
                .flatMap(Arrays::stream)
                .map(PsiReference::resolve)
                .filter(Objects::nonNull)
                .filter(referencedElement -> referencedElement instanceof PsiMethod)
                .filter(contextResolver.isInProductionCodeContext())
                .map(element -> (PsiMethod) element)
                .collect(Collectors.toList()));
        return result.stream().distinct().collect(Collectors.toList());
    }

    public List<PsiMethodCallExpression> allTestedMethodsExpressions(PsiMethod testMethod) {
        List<PsiMethodCallExpression> assertionMethods = elementResolver
                .allChildrenOfTypeMeetingConditionWithReferences(
                        testMethod,
                        PsiMethodCallExpression.class,
                        (psiMethodCallExpression ->
                                assertionMethod(psiMethodCallExpression).isPresent()),
                        contextResolver.isInTestContext())
                .stream()
                .distinct()
                .collect(Collectors.toList());
        List<PsiMethodCallExpression> result = assertionMethods.stream()
                .map(element -> elementResolver
                        .allChildrenOfTypeWithReferences(
                                element,
                                PsiMethodCallExpression.class,
                                contextResolver.isInTestContext()))
                .flatMap(Collection::stream)
                .filter(psiMethodCallExpression -> {
                    PsiMethod method = psiMethodCallExpression.resolveMethod();
                    return method != null && contextResolver.isInProductionCodeContext().test(method);

                }).collect(Collectors.toList());
        return result.stream().distinct().collect(Collectors.toList());
    }

    public List<PsiReference> allTestedMethodsFromReference(PsiMethod testMethod){
        List<PsiMethodCallExpression> assertionMethods = elementResolver
                .allChildrenOfTypeMeetingConditionWithReferences(
                        testMethod,
                        PsiMethodCallExpression.class,
                        (psiMethodCallExpression ->
                                assertionMethod(psiMethodCallExpression).isPresent()),
                        contextResolver.isInTestContext())
                .stream()
                .distinct()
                .collect(Collectors.toList());
        List<PsiReference> result = assertionMethods
                .stream()
                .map(assertionMethod -> elementResolver
                        .allChildrenOfTypeWithReferences(
                                assertionMethod,
                                PsiLiteralExpression.class,
                                contextResolver.isInTestContext()))
                .flatMap(Collection::stream)
                .map(ReferenceProvidersRegistry::getReferencesFromProviders)
                .flatMap(Arrays::stream)
                .filter(reference -> {
                    PsiElement resolvedElement = reference.resolve();
                    return resolvedElement instanceof PsiMethod && contextResolver.isInProductionCodeContext().test(resolvedElement);

                }).collect(Collectors.toList());
        return result.stream().distinct().collect(Collectors.toList());
    }
    private List<PsiMethod> methodsWithAnnotations(PsiClass psiClass, List<String> annotationQualifiedNames) {
        return Arrays.stream(psiClass.getMethods())
                .filter(psiMethod -> methodHasAnyOfAnnotations(psiMethod, annotationQualifiedNames))
                .collect(Collectors.toList());
    }

    public List<PsiMethod> methodsWithAnnotations(List<PsiElement> fromElements, List<String> annotationQualifiedNames) {
        List<PsiMethod> psiMethods = new ArrayList<>();
        for (PsiElement psiElement : fromElements) {
            if (psiElement instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiElement;
                psiMethods.addAll(Arrays.stream(psiJavaFile.getClasses())
                        .map(psiClass -> methodsWithAnnotations(psiClass, annotationQualifiedNames))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
            } else if (psiElement instanceof PsiClass) {
                PsiClass psiClass = (PsiClass) psiElement;
                psiMethods.addAll(methodsWithAnnotations(psiClass, annotationQualifiedNames));
            } else if (psiElement instanceof PsiMethod) {
                PsiMethod method = (PsiMethod) psiElement;
                if (annotationQualifiedNames.stream().anyMatch(method::hasAnnotation) || (annotationQualifiedNames.isEmpty() && method.getAnnotations().length == 0)) {
                    psiMethods.add((PsiMethod) psiElement);
                }
            }
        }
        return psiMethods;
    }

    public Optional<PsiMethod> assertionMethod(PsiMethodCallExpression psiMethodCallExpression) {
        PsiMethod assertionMethodCandidate = psiMethodCallExpression.resolveMethod();
        if (assertionMethodCandidate != null) {
            return assertionMethod(assertionMethodCandidate);
        }
        return Optional.empty();
    }

    public Optional<PsiMethod> assertionMethod(PsiMethod method) {
        if ((method.getContainingClass() != null && ASSERTION_CLASSES_CLASS_PATHS.contains(method.getContainingClass().getQualifiedName()))
                || (method.getName().toLowerCase().contains("assert") && !contextResolver.isInProductionCodeContext().test(method))) {
            return Optional.of(method);
        }
        return Optional.empty();
    }

    public boolean isGetter(PsiMethod method) {
        PsiCodeBlock body = method.getBody();
        if (body != null) {
            Optional<PsiElement> returnCandidate = elementResolver.firstImmediateChildIgnoring(body, Arrays.asList(PsiJavaToken.class, PsiWhiteSpace.class));
            if (returnCandidate.isPresent() && returnCandidate.get() instanceof PsiReturnStatement) {
                PsiReturnStatement returnStatement = (PsiReturnStatement) returnCandidate.get();
                PsiExpression returnValueExpression = returnStatement.getReturnValue();
                if (returnValueExpression instanceof PsiReferenceExpression) {
                    return Optional.ofNullable(((PsiReferenceExpression) returnValueExpression).resolve())
                            .filter(element -> element instanceof PsiField)
                            .isPresent();
                }
            }
        }

        return false;
    }

    public boolean methodHasAnyOfAnnotations(PsiMethod method, List<String> annotationQualifiedNames) {
        return annotationQualifiedNames.stream().anyMatch(method::hasAnnotation)
                || (annotationQualifiedNames.isEmpty() && method.getAnnotations().length == 0);
    }


}
