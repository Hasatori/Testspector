package com.testspector;

import com.testspector.model.checking.*;
import com.testspector.model.checking.java.junit.JUnitUnitTestFrameworkResolveIndicationStrategy;
import com.testspector.model.checking.java.junit.JUnitUnitTestLineLineResolveStrategy;
import com.testspector.model.enums.ProgrammingLanguage;

import static com.testspector.model.enums.ProgrammingLanguage.*;

import java.util.*;

public final class Configuration {

    private Configuration(){}

    public static final Map<ProgrammingLanguage, List<UnitTestLineResolveStrategy>> PROGRAMMING_LANGUAGE_TEST_LINE_RESOLVE_STRATEGY_HASH_MAP = Collections.unmodifiableMap(new HashMap<>(){{
        put(JAVA,Arrays.asList(new JUnitUnitTestLineLineResolveStrategy()));
    }});
    public static final Map<ProgrammingLanguage, List<UnitTestFrameworkResolveIndicationStrategy>> PROGRAMMING_LANGUAGE_UNIT_TEST_FRAMEWORK_RESOLVE_INDICATION_STRATEGY_HASH_MAP = Collections.unmodifiableMap(new HashMap<>(){{
        put(JAVA,Arrays.asList(new JUnitUnitTestFrameworkResolveIndicationStrategy()));
    }});
    public static final UnitTestFrameworkFactory UNIT_TEST_FRAMEWORK_RESOLVE_STRATEGY_FACTORY = new UnitTestFrameworkFactory(PROGRAMMING_LANGUAGE_UNIT_TEST_FRAMEWORK_RESOLVE_INDICATION_STRATEGY_HASH_MAP);
    public static final ProgrammingLanguageFactory PROGRAMMING_LANGUAGE_FACTORY = new ProgrammingLanguageFactory();
    public static final BestPracticeCheckingStrategyFactory BEST_PRACTICE_CHECKING_STRATEGY_FACTORY = new BestPracticeCheckingStrategyFactory();


}
