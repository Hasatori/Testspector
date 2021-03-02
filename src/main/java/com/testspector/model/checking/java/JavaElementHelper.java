package com.testspector.model.checking.java;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.testspector.model.checking.java.junit.JUnitConstants.ASSERTION_CLASS_PATHS;

public class JavaElementHelper {

    private final JavaClassHelper javaClassHelper;

    public JavaElementHelper(JavaClassHelper javaClassHelper) {
        this.javaClassHelper = javaClassHelper;
    }

    public Optional<PsiJavaFile> castToFile(PsiElement psiElement) {
        if (psiElement instanceof PsiJavaFile) {
            return Optional.of((PsiJavaFile) psiElement);
        }
        return Optional.empty();
    }

    public Optional<PsiMethod> castToMethod(PsiElement psiElement) {
        if (psiElement instanceof PsiMethod) {
            return Optional.of((PsiMethod) psiElement);
        }
        return Optional.empty();
    }

    public Optional<PsiClass> castToClass(PsiElement psiElement) {
        if (psiElement instanceof PsiClass) {
            return Optional.of((PsiClass) psiElement);
        }
        return Optional.empty();
    }

    public List<PsiMethod> getMethodsFromElementByAnnotations(List<PsiElement> psiElements, List<String> annotationQualifiedNames) {
        List<PsiMethod> psiMethods = new ArrayList<>();
        for (PsiElement psiElement : psiElements) {
            if (castToFile(psiElement).isPresent()) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiElement;
                psiMethods.addAll(Arrays.stream(psiJavaFile.getClasses())
                        .map(psiClass -> javaClassHelper.getMethodsByAnnotations(psiClass, annotationQualifiedNames))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
            } else if (castToClass(psiElement).isPresent()) {
                PsiClass psiClass = (PsiClass) psiElement;
                psiMethods.addAll(javaClassHelper.getMethodsByAnnotations(psiClass, annotationQualifiedNames));
            } else if (castToMethod(psiElement).isPresent()) {
                psiMethods.add((PsiMethod) psiElement);
            }
        }
        return psiMethods;
    }


    public <T extends PsiElement> List<T> getImmediateChildrenOfType(PsiElement psiElement, Class<T> elementType) {
        return Arrays.stream(psiElement.getChildren())
                .filter(elementType::isInstance)
                .map(elementType::cast)
                .collect(Collectors.toList());
    }

    public boolean isInTestContext(PsiElement element) {
        PsiJavaFile psiJavaFile = (PsiJavaFile) element.getContainingFile();
        String absolutePath = psiJavaFile.getVirtualFile().getPath();
        String packagePath = psiJavaFile.getPackageName();
        return Pattern.compile(String.format("src/test/java/%s/%s$", packagePath.replaceAll("\\.", "/"), psiJavaFile.getName())).matcher((absolutePath)).find();

    }

    public boolean isInProductionCodeContext(PsiElement element) {
        PsiJavaFile psiJavaFile = (PsiJavaFile) element.getContainingFile();
        String absolutePath = psiJavaFile.getVirtualFile().getPath();
        String packagePath = psiJavaFile.getPackageName();
        return Pattern.compile(String.format("src/main/java/%s/%s$", packagePath.replaceAll("\\.", "/"), psiJavaFile.getName())).matcher((absolutePath)).find();

    }

    public <T extends PsiElement> List<T> getAllChildrenOfType(PsiElement psiElement, Class<T> elementType) {
        List<T> result = new ArrayList<>();
        for (PsiElement child : psiElement.getChildren()) {
            if (elementType.isInstance(child)) {
                result.add(elementType.cast(child));
            }
            result.addAll(getAllChildrenOfType(child, elementType));
        }
        return result;
    }

    public Optional<PsiElement> getFirstChildIgnoring(PsiElement psiElement, List<Class<? extends PsiElement>> ignoredList) {
        for (PsiElement child : psiElement.getChildren()) {
            if (ignoredList.stream().noneMatch(ignored -> ignored.isInstance(child))) {
                return Optional.of(child);
            }
        }
        return Optional.empty();
    }


    public List<PsiMethod> getTestedMethods(PsiMethod testMethod) {
        List<PsiMethod> result = new ArrayList<>();
        List<PsiMethodCallExpression> assertionMethods = getAssertionsMethods(testMethod);
        result.addAll(assertionMethods.stream()
                .map(element -> getAllChildrenOfTypeWithReferencesMeetingCondition(element, PsiMethodCallExpression.class,this::isInTestContext))
                .flatMap(Collection::stream)
                .map(PsiCall::resolveMethod)
                .filter(Objects::nonNull)
                .filter(this::isInProductionCodeContext)
                .collect(Collectors.toList()));

        result.addAll(assertionMethods
                .stream()
                .map(assertionMethod -> getAllChildrenOfTypeWithReferencesMeetingCondition(assertionMethod, PsiLiteralExpression.class, this::isInTestContext))
                .flatMap(Collection::stream)
                .map(ReferenceProvidersRegistry::getReferencesFromProviders)
                .flatMap(Arrays::stream)
                .map(PsiReference::resolve)
                .filter(Objects::nonNull)
                .filter(referencedElement -> referencedElement instanceof PsiMethod)
                .filter(this::isInProductionCodeContext)
                .map(element -> (PsiMethod) element)
                .collect(Collectors.toList()));
        return result;
    }

    public <T extends PsiElement> List<T> getAllChildrenOfTypeWithReferencesMeetingCondition(PsiElement psiElement, Class<T> elementType, Predicate<PsiElement> referencedElementCondition) {
        List<T> result = new ArrayList<>();
        for (PsiElement child : psiElement.getChildren()) {
            if (elementType.isInstance(child)) {
                result.add(elementType.cast(child));
            }
            if (child instanceof PsiReferenceExpression) {
                PsiElement referencedElement = ((PsiReferenceExpression) child).resolve();
                if (referencedElement != null) {
                     if (referencedElementCondition.test(referencedElement)) {
                        result.addAll(getAllChildrenOfTypeWithReferencesMeetingCondition(referencedElement, elementType, referencedElementCondition));
                    }

                }
            }
            result.addAll(getAllChildrenOfTypeWithReferencesMeetingCondition(child, elementType, referencedElementCondition));
        }
        return result;
    }

    public List<PsiMethodCallExpression> getAssertionsMethods(PsiElement psiElement) {
        List<PsiMethodCallExpression> assertionMethods = new ArrayList<>();
        for (PsiElement child : psiElement.getChildren()) {
            if (child instanceof PsiMethodCallExpression) {
                PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) child;
                if (isAssertionMethod().test(methodCallExpression)) {
                    assertionMethods.add(methodCallExpression);
                } else {
                    PsiMethod referencedMethod = methodCallExpression.resolveMethod();
                    if (referencedMethod != null && this.isInTestContext(referencedMethod)) {
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

}
