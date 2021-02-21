package com.testspector.model.checking.java.junit;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.testspector.model.checking.UnitTestLineResolveStrategy;
import com.testspector.model.enums.UnitTestFramework;

import java.util.Arrays;
import java.util.Optional;

import static com.testspector.model.checking.java.junit.JUnitConstants.TEST_QUALIFIED_NAMES;

public class JUnitUnitTestLineResolveStrategy extends UnitTestLineResolveStrategy {

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

    @Override
    public UnitTestFramework getUnitTestFramework() {
        return UnitTestFramework.JUNIT;
    }
}
