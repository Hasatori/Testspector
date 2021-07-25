package com.testspector.view.inspection.creatingtests.assertions;

import com.testspector.model.enums.BestPractice;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OnlyOneAssertion extends AssertionsInspection {

    @Override
    public @Nullable
    @Nls
    String getStaticDescription() {
        return String.format(
                "<p>Test should fail for only one reason. If one assertion fails other will not be executed and therefore you will not get overview of all problems.</p>" +
                        "<br/>" +
                        "<a href=\"%s\">Get more information about the rule</a>"
                        , BestPractice.AT_LEAST_ONE_ASSERTION.getWebPageHyperlink());
    }

    @NotNull
    @Override
    public BestPractice getBestPractice() {
        return BestPractice.ONLY_ONE_ASSERTION;
    }
}
