package com.testspector.model.checking.java.junit.strategy.action;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;

import java.util.HashSet;
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
        Project project = tryStatement.getProject();
        if (catchStatementAtMethodLevel) {
            Optional.ofNullable(PsiTreeUtil.getParentOfType(tryStatement, PsiMethod.class)).ifPresent(method -> addThrowsListToAllReferencedMethods(new HashSet<>(), PsiElementFactory.getInstance(tryStatement.getProject()), method));
        }
        Optional.ofNullable(tryStatement.getTryBlock()).map(PsiCodeBlock::getLBrace).ifPresent(PsiElement::delete);
        Optional.ofNullable(tryStatement.getTryBlock()).map(PsiCodeBlock::getRBrace).ifPresent(PsiElement::delete);
        CodeStyleManager.getInstance(project).reformat( tryStatement.replace(tryStatement.getTryBlock()));

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
