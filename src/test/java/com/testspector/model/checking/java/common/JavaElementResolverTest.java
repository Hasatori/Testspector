package com.testspector.model.checking.java.common;

import com.intellij.psi.*;
import com.testspector.model.checking.java.JavaTest;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;

public class JavaElementResolverTest extends JavaTest {


    @Test
    public void allChildrenOfType_searchingForIfStatementAndElementContainsTwo_ShouldReturnListWithTwoIfStatements() {
        PsiMethod method = this.javaTestElementUtil.createMethod("TestMethod", "String", Collections.singletonList("public"));
        PsiIfStatement firstPsiIfStatement = (PsiIfStatement) method.add(this.psiElementFactory.createStatementFromText("if(true){}", null));
        PsiIfStatement secondPsiIfStatement = (PsiIfStatement) firstPsiIfStatement.getChildren()[4].getChildren()[0].add(this.psiElementFactory.createStatementFromText("if(true){}", null));

        JavaElementResolver javaElementResolver = new JavaElementResolver();
        List<PsiIfStatement> result = javaElementResolver.allChildrenOfType(method, PsiIfStatement.class);

        assertAll(
                () -> assertSame("First if statement was not found!", firstPsiIfStatement, result.get(0)),
                () -> assertSame("Second if statement was not found!", secondPsiIfStatement, result.get(1))
        );
    }

    @Test
    public void firstImmediateChildIgnoring_IgnoringPsiJavaTokenAndFirstImmediateChildIsForStatement_ShouldReturnForStatement() {
        PsiMethod method = this.javaTestElementUtil.createMethod("TestMethod", "String", Collections.singletonList("public"));
        PsiForStatement firstPsiForStatement = (PsiForStatement) method.getBody().add(this.javaTestElementUtil.createForStatement());

        JavaElementResolver javaElementResolver = new JavaElementResolver();
        PsiElement result = javaElementResolver.firstImmediateChildIgnoring(method.getBody(), Collections.singletonList(PsiJavaToken.class)).get();

        assertSame(firstPsiForStatement, result);
    }

}
