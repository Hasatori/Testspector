package com.testspector.model.checking.java.junit;

import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JUnitBestPracticeCheckingStrategyFactoryTest {


    @Test
    public void getBestPracticeCheckingStrategy_noParams_ShouldReturnGroupBestPracticeCheckingStrategy() {
        JUnitBestPracticeCheckingStrategyFactory jUnitBestPracticeCheckingStrategyFactory = new JUnitBestPracticeCheckingStrategyFactory();

        BestPracticeCheckingStrategy<PsiElement> optionalBestPracticeCheckingStrategy = jUnitBestPracticeCheckingStrategyFactory.getBestPracticeCheckingStrategy();

        assertTrue(optionalBestPracticeCheckingStrategy instanceof JUnitTestMethodBestPracticeCheckingStrategyAdapter);
    }
}
