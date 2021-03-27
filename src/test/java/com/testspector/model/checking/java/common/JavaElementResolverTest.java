package com.testspector.model.checking.java.common;

import com.intellij.lang.Language;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiPackageBase;
import com.testspector.model.checking.java.JavaTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;

public class JavaElementResolverTest extends JavaTest {


    private JavaElementResolver javaElementResolver;

    @BeforeEach
    public void beforeEach() {
        this.javaElementResolver = new JavaElementResolver();
    }

    @Test
    public void allChildrenOfType_searchingForIfStatementAndElementContainsTwo_ShouldReturnListWithTwoIfStatements() {
        PsiMethod method = this.javaTestElementUtil.createMethod("testMethod", "String", Collections.singletonList("public"));
        PsiIfStatement firstPsiIfStatement = (PsiIfStatement) method.add(this.psiElementFactory.createStatementFromText("if(true){}", null));
        PsiIfStatement secondPsiIfStatement = (PsiIfStatement) firstPsiIfStatement.getChildren()[4].getChildren()[0].add(this.psiElementFactory.createStatementFromText("if(true){}", null));

        List<PsiIfStatement> result = javaElementResolver.allChildrenOfTypeMeetingConditionWithReferences(method, PsiIfStatement.class);

        assertAll(
                () -> assertSame("First if statement was not found!", firstPsiIfStatement, result.get(0)),
                () -> assertSame("Second if statement was not found!", secondPsiIfStatement, result.get(1))
        );
    }

    @Test
    public void allChildrenOfType_searchingForTryStatementAndElementContainsNone_ShouldReturnEmptyList() {
        PsiMethod method = this.javaTestElementUtil.createMethod("testMethod", "String", Collections.singletonList("public"));
        PsiIfStatement firstPsiIfStatement = (PsiIfStatement) method.add(this.psiElementFactory.createStatementFromText("if(true){}", null));
        firstPsiIfStatement.getChildren()[4].getChildren()[0].add(this.psiElementFactory.createStatementFromText("if(true){}", null));

        List<PsiTryStatement> result = javaElementResolver.allChildrenOfTypeMeetingConditionWithReferences(method, PsiTryStatement.class);

        assertTrue(result.isEmpty());
    }

    @Test
    public void allChildrenOfType_searchingForTryStatementWithReferencesFromMethodsAndOneTryStatementIsInTheReferencedMethod_ShouldReturnTryStatement() {
        PsiClass psiClass = this.psiElementFactory.createClass("Test");
        PsiMethod searchStartElement = (PsiMethod) psiClass.add(this.javaTestElementUtil.createMethod("testMethod", "String", Collections.singletonList("public")));
        String referencedMethodName = "referencedMethod";
        PsiMethod referencedMethod = (PsiMethod) psiClass.add(this.javaTestElementUtil.createMethod(referencedMethodName, "String", Collections.singletonList("public")));
        PsiTryStatement psiTryStatement = (PsiTryStatement) referencedMethod.getBody().add(this.psiElementFactory
                .createStatementFromText("try {}catch (Exception e){}", null));
        searchStartElement.getBody().add(this.psiElementFactory.createExpressionFromText(String.format("%s()", referencedMethodName), psiClass));

        List<PsiTryStatement> result = javaElementResolver.allChildrenOfTypeWithReferences(searchStartElement, PsiTryStatement.class, (element -> element instanceof PsiMethod));

        assertAll(
                () -> assertSame("Only one try statement should be found!", 1, result.size()),
                () -> assertSame(psiTryStatement, result.get(0))
        );
    }

    @Test
    public void allChildrenOfType_searchingForSwitchStatementWithReferencesFromMethodsAndReferencedMethodContainsOneAndUsesRecursionCallToItself_ShouldReturnSwitchStatement() {
        PsiClass psiClass = this.psiElementFactory.createClass("Test");
        PsiMethod searchStartElement = (PsiMethod) psiClass.add(this.javaTestElementUtil.createMethod("testMethod", "String", Collections.singletonList("public")));
        String referencedMethodName = "referencedMethod";
        PsiMethod referencedMethod = (PsiMethod) psiClass.add(this.javaTestElementUtil.createMethod(referencedMethodName, "String", Collections.singletonList("public")));
        PsiSwitchStatement psiSwitchStatement = (PsiSwitchStatement) referencedMethod.getBody().add(this.psiElementFactory
                .createStatementFromText("switch (\"\"){}", null));
        referencedMethod.getBody().add(this.psiElementFactory.createExpressionFromText(String.format("%s()", referencedMethodName), psiClass));
        searchStartElement.getBody().add(this.psiElementFactory.createExpressionFromText(String.format("%s()", referencedMethodName), psiClass));

        List<PsiSwitchStatement> result = javaElementResolver.allChildrenOfTypeWithReferences(searchStartElement, PsiSwitchStatement.class, (element -> element instanceof PsiMethod));

        assertAll(
                () -> assertSame("Only one switch statement should be found!", 1, result.size()),
                () -> assertSame(psiSwitchStatement, result.get(0))
        );
    }

    @Test
    public void allChildrenOfType_searchingForMethodsWithReferencesFromClassesInSpecificPackageAndReferencedMethodIsNotInTheClassWithThePackage_ShouldReturnEmptyList() {
        PsiClass psiClass = this.psiElementFactory.createClass("Test");
        PsiMethod searchStartElement = (PsiMethod) psiClass.add(this.javaTestElementUtil.createMethod("testMethod", "String", Collections.singletonList("public")));
        String referencedMethodName = "referencedMethod";
        psiClass.add(this.javaTestElementUtil.createMethod(referencedMethodName, "String", Collections.singletonList("public")));
        searchStartElement.getBody().add(this.psiElementFactory.createExpressionFromText(String.format("%s()", referencedMethodName), psiClass));

        List<PsiMethod> result = javaElementResolver.allChildrenOfTypeWithReferences(
                searchStartElement,
                PsiMethod.class,
                (element -> element instanceof PsiMethod &&
                        "com.org".equals(((PsiMethod) element).getContainingClass().getQualifiedName())));

        assertTrue(result.isEmpty());
    }

