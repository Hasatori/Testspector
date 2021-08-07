package com.testspector.model.checking.java.junit;

import com.intellij.lang.jvm.annotation.JvmAnnotationClassValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.*;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.enums.BestPractice;

import java.util.*;
import java.util.stream.Collectors;

public class JUnitTestMethodBestPracticeCheckingStrategyAdapter implements BestPracticeCheckingStrategy<PsiElement> {


    private final BestPracticeCheckingStrategy<PsiMethod> decoratedMethodSpecificStrategy;
    private final JavaMethodResolver methodResolver;

    public JUnitTestMethodBestPracticeCheckingStrategyAdapter(BestPracticeCheckingStrategy<PsiMethod> decoratedMethodSpecificStrategy, JavaMethodResolver methodResolver) {
        this.decoratedMethodSpecificStrategy = decoratedMethodSpecificStrategy;
        this.methodResolver = methodResolver;
    }


    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiElement psiElement) {
        return checkBestPractices(Collections.singletonList(psiElement));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiElement> psiElements) {
        psiElements = filterOutElementsRelatedToIntegrationTests(psiElements);
        List<PsiMethod> methods = methodResolver.getMethodsWithAnnotations(psiElements, JUnitConstants.JUNIT_ALL_TEST_QUALIFIED_NAMES)
                .stream()
                .filter(method -> Arrays.stream(method.getAnnotations()).noneMatch(this::isIntegrationTag))
                .collect(Collectors.toList());

        return decoratedMethodSpecificStrategy.checkBestPractices(methods);
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return decoratedMethodSpecificStrategy.getCheckedBestPractice();
    }

    private List<PsiElement> filterOutElementsRelatedToIntegrationTests(List<PsiElement> psiElements) {
        return psiElements.stream()
                .filter(element -> {
                    if (element instanceof PsiMethod) {
                        PsiMethod method = (PsiMethod) element;
                        return Arrays.stream(method.getAnnotations()).noneMatch(this::isIntegrationTag) && isNotIntegrationTest((PsiJavaFile) element.getContainingFile());
                    }
                    if (!(element instanceof PsiClass)) {
                        PsiFile file = element.getContainingFile();
                        if (file instanceof PsiJavaFile) {
                            return Arrays.stream(((PsiJavaFile) file).getClasses()).map(psiClass -> Arrays.asList(psiClass.getAnnotations())).flatMap(Collection::stream).noneMatch(this::isIntegrationTag)
                                    && isNotIntegrationTest((PsiJavaFile) file);
                        } else {
                            return false;
                        }
                    } else {
                        return Arrays.stream(((PsiClass) element).getAnnotations()).noneMatch(this::isIntegrationTag) &&
                                isNotIntegrationTest((PsiJavaFile) element.getContainingFile());
                    }
                }).collect(Collectors.toList());
    }

    private boolean isNotIntegrationTest(PsiJavaFile file) {

        return !file.getName().matches(".*IT\\.java") &&
                !"it".equalsIgnoreCase(file.getPackageName()) &&
                !file.getPackageName().matches("(it\\..*)") &&
                !file.getPackageName().matches("(.*\\.it\\..*)") &&
                !file.getPackageName().matches("(.*\\.it)") &&
                !Optional.ofNullable(ProjectRootManager.getInstance(file.getProject()).getFileIndex().getModuleForFile(file.getVirtualFile()))
                        .map(Module::getName)
                        .map(name -> name.split("_")[0])
                        .map(val -> "it".equalsIgnoreCase(val) || val.contains("integration"))
                        .orElse(false);

    }

    private boolean isIntegrationTag(PsiAnnotation psiAnnotation) {
        return (psiAnnotation.hasQualifiedName("org.junit.jupiter.api.Tag") &&
                psiAnnotation.getAttributes().stream()
                        .anyMatch(attribute -> "value".equals(attribute.getAttributeName()) &&
                                Optional.ofNullable(attribute.getAttributeValue())
                                        .filter(attributeValue -> attributeValue instanceof JvmAnnotationConstantValue && ((JvmAnnotationConstantValue) attributeValue).getConstantValue() instanceof String)
                                        .map(attributeValue -> ((JvmAnnotationConstantValue) attributeValue))
                                        .map(jvmAnnotationConstantValue -> (String) jvmAnnotationConstantValue.getConstantValue())
                                        .map(constantValue -> constantValue.toLowerCase().contains("integration") || constantValue.matches(".*IT.*"))
                                        .orElse(false)
                        )) ||
                (psiAnnotation.hasQualifiedName("org.junit.experimental.categories.Category") &&
                        psiAnnotation.getAttributes().stream()
                                .anyMatch(jvmAnnotationAttribute -> (Optional.ofNullable(jvmAnnotationAttribute.getAttributeValue())
                                        .filter(attribute -> attribute instanceof JvmAnnotationClassValue)
                                        .map(attribute -> ((JvmAnnotationClassValue) attribute).getClazz())
                                        .map(clazz -> clazz.getName().toLowerCase())
                                        .map(name -> name.contains("integration"))
                                        .orElse(false))));
    }

}
