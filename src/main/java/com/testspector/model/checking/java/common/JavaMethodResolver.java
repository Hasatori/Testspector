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
                .allChildrenOfType(testMethod, PsiMethodCallExpression.class, (psiMethodCallExpression -> assertionMethod(psiMethodCallExpression).isPresent()), contextResolver.isInTestContext())
                .stream()
                .distinct()
                .collect(Collectors.toList());
        result.addAll(assertionMethods.stream()
                .map(element -> elementResolver.allChildrenOfType(element, PsiMethodCallExpression.class, contextResolver.isInTestContext()))
                .flatMap(Collection::stream)
                .map(PsiCall::resolveMethod)
                .filter(Objects::nonNull)
                .filter(contextResolver.isInProductionCodeContext())
                .collect(Collectors.toList()));

        result.addAll(assertionMethods
                .stream()
                .map(assertionMethod -> elementResolver.allChildrenOfType(assertionMethod, PsiLiteralExpression.class, contextResolver.isInTestContext()))
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

    public List<PsiMethod> testMethodsWithAnnotations(PsiClass psiClass, List<String> annotationQualifiedNames) {
        return Arrays.stream(psiClass.getMethods())
                .filter(psiMethod -> annotationQualifiedNames.stream().anyMatch(psiMethod::hasAnnotation))
                .collect(Collectors.toList());
    }

    public List<PsiMethod> testMethodsWithAnnotations(List<PsiElement> fromElements, List<String> annotationQualifiedNames) {
        List<PsiMethod> psiMethods = new ArrayList<>();
        for (PsiElement psiElement : fromElements) {
            if (psiElement instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiElement;
                psiMethods.addAll(Arrays.stream(psiJavaFile.getClasses())
                        .map(psiClass -> testMethodsWithAnnotations(psiClass, annotationQualifiedNames))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
            } else if (psiElement instanceof PsiClass) {
                PsiClass psiClass = (PsiClass) psiElement;
                psiMethods.addAll(testMethodsWithAnnotations(psiClass, annotationQualifiedNames));
            } else if (psiElement instanceof PsiMethod) {
                psiMethods.add((PsiMethod) psiElement);
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
                || (method.getName().toLowerCase().contains("assert") && elementResolver.allChildrenOfType(
                method,
                PsiThrowStatement.class,
                psiThrowStatement -> Optional
                        .ofNullable(psiThrowStatement.getException())
                        .map(PsiExpression::getType)
                        .map(type -> type.isAssignableFrom(PsiType.getTypeByName("AssertionError", method.getProject(), GlobalSearchScope.EMPTY_SCOPE)))
                        .isPresent(),
                (element -> contextResolver.isInTestContext().test(element))).size() > 0)) {
            return Optional.of(method);
        }
        return Optional.empty();
    }

    public boolean isGetter(PsiMethod method) {
        PsiCodeBlock body = method.getBody();
        if (body != null) {
            Optional<PsiElement> returnCandidate = elementResolver.firstChildIgnoring(body, Arrays.asList(PsiJavaToken.class, PsiWhiteSpace.class));
            if (returnCandidate.isPresent() && returnCandidate.get() instanceof PsiReturnStatement) {
                return elementResolver.immediateChildrenOfType(returnCandidate.get(), PsiReferenceExpression.class)
                        .stream()
                        .map(PsiReference::resolve)
                        .anyMatch(element -> element instanceof PsiField);
            }
        }

        return false;
    }

}
