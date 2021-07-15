package com.testspector.model.checking.java.common.search;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaMethodResolver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class QueriesRepository {

    private static final List<Class<? extends PsiStatement>> SUPPORTED_STATEMENT_CLASSES = Collections.unmodifiableList(Arrays.asList(
            PsiIfStatement.class,
            PsiWhileStatement.class,
            PsiSwitchStatement.class,
            PsiForStatement.class,
            PsiForeachStatement.class
    ));

    private QueriesRepository() {
    }

    private static ElementSearchEngine elementSearchEngine() {
        return new ElementSearchEngine();
    }

    private static JavaContextIndicator contextIndicator() {
        return new JavaContextIndicator();
    }

    private static JavaMethodResolver methodResolver() {
        return new JavaMethodResolver(elementSearchEngine(), contextIndicator());
    }


    public static final ElementSearchQuery<PsiMethodCallExpression> FIND_ALL_ASSERTION_METHOD_CALL_EXPRESSIONS = new ElementSearchQueryBuilder<PsiMethodCallExpression>()
            .elementOfType(PsiMethodCallExpression.class)
            .whereElement(psiMethodCallExpression -> methodResolver().assertionMethod(psiMethodCallExpression).isPresent())
            .whereReferences(el -> !(el instanceof PsiClass) && contextIndicator().isInTestContext().test(el))
            .build();

    public static final ElementSearchQuery<PsiLiteralExpression> FIND_ALL_LITERAL_EXPRESSIONS = new ElementSearchQueryBuilder<PsiLiteralExpression>()
            .elementOfType(PsiLiteralExpression.class)
            .whereReferences(el -> !(el instanceof PsiClass) && contextIndicator().isInTestContext().test(el))
            .build();

    public static final ElementSearchQuery<PsiTryStatement> FIND_ALL_TRY_STATEMENTS = new ElementSearchQueryBuilder<PsiTryStatement>()
            .elementOfType(PsiTryStatement.class)
            .whereReferences(el -> !(el instanceof PsiClass) && contextIndicator().isInTestContext().test(el))
            .build();

    public static final ElementSearchQuery<PsiStatement> FIND_ALL_CONDITIONAL_STATEMENTS = new ElementSearchQueryBuilder<PsiStatement>()
            .elementOfType(PsiStatement.class)
            .whereElement(psiStatement -> SUPPORTED_STATEMENT_CLASSES
                    .stream()
                    .anyMatch(supportedStatement -> supportedStatement.isInstance(psiStatement)))
            .whereReferences(el -> !(el instanceof PsiClass) && contextIndicator().isInTestContext().test(el))
            .build();

    public static final ElementSearchQuery<PsiMethodCallExpression> FIND_ALL_PRODUCTION_CODE_METHOD_CALL_EXPRESSIONS = new ElementSearchQueryBuilder<PsiMethodCallExpression>()
            .elementOfType(PsiMethodCallExpression.class)
            .whereElement(psiMethodCallExpression -> {
                PsiMethod methodFromAssertion = psiMethodCallExpression.resolveMethod();
                return methodFromAssertion != null && contextIndicator().isInProductionCodeContext().test(methodFromAssertion);
            })
            .whereReferences(contextIndicator().isInTestContext())
            .build();

    public static final ElementSearchQuery<PsiMethodCallExpression> FIND_ALL_METHOD_CALL_EXPRESSIONS_THROWING_ANY_EXCEPTION_WITHOUT_REFERENCES = new ElementSearchQueryBuilder<PsiMethodCallExpression>()
            .elementOfType(PsiMethodCallExpression.class)
            .whereElement(psiMethodCallExpression -> {
                PsiMethod method = psiMethodCallExpression.resolveMethod();
                return method != null && method.getThrowsList().getReferenceElements().length > 0;
            })
            .withoutReferences()
            .build();

    public static final ElementSearchQuery<PsiField> FIND_ALL_STATIC_PROPS = new ElementSearchQueryBuilder<PsiField>()
            .elementOfType(PsiField.class)
            .whereElement(psiField -> !(psiField instanceof PsiEnumConstant))
            .whereReferences(el -> (el instanceof PsiMethod || el instanceof PsiField) && contextIndicator().isInTestContext().test(el))
            .build();

    public static final ElementSearchQuery<PsiThrowStatement> FIND_ASSERTION_THROW_STATEMENTS = new ElementSearchQueryBuilder<PsiThrowStatement>()
            .elementOfType(PsiThrowStatement.class)
            .whereElement(psiThrowStatement -> Optional.ofNullable(psiThrowStatement.getException())
                    .map(PsiExpression::getType)
                    .filter(psiType -> "java.lang.AssertionError"
                            .equals(Optional.ofNullable(((PsiClassReferenceType) psiType).resolve())
                                    .map(PsiClass::getQualifiedName).orElse("")))
                    .isPresent())
            .whereReferences(el -> !(el instanceof PsiClass))
            .build();
}