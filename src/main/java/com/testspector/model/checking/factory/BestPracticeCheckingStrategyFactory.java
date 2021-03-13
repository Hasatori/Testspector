package com.testspector.model.checking.factory;

import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeCheckingStrategy;

public interface BestPracticeCheckingStrategyFactory {

    BestPracticeCheckingStrategy<PsiElement> getBestPracticeCheckingStrategy();
}
