package com.testspector.model.checking;

import com.testspector.model.checking.java.JavaClassHelper;
import com.testspector.model.checking.java.JavaElementHelper;
import com.testspector.model.checking.java.junit.strategy.*;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BestPracticeCheckingStrategyFactory {

    public Optional<BestPracticeCheckingStrategy> getBestPracticeCheckingStrategy(ProgrammingLanguage programmingLanguage, UnitTestFramework unitTestFramework) {
        if (programmingLanguage == ProgrammingLanguage.JAVA && unitTestFramework == UnitTestFramework.JUNIT) {
            JavaElementHelper javaElementHelper = new JavaElementHelper(new JavaClassHelper());
            return Optional.of(new GroupBestPracticeCheckingStrategyDecorator(Arrays.asList(
                    new NoSimpleTestsJUnitCheckingStrategy(),
                    new AssertionCountJUnitCheckingStrategy(javaElementHelper),
                    new CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy(javaElementHelper),
                    new CreateCustomDataSourcesJUnitCheckingStrategy(),
                    new NoConditionalLogicJUnitCheckingStrategy(javaElementHelper),
                    new NoGlobalStaticPropertiesJUnitCheckingStrategy(),
                    new NoSimpleTestsJUnitCheckingStrategy(),
                    new TestNamingStrategyJUnitCheckingStrategy(),
                    new TestOnlyPublicBehaviourJUnitCheckingStrategy(),
                    new ThreePhaseTestStructureJUnitCheckingStrategy()
            )
            ));
        }
        return Optional.empty();
    }

    public List<ProgrammingLanguage> getSupportedLanguages(){
        return Collections.singletonList(ProgrammingLanguage.JAVA);
    }

    public List<UnitTestFramework> getSupportedUnitTestFrameworks(){
        return Collections.singletonList(UnitTestFramework.JUNIT);
    }
}
