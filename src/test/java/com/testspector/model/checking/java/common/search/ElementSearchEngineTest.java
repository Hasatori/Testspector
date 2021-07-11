package com.testspector.model.checking.java.common.search;

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

public class ElementSearchEngineTest extends JavaTest {


    private ElementSearchEngine javaElementResolver;
    private static final ElementSearchQuery<PsiTryStatement> FIND_ALL_TRY_STATEMENTS = new ElementSearchQueryBuilder<PsiTryStatement>()
            .elementOfType(PsiTryStatement.class)
            .withReferences()
            .build();
    private static final ElementSearchQuery<PsiIfStatement> FIND_ALL_IF_STATEMENTS = new ElementSearchQueryBuilder<PsiIfStatement>()
            .elementOfType(PsiIfStatement.class)
            .withReferences()
            .build();

    @BeforeEach
    public void beforeEach() {
        this.javaElementResolver = new ElementSearchEngine();
    }

    @Test
    public void allChildrenOfType_searchingForIfStatementAndElementContainsTwo_ShouldReturnElementSearchResultWithTwoIfStatements() {
        PsiMethod method = this.javaTestElementUtil.createMethod("testMethod", "String", Collections.singletonList("public"));
        PsiIfStatement firstPsiIfStatement = (PsiIfStatement) method.add(this.psiElementFactory.createStatementFromText("if(true){}", null));
        PsiIfStatement secondPsiIfStatement = (PsiIfStatement) firstPsiIfStatement.getChildren()[4].getChildren()[0].add(this.psiElementFactory.createStatementFromText("if(true){}", null));

        ElementSearchResult<PsiIfStatement> result = javaElementResolver.findByQuery(method, FIND_ALL_IF_STATEMENTS);
        List<PsiIfStatement> statements = result.getElementsFromAllLevels();

        assertAll(
                () -> assertSame("Only two if statements should have been found!", 2, statements.size()),
                () -> assertSame("First if statement was not found!", firstPsiIfStatement, statements.get(0)),
                () -> assertSame("Second if statement was not found!", secondPsiIfStatement, statements.get(1))
        );
    }

    @Test
    public void allChildrenOfType_searchingForTryStatementAndElementContainsNone_ShouldReturnEmptySearchResult() {
        PsiMethod method = this.javaTestElementUtil.createMethod("testMethod", "String", Collections.singletonList("public"));
        PsiIfStatement firstPsiIfStatement = (PsiIfStatement) method.add(this.psiElementFactory.createStatementFromText("if(true){}", null));
        firstPsiIfStatement.getChildren()[4].getChildren()[0].add(this.psiElementFactory.createStatementFromText("if(true){}", null));

        ElementSearchResult<PsiTryStatement> result = javaElementResolver.findByQuery(method, FIND_ALL_TRY_STATEMENTS);

        assertTrue(result.getElementsFromAllLevels().isEmpty());
    }

    @Test
    public void allChildrenOfType_searchingForTryStatementWithReferencesFromMethodsAndOneTryStatementIsInTheReferencedMethod_ShouldReturnElementSearchWithOneTryStatement() {
        PsiClass psiClass = this.psiElementFactory.createClass("Test");
        PsiMethod searchStartElement = (PsiMethod) psiClass.add(this.javaTestElementUtil.createMethod("testMethod", "String", Collections.singletonList("public")));
        String referencedMethodName = "referencedMethod";
        PsiMethod referencedMethod = (PsiMethod) psiClass.add(this.javaTestElementUtil.createMethod(referencedMethodName, "String", Collections.singletonList("public")));
        PsiTryStatement psiTryStatement = (PsiTryStatement) referencedMethod.getBody().add(this.psiElementFactory
                .createStatementFromText("try {}catch (Exception e){}", null));
        searchStartElement.getBody().add(this.psiElementFactory.createExpressionFromText(String.format("%s()", referencedMethodName), psiClass));

        ElementSearchResult<PsiTryStatement> result = javaElementResolver.findByQuery(searchStartElement, FIND_ALL_TRY_STATEMENTS);

        assertAll(
                () -> assertSame("Only one try statement should be found!", 1, result.getElementsFromAllLevels().size()),
                () -> assertSame(psiTryStatement, result.getElementsFromAllLevels().get(0))
        );
    }

