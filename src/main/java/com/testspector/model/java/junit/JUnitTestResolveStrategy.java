package com.testspector.model.java.junit;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.testspector.model.TestResolveStrategy;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JUnitTestResolveStrategy extends TestResolveStrategy {

    private static final List<String> TEST_QUALIFIED_NAMES = Arrays.asList(
            "org.junit.jupiter.api.Test",
            "org.junit.jupiter.params.ParameterizedTest"
    );


    @Override
    public PsiElement resolveTest(PsiElement element) {
        if (element instanceof PsiMethod) {
            Optional<PsiIdentifier> identifier = Arrays.stream(element.getChildren()).filter(psiElement -> psiElement instanceof PsiIdentifier).findFirst().map(ident -> (PsiIdentifier) ident);
            if (identifier.isPresent() && Arrays.stream(((PsiMethod) element).getAnnotations()).anyMatch(psiAnnotation -> TEST_QUALIFIED_NAMES.contains(psiAnnotation.getQualifiedName()))) {
                return identifier.get();
            }
        }
        return null;
    }
}
