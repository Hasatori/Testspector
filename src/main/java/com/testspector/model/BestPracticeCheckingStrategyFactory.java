package com.testspector.model;

import com.testspector.model.java.junit.JUnitBestPracticeCheckingStrategy;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;

import java.util.Optional;

public class BestPracticeCheckingStrategyFactory {

    public Optional<BestPracticeCheckingStrategy> getBestPracticeChecker(ProgrammingLanguage programmingLanguage, UnitTestFramework unitTestFramework) {
        if (programmingLanguage == ProgrammingLanguage.JAVA && unitTestFramework == UnitTestFramework.JUNIT) {
            return Optional.of(new JUnitBestPracticeCheckingStrategy());
        }
        return Optional.empty();
    }

}
