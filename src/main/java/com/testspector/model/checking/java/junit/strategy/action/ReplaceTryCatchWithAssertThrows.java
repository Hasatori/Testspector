package com.testspector.model.checking.java.junit.strategy.action;

import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTypesUtil;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.ElementUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ReplaceTryCatchWithAssertThrows implements Action<BestPracticeViolation> {

    private final PsiTryStatement psiTryStatement;
    private final HashMap<PsiType, List<PsiMethodCallExpression>> exceptionMethodCallsMap;

    public ReplaceTryCatchWithAssertThrows(PsiTryStatement psiTryStatement, HashMap<PsiType, List<PsiMethodCallExpression>> exceptionMethodCallsMap) {
        this.psiTryStatement = psiTryStatement;
        this.exceptionMethodCallsMap = exceptionMethodCallsMap;
    }

    @Override
    public String getName() {
        return "Replace try catch statement with assert throws";
    }

    @Override
    public void execute(BestPracticeViolation bestPracticeViolation) {
        PsiFile psiFile = psiTryStatement.getContainingFile();
        if (psiFile instanceof PsiJavaFile) {
            PsiJavaFile javaFile = (PsiJavaFile) psiFile;
            List<PsiElement> elements = new ArrayList<>();
            Optional.ofNullable(psiTryStatement.getTryBlock())
                    .map(tryBlock -> Arrays.asList(tryBlock.getStatements()))
                    .ifPresent(statements -> statements.forEach(statement -> exceptionMethodCallsMap.forEach((psiType, psiMethodCallExpressions) -> {
                        for (PsiMethodCallExpression psiMethodCallExpression : psiMethodCallExpressions) {
                            if (ElementUtils.containsElement(statement, psiMethodCallExpression)) {
                                boolean isDefaultImportStatement = Optional.ofNullable(javaFile.getImportList())
                                        .filter(importList -> Arrays.stream(importList.getImportStatements()).anyMatch(psiImportStatement -> "org.junit.jupiter.api.Assertions".equals(psiImportStatement.getQualifiedName())))
                                        .isPresent();
                                String expressionText;
                                if (isDefaultImportStatement) {
                                    expressionText = String.format("Assertions.assertThrows(%s.class,()->{%s})", psiType.getPresentableText(), psiMethodCallExpressions.stream().map(PsiElement::getText).collect(Collectors.joining(";\n", "", ";")));
                                } else {
                                    expressionText = String.format("assertThrows(%s.class,()->{%s})", psiType.getPresentableText(), psiMethodCallExpressions.stream().map(PsiElement::getText).collect(Collectors.joining(";\n", "", ";")));
                                    boolean isStaticImportStatement = Optional.ofNullable(javaFile.getImportList())
                                            .filter(importList -> Arrays.stream(importList.getImportStaticStatements()).anyMatch(psiImportStatement -> "import static org.junit.jupiter.api.Assertions.assertThrows;".equals(psiImportStatement.getText())))
                                            .isPresent();
                                    if (!isStaticImportStatement) {
                                        javaFile.getImportList().add(PsiElementFactory.getInstance(psiTryStatement.getProject()).createImportStaticStatement(PsiTypesUtil.getPsiClass(PsiType.getTypeByName("org.junit.jupiter.api.Assertions", psiTryStatement.getProject(), GlobalSearchScope.allScope(psiTryStatement.getProject()))), "assertThrows"));
                                    }
                                }
                                elements.add(PsiElementFactory.getInstance(javaFile.getProject()).createExpressionFromText(expressionText, null));
                                elements.add(PsiElementFactory.getInstance(javaFile.getProject()).createStatementFromText(";\n", null));
                            } else {
                                elements.add(PsiElementFactory.getInstance(javaFile.getProject()).createStatementFromText(statement.getText(), null));
                            }
                        }
                    })));
            PsiElement toAdd = null;
            int startOffset=0;
            int endOffset=0;
            for (int i = 0; i < elements.size(); i++) {
                if (i == 0) {
                    toAdd = psiTryStatement.replace(elements.get(i));
                    startOffset = toAdd.getTextOffset();
                } else {
                    toAdd = toAdd.add(elements.get(i));
                }
                if (i == elements.size()-1){
                    endOffset = toAdd.getTextRange().getEndOffset();
                }
            }
            CodeStyleManager.getInstance(psiFile.getProject()).reformatText(psiFile,startOffset,endOffset);

        }
    }
}
