package com.testspector.model.checking;

import com.testspector.model.checking.java.junit.JUnitBestPracticeCheckingStrategy;
import com.testspector.model.checking.java.junit.singlebestpracticestrategy.*;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;

import java.util.Arrays;
import java.util.Optional;

public class BestPracticeCheckingStrategyFactory {

    public Optional<BestPracticeCheckingStrategy> getBestPracticeCheckingStrategy(ProgrammingLanguage programmingLanguage, UnitTestFramework unitTestFramework) {
        if (programmingLanguage == ProgrammingLanguage.JAVA && unitTestFramework == UnitTestFramework.JUNIT) {
            return Optional.of(new JUnitBestPracticeCheckingStrategy(Arrays.asList(
                    new NoSimpleTestsJUnitCheckingStrategy(),
                    new AtLeastOneAssertionJUnitCheckingStrategy(),
                    new CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy(),
                    new CreateCustomDataSourcesJUnitCheckingStrategy(),
                    new NoConditionalLogicJUnitCheckingStrategy(),
                    new NoGlobalStaticPropertiesJUnitCheckingStrategy(),
                    new NoSimpleTestsJUnitCheckingStrategy(),
                    new OnlyOneAssertionJUnitCheckingStrategy(),
                    new TestNamingStrategyJUnitCheckingStrategy(),
                    new TestOnlyPublicBehaviourJUnitCheckingStrategy(),
                    new ThreePhaseTestStructureJUnitCheckingStrategy()
            )
            ));
        }
        return Optional.empty();
    }

}
