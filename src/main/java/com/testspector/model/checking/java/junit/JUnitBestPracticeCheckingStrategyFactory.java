package com.testspector.model.checking.java.junit;

import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.factory.BestPracticeCheckingStrategyFactory;
import com.testspector.model.checking.java.common.JavaContextIndicator;
import com.testspector.model.checking.java.common.JavaMethodResolver;
import com.testspector.model.checking.java.common.search.ElementSearchEngine;
import com.testspector.model.checking.java.junit.strategy.*;
import com.testspector.model.enums.BestPractice;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JUnitBestPracticeCheckingStrategyFactory implements BestPracticeCheckingStrategyFactory {
    JavaContextIndicator contextIndicator = new JavaContextIndicator();
    ElementSearchEngine elementSearchEngine = new ElementSearchEngine();
    JavaMethodResolver methodResolver = new JavaMethodResolver(elementSearchEngine, contextIndicator);

    List<BestPracticeCheckingStrategy<PsiElement>> allStrategies = Arrays.asList(
            new JUnitTestMethodBestPracticeCheckingStrategyAdapter(new AtLeastOneAssertionJUnitCheckingStrategy(elementSearchEngine, contextIndicator, methodResolver), methodResolver),
            new JUnitTestMethodBestPracticeCheckingStrategyAdapter(new OnlyOneAssertionJUnitCheckingStrategy(elementSearchEngine, contextIndicator, methodResolver), methodResolver),
            new JUnitTestMethodBestPracticeCheckingStrategyAdapter(new CatchExceptionsWithFrameworkToolsJUnitCheckingStrategy(elementSearchEngine, contextIndicator, methodResolver), methodResolver),
            new JUnitTestMethodBestPracticeCheckingStrategyAdapter(new NoConditionalLogicJUnitCheckingStrategy(elementSearchEngine, contextIndicator, methodResolver), methodResolver),
            new JUnitTestMethodBestPracticeCheckingStrategyAdapter(new NoGlobalStaticPropertiesJUnitCheckingStrategy(elementSearchEngine, contextIndicator, methodResolver), methodResolver),
            new JUnitTestMethodBestPracticeCheckingStrategyAdapter(new SetupTestNamingStrategyJUnitCheckingStrategy(elementSearchEngine, contextIndicator, methodResolver), methodResolver),
            new JUnitTestMethodBestPracticeCheckingStrategyAdapter(new TestOnlyPublicBehaviourJUnitCheckingStrategy(elementSearchEngine, contextIndicator, methodResolver), methodResolver)
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
