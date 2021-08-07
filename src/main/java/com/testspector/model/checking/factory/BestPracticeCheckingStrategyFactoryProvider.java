package com.testspector.model.checking.factory;

import com.testspector.model.checking.java.junit.JUnitBestPracticeCheckingStrategyFactory;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BestPracticeCheckingStrategyFactoryProvider {


    public Optional<BestPracticeCheckingStrategyFactory> getBestPracticeCheckingStrategyFactory(ProgrammingLanguage programmingLanguage, UnitTestFramework unitTestFramework) {
        if (programmingLanguage == ProgrammingLanguage.JAVA) {
            if (unitTestFramework == UnitTestFramework.JUNIT) {
                return Optional.of(new JUnitBestPracticeCheckingStrategyFactory());
            }
        }
        return Optional.empty();
    }

}
