package com.testspector.model.checking.java;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.*;

import java.util.List;
import java.util.stream.Collectors;

public class JavaTestElementUtil {

    private final PsiElementFactory psiElementFactory;
    private final PsiFileFactory psiFileFactory;

    public JavaTestElementUtil(PsiFileFactory psiFileFactory, PsiElementFactory psiElementFactory) {
        this.psiFileFactory = psiFileFactory;
        this.psiElementFactory = psiElementFactory;
    }

    public PsiIfStatement createIfStatement() {
        return (PsiIfStatement) psiElementFactory.createStatementFromText("if(true){}else if(false){}", null);
    }

    public PsiForStatement createForStatement() {
        return (PsiForStatement) psiElementFactory.createStatementFromText("" +
                "for (int i = 0; i < 50; i++) {\n}", null);
    }

    public PsiJavaFile createFile(String name, String packageName, List<String> importStatements, List<String> staticImportStatementList) {
        String packageDeclaration = String.format("package %s;", packageName);
        String statementsDeclaration = importStatements
                .stream()
                .map(importStatement -> String.format("import %s", importStatement))
                .collect(Collectors.joining(";"));
        String staticStatementsDeclaration = staticImportStatementList
                .stream()
                .map(staticImportStatement -> String.format("import static %s", staticImportStatement))
                .collect(Collectors.joining(";"));
        return (PsiJavaFile) psiFileFactory.createFileFromText(name, JavaLanguage.INSTANCE, String.format("%s\n%s\n%s\n", packageDeclaration, statementsDeclaration, staticStatementsDeclaration));
    }

    public PsiMethod createMethod(String name, String returnType, List<String> accessModifiers) {
        return createMethod(name, returnType, null, accessModifiers);
    }

    public PsiMethod createMethod(String name, String returnType, String content, List<String> accessModifiers) {
        return psiElementFactory.createMethodFromText(String.format("%s %s %s(){%s}", String.join(" ", accessModifiers), returnType, name, content).trim(), null);
    }

    public PsiMethod createTestMethod(String name, List<String> annotations) {
        return psiElementFactory.createMethodFromText(String.format("" +
                        "%s\n" +
                        "public void %s(){}", String.join("\n", annotations), name)
                , null);

    }

}
