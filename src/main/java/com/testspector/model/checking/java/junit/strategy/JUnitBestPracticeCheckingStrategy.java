package com.testspector.model.checking.java.junit.strategy;

import com.intellij.lang.jvm.annotation.JvmAnnotationClassValue;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTypesUtil;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.common.search.ElementSearchEngine;
import com.testspector.model.checking.java.junit.JUnitConstants;

import java.util.Arrays;
import java.util.Optional;

import static com.testspector.model.checking.java.junit.JUnitConstants.JUNIT4_ASSERTIONS_CLASS_PATH;
import static com.testspector.model.checking.java.junit.JUnitConstants.JUNIT5_ASSERTIONS_CLASS_PATH;

public abstract class JUnitBestPracticeCheckingStrategy implements BestPracticeCheckingStrategy<PsiMethod> {
    protected final ElementSearchEngine elementSearchEngine;
    protected final JavaContextIndicator contextIndicator;
    protected final JavaMethodResolver methodResolver;

    public JUnitBestPracticeCheckingStrategy(ElementSearchEngine elementSearchEngine, JavaContextIndicator contextIndicator, JavaMethodResolver methodResolver) {
        this.elementSearchEngine = elementSearchEngine;
        this.contextIndicator = contextIndicator;
        this.methodResolver = methodResolver;
    }

    protected boolean areJUnit5ClassesAvailable(PsiMethod method) {
        Module module = ProjectRootManager.getInstance(method.getProject()).getFileIndex().getModuleForFile(method.getContainingFile().getVirtualFile());
        if (module != null) {
            return PsiTypesUtil.getPsiClass(PsiType.getTypeByName(JUNIT5_ASSERTIONS_CLASS_PATH, method.getProject(), GlobalSearchScope.moduleScope(module))) != null;
        }
        return false;
    }

    protected boolean areJUnit4ClassesAvailable(PsiMethod method) {
        Module module = ProjectRootManager.getInstance(method.getProject()).getFileIndex().getModuleForFile(method.getContainingFile().getVirtualFile());
        if (module != null) {
            return PsiTypesUtil.getPsiClass(PsiType.getTypeByName(JUNIT4_ASSERTIONS_CLASS_PATH, method.getProject(), GlobalSearchScope.moduleScope(module))) != null;
        }
        return false;
    }

    protected boolean isJUnit4ExpectedTest(PsiMethod testMethod) {
        return Arrays.stream(testMethod.getAnnotations())
                .anyMatch(psiAnnotation -> psiAnnotation.hasQualifiedName(JUnitConstants.JUNIT4_TEST_QUALIFIED_NAME) &&
                        psiAnnotation.getAttributes().stream().anyMatch(jvmAnnotationAttribute ->
                                "expected".equals(jvmAnnotationAttribute.getAttributeName()) &&
                                        Optional.ofNullable(jvmAnnotationAttribute.getAttributeValue())
                                                .filter(attributeValue -> attributeValue instanceof JvmAnnotationClassValue)
                                                .isPresent()
                        )
                );

    }
}
