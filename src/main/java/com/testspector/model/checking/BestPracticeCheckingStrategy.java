package com.testspector.model.checking;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.testspector.model.enums.BestPractice;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;

import java.util.List;

public interface  BestPracticeCheckingStrategy {

  List<BestPracticeViolation> checkBestPractices(PsiElement psiElement);

  List<BestPracticeViolation> checkBestPractices(List<PsiElement> psiElements);

}
