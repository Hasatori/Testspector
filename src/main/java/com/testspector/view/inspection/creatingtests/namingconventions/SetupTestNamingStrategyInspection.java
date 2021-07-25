package com.testspector.view.inspection.creatingtests.namingconventions;

import com.testspector.model.enums.BestPractice;
import com.testspector.view.inspection.BestPracticeInspection;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SetupTestNamingStrategyInspection extends NamingConventionsInspection {

    @Override
    public @Nullable
    @Nls
    String getStaticDescription() {
        return String.format(
                "<p>The test name is more or less the same as the tested method. " +
                "This says nothing about tests scenario. You should setup a clear strategy" +
                " for naming your tests so that the person reading then knows what is tested</p>" +
                "<p>There are several recommended strategies which can be used to name your tests:</p>" +
                "<ul>" +
                "<li>doingSomeOperationGeneratesSomeResult</li>" +
                "<li>someResultOccursUnderSomeCondition</li>" +
                "<li>given-when-then</li>" +
                "<li>givenSomeContextWhenDoingSomeBehaviorThenSomeResultOccurs</li>" +
                "<li>whatIsTested_conditions_expectedResult</li>" +
                "</ul>" +
                "<p>Chosen naming strategy is subjective. The key thing to remember is that name of the test should say:</p>" +
                "<ul>" +
                "<li>What is tests</li>" +
                "<li>What are the conditions</li>" +
                "<li>What is expected result</li>" +
                "</ul>" +
                "<br/>" +
                "<a href=\"%s\">Get more information about the rule</a>"
                , getBestPractice().getWebPageHyperlink());
    }

    @NotNull
    @Override
    public BestPractice getBestPractice() {
        return BestPractice.SETUP_A_TEST_NAMING_STRATEGY;
    }
}
