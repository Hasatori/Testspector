package com.testspector.model.checking.java.junit.strategy.action;

import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTypesUtil;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReplaceTryCatchWithAssertDoesNotThrow implements Action<BestPracticeViolation> {

    private final PsiTryStatement psiTryStatement;

    public ReplaceTryCatchWithAssertDoesNotThrow(PsiTryStatement psiTryStatement) {
        this.psiTryStatement = psiTryStatement;
    }

    @Override
    public String getName() {
        return "Replace try catch statement with assert does not throw";
    }

    @Override
    public void execute(BestPracticeViolation bestPracticeViolation) {
        PsiFile psiFile = psiTryStatement.getContainingFile();

        PsiJavaFile javaFile = (PsiJavaFile) psiFile;
        List<PsiStatement> statements = Optional.ofNullable(psiTryStatement.getTryBlock()).map(tryBlock -> Arrays.asList(tryBlock.getStatements())).orElse(new ArrayList<>());
        boolean isDefaultImportStatement = Optional.ofNullable(javaFile.getImportList())
                .filter(importList -> Arrays.stream(importList.getImportStatements()).anyMatch(psiImportStatement -> "org.junit.jupiter.api.Assertions".equals(psiImportStatement.getQualifiedName())))
                .isPresent();
        String expressionText;
        if (isDefaultImportStatement) {
            expressionText = String.format("Assertions.assertDoesNotThrow(()->{%s})", statements.stream().map(PsiElement::getText).collect(Collectors.joining("")));
        } else {
            expressionText = String.format("assertDoesNotThrow(()->{%s})", statements.stream().map(PsiElement::getText).collect(Collectors.joining("")));
            boolean isStaticImportStatement = Optional.ofNullable(javaFile.getImportList())
                    .map(importList -> Arrays.stream(importList.getImportStaticStatements()).anyMatch(psiImportStatement -> "import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;".equals(psiImportStatement.getText())))
                    .orElse(false);
            if (!isStaticImportStatement) {
                javaFile.getImportList().add(PsiElementFactory.getInstance(psiTryStatement.getProject()).createImportStaticStatement(PsiTypesUtil.getPsiClass(PsiType.getTypeByName("org.junit.jupiter.api.Assertions", psiTryStatement.getProject(), GlobalSearchScope.allScope(psiTryStatement.getProject()))), "assertThrows"));
            }
        }
        PsiElement element = psiTryStatement.replace(PsiElementFactory.getInstance(psiTryStatement.getProject()).createExpressionFromText(expressionText, null));
        element.add(PsiElementFactory.getInstance(element.getProject()).createStatementFromText(";\n", null));
    }


}

