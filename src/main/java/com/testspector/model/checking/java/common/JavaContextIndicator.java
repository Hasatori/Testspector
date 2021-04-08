package com.testspector.model.checking.java.common;

import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;

import java.util.Optional;
import java.util.function.Predicate;

public class JavaContextIndicator {

    public Predicate<PsiElement> isInTestContext() {
        return element -> getVirtualFileFromElement(element)
                .map(ProjectRootManager.getInstance(element.getProject()).getFileIndex()::isInTestSourceContent)
                .orElse(false);
    }

    public Predicate<PsiElement> isInProductionCodeContext() {
        return element -> getVirtualFileFromElement(element)
                .map(virtualFie-> ProjectRootManager
                        .getInstance(element.getProject())
                        .getFileIndex().isInSourceContent(virtualFie) &&
                !ProjectRootManager
                        .getInstance(element.getProject())
                        .getFileIndex().isInTestSourceContent(virtualFie)
                )
                .orElse(false);
    }

    private Optional<VirtualFile> getVirtualFileFromElement(PsiElement element) {
        PsiJavaFile psiJavaFile = (PsiJavaFile) element.getContainingFile();
        if (psiJavaFile != null) {
            VirtualFile virtualFile = psiJavaFile.getVirtualFile();
            if (virtualFile != null) {
                return Optional.of(virtualFile);
            }
        }
        return Optional.empty();
    }
}