    @Test
    public void allChildrenOfType_searchingForForStatementWhichContainsIfStatementWithReferencesFromMethodsWhichNameStartsWithAssertAndReferencedMethodContainsOne_ShouldReturnForStatement() {
        PsiClass psiClass = this.psiElementFactory.createClass("Test");
        PsiMethod searchStartElement = (PsiMethod) psiClass.add(this.javaTestElementUtil.createMethod("testMethod", "String", Collections.singletonList("public")));
        String referencedMethodName = "assertMethod";
        PsiMethod referencedMethod = (PsiMethod) psiClass.add(this.javaTestElementUtil.createMethod(referencedMethodName, "String", Collections.singletonList("public")));
        PsiForStatement psiForStatement = (PsiForStatement) referencedMethod.getBody().add(this.javaTestElementUtil.createForStatement());
        psiForStatement.getBody().getChildren()[0].add(this.javaTestElementUtil.createIfStatement());
        searchStartElement.getBody().add(this.psiElementFactory.createExpressionFromText(String.format("%s()", referencedMethodName), psiClass));

        List<PsiForStatement> result = javaElementResolver.allChildrenOfTypeMeetingConditionWithReferences(
                searchStartElement,
                PsiForStatement.class,
                (forStatement) -> javaElementResolver.allChildrenOfTypeMeetingConditionWithReferences(forStatement, PsiIfStatement.class).size() > 0,
                (element -> element instanceof PsiMethod && ((PsiMethod) element).getName().startsWith("assert")));

        assertAll(
                () -> assertSame("Only one try statement should be found!", 1, result.size()),
                () -> assertSame(psiForStatement, result.get(0))
        );
    }

    @Test
    public void allChildrenOfType_searchingForForStatementWhichContainsIfStatementWithReferencesToMethodsAndThereIsOneForStatementButWithoutIfStatement_ShouldReturnEmptyList() {
        PsiMethod searchStartElement = this.javaTestElementUtil.createMethod("testMethod", "String", Collections.singletonList("public"));
        searchStartElement.getBody().add(this.javaTestElementUtil.createForStatement());

        List<PsiForStatement> result = javaElementResolver.allChildrenOfTypeMeetingConditionWithReferences(
                searchStartElement,
                PsiForStatement.class,
                (forStatement) -> javaElementResolver.allChildrenOfTypeMeetingConditionWithReferences(forStatement, PsiIfStatement.class).size() > 0,
                (element -> element instanceof PsiMethod));

        assertTrue(result.isEmpty());
    }

    @Test
    public void allChildrenOfType_searchingForForStatementWithReferencesFromMethodsAndThereIsOneReferenceButItIsNull_ShouldReturnEmpty() {
        PsiClass psiClass = this.psiElementFactory.createClass("Test");
        PsiMethod searchStartElement = (PsiMethod) psiClass.add(this.javaTestElementUtil.createMethod("testMethod", "String", Collections.singletonList("public")));
        searchStartElement.getBody().add(this.psiElementFactory.createExpressionFromText("reference()", psiClass));

        List<PsiForStatement> result = javaElementResolver.allChildrenOfTypeWithReferences(
                searchStartElement,
                PsiForStatement.class,
                (element -> element instanceof PsiMethod && ((PsiMethod) element).getName().startsWith("assert")));

        assertTrue(result.isEmpty());
    }

    @Test
    public void allChildrenOfType_searchingForIfStatementInPsiPackageBase_ShouldReturnEmptyList() {
        PsiPackageBase searchStartElement = createSomePsiPackageBase();

        List<PsiForStatement> result = javaElementResolver.allChildrenOfTypeMeetingConditionWithReferences(
                searchStartElement,
                PsiForStatement.class);

        assertTrue(result.isEmpty());
    }

    @Test
    public void firstImmediateChildIgnoring_IgnoringPsiJavaTokenAndFirstImmediateChildIsForStatement_ShouldReturnForStatement() {
        PsiMethod method = this.javaTestElementUtil.createMethod("TestMethod", "String", Collections.singletonList("public"));
        PsiForStatement firstPsiForStatement = (PsiForStatement) method.getBody().add(this.javaTestElementUtil.createForStatement());

        PsiElement result = javaElementResolver.firstImmediateChildIgnoring(method.getBody(), Collections.singletonList(PsiJavaToken.class)).get();

        assertSame(firstPsiForStatement, result);
    }

    @Test
    public void firstImmediateChildIgnoring_IgnoredListEmptyTokenAndFirstImmediateChildIsPsiModifierList_ShouldReturnPsiModifierList() {
        PsiClass psiClass = this.psiElementFactory.createClass("Test");
        PsiModifierList expectedElement = (PsiModifierList) psiClass.getChildren()[0];

        PsiElement result = javaElementResolver.firstImmediateChildIgnoring(psiClass, Collections.emptyList()).get();

        assertSame(expectedElement, result);
    }


    private PsiPackageBase createSomePsiPackageBase() {
        return new PsiPackageBase(getPsiManager(), "com.test") {
            @Override
            protected Collection<PsiDirectory> getAllDirectories(boolean includeLibrarySources) {
                return null;
            }

            @Override
            protected PsiPackageBase findPackage(String qName) {
                return null;
            }

            @NotNull
            @Override
            public Language getLanguage() {
                return null;
            }
        };
    }

}
