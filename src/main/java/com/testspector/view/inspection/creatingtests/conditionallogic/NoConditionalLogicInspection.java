package com.testspector.view.inspection.creatingtests.conditionallogic;

import com.testspector.model.enums.BestPractice;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NoConditionalLogicInspection extends ConditionalLogicInspection {

    @Override
    public @Nullable
    @Nls
    String getStaticDescription() {
        return String.format(
                "<p>Conditional logic in the form of if, else, for, or while should not be part of the test code. " +
                "In general, it increases the complexity of the test method, which complicates its comprehensibility, readability. " +
                "It is very hard to determine what is exactly tested as the person reading the test has to think about which conditional branch is going to be executed. " +
                "It leads to the skipping of some verification methods and thus gives the illusion of correctness. " +
                "A general solution to this problem is to extract all conditional branches into separate tests. Another option is to use the so-called parameterized tests, each option is represented by one set of parameters. " +
                "</p>" +
                "<br/>" +
                "<a href=\"%s\">Get more information about the rule</a>"
                 , BestPractice.AT_LEAST_ONE_ASSERTION.getWebPageHyperlink());
    }

    @NotNull
    @Override
    public BestPractice getBestPractice() {
        return BestPractice.NO_CONDITIONAL_LOGIC;
    }
}
