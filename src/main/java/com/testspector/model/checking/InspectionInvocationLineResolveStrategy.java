package com.testspector.model.checking;

import com.intellij.psi.PsiElement;

import java.util.List;
import java.util.Optional;

public abstract class InspectionInvocationLineResolveStrategy implements UnitTestFrameworkComponent {

    public abstract Optional<PsiElement> resolveInspectionInvocationLine(PsiElement element);

}
