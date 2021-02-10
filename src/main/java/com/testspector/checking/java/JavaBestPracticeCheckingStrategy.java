package com.testspector.checking.java;

import com.testspector.checking.BestPracticeCheckingStrategy;
import com.testspector.enums.BestPractice;
import com.testspector.enums.ProgrammingLanguage;
import com.testspector.enums.UnitTestFramework;

import java.util.List;

public abstract class JavaBestPracticeCheckingStrategy extends BestPracticeCheckingStrategy {
    public JavaBestPracticeCheckingStrategy(ProgrammingLanguage supportedProgrammingLanguage, List<BestPractice> supportedRules, UnitTestFramework supportedFramework) {
        super(supportedProgrammingLanguage, supportedRules, supportedFramework);
    }
}
