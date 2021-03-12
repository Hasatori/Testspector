package com.testspector.model.checking;


import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.testspector.model.checking.java.junit.JUnitGroupMethodBestPracticeCheckingStrategyAdapter;
import com.testspector.model.checking.java.junit.JUnitUnitTestFrameworkResolveIndicationStrategy;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
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


    @Test
    public void test() {
        BestPracticeCheckingStrategyFactory bestPracticeCheckingStrategyFactory = new BestPracticeCheckingStrategyFactory();
        for (ProgrammingLanguage programmingLanguage : ProgrammingLanguage.values()) {
            for (UnitTestFramework unitTestFramework : UnitTestFramework.values()) {
                if (programmingLanguage == ProgrammingLanguage.JAVA && unitTestFramework == UnitTestFramework.JUNIT){
                    BestPracticeCheckingStrategy<PsiElement> returnedStrategy = bestPracticeCheckingStrategyFactory
                            .getBestPracticeCheckingStrategy(programmingLanguage,unitTestFramework)
                            .get();

                    Assertions.assertTrue(returnedStrategy instanceof JUnitGroupMethodBestPracticeCheckingStrategyAdapter);
                }
                if (programmingLanguage == ProgrammingLanguage.TYPESCRIPT && unitTestFramework == UnitTestFramework.JUNIT){
                    Optional<BestPracticeCheckingStrategy<PsiElement>> optionalBestPracticeCheckingStrategy = bestPracticeCheckingStrategyFactory
                            .getBestPracticeCheckingStrategy(programmingLanguage,unitTestFramework);

                    Assertions.assertFalse(optionalBestPracticeCheckingStrategy.isPresent());
                }
            }
        }
    }
}
