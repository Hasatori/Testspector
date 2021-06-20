package com.testspector.model.checking.java.junit.strategy;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaElementResolver;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.enums.BestPractice;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TestOnlyPublicBehaviourJUnitCheckingStrategy implements BestPracticeCheckingStrategy<PsiMethod> {


    private final JavaElementResolver elementResolver;
    private final JavaMethodResolver methodResolver;
    private final JavaContextIndicator contextIndicator;

    public TestOnlyPublicBehaviourJUnitCheckingStrategy(JavaElementResolver elementResolver, JavaMethodResolver methodResolver, JavaContextIndicator contextIndicator) {
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

        for (PsiMethod testMethod : methods) {
            List<PsiMethodCallExpression> nonPublicTestedMethodsFromMethodCallExpressions = methodResolver.allTestedMethodsExpressions(testMethod)
                    .stream()
                    .filter(methodCall -> {
                        PsiMethod testedMethod = methodCall.resolveMethod();
                        return methodHasModifier(testedMethod, PsiModifier.PROTECTED) ||
                                isMethodPackagePrivate(testedMethod) ||
                                methodHasModifier(testedMethod, PsiModifier.PRIVATE);

                    }).collect(Collectors.toList());
            ;
            nonPublicTestedMethodsFromMethodCallExpressions.forEach(nonPublicFromMethodCallExpression -> {
                bestPracticeViolations.add(new BestPracticeViolation(
                        nonPublicFromMethodCallExpression,
                        "Not public method called by a method call",
                        this.getCheckedBestPractice().get(0),
                        Collections.singletonList(makePublicQuickFix(nonPublicFromMethodCallExpression.resolveMethod(),nonPublicFromMethodCallExpression.getMethodExpression())
                        )));
            });

            List<PsiReference> nonPublicTestedMethodsFromReferences = methodResolver.allTestedMethodsFromReference(testMethod)
                    .stream()
                    .filter(reference -> {
                        PsiMethod testedMethod = (PsiMethod) reference.resolve();
                        return methodHasModifier(testedMethod, PsiModifier.PROTECTED) ||
                                isMethodPackagePrivate(testedMethod) ||
                                methodHasModifier(testedMethod, PsiModifier.PRIVATE);
                    }).collect(Collectors.toList());
            nonPublicTestedMethodsFromReferences.forEach(reference -> {
                bestPracticeViolations.add(new BestPracticeViolation(
                        reference.getElement(),
                        "Not public method called by a method call",
                        this.getCheckedBestPractice().get(0),
                        Collections.singletonList(makePublicQuickFix((PsiMethod) reference.resolve(),reference)
                        )));
            });

        }

        return bestPracticeViolations;
    }

    private boolean methodHasModifier(PsiMethod method, String modifier) {
        return method.getModifierList().hasModifierProperty(modifier);
    }

    private boolean isMethodPackagePrivate(PsiMethod method) {
        PsiModifierList modifierList = method.getModifierList();
        return !modifierList.hasModifierProperty(PsiModifier.PUBLIC) &&
                !modifierList.hasModifierProperty(PsiModifier.PRIVATE) &&
                !modifierList.hasModifierProperty(PsiModifier.PROTECTED);
    }

    private Action<BestPracticeViolation> makePublicQuickFix(PsiMethod method,PsiReference psiReference){
      return new Action<>() {
          @Override
          public
          @NotNull
          String getName() {
              return String.format("Make %s public",method.getName());
          }

          @Override
          public void execute(BestPracticeViolation bestPracticeViolation) {
              method.replace(PsiElementFactory.getInstance(method.getProject()).createMethodFromText("public " + method.getText(), null));
              ((Navigatable) psiReference.resolve().getNavigationElement()).navigate(true);
          }
      };
    }

    @Override
    public List<BestPractice> getCheckedBestPractice() {
        return Collections.singletonList(BestPractice.TEST_ONLY_PUBLIC_BEHAVIOUR);
    }
}
