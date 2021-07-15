package com.testspector.model.checking.java.junit.strategy.action;

import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.ElementUtils;

public class NavigateElementAction implements Action<BestPracticeViolation> {

    private final String name;
    private final PsiElement element;


    public NavigateElementAction(String elementName, PsiElement element) {
        PsiFile containingFile = element.getContainingFile();
        this.name = String.format("Go to %s in file %s ( line %s)",
                elementName,
                containingFile.getName(),
                ElementUtils.getElementLineNumber(element).orElse(0));
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
