package com.testspector.model.checking;

import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaElementResolver;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.junit.strategy.*;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BestPracticeCheckingStrategyFactory {

    public Optional<BestPracticeCheckingStrategy> getBestPracticeCheckingStrategy(ProgrammingLanguage programmingLanguage, UnitTestFramework unitTestFramework) {
        if (programmingLanguage == ProgrammingLanguage.JAVA) {
            return getJavaBestPracticeCheckingStrategy(unitTestFramework);
        }
        return Optional.empty();
    }

    public List<ProgrammingLanguage> getSupportedLanguages() {
        return Collections.singletonList(ProgrammingLanguage.JAVA);
    }

    public List<UnitTestFramework> getSupportedUnitTestFrameworks() {
        return Collections.singletonList(UnitTestFramework.JUNIT);
    }


    private Optional<BestPracticeCheckingStrategy> getJavaBestPracticeCheckingStrategy(UnitTestFramework unitTestFramework) {
        if (unitTestFramework == UnitTestFramework.JUNIT) {
            JavaContextIndicator contextIndicator = new JavaContextIndicator();
            JavaElementResolver javaElementResolver = new JavaElementResolver();
            JavaMethodResolver methodResolver = new JavaMethodResolver(javaElementResolver, contextIndicator);
            return Optional.of(new GroupBestPracticeCheckingStrategyDecorator(Arrays.asList(
                    new NoSimpleTestsJUnitCheckingStrategy(javaElementResolver, methodResolver),
                    new AssertionCountJUnitCheckingStrategy(javaElementResolver, contextIndicator, methodResolver),
                    new CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy(javaElementResolver, contextIndicator, methodResolver),
                    new NoConditionalLogicJUnitCheckingStrategy(javaElementResolver, contextIndicator, methodResolver),
                    new NoGlobalStaticPropertiesJUnitCheckingStrategy(javaElementResolver, methodResolver, contextIndicator),
                    new TestNamingStrategyJUnitCheckingStrategy(javaElementResolver, methodResolver, contextIndicator),
                    new TestOnlyPublicBehaviourJUnitCheckingStrategy(javaElementResolver, methodResolver, contextIndicator)
            )
            ));
        }

        return Optional.empty();
    }
}