    @Test
    public void allChildrenOfType_searchingForSwitchStatementWithReferencesFromMethodsAndReferencedMethodContainsOneAndUsesRecursionCallToItself_ShouldReturnElementSearchWithOneSwitchStatement() {
        PsiClass psiClass = this.psiElementFactory.createClass("Test");
        PsiMethod searchStartElement = (PsiMethod) psiClass.add(this.javaTestElementUtil
                .createMethod("testMethod", "String", Collections.singletonList("public")));
        String referencedMethodName = "referencedMethod";
        PsiMethod referencedMethod = (PsiMethod) psiClass.add(this.javaTestElementUtil
                .createMethod(referencedMethodName, "String", Collections.singletonList("public")));
        PsiSwitchStatement psiSwitchStatement = (PsiSwitchStatement) referencedMethod.getBody().add(this.psiElementFactory
                .createStatementFromText("switch (\"\"){}", null));
        referencedMethod.getBody().add(this.psiElementFactory
                .createExpressionFromText(String.format("%s()", referencedMethodName), psiClass));
        searchStartElement.getBody().add(this.psiElementFactory
                .createExpressionFromText(String.format("%s()", referencedMethodName), psiClass));
        ElementSearchQuery<PsiSwitchStatement> findAllSwitchStatements = new ElementSearchQueryBuilder<PsiSwitchStatement>()
                .elementOfType(PsiSwitchStatement.class)
                .withReferences()
                .build();

        ElementSearchResult<PsiSwitchStatement> result = javaElementResolver.findByQuery(
                searchStartElement, findAllSwitchStatements);

        assertAll(
                () -> assertSame("Only one switch statement should be found!", 1, result.getElementsFromAllLevels().size()),
                () -> assertSame(psiSwitchStatement, result.getElementsFromAllLevels().get(0))
        );
    }

    @Test
    public void allChildrenOfType_searchingForMethodsWithReferencesFromClassesInSpecificPackageAndReferencedMethodIsNotInTheClassWithThePackage_ShouldReturnEmptyList() {
        PsiClass psiClass = this.psiElementFactory.createClass("Test");
        PsiMethod searchStartElement = (PsiMethod) psiClass.add(this.javaTestElementUtil
                .createMethod("testMethod", "String", Collections.singletonList("public")));
        String referencedMethodName = "referencedMethod";
        psiClass.add(this.javaTestElementUtil.createMethod(referencedMethodName, "String", Collections.singletonList("public")));
        searchStartElement.getBody().add(this.psiElementFactory.createExpressionFromText(String.format("%s()", referencedMethodName), psiClass));
        ElementSearchQuery<PsiMethod> findAllMethodsWithReferencesFromSpecificClasses = new ElementSearchQueryBuilder<PsiMethod>()
                .elementOfType(PsiMethod.class)
                .whereReferences(element -> element instanceof PsiMethod &&
                        "com.org".equals(((PsiMethod) element).getContainingClass().getQualifiedName()))
                .build();

        ElementSearchResult<PsiMethod> result = javaElementResolver.findByQuery(searchStartElement, findAllMethodsWithReferencesFromSpecificClasses);

        assertTrue(result.getElementsFromAllLevels().isEmpty());
    }

