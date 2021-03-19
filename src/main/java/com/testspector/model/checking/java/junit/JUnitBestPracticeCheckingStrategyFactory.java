package com.testspector.model.checking.java.junit;

import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.factory.BestPracticeCheckingStrategyFactory;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaElementResolver;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.junit.strategy.*;

import java.util.Arrays;

public class JUnitBestPracticeCheckingStrategyFactory implements BestPracticeCheckingStrategyFactory {

    @Override
    public BestPracticeCheckingStrategy<PsiElement> getBestPracticeCheckingStrategy() {
        JavaContextIndicator contextIndicator = new JavaContextIndicator();
        JavaElementResolver javaElementResolver = new JavaElementResolver();
        JavaMethodResolver methodResolver = new JavaMethodResolver(javaElementResolver, contextIndicator);
        return new JUnitGroupMethodBestPracticeCheckingStrategyAdapter(Arrays.asList(
                new AssertionCountJUnitCheckingStrategy(javaElementResolver, contextIndicator, methodResolver),
                new CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy(javaElementResolver, contextIndicator, methodResolver),
                new NoConditionalLogicJUnitCheckingStrategy(javaElementResolver, contextIndicator, methodResolver),
                new NoGlobalStaticPropertiesJUnitCheckingStrategy(javaElementResolver, methodResolver, contextIndicator),
                new SetupTestNamingStrategyJUnitCheckingStrategy(javaElementResolver, methodResolver, contextIndicator),
                new TestOnlyPublicBehaviourJUnitCheckingStrategy(javaElementResolver, methodResolver, contextIndicator)
        ), methodResolver);
    }
}
