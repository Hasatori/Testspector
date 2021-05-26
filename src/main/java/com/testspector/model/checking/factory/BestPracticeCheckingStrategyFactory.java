package com.testspector.model.checking.factory;

import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.enums.BestPractice;

public interface BestPracticeCheckingStrategyFactory {

    BestPracticeCheckingStrategy<PsiElement> getBestPracticeCheckingStrategy();

    BestPracticeCheckingStrategy<PsiElement> getBestPracticeCheckingStrategy(BestPractice bestPractice);
}
