package com.testspector.model.checking.java.junit.strategy.action;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTypesUtil;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WrapAllAssertionsIntoAssertAll implements Action<BestPracticeViolation> {

    private final PsiMethod testMethod;
    private final List<PsiMethodCallExpression> allAssertionMethodCalls;

    public WrapAllAssertionsIntoAssertAll(PsiMethod testMethod, List<PsiMethodCallExpression> allAssertionMethodCalls) {
        this.testMethod = testMethod;
        this.allAssertionMethodCalls = allAssertionMethodCalls;
    }


    @Override
    public String getName() {
        return "Wrap all assertions into assertAll method and place at the end of the test";
    }

    @Override
    public void execute(BestPracticeViolation bestPracticeViolation) {
        Optional.ofNullable(testMethod.getBody())
                .ifPresent(psiCodeBlock -> {
                    PsiJavaFile file = (PsiJavaFile) testMethod.getContainingFile();
                    Project project = testMethod.getProject();
                    boolean isDefaultImportStatement = Optional.ofNullable(file.getImportList())
                            .filter(importList -> Arrays.stream(importList.getImportStatements()).anyMatch(psiImportStatement -> "org.junit.jupiter.api.Assertions".equals(psiImportStatement.getQualifiedName())))
                            .isPresent();
                    if (!isDefaultImportStatement) {
                        Optional.ofNullable(file.getImportList()).ifPresent(importList ->
                                importList.add(PsiElementFactory.getInstance(project)
                                        .createImportStatement(PsiTypesUtil.getPsiClass(PsiType.getTypeByName("org.junit.jupiter.api.Assertions", project, GlobalSearchScope.allScope(project))))));
                    }
                    String expressionText = String.format(
                            "Assertions.assertAll(\n%s\n)", allAssertionMethodCalls.stream().distinct().map(assertionMethod -> String.format("() -> %s", assertionMethod.getText())).collect(Collectors.joining(",\n")));
                    psiCodeBlock.add(PsiElementFactory.getInstance(testMethod.getProject()).createStatementFromText(expressionText, null));
                    psiCodeBlock.add(PsiElementFactory.getInstance(testMethod.getProject()).createStatementFromText(";", null));
                    allAssertionMethodCalls.forEach(PsiElement::delete);
                });
    }
}
