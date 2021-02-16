package com.testspector.checking;

import com.testspector.checking.java.junit.JUnitBestPracticeCheckingStrategy;
import com.testspector.enums.ProgrammingLanguage;
import com.testspector.enums.UnitTestFramework;

import java.util.Optional;

public class BestPracticeCheckingStrategyFactory {

    public Optional<BestPracticeCheckingStrategy> getBestPracticeChecker(ProgrammingLanguage programmingLanguage, UnitTestFramework unitTestFramework) {
        if (programmingLanguage == ProgrammingLanguage.JAVA && unitTestFramework == UnitTestFramework.JUNIT) {
            return Optional.of(new JUnitBestPracticeCheckingStrategy());
        }
        return Optional.empty();
    }

}
