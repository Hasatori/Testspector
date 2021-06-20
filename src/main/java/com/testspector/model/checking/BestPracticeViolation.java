package com.testspector.model.checking;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.psi.PsiElement;
import com.testspector.model.enums.BestPractice;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BestPracticeViolation {

    private final PsiElement element;

    private final PsiElement startElement;

    private final PsiElement endElement;

    private final String problemDescription;

    private final BestPractice violatedBestPractice;

    private final List<Action<BestPracticeViolation>> actions;

    private final List<String> hints;

    public BestPracticeViolation(PsiElement element, PsiElement startElement, PsiElement endElement, String problemDescription, BestPractice violatedBestPractice, List<Action<BestPracticeViolation>> actions, List<String> hints) {
        this.element = element;
        this.startElement = startElement;
        this.endElement = endElement;
        this.problemDescription = problemDescription;
        this.violatedBestPractice = violatedBestPractice;
        this.actions = actions;
        this.hints = hints;
    }

    public BestPracticeViolation(PsiElement element, String problemDescription, BestPractice violatedBestPractice, List<Action<BestPracticeViolation>> actions, List<String> hints) {
        this(element, null, null, problemDescription, violatedBestPractice, actions, hints);
    }

    public BestPracticeViolation(PsiElement element, String problemDescription, BestPractice violatedBestPractice, List<Action<BestPracticeViolation>> actions) {
        this(element, null, null, problemDescription, violatedBestPractice, actions, null);
    }

    public BestPracticeViolation(PsiElement startElement, PsiElement endElement, String problemDescription, BestPractice violatedBestPractice, List<Action<BestPracticeViolation>> actions) {
        this(startElement, startElement, endElement, problemDescription, violatedBestPractice, actions, null);
    }


    public PsiElement getElement() {
        return element;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public BestPractice getViolatedBestPractice() {
        return violatedBestPractice;
    }

    public List<Action<BestPracticeViolation>> getActions() {
        return actions;
    }

    @Nullable
    public List<String> getHints() {
        return hints;
    }

    public PsiElement getStartElement() {
        return startElement;
    }

    public PsiElement getEndElement() {
        return endElement;
    }
}
