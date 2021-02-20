package com.testspector.model.checking.java.junit.singlebestpracticestrategy;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.java.junit.JUnitSingleBestPracticeCheckingStrategy;

import java.util.ArrayList;
import java.util.List;

public class OnlyOneAssertionJUnitCheckingStrategy implements JUnitSingleBestPracticeCheckingStrategy {

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiElement psiElement) {
        return null;
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiElement> psiElements) {
        return null;
    }
}
