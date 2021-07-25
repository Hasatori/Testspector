package com.testspector.model.checking.java.junit.strategy.action;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiKeyword;
import com.intellij.psi.impl.source.tree.java.PsiKeywordImpl;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;

import java.util.Arrays;
import java.util.Optional;

public class RemoveStaticModifierFromField implements Action<BestPracticeViolation> {

    private final PsiField psifield;

    public RemoveStaticModifierFromField(PsiField psiField) {
        this.psifield = psiField;
    }


    @Override
    public String getName() {
        return "remove static modifier";
    }

    @Override
    public void execute(BestPracticeViolation bestPracticeViolation) {
        Optional.ofNullable(psifield.getModifierList())
                .ifPresent((modifierList) -> Arrays.stream(modifierList.getChildren()).forEach(element ->
                {
                    if (element instanceof PsiKeywordImpl && ((PsiKeywordImpl) element).getText().equals(PsiKeyword.STATIC)) {
                        element.delete();
                    }
                }));
    }
}
