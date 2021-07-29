package com.testspector.model.checking.java.junit.strategy;

import com.intellij.lang.jvm.annotation.JvmAnnotationClassValue;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiTypesUtil;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.common.search.ElementSearchEngine;
import com.testspector.model.checking.java.junit.JUnitConstants;

import java.util.Arrays;
import java.util.Optional;

import static com.testspector.model.checking.java.junit.JUnitConstants.*;

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
            return PsiTypesUtil.getPsiClass(PsiType.getTypeByName(JUNIT5_ASSERTIONS_CLASS_PATH, method.getProject(), GlobalSearchScope.moduleWithLibrariesScope(module))) != null;
        }
        return false;
    }

    protected boolean areJUnit4ClassesAvailable(PsiMethod method) {
        Module module = ProjectRootManager.getInstance(method.getProject()).getFileIndex().getModuleForFile(method.getContainingFile().getVirtualFile());
        if (module != null) {
            return PsiTypesUtil.getPsiClass(PsiType.getTypeByName(JUNIT4_ASSERTIONS_CLASS_PATH, method.getProject(), GlobalSearchScope.moduleWithLibrariesScope(module))) != null;
        }
        return false;
    }

    protected boolean isHamcrestAvailable(PsiMethod method) {
        Module module = ProjectRootManager.getInstance(method.getProject()).getFileIndex().getModuleForFile(method.getContainingFile().getVirtualFile());
        if (module != null) {
            return PsiTypesUtil.getPsiClass(PsiType.getTypeByName(HAMCREST_ASSERTIONS_CLASS_PATH, method.getProject(), GlobalSearchScope.moduleWithLibrariesScope(module))) != null;
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

    protected PsiIdentifier getReferenceExpressionIdentifier(PsiReferenceExpression psiReferenceExpression){
        return PsiTreeUtil.getChildOfType(psiReferenceExpression,PsiIdentifier.class);
    }

    protected PsiIdentifier getMethodCallExpressionIdentifier(PsiMethodCallExpression methodCallExpression){
        return PsiTreeUtil.getChildOfType(PsiTreeUtil.getChildOfType(methodCallExpression, PsiReferenceExpression.class),PsiIdentifier.class);
    }
}
