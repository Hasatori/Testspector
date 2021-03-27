package com.testspector.model.checking.factory;

import com.testspector.model.checking.java.junit.JUnitBestPracticeCheckingStrategyFactory;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Optional;


public class BestPracticeCheckingStrategyFactoryProviderTest {


    @Test
    public void bestPracticeCheckingStrategyFactory_JavaAndJUnit_ShouldReturnJUniBestPracticeCheckingStrategy() {
        BestPracticeCheckingStrategyFactoryProvider bestPracticeCheckingStrategyFactoryProvider = new BestPracticeCheckingStrategyFactoryProvider();

        BestPracticeCheckingStrategyFactory bestPracticeCheckingStrategyFactory = bestPracticeCheckingStrategyFactoryProvider
                .getBestPracticeCheckingStrategyFactory(ProgrammingLanguage.JAVA, UnitTestFramework.JUNIT).get();

        Assertions.assertTrue(bestPracticeCheckingStrategyFactory instanceof JUnitBestPracticeCheckingStrategyFactory);
    }

    @Test
    public void bestPracticeCheckingStrategyFactory_JavaAndPhpUnit_ShouldBeEmpty() {
        BestPracticeCheckingStrategyFactoryProvider bestPracticeCheckingStrategyFactoryProvider = new BestPracticeCheckingStrategyFactoryProvider();

        Optional<BestPracticeCheckingStrategyFactory> optionalBestPracticeCheckingStrategyFactory = bestPracticeCheckingStrategyFactoryProvider
                .getBestPracticeCheckingStrategyFactory(ProgrammingLanguage.JAVA, UnitTestFramework.PHP_UNIT);

        Assertions.assertFalse(optionalBestPracticeCheckingStrategyFactory.isPresent());
    }

}
