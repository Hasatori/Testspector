package com.testspector.model.checking.java.junit;

import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.factory.BestPracticeCheckingStrategyFactory;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaElementResolver;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.junit.strategy.*;
import com.testspector.model.enums.BestPractice;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class JUnitBestPracticeCheckingStrategyFactory implements BestPracticeCheckingStrategyFactory {
    JavaContextIndicator contextIndicator = new JavaContextIndicator();
    JavaElementResolver javaElementResolver = new JavaElementResolver();
    JavaMethodResolver methodResolver = new JavaMethodResolver(javaElementResolver, contextIndicator);

    List<BestPracticeCheckingStrategy<PsiElement>> allStrategies = Arrays.asList(
            new JUnitTestMethodBestPracticeCheckingStrategyAdapter(new AtLeastOneAssertionJUnitCheckingStrategy(javaElementResolver, contextIndicator, methodResolver), methodResolver),
            new JUnitTestMethodBestPracticeCheckingStrategyAdapter(new OnlyOneAssertionJUnitCheckingStrategy(javaElementResolver, contextIndicator, methodResolver), methodResolver),
            new JUnitTestMethodBestPracticeCheckingStrategyAdapter(new CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy(javaElementResolver, contextIndicator, methodResolver), methodResolver),
            new JUnitTestMethodBestPracticeCheckingStrategyAdapter(new NoConditionalLogicJUnitCheckingStrategy(javaElementResolver, contextIndicator, methodResolver), methodResolver),
            new JUnitTestMethodBestPracticeCheckingStrategyAdapter(new NoGlobalStaticPropertiesJUnitCheckingStrategy(javaElementResolver, methodResolver, contextIndicator), methodResolver),
            new JUnitTestMethodBestPracticeCheckingStrategyAdapter(new SetupTestNamingStrategyJUnitCheckingStrategy(javaElementResolver, methodResolver, contextIndicator), methodResolver),
            new JUnitTestMethodBestPracticeCheckingStrategyAdapter(new TestOnlyPublicBehaviourJUnitCheckingStrategy(javaElementResolver, methodResolver, contextIndicator), methodResolver),
            new NoDeadCodeJUnitCheckingStrategy(javaElementResolver, contextIndicator, methodResolver)
    );

    @Override
    public BestPracticeCheckingStrategy<PsiElement> getBestPracticeCheckingStrategy() {
        return new JUnitGroupBestPracticeCheckingStrategy(allStrategies);
    }

    @Override
    public BestPracticeCheckingStrategy<PsiElement> getBestPracticeCheckingStrategy(BestPractice bestPractice) {
        List<BestPracticeCheckingStrategy<PsiElement>> strategiesForBestPractice = allStrategies
                .stream()
                .filter(strategy -> strategy.getCheckedBestPractice().contains(bestPractice))
                .collect(Collectors.toList());
        if (strategiesForBestPractice.size() > 0) {
            return new JUnitGroupBestPracticeCheckingStrategy(strategiesForBestPractice);
        } else {
            return null;
        }
    }
}
