package com.testspector.model.checking.java.junit.strategy.action;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;

public class MakeMethodPublicAction implements Action<BestPracticeViolation> {

    private final String name;
    private final PsiMethod method;

    public MakeMethodPublicAction(PsiMethod method) {
        this.name = String.format("Make %s public", method.getName());
        this.method = method;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void execute(BestPracticeViolation bestPracticeViolation) {
        method.getModifierList().setModifierProperty(PsiModifier.PUBLIC, true);
    }
}
