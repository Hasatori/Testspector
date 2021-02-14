package com.testspector.checking;

import com.testspector.checking.java.junit.JUnitBestPracticeCheckingStrategy;
import com.testspector.enums.ProgrammingLanguage;
import com.testspector.enums.UnitTestFramework;

public class BestPracticeCheckingStrategyFactory {

    public BestPracticeCheckingStrategy getBestPracticeChecker(ProgrammingLanguage programmingLanguage, UnitTestFramework unitTestFramework) {
        if (programmingLanguage == ProgrammingLanguage.JAVA && unitTestFramework == UnitTestFramework.JUNIT) {
            return new JUnitBestPracticeCheckingStrategy();
        }
        return null;
    }

}
