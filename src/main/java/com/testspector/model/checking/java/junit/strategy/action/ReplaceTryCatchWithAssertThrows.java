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
            List<PsiElement> replacingElements = createReplacingElements(javaFile);
            PsiElement toAdd = null;
            int startOffset = 0;
            int endOffset = 0;
            for (int i = 0; i < replacingElements.size(); i++) {
                if (i == 0) {
                    toAdd = psiTryStatement.replace(replacingElements.get(i));
                    startOffset = toAdd.getTextOffset();
                } else {
                    toAdd = toAdd.add(replacingElements.get(i));
                }
                if (i == replacingElements.size() - 1) {
                    endOffset = toAdd.getTextRange().getEndOffset();
                }
            }
            CodeStyleManager.getInstance(psiFile.getProject()).reformatText(psiFile, startOffset, endOffset);

        }
    }

    private List<PsiElement> createReplacingElements(PsiJavaFile javaFile) {
        List<PsiElement> replacingElements = new ArrayList<>();
        Optional.ofNullable(psiTryStatement.getTryBlock())
                .map(tryBlock -> Arrays.asList(tryBlock.getStatements()))
                .ifPresent(statements -> statements.forEach(statement -> exceptionMethodCallsMap.forEach((psiType, psiMethodCallExpressions) -> {
                    for (PsiMethodCallExpression psiMethodCallExpression : psiMethodCallExpressions) {
                        if (ElementUtils.containsElement(statement, psiMethodCallExpression)) {
                            String expressionText;
                            if (containsAssertionsClassImportStatement(javaFile)) {
                                expressionText = String.format("Assertions.assertThrows(%s.class,()->{%s})", psiType.getPresentableText(), psiMethodCallExpressions.stream().map(PsiElement::getText).collect(Collectors.joining(";\n", "", ";")));
                            } else {
                                expressionText = String.format("assertThrows(%s.class,()->{%s})", psiType.getPresentableText(), psiMethodCallExpressions.stream().map(PsiElement::getText).collect(Collectors.joining(";\n", "", ";")));
                                if (!containsAssertDoesThrowStaticImportStatement(javaFile)) {
                                    javaFile.getImportList().add(PsiElementFactory.getInstance(psiTryStatement.getProject())
                                            .createImportStaticStatement(getAssertionsPsiClass(),
                                                    "assertThrows"));
                                }
                            }
                            replacingElements.add(PsiElementFactory.getInstance(javaFile.getProject()).createExpressionFromText(expressionText, null));
                            replacingElements.add(PsiElementFactory.getInstance(javaFile.getProject()).createStatementFromText(";\n", null));
                        } else {
                            replacingElements.add(PsiElementFactory.getInstance(javaFile.getProject()).createStatementFromText(statement.getText(), null));
                        }
                    }
                })));
        return replacingElements;
    }

    private boolean containsAssertionsClassImportStatement(PsiJavaFile javaFile) {
        return Optional.ofNullable(javaFile.getImportList())
                .filter(importList -> Arrays.stream(importList.getImportStatements())
                        .anyMatch(psiImportStatement -> "org.junit.jupiter.api.Assertions".equals(psiImportStatement.getQualifiedName())))
                .isPresent();
    }

    private boolean containsAssertDoesThrowStaticImportStatement(PsiJavaFile javaFile) {
        return Optional.ofNullable(javaFile.getImportList())
                .filter(importList -> Arrays.stream(importList.getImportStaticStatements())
                        .anyMatch(psiImportStatement -> "import static org.junit.jupiter.api.Assertions.assertThrows;".equals(psiImportStatement.getText())))
                .isPresent();
    }

    private PsiClass getAssertionsPsiClass() {
        return PsiTypesUtil.getPsiClass(PsiType.getTypeByName(
                "org.junit.jupiter.api.Assertions",
                psiTryStatement.getProject(),
                GlobalSearchScope.allScope(psiTryStatement.getProject())));
    }
}
