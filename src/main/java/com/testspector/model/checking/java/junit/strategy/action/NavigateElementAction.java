package com.testspector.model.checking.java.junit.strategy.action;

import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;

import java.util.Optional;

public class NavigateElementAction implements Action<BestPracticeViolation> {

    private final String name;
    private final PsiElement element;


    public NavigateElementAction(String elementName, PsiElement element) {
        PsiFile containingFile = element.getContainingFile();
        this.name = String.format("Go to %s in file %s ( line %s)",
                elementName,
                containingFile.getName(),
                Optional.ofNullable(PsiDocumentManager.getInstance(element.getProject()).getDocument(containingFile))
                        .map(file -> file.getLineNumber(element.getTextOffset()))
                        .orElse(0) + 1);
        this.element = element;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void execute(BestPracticeViolation bestPracticeViolation) {
        ((Navigatable) element.getNavigationElement()).navigate(true);
    }
}
