package com.testspector.model.checking.java.junit.strategy.action;

import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;

import java.util.Objects;
import java.util.Optional;

public class RemoveTryCatchStatementAction implements Action<BestPracticeViolation> {

    private final String name;
    private final PsiTryStatement tryStatement;
    private final boolean catchStatementAtMethodLevel;

    public RemoveTryCatchStatementAction(PsiTryStatement tryStatement, boolean catchStatementAtMethodLevel) {
        this.catchStatementAtMethodLevel = catchStatementAtMethodLevel;
        if (this.catchStatementAtMethodLevel) {
            this.name = "Remove try catch block and catch exception at method level";
        } else {
            this.name = "Remove try catch block";
        }
        this.tryStatement = tryStatement;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void execute(BestPracticeViolation bestPracticeViolation) {
        if (catchStatementAtMethodLevel) {
            Optional.ofNullable(PsiTreeUtil.getParentOfType(tryStatement, PsiMethod.class)).ifPresent(method -> addThrowsListToAllReferencedMethods(PsiElementFactory.getInstance(tryStatement.getProject()), method));
        }
        Optional.ofNullable(tryStatement.getTryBlock()).map(PsiCodeBlock::getLBrace).ifPresent(PsiElement::delete);
        Optional.ofNullable(tryStatement.getTryBlock()).map(PsiCodeBlock::getRBrace).ifPresent(PsiElement::delete);
        tryStatement.replace(tryStatement.getTryBlock());
    }

    private void addThrowsListToAllReferencedMethods(PsiElementFactory psiElementFactory, PsiMethod method) {
        method.getThrowsList().replace(psiElementFactory.createReferenceList(new PsiJavaCodeReferenceElement[]{psiElementFactory.createReferenceFromText("Exception", null)}));
        ReferencesSearch.search(method)
                .findAll()
                .stream().map(reference -> PsiTreeUtil.getParentOfType(reference.getElement(), PsiMethod.class))
                .filter(Objects::nonNull)
                .forEach(met -> addThrowsListToAllReferencedMethods(psiElementFactory, met));
    }

}
