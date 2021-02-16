package com.testspector.model.checking.java;

import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.enums.BestPractice;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;

import java.util.List;

public abstract class JavaBestPracticeCheckingStrategy extends BestPracticeCheckingStrategy {

    public JavaBestPracticeCheckingStrategy(ProgrammingLanguage supportedProgrammingLanguage, List<BestPractice> supportedRules, UnitTestFramework supportedFramework) {
        super(supportedProgrammingLanguage, supportedRules, supportedFramework);
    }
}
