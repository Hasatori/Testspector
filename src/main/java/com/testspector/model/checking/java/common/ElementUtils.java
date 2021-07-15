package com.testspector.model.checking.java.common;

import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.Optional;

public final class ElementUtils {

    private ElementUtils() {
    }

    public static Optional<Integer> getElementLineNumber(PsiElement element) {
        PsiFile containingFile = element.getContainingFile();
        return Optional.ofNullable(PsiDocumentManager.getInstance(element.getProject()).getDocument(containingFile))
                .map(file -> file.getLineNumber(element.getTextOffset() + 1));
    }

    public static boolean containsElement(PsiElement root, PsiElement toFind) {
        for (PsiElement child : root.getChildren()) {
            if (child == toFind) {
                return true;
            } else {
                return containsElement(child, toFind);
            }
        }
        return false;
    }

}
