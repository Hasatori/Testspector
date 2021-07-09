package com.testspector.model.checking.java.common.search;

import com.intellij.psi.*;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaMethodResolver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public final class QueriesRepository {

    private static final JavaContextIndicator CONTEXT_INDICATOR = new JavaContextIndicator();
    protected static final ElementSearchEngine ELEMENT_RESOLVER = new ElementSearchEngine();
    private static final JavaMethodResolver METHOD_RESOLVER = new JavaMethodResolver(ELEMENT_RESOLVER, CONTEXT_INDICATOR);
    private static final List<Class<? extends PsiStatement>> SUPPORTED_STATEMENT_CLASSES = Collections.unmodifiableList(Arrays.asList(
            PsiIfStatement.class,
            PsiWhileStatement.class,
            PsiSwitchStatement.class,
            PsiForStatement.class,
            PsiForeachStatement.class
    ));

    private QueriesRepository() {
    }

    public static final ElementSearchQuery<PsiMethodCallExpression> FIND_ALL_ASSERTION_METHOD_CALL_EXPRESSIONS = new ElementSearchQueryBuilder<PsiMethodCallExpression>()
            .elementOfType(PsiMethodCallExpression.class)
            .whereElement(psiMethodCallExpression -> METHOD_RESOLVER.assertionMethod(psiMethodCallExpression).isPresent())
            .whereReferences(el -> !(el instanceof PsiClass) && CONTEXT_INDICATOR.isInTestContext().test(el))
            .build();

    public static final ElementSearchQuery<PsiLiteralExpression> FIND_ALL_LITERAL_EXPRESSIONS = new ElementSearchQueryBuilder<PsiLiteralExpression>()
            .elementOfType(PsiLiteralExpression.class)
            .whereReferences(el ->!(el instanceof PsiClass) && CONTEXT_INDICATOR.isInTestContext().test(el))
            .build();

    public static final ElementSearchQuery<PsiTryStatement> FIND_ALL_TRY_STATEMENTS = new ElementSearchQueryBuilder<PsiTryStatement>()
            .elementOfType(PsiTryStatement.class)
            .whereReferences(el ->!(el instanceof PsiClass) && CONTEXT_INDICATOR.isInTestContext().test(el))
            .build();

    public static final ElementSearchQuery<PsiStatement> FIND_ALL_CONDITIONAL_STATEMENTS = new ElementSearchQueryBuilder<PsiStatement>()
            .elementOfType(PsiStatement.class)
            .whereElement(psiStatement -> SUPPORTED_STATEMENT_CLASSES
                    .stream()
                    .anyMatch(supportedStatement -> supportedStatement.isInstance(psiStatement)))
            .whereReferences(el ->!(el instanceof PsiClass) && CONTEXT_INDICATOR.isInTestContext().test(el))
            .build();

    public static final ElementSearchQuery<PsiMethodCallExpression> FIND_ALL_TESTED_METHOD_CALL_EXPRESSIONS = new ElementSearchQueryBuilder<PsiMethodCallExpression>()
            .elementOfType(PsiMethodCallExpression.class)
            .whereElement(psiMethodCallExpression -> {
                PsiMethod methodFromAssertion = psiMethodCallExpression.resolveMethod();
                return methodFromAssertion != null && CONTEXT_INDICATOR.isInProductionCodeContext().test(methodFromAssertion);
            })
            .whereReferences(CONTEXT_INDICATOR.isInTestContext())
            .build();

    public static final ElementSearchQuery<PsiField> FIND_ALL_STATIC_NOT_FINAL_PROPS = new ElementSearchQueryBuilder<PsiField>()
            .elementOfType(PsiField.class)
            .whereElement(psiField -> !(psiField instanceof PsiEnumConstant) && isStaticAndNotFinal().test(psiField))
            .whereReferences(el -> (el instanceof PsiMethod || el instanceof PsiField) && CONTEXT_INDICATOR.isInTestContext().test(el))
            .build();

    private static Predicate<PsiField> isStaticAndNotFinal() {
        return psiField -> {
            PsiModifierList modifierList = psiField.getModifierList();
            if (modifierList != null) {
                return modifierList.hasModifierProperty(PsiModifier.STATIC) && !modifierList.hasExplicitModifier(PsiModifier.FINAL);
            }
            return false;
        };
    }

}