    @Test
    public void allChildrenOfType_searchingForForStatementWhichContainsIfStatementWithReferencesFromMethodsWhichNameStartsWithAssertAndReferencedMethodContainsOne_ShouldReturnElementSearchWithOneForStatement() {
        PsiClass psiClass = this.psiElementFactory.createClass("Test");
        PsiMethod searchStartElement = (PsiMethod) psiClass.add(this.javaTestElementUtil
                .createMethod("testMethod", "String", Collections.singletonList("public")));
        String referencedMethodName = "assertMethod";
        PsiMethod referencedMethod = (PsiMethod) psiClass.add(this.javaTestElementUtil
                .createMethod(referencedMethodName, "String", Collections.singletonList("public")));
        PsiForStatement psiForStatement = (PsiForStatement) referencedMethod
                .getBody()
                .add(this.javaTestElementUtil.createForStatement());
        psiForStatement.getBody().getChildren()[0].add(this.javaTestElementUtil.createIfStatement());
        searchStartElement.getBody().add(this.psiElementFactory
                .createExpressionFromText(String.format("%s()", referencedMethodName), psiClass));
        ElementSearchQuery<PsiForStatement> findForStatementWhichContainsIfStatementWithReferencesFromMethodsWhichNameStartsWithAssert =
                new ElementSearchQueryBuilder<PsiForStatement>()
                        .elementOfType(PsiForStatement.class)
                        .whereElement((forStatement) -> javaElementResolver.findByQuery(forStatement, FIND_ALL_IF_STATEMENTS).getElementsFromAllLevels().size() > 0)
                        .whereReferences(element -> element instanceof PsiMethod && ((PsiMethod) element).getName().startsWith("assert"))
                        .build();

        ElementSearchResult<PsiForStatement> result = javaElementResolver.findByQuery(
                searchStartElement,
                findForStatementWhichContainsIfStatementWithReferencesFromMethodsWhichNameStartsWithAssert
        );

        assertAll(
                () -> assertSame("Only one try statement should be found!", 1, result.getElementsFromAllLevels().size()),
                () -> assertSame(psiForStatement, result.getElementsFromAllLevels().get(0))
        );
    }

    @Test
    public void allChildrenOfType_searchingForForStatementWhichContainsIfStatementWithReferencesToMethodsAndThereIsOneForStatementButWithoutIfStatement_ShouldReturnEmptyList() {
        PsiMethod searchStartElement = this.javaTestElementUtil
                .createMethod("testMethod", "String", Collections.singletonList("public"));
        searchStartElement.getBody().add(this.javaTestElementUtil.createForStatement());
        ElementSearchQuery<PsiForStatement> findAllForStatementsWhichContainsIfStatements = new ElementSearchQueryBuilder<PsiForStatement>()
                .elementOfType(PsiForStatement.class)
                .whereElement(forStatement ->
                        javaElementResolver.findByQuery(forStatement, FIND_ALL_IF_STATEMENTS)
                                .getElementsFromAllLevels()
                                .size() > 0)
                .whereReferences(element -> element instanceof PsiMethod)
                .build();

        ElementSearchResult<PsiForStatement> result = javaElementResolver.findByQuery(
                searchStartElement, findAllForStatementsWhichContainsIfStatements);

        assertTrue(result.getElementsFromAllLevels().isEmpty());
    }

    @Test
    public void allChildrenOfType_searchingForForStatementWithReferencesFromMethodsAndThereIsOneReferenceButItIsNull_ShouldReturnEmpty() {
        PsiClass psiClass = this.psiElementFactory.createClass("Test");
        PsiMethod searchStartElement = (PsiMethod) psiClass.add(this.javaTestElementUtil
                .createMethod("testMethod", "String", Collections.singletonList("public")));
        searchStartElement.getBody().add(this.psiElementFactory.createExpressionFromText("reference()", psiClass));
        ElementSearchQuery<PsiForStatement> findAllForStatementsWithReferencesFromAssertMethods = new ElementSearchQueryBuilder<PsiForStatement>()
                .elementOfType(PsiForStatement.class)
                .whereReferences(element -> element instanceof PsiMethod && ((PsiMethod) element).getName().startsWith("assert"))
                .build();

        ElementSearchResult<PsiForStatement> result = javaElementResolver.findByQuery(
                searchStartElement, findAllForStatementsWithReferencesFromAssertMethods);

        assertTrue(result.getElementsFromAllLevels().isEmpty());
    }

    @Test
    public void allChildrenOfType_searchingForIfStatementInPsiPackageBase_ShouldReturnEmptyList() {
        PsiPackageBase searchStartElement = createSomePsiPackageBase();

        ElementSearchResult<PsiIfStatement> result = javaElementResolver.findByQuery(
                searchStartElement, FIND_ALL_IF_STATEMENTS);

        assertTrue(result.getElementsFromAllLevels().isEmpty());
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
