package com.testspector.model.checking.java.junit;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.testspector.model.checking.TestResolveStrategy;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.testspector.model.checking.java.junit.JUnitConstants.TEST_QUALIFIED_NAMES;

public class JUnitTestLineResolveStrategy extends TestResolveStrategy {

    @Override
    public Optional<PsiElement> resolveTestLine(PsiElement element) {
        if (element instanceof PsiMethod) {
            Optional<PsiIdentifier> identifier = Arrays.stream(element.getChildren()).filter(psiElement -> psiElement instanceof PsiIdentifier).findFirst().map(ident -> (PsiIdentifier) ident);
            if (identifier.isPresent() && Arrays.stream(((PsiMethod) element).getAnnotations()).anyMatch(psiAnnotation -> TEST_QUALIFIED_NAMES.contains(psiAnnotation.getQualifiedName()))) {
                return Optional.of(identifier.get());
            }
        }
        return Optional.empty();
    }
}
