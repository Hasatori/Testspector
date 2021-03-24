package com.testspector.model.checking.java.junit.strategy;

import com.intellij.psi.*;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.RelatedElementWrapper;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaElementResolver;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.enums.BestPractice;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NoGlobalStaticPropertiesJUnitCheckingStrategy implements BestPracticeCheckingStrategy<PsiMethod> {

    private final JavaElementResolver elementResolver;
    private final JavaMethodResolver methodResolver;
    private final JavaContextIndicator contextIndicator;

    public NoGlobalStaticPropertiesJUnitCheckingStrategy(JavaElementResolver elementResolver, JavaMethodResolver methodResolver, JavaContextIndicator contextIndicator) {
        this.elementResolver = elementResolver;
        this.methodResolver = methodResolver;
        this.contextIndicator = contextIndicator;
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiMethod method) {
        return checkBestPractices(Collections.singletonList(method));
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiMethod> methods) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();


        for (PsiMethod method : methods) {
            List<PsiField> staticProperties = elementResolver
                    .allChildrenOfTypeMeetingConditionWithReferences(method, PsiField.class,(psiField -> !(psiField instanceof PsiEnumConstant)), contextIndicator.isInTestContext())
                    .stream()
                    .filter(isStaticAndNotFinal())
                    .collect(Collectors.toList());
            PsiIdentifier methodIdentifier = method.getNameIdentifier();
            if (staticProperties.size() > 0) {
                bestPracticeViolations.add(new BestPracticeViolation(
                        String.format("%s#%s", method.getContainingClass().getQualifiedName(), method.getName()),
                        method,
                        methodIdentifier != null ? methodIdentifier.getTextRange() : method.getTextRange(),
                        "Global static properties should not be part of a test. Tests are sharing the reference and if some of them would update it it might influence behaviour of other tests.",
                        Arrays.asList(
                                "If the property is immutable e.g.,String, Integer, Byte, Character etc. then you can add 'final' identifier so that tests can not change reference",
                                "If the property is mutable then delete static modifier and make property reference unique for each test."),
                        getCheckedBestPractice().get(0),
                        createRelatedElements(method, staticProperties)
                ));
            }

        }

        return bestPracticeViolations;
    }

    private Predicate<PsiField> isStaticAndNotFinal() {
        return psiField -> {
            PsiModifierList modifierList = psiField.getModifierList();
            if (modifierList != null) {
                return modifierList.hasModifierProperty(PsiModifier.STATIC) && !modifierList.hasExplicitModifier(PsiModifier.FINAL);
            }
            return false;
        };
    }

    private List<RelatedElementWrapper> createRelatedElements(PsiMethod method, List<PsiField> staticProperties) {
        List<RelatedElementWrapper> result = new ArrayList<>();
        for (PsiField staticProperty : staticProperties) {
            HashMap<PsiElement, String> elementNameHashMap = new HashMap<>();
            Optional<PsiReferenceExpression> optionalPsiReferenceExpression = firstReferenceToGlobalStaticProperty(method, staticProperty);
            if (optionalPsiReferenceExpression.isPresent()) {
                elementNameHashMap.put(optionalPsiReferenceExpression.get(), "property reference from test method");
            }
            elementNameHashMap.put(staticProperty, "property");
            result.add(new RelatedElementWrapper(staticProperty.getName(), elementNameHashMap));
        }

        return result;
    }


    private Optional<PsiReferenceExpression> firstReferenceToGlobalStaticProperty(PsiElement element, PsiField psiField) {
        List<PsiReferenceExpression> references = elementResolver.allChildrenOfTypeMeetingConditionWithReferences(element, PsiReferenceExpression.class);
        for (PsiReferenceExpression reference : references) {
            if (!elementResolver.allChildrenOfTypeMeetingConditionWithReferences(reference.getParent(), PsiField.class, field -> psiField == field, contextIndicator.isInTestContext()).isEmpty()) {
                return Optional.of(reference);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.NO_GLOBAL_STATIC_PROPERTIES);
    }
}
