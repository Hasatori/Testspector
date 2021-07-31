package com.testspector.model.checking.java.junit.strategy.action;

import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

public class MakeMethodAndReferencesCatchGeneralLevelException implements Action<BestPracticeViolation> {

    private final PsiMethod method;

    public MakeMethodAndReferencesCatchGeneralLevelException(PsiMethod method) {
        this.method = method;
    }


    @Override
    public String getName() {
        return "Make method and all references catch general level exception";
    }

    @Override
    public void execute(BestPracticeViolation bestPracticeViolation) {
        addThrowsListToAllReferencedMethods(new HashSet<>(), PsiElementFactory.getInstance(method.getProject()), method);
    }

    private void addThrowsListToAllReferencedMethods(HashSet<PsiMethod> visitedMethods, PsiElementFactory psiElementFactory, PsiMethod method) {
        if (!visitedMethods.contains(method)) {
            visitedMethods.add(method);
            method.getThrowsList().replace(psiElementFactory.createReferenceList(new PsiJavaCodeReferenceElement[]{psiElementFactory.createReferenceFromText("Exception", null)}));
            ReferencesSearch.search(method)
                    .findAll()
                    .stream().map(reference -> PsiTreeUtil.getParentOfType(reference.getElement(), PsiMethod.class))
                    .filter(Objects::nonNull)
                    .forEach(met ->
                            {
                                addThrowsListToAllReferencedMethods(visitedMethods, psiElementFactory, met);
                            }
                    );
        }
    }

}
