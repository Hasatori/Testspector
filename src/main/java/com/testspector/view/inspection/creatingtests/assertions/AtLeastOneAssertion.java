package com.testspector.view.inspection.creatingtests.assertions;

import com.testspector.model.enums.BestPractice;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AtLeastOneAssertion extends AssertionsInspection {

    @Override
    public @Nullable
    @Nls
    String getStaticDescription() {
        return String.format(
                "<p>At least one assertion should be part of the test. In many testing frameworks if there is no assertion method in the test the framework usually reports such test as positive. This will lead to false positive tests.</p>" +
                        "<br/>" +
                        "<a href=\"%s\">Get more information about the rule</a>"
                        , BestPractice.AT_LEAST_ONE_ASSERTION.getWebPageHyperlink());
    }

    @NotNull
    @Override
    public BestPractice getBestPractice() {
        return BestPractice.AT_LEAST_ONE_ASSERTION;
    }
}
