package com.testspector.model.checking;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.testspector.model.enums.BestPractice;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;

import java.util.List;

public interface  BestPracticeCheckingStrategy<T> {

  List<BestPracticeViolation> checkBestPractices(T checkedElement);

  List<BestPracticeViolation> checkBestPractices(List<T> checkedElements);

  List<BestPractice> getCheckedBestPractice();

}
