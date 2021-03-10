package com.testspector.model.checking;


import com.intellij.psi.PsiElement;
import com.testspector.model.checking.java.junit.JUnitGroupMethodBestPracticeCheckingStrategyAdapter;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Optional;

@RunWith(JUnitPlatform.class)
public class BestPracticeCheckingStrategyFactoryTest {


    @Test
    public void getBestPracticeStrategy_ProgramingLanguageJavaAndTestingFrameworkIsJunit_ShouldReturnStrategyForJUnit() {
        BestPracticeCheckingStrategyFactory bestPracticeCheckingStrategyFactory = new BestPracticeCheckingStrategyFactory();

        BestPracticeCheckingStrategy<PsiElement> returnedStrategy = bestPracticeCheckingStrategyFactory
                .getBestPracticeCheckingStrategy(ProgrammingLanguage.JAVA, UnitTestFramework.JUNIT)
                .get();

        Assertions.assertTrue(returnedStrategy instanceof JUnitGroupMethodBestPracticeCheckingStrategyAdapter);

    }

    @Test
    public void getBestPracticeStrategy_NotSupportedProgrammingLanguageFrameworkCombination_ShouldNotReturnStrategy() {
        BestPracticeCheckingStrategyFactory bestPracticeCheckingStrategyFactory = new BestPracticeCheckingStrategyFactory();

        Optional<BestPracticeCheckingStrategy<PsiElement>> optionalBestPracticeCheckingStrategy = bestPracticeCheckingStrategyFactory
                .getBestPracticeCheckingStrategy(ProgrammingLanguage.TYPESCRIPT, UnitTestFramework.JUNIT);

        Assertions.assertFalse(optionalBestPracticeCheckingStrategy.isPresent());

    }
}
