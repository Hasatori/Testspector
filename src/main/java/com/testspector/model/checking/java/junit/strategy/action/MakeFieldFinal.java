package com.testspector.model.checking.java.junit.strategy.action;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;

import java.util.Optional;

public class MakeFieldFinal implements Action<BestPracticeViolation> {

    private final PsiField psifield;

    public MakeFieldFinal(PsiField psiField) {
        this.psifield = psiField;
    }


    @Override
    public String getName() {
        return "make final";
    }

    @Override
    public void execute(BestPracticeViolation bestPracticeViolation) {
        Optional.ofNullable(psifield.getModifierList()).ifPresent((modifierList) -> modifierList.setModifierProperty(PsiModifier.FINAL, true));
    }
}
