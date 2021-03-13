package com.testspector.model.checking;

import com.intellij.psi.PsiElement;

import java.util.List;
import java.util.Optional;

public abstract class InspectionInvocationLineResolveStrategy {

    public abstract Optional<PsiElement> resolveInspectionInvocationLine(PsiElement element);

}
